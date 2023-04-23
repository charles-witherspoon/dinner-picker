package com.wtech.fitness.models

import com.wtech.fitness.Sections
import com.wtech.fitness.getSection
import com.wtech.fitness.models.quantity.*
import com.wtech.fitness.models.quantity.Unit
import kotlinx.serialization.Serializable
import java.time.Duration
import java.time.LocalDateTime

class Ingredient(val quantity: Quantity?, val unit: Unit?, val grocery: String) {
    override fun toString(): String {
        return "${quantity?.toString() ?: ""}${unit?.let { " " + it.abbreviation } ?: ""} $grocery".trim()
    }

    operator fun plus(other: Ingredient): Ingredient {
        val sum: Quantity = this.quantity + other.quantity
        return Ingredient(sum, unit, grocery)
    }

    companion object {
        fun fromString(rawString: String): Ingredient {
            val listDelimiter = "^\\s*-\\s+".toRegex().find(rawString)?.value ?: ""
            val s = rawString.substringAfter(listDelimiter)
            val grocery = s.extractGrocery()

            if (grocery == s) {
                return Ingredient(null, null, grocery)
            }

            val quantity = try {
                s.extractQuantity()
            } catch (e: IllegalStateException) {
                Quantity(1)
            }

            return try {
                Ingredient(quantity, s.extractUnit(), grocery)
            } catch (e: IllegalStateException) {
                Ingredient(quantity, null, grocery)
            }
        }

        fun merge(ingredients: List<Ingredient>, grocery: String? = null): Ingredient {
            check(ingredients.isNotEmpty()) { "Cannot merge list of empy ingredients" }
            check(ingredients.all { it.unit == null } || ingredients.none { it.unit == null }) { "Either no units must be null or all units must be null" }

            return ingredients.groupBy { ingredient -> ingredient.unit in Unit.volumeUnits }
                .map { (_, ingredients) -> mergeUnits(ingredients, grocery) }
                .run {
                    val ingredient = this[0]

                    if (size == 1) ingredient
                    else {
                        val volumeIngredient = first { it.unit in Unit.volumeUnits }
                        val containerIngredient = first { it.unit in Unit.containerUnits }
                        mergeVolumeWithContainer(volumeIngredient, containerIngredient, grocery ?: containerIngredient.grocery)
                    }
                }
        }

        private fun mergeUnits(ingredients: List<Ingredient>, grocery: String?): Ingredient {


            check(
                ingredients.all { it.unit in Unit.containerUnits } ||
                        ingredients.all { it.unit in Unit.volumeUnits } ||
                        ingredients.all { it.unit == null } ||
                        ingredients.map { it.unit }.toSet().size == 1
            )

            val baseIngredient = ingredients[0]

            val quantity: Quantity? = ingredients.map { it.quantity }
                .reduce { acc, quantity ->
                    acc + quantity
                }

            return Ingredient(quantity, baseIngredient.unit, grocery ?: baseIngredient.grocery)
        }

        private fun mergeVolumeWithContainer(volumeIngredient: Ingredient, containerIngredient: Ingredient, grocery: String = containerIngredient.grocery): Ingredient {
            check(containerIngredient.quantity != null && volumeIngredient.quantity != null)
            check(containerIngredient.unit != null && volumeIngredient.unit != null)

            val threshold: Quantity = Unit.getThreshold(containerIngredient.unit)
            val additionalContainers: Quantity = Unit.checkThreshold(threshold, volumeIngredient.quantity + containerIngredient.quantity.fraction)

            val quantity = containerIngredient.quantity + additionalContainers
            return Ingredient(quantity, containerIngredient.unit, grocery)
        }
    }

    override fun equals(other: Any?): Boolean {
        if (other !is Ingredient) return false

        return this.quantity == other.quantity
                && this.unit == other.unit
                && this.grocery == other.grocery
    }

    override fun hashCode(): Int {
        return super.hashCode()
    }
}

class GroceryList(ingredients: List<Ingredient>, private val mergeMap: MergeMap?) {

    val ingredients: MutableList<Ingredient> = mutableListOf()

    init {
        val mergeableIngredients: List<Ingredient> = mergeMap?.getGroups()?.flatten() ?: listOf()
        val unMergeableIngredients: List<Ingredient> = ingredients.filter { it !in mergeableIngredients }
        this.ingredients.addAll(unMergeableIngredients)
        mergeMap?.getGroups()
            ?.filter { group -> ingredients.any { it in group }  }
            ?.map { Ingredient.merge(it.toList()) }
            ?.also { this.ingredients.addAll(it) }
    }

