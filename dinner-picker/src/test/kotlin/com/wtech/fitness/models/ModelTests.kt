package com.wtech.fitness.models

import com.wtech.fitness.models.quantity.*
import com.wtech.fitness.models.quantity.Unit
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import kotlin.test.assertEquals
import kotlin.test.assertNotEquals


const val grocery = "Hot cooked rice of your choice"

const val quantityGrocery = "1 apple"
const val mixedQuantityGrocery = "5/12 apples"
const val quantityRangeGrocery = "1 to 3 freshly-peeled apples"

const val quantityUnitGrocery = "16 oz apples"
const val mixedQuantityUnitGrocery = "1 3/4 ounces of freshly-peeled apple slices"
const val quantityRangeUnitGrocery = "1 to 2 oz apples"

const val weirdOrderGrocery = "2 boneless skinless chicken breast halves (4 ounces each)"
const val pluralUnitGrocery = "2 teaspoons olive oil"
const val fractionalUnitGrocery = "1-1/2 cups shredded iceberg lettuce"

class ModelTests {

    @Test
    fun testExtractGrocery() {
        listOf(
            Pair(grocery, grocery),
            Pair("apple", quantityGrocery),
            Pair("apples", mixedQuantityGrocery),
            Pair("freshly-peeled apples", quantityRangeGrocery),
            Pair("apples", quantityUnitGrocery),
            Pair("freshly-peeled apple slices", mixedQuantityUnitGrocery),
            Pair("apples", quantityRangeUnitGrocery),
            Pair("boneless skinless chicken breast halves (4 ounces each)", weirdOrderGrocery),
            Pair("olive oil", pluralUnitGrocery),
            Pair("shredded iceberg lettuce", fractionalUnitGrocery)
            ).forEach { (expected, rawString) ->
                val actual = rawString.extractGrocery()
                assertEquals(expected, actual)
        }
    }

    @Test
    fun testExtractUnit() {
         listOf(
             grocery,
             quantityGrocery,
             mixedQuantityGrocery,
             quantityRangeGrocery,
             weirdOrderGrocery,
             ).forEach {rawString ->
                 assertThrows<IllegalStateException> { rawString.extractUnit() }
             }

        assertEquals(Unit.OUNCE, quantityUnitGrocery.extractUnit())
        assertEquals(Unit.OUNCE, mixedQuantityUnitGrocery.extractUnit())
        assertEquals(Unit.OUNCE, quantityRangeUnitGrocery.extractUnit())
        assertEquals(Unit.TSP, pluralUnitGrocery.extractUnit())
        assertEquals(Unit.CUP, fractionalUnitGrocery.extractUnit())
    }

    @Test
    fun testExtractQuantity() {
        assertThrows<IllegalStateException> { grocery.extractQuantity() }

        listOf(
            Pair("1", quantityGrocery),
            Pair("5/12", mixedQuantityGrocery),
            Pair("1 to 3", quantityRangeGrocery),

            Pair("16", quantityUnitGrocery),
            Pair("1 3/4", mixedQuantityUnitGrocery),
            Pair("1 to 2", quantityRangeUnitGrocery),

            Pair("2", weirdOrderGrocery),
            Pair("2", pluralUnitGrocery),
            Pair("1 1/2", fractionalUnitGrocery)
            )
            .forEach { (expected, rawString) ->
                val actual = rawString.extractQuantity().toString()
                assertEquals(expected, actual)
            }
    }

    @Test
    fun testIngredientToString() {
        listOf(
            Pair("Hot cooked rice of your choice", grocery),
            Pair("1 apple", quantityGrocery),
            Pair("5/12 apples", mixedQuantityGrocery),
            Pair("1 to 3 freshly-peeled apples", quantityRangeGrocery),
            Pair("16 oz apples", quantityUnitGrocery),
            Pair("1 3/4 oz freshly-peeled apple slices", mixedQuantityUnitGrocery),
            Pair("1 to 2 oz apples", quantityRangeUnitGrocery),
            Pair("2 boneless skinless chicken breast halves (4 ounces each)", weirdOrderGrocery),
            Pair("2 tsp olive oil", pluralUnitGrocery),
            Pair("1 1/2 cup shredded iceberg lettuce", fractionalUnitGrocery)
        ).forEach { (expected, rawString) ->
            val actual: String = Ingredient.fromString(rawString).toString()
            assertEquals(expected, actual)
        }
    }

