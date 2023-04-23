package com.wtech.fitness

import com.wtech.fitness.models.*
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.json.*
import okhttp3.OkHttpClient
import okhttp3.Request
import java.io.File
import java.io.IOException
import kotlin.system.exitProcess

const val RECIPE_DIR: String = "src/main/resources/recipes"
const val BASE_URL: String = "https://api.nal.usda.gov/fdc/v1"
const val API_KEY: String = "iGyfkNjVd5hkcRaV4lexQg9WGDSqnkvJRYU7Jfmm"

class IngredientsManager {

}

enum class Sections {
    INGREDIENTS,
    DIRECTIONS,
    TIPS,
    NUTRITION,
    LINK;

    fun previous(): Sections? {
        return when (this) {
            INGREDIENTS -> null
            DIRECTIONS -> INGREDIENTS
            TIPS -> DIRECTIONS
            NUTRITION -> TIPS
            LINK -> NUTRITION
        }
    }

    fun next(): Sections? {
        return when (this) {
            INGREDIENTS -> DIRECTIONS
            DIRECTIONS -> TIPS
            TIPS -> NUTRITION
            NUTRITION -> LINK
            LINK -> null
        }
    }
    companion object {
        fun fromString(s: String): Sections? {
            return when (s.uppercase()) {
                INGREDIENTS.name -> INGREDIENTS
                DIRECTIONS.name -> DIRECTIONS
                TIPS.name -> TIPS
                "NUTRITION FACTS" -> NUTRITION
                LINK.name -> LINK
                else -> null
            }
        }
    }
}

fun String.capitalize(): String {
    return this[0].uppercase() + substring(1, length).lowercase()
}

fun String.toTitle(): String {
    return split("_")
        .joinToString(" ") { it.capitalize() }
}


fun List<String>.getSection(p: Pair<Int, Int>): List<String> {
    return subList(p.first + 1, p.second - 1)
}
fun main() {
    val files: Map<String, List<String>> =
        File(RECIPE_DIR)
            .listFiles()
            ?.sortedBy { it.name }
            ?.associate { it.name to it.readLines() }
            ?: error("nothing here")

    val recipes: List<Recipe> =
        files.mapKeys {
            (name, _) -> name.substringBefore(".txt").toTitle()
        }.map {
            (name, lines) -> Recipe.fromString(name, lines.joinToString("\n"))
        }

    recipes.forEach { recipe ->
        with(recipe) {
            println(name)
            println(ingredients.joinToString("\n") + "\n")
        }
    }
    println()

    val groceryList: GroceryList = recipes.flatMap {it.ingredients }.toGroceryList()
    println(groceryList)
    println()

    val menu = """
        What would you like to do?
        1) Merge ingredients
        2) Main
        3) Exit
    """.trimIndent()

    while (true) {
        when (prompt(menu)) {
            "1" -> {
                println(groceryList)
                doMerge(groceryList)
                println()
            }
            "2" -> doMain()
            "3" -> break
        }
    }
//    doMain()
}

fun prompt(msg: String): String {
    println(msg)
    return readln()
}

fun doMerge(groceryList: GroceryList) {
    val ingredientsToMerge: List<Ingredient> =
        prompt("Enter a comma-separated list of indices for the ingredients you would like to merge:\n")
            .split(",")
            .map { groceryList.ingredients[it.toInt()] }

    groceryList.mergeIngredients(ingredientsToMerge)
}

fun doMain() {
    val client = OkHttpClient()
    val searchString: String = prompt("What food would you like to search?")

    val url = "$BASE_URL/foods/search?query=$searchString&api_key=$API_KEY"
    val request: Request = Request.Builder().url(url).build()

    val foods: List<SearchResultFood> =
        client.newCall(request)
            .execute()
            .use {
                response ->
                    if (!response.isSuccessful)
                        throw IOException("Unexpected code: $response")

                    val mapper = Json {
                        ignoreUnknownKeys = true
                        isLenient = true
                    }
                    mapper.decodeFromString<SearchResult>(response.body.string()).foods
            }

    println(foods)
    exitProcess(0)
}

private fun readFoundationFoodsFile() {
    val file = File("/Users/charleswitherspoon/Downloads/foundationDownload.json")
    val json: JsonObject = Json.parseToJsonElement(file.readText()).jsonObject
    val foundationFoods: List<JsonElement> = json["FoundationFoods"]!!.jsonArray.take(5)
    val names: MutableList<String> = mutableListOf()
    val nutrients: MutableList<String> = mutableListOf()

    foundationFoods.forEach { el: JsonElement ->
        val description = el.jsonObject["description"]
        names.add(
            prompt("What is the name for this description: $description")
        )
        val foodNutrients: JsonArray = el.jsonObject["foodNutrients"]!!.jsonArray
        nutrients.addAll(
            foodNutrients.map { n ->
                n.jsonObject["nutrient"]!!.jsonObject["name"].toString()
            }
        )
    }

    val ffoods: List<FoundationFood> = foundationFoods.map {
        Json.decodeFromJsonElement<FoundationFood>(it)
    }.apply {
    }

    println(ffoods)
}