    fun getIngredientsAsStrings(): List<String> {
        return ingredients.map { it.toString() }
    }

    fun mergeIngredients(ingredientsToMerge: List<Ingredient>, grocery: String? = null) {
        ingredients.removeAll(ingredientsToMerge)
        mergeMap?.mergeIngredients(ingredientsToMerge)
        val mergedIngredient: Ingredient = Ingredient.merge(ingredientsToMerge, grocery)
        ingredients.add(mergedIngredient)
    }

    override fun toString(): String {
        return ingredients.mapIndexed { index, ingredient ->  "$index) $ingredient" }
            .joinToString("\n")
    }
}

fun List<Ingredient>.toGroceryList(mergeMap: MergeMap? = null): GroceryList {
    return GroceryList(this, mergeMap)
}

class MergeMap(ingredients: List<Ingredient> = mutableListOf()) {

    private val ingredientsToGroups: MutableMap<Ingredient, Int> =
        ingredients.mapIndexed { index, ingredient -> ingredient to index }
            .toMap()
            .toMutableMap()

    private val mergeGroups: MutableList<MutableSet<Ingredient>> = mutableListOf()

    private fun Ingredient.canJoinGroup(group: MutableSet<Ingredient>): Boolean = group.any { it.grocery == grocery }

    init {
        ingredients.forEach { ingredient ->
            val group: MutableSet<Ingredient> = mergeGroups.find { ingredient.canJoinGroup(it) }
                ?: mutableSetOf<Ingredient>()
                    .also { mergeGroups.add(it) }

            group.add(ingredient)
            ingredientsToGroups[ingredient] = mergeGroups.indexOf(group)
        }

    }

    fun mergeIngredients(ingredients: List<Ingredient>): List<Ingredient> {
        check(ingredients.isNotEmpty()) { "Cannot merge empty list" }

        ingredients.zipWithNext().forEach { (a, b) ->
            val aGroupIndex: Int = ingredientsToGroups[a] ?: -1
            val bGroupIndex: Int = ingredientsToGroups[b] ?: -2

            if (aGroupIndex < 0 && bGroupIndex < 0) {
                mergeGroups.add(mutableSetOf(a, b))
                ingredientsToGroups[a] = mergeGroups.lastIndex
                ingredientsToGroups[b] = mergeGroups.lastIndex
            } else if (aGroupIndex < 0) {
                mergeGroups[bGroupIndex].add(a)
                ingredientsToGroups[a] = bGroupIndex
            } else if (bGroupIndex < 0) {
                mergeGroups[aGroupIndex].add(b)
                ingredientsToGroups[b] = aGroupIndex
            } else if (aGroupIndex < bGroupIndex) {
                mergeGroups[aGroupIndex].add(b)
                ingredientsToGroups[b] = aGroupIndex
                mergeGroups.remove(mergeGroups[bGroupIndex])
                ingredientsToGroups.forEach { (ingredient, index) ->
                    if (index > bGroupIndex) ingredientsToGroups[ingredient] = index - 1
                }
            } else {
                mergeGroups[bGroupIndex].add(a)
                ingredientsToGroups[a] = bGroupIndex
                mergeGroups.remove(mergeGroups[aGroupIndex])
                ingredientsToGroups.forEach { (ingredient, index) ->
                    if (index > aGroupIndex) ingredientsToGroups[ingredient] = index - 1
                }
            }
        }

        return mergeGroups.map { Ingredient.merge(it.toList()) }
    }

    fun getGroups(): List<Set<Ingredient>> {
        return mergeGroups
    }
}