    @Test
    fun testMergeIngredients() {
        val ingredient1 = Ingredient.fromString("1/2 pound of wheat")
        val ingredient2 = Ingredient.fromString("1 3/4 pounds of wheat")
        val mergedIngredient =  ingredient1 + ingredient2

        assertEquals("2 1/4 lbs wheat", mergedIngredient.toString())

        val mergedIngredient2 = ingredient1 + ingredient1
        assertEquals("1 lbs wheat", mergedIngredient2.toString())
    }

    @Test
    fun testDefaultMergeGroceries() {
        val ingredients: List<String> = listOf(
            "1/2 pound of wheat",
            "1 3/4 pounds of wheat",
            "3/4 lb of wheat",
            "1/2 cup of milk",
            "1/2 cups of milk",
        )
            .map { Ingredient.fromString(it) }
            .groupBy { it.grocery }
            .map { (grocery, ingredients) ->
                val quantity = ingredients
                    .map { it.quantity }
                    .reduce { acc, quantity ->
                        acc + quantity
                    }
                val unit = ingredients[0].unit
                Ingredient(quantity, unit, grocery).toString()
            }

        assertEquals(
            listOf(
                "3 lbs wheat",
                "1 cup milk"
            ),
            ingredients
        )
    }

    @Test
    fun testLinkingMergeGroceries() {

        val groceryList: GroceryList = listOf(
            "3/4 lb of wheat",
            "1 3/4 pounds of wheat dude",
            "1/2 pound of that good good wheat",
            "1/2 cup of milk milk milk",
            "1/2 cups of milk",
        )
            .map { Ingredient.fromString(it) }
            .toGroceryList()

        val ingredients = groceryList.ingredients

        val wheatGroup: List<Ingredient> = listOf(ingredients[0], ingredients[1], ingredients[2])
        val milkMilkMilkGroup: List<Ingredient> = listOf(ingredients[3], ingredients[4])

        groceryList.mergeIngredients(wheatGroup)
        groceryList.mergeIngredients(milkMilkMilkGroup)

        val expected: List<String> = listOf("3 lbs wheat", "1 cup milk milk milk")
        assertEquals(expected, groceryList.getIngredientsAsStrings())
    }

    @Test
    fun testLinkingMergeFail() {
        val groceryList: GroceryList = listOf(
            "3/4 lb of wheat",
            "1/2 cups of milk",
        )
            .map { Ingredient.fromString(it) }
            .toGroceryList()


        assertThrows<IllegalStateException> {
            groceryList.mergeIngredients(groceryList.ingredients)
        }
    }

    @Test
    fun testMergeVolumeAndContainers() {
        val groceryList: GroceryList = listOf(
            "3 1/2 cups of chicken",
            "1 can chicken",
            "1 can chicken"
        )
            .map { Ingredient.fromString(it) }
            .toGroceryList()

        val expected = "6 can chicken"
        val actual = Ingredient.merge(groceryList.ingredients).toString()

        assertEquals(expected, actual)

    }

    @Test
    fun testPersistMerge() {
        val ingredients: List<Ingredient> = listOf(
            "3/4 lb of wheat",
            "1 3/4 pounds of wheat dude",
            "1/2 pound of that good good wheat",
            "1/2 cup of milk milk milk",
            "1/2 cups of milk",
            )
            .map { Ingredient.fromString(it) }

        val mergeMap = MergeMap(ingredients)
        val groceryList: GroceryList = ingredients.toGroceryList(mergeMap)

        val wheatGroup: List<Ingredient> = listOf(ingredients[0], ingredients[1], ingredients[2])
        val milkMilkMilkGroup: List<Ingredient> = listOf(ingredients[3], ingredients[4])

        groceryList.mergeIngredients(wheatGroup)
        groceryList.mergeIngredients(milkMilkMilkGroup)

        val separateGroceryList: GroceryList = ingredients.toGroceryList(mergeMap)
        assertEquals(groceryList.getIngredientsAsStrings(), separateGroceryList.getIngredientsAsStrings())
    }
}