data class Recipe(
    val name: String,
    val ingredients: List<Ingredient>,
    val directions: List<String>,
    val tips: List<String>?,
    val nutrition: Nutrition,
    val link: String
) {
    override fun toString(): String {
        val result = buildString {
            appendLine(name)

            appendLine("Ingredients")
            appendLine(ingredients.joinToString("\n"){ "- $it" })

            appendLine("\nDirections")
            appendLine(directions.joinToString("\n"))

            tips?.apply {
                appendLine("\nTips")
                appendLine(joinToString("\n"))
            }

            appendLine("\nNutrition Facts")
            appendLine(nutrition)

            appendLine("\nLink")
            append(link)
        }
        println(result)
        return result
    }

    companion object {
        fun fromString(name: String, rawString: String): Recipe {
            val lines = rawString.split("\n")
            val sectionBoundaries: List<Pair<Int,Int>> = lines
                .mapIndexed { i, line ->
                    Sections.fromString(line)?.let { i }
                }
                .filterNotNull()
                .zipWithNext()

            val ingredients: List<Ingredient> = lines.getSection(sectionBoundaries[0])
                .map { Ingredient.fromString(it) }
            val directions: List<String> = lines.getSection(sectionBoundaries[1])

            val tipsIndex: Int = Sections.fromString(lines[sectionBoundaries[2].first])
                ?.run {
                    if (this == Sections.TIPS) 2 else -1
                } ?: -1
            val tips: List<String>? = if (tipsIndex > 0) lines.getSection(sectionBoundaries[tipsIndex]) else null

            val nutrition: Nutrition = Nutrition.fromString(
                lines.getSection(
                    sectionBoundaries[if (tipsIndex > 0) 3 else 2]
                )[0]
            )

            val link: String = lines.last()


            return Recipe(name, ingredients, directions, tips, nutrition, link)
        }
    }
}

data class Nutrition(val facts: Map<String, Quantity> = mapOf()) {
    companion object {
        fun fromString(rawString: String): Nutrition {
            val factPattern: Regex = """\d+\s*\w+.*?(\([^\\)]*+\))*[,.]""".toRegex()
            val matches: Sequence<MatchResult> = factPattern.findAll(rawString)

            fun parseString(entry: String): Pair<String, Quantity> {
                val (amount, metric) = entry.split("\\s+".toRegex())
                val number: Int = "^\\d+".toRegex()
                    .find(amount)?.value?.toInt() ?: error("Cannot parse fact amount $entry")
                val unit: Unit? = amount.substringAfter(number.toString())
                    .run {
                        if (isEmpty()) null
                        else Unit.fromString(this)
                    }

                return Pair(metric.replace("[,:.]".toRegex(), ""), Quantity(number, unit))
            }

            val facts: Map<String, Quantity> = matches
                .flatMap { it.value
                    .split("""[(),.]""".toRegex())
                    .map { s -> s.trim() }
                }
                .filter { it.isNotEmpty() }
                .associate { parseString(it) }

            return Nutrition(facts)
        }
    }
}

class RecipeManager(val recipes: List<Recipe>) {
    fun getRandom(n: Int): List<Recipe> {
        TODO()
    }

    fun getFirst(n: Int): List<Recipe> {
        TODO()
    }
}

open class Food(val nutrition: Nutrition): Item {
    override fun equals(other: Any?): Boolean {
        return other is Food && other.nutrition == this.nutrition
    }

    override fun hashCode(): Int {
        return super.hashCode()
    }
}


data class Consumption(val food: Food, val ingestedAt: LocalDateTime): Event<Food>

class ConsumptionLog(val history: List<Consumption> = listOf()): EventLog<Consumption>() {

    private val log: MutableList<Consumption> = history.toMutableList()

    val size: Int get() = log.size

    val nutrition: Map<String, Quantity> get() {
        return log.flatMap { consumption ->
            consumption.food.nutrition.facts.toList()
        }.groupBy({it.first}) {
            it.second
        }.mapValues { (_, values) ->
            values.reduce { acc, quantity -> acc + quantity }
        }
    }

    fun add(food: Food, insertionTime: LocalDateTime) {
        add(Consumption(food, insertionTime), insertionTime)
    }

    override fun add(event: Consumption, insertionTime: LocalDateTime) {
        log.add(event)
    }

    override fun getHistory(startingOffset: Int, numRecords: Int): List<Consumption> {
        return history.subList(startingOffset, startingOffset + numRecords)
    }

    fun refreshHistory(startTime: LocalDateTime, endTime: LocalDateTime): EventLog<Consumption> {
        val window = startTime .. endTime
        val rangeEnd = LocalDateTime.now()
        log.removeIf {
            val rangeStart = rangeEnd - Duration.ofDays(1)
            it.ingestedAt !in rangeStart..rangeEnd
        }
        return ConsumptionLog()
    }
}

fun List<Food>.nutrition(): Nutrition = map { it.nutrition.facts }.run {

    val facts: Map<String, Quantity> = first()
    val others = subList(1, lastIndex)

    facts.toMutableMap().apply {
        others.forEach { map ->
            map.forEach {
                merge(it.key, it.value) { a,b -> a + b }
            }
        }
    }

    Nutrition(facts)
}

class Meal(private val foods: List<Food>): Food(foods.nutrition()) {

    override fun equals(other: Any?): Boolean {
        return when (other) {
            is Meal -> this.foods == other.foods
            is Food -> this.foods.size == 1 && this.foods.first() == other
            else -> false
        }
    }

    override fun hashCode(): Int {
        return super.hashCode()
    }

    companion object {
        private val mockNutrition1 = Nutrition(
            mapOf(
                Pair("potassium", Quantity(300, Unit.MILLIGRAM))
            )
        )
        private val mockNutrition2 = Nutrition(
            mapOf(
                Pair("potassium", Quantity(1450, Unit.MILLIGRAM))
            )
        )

        private val bananaNutrition = Nutrition(
            mapOf(
                Pair("potassium", Quantity(422, Unit.MILLIGRAM)),
            )
        )
        private val potatoNutrition = Nutrition(
            mapOf(
                Pair("potassium", Quantity(620, Unit.MILLIGRAM)),
            )
        )


        private val now = LocalDateTime.of(2023, 6, 7, 0, 0)
        private val database = listOf(
            Food(bananaNutrition),
            Food(potatoNutrition),
            Food(mockNutrition1),
            Food(mockNutrition2)
        )

        fun satisfying(nutritionRequirements: List<Nutrition>): Meal {
            return Meal(database)
        }
    }
}

@Serializable
data class Nutrient(val id: Int, val number: String, val name: String, val rank: Int, val unitName: String)

@Serializable
data class FoodNutrientSource(val id: Int, val code: String, val description: String)

@Serializable
data class FoodNutrientDerivation(val code: String, val description: String, val foodNutrientSource: FoodNutrientSource)

@Serializable
data class FoodNutrient(
    val type: String,
    val id: Int,
    val nutrient: Nutrient,
    val dataPoints: Int? = null,
    val foodNutrientDerivation: FoodNutrientDerivation,
    val max: Double? = null,
    val min: Double? = null,
    val median: Double? = null,
    val amount: Double
)

@Serializable
data class NutrientConversionFactor(
    val type: String,
    val proteinValue: Double? = null,
    val fatValue: Double? = null,
    val carbohydrateValue: Double? = null,
    val value: Double? = null
)

@Serializable
data class FoodCategory(val description: String, val id: Int? = null, val code: String? = null)

@Serializable
data class MeasureUnit(val id: Int, val name: String, val abbreviation: String)

@Serializable
data class FoodPortion(
    val id: Int,
    val value: Double,
    val measureUnit: MeasureUnit,
    val modifier: String,
    val gramWeight: Double,
    val sequenceNumber: Int,
    val minYearAcquired: Int,
    val amount: Double
)

@Serializable
data class InputFood(
    val id: Int,
    val foodDescription: String,
    val inputFood: InnerInputFood,
) {
    @Serializable
    data class InnerInputFood(
        val foodClass: String,
        val description: String,
        val dataType: String,
        val foodCategory: FoodCategory,
        val fdcId: Int,
        val publicationDate: String
    )
}

@Serializable
data class FoundationFood(
    val foodClass: String,
    val description: String,
    val foodNutrients: List<FoodNutrient>,
    val scientificName: String? = null,
    val foodAttributes: List<String>,
    val nutrientConversionFactors: List<NutrientConversionFactor>,
    val isHistoricalReference: Boolean,
    val ndbNumber: Int,
    val dataType: String,
    val foodCategory: FoodCategory,
    val fdcId: Int,
    val foodPortions: List<FoodPortion>,
    val publicationDate: String,
    val inputFoods: List<InputFood>
)

@Serializable
data class AbridgedFoodNutrient(
    val number: Int? = null,
    val name: String? = null,
    val amount: Double? = null,
    val unitName: String,
    val derivationCode: String? = null,
    val derivationDescription: String? = null
)

@Serializable
data class SearchResultFood(
    val fdcId: Int,
    val description: String,
    val foodCode: String? = null,
    val foodNutrients: List<AbridgedFoodNutrient>,
    val ingredients: String? = null,
    val additionalDescriptions: String? = null,
    val score: Double
)

@Serializable
data class SearchResult(val foods: List<SearchResultFood>)