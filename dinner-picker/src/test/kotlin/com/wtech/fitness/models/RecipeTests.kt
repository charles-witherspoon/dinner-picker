package com.wtech.fitness.models

import com.wtech.fitness.models.quantity.Quantity
import com.wtech.fitness.models.quantity.Unit
import org.junit.jupiter.api.Test
import kotlin.test.assertEquals


class RecipeTests {
    private val recipeString = """
        Ingredients
        - 1/2 cup panko bread crumbs
        - 1/2 cup potato sticks, crushed
        - 1/2 cup crushed cheese crackers
        - 1/4 cup grated Parmesan cheese
        - 2 bacon strips, cooked and crumbled
        - 2 teaspoons minced fresh chives
        - 1/4 cup butter, melted
        - 1 tablespoon sour cream
        - 1 pound chicken tenderloins
        - Additional sour cream and chives

        Directions
        1. Preheat air fryer to 400°. In a shallow bowl, combine the first 6 ingredients. In another shallow bowl, whisk butter and sour cream. Dip chicken in butter mixture, then in crumb mixture, patting to help coating adhere.
        2. In batches, arrange chicken in a single layer on greased tray in air-fryer basket; spritz with cooking spray. Cook until coating is golden brown and chicken is no longer pink, 7-8 minutes on each side. Serve with additional sour cream and chives.

        Nutrition Facts
        1 serving: 256 calories, 14g fat (7g saturated fat), 84mg cholesterol, 267mg sodium, 6g carbohydrate (0 sugars, 0 fiber), 29g protein.

        Link
        https://www.tasteofhome.com/recipes/air-fryer-chicken-tenders/
        """.trimIndent()

    @Test
    fun testPrintRecipe() {
        val expected = """
            Test Recipe
            Ingredients
            - 1/2 cup panko bread crumbs
            - 1/2 cup potato sticks, crushed
            - 1/2 cup crushed cheese crackers
            - 1/4 cup grated Parmesan cheese
            - 2 bacon strips, cooked and crumbled
            - 2 tsp minced fresh chives
            - 1/4 cup butter, melted
            - 1 tbsp sour cream
            - 1 lbs chicken tenderloins
            - Additional sour cream and chives
            
            Directions
            1. Preheat air fryer to 400°. In a shallow bowl, combine the first 6 ingredients. In another shallow bowl, whisk butter and sour cream. Dip chicken in butter mixture, then in crumb mixture, patting to help coating adhere.
            2. In batches, arrange chicken in a single layer on greased tray in air-fryer basket; spritz with cooking spray. Cook until coating is golden brown and chicken is no longer pink, 7-8 minutes on each side. Serve with additional sour cream and chives.
            
            Nutrition Facts
            1 serving: 256 calories, 14g fat (7g saturated fat), 84mg cholesterol, 267mg sodium, 6g carbohydrate (0 sugars, 0 fiber), 29g protein.
            
            Link
            https://www.tasteofhome.com/recipes/air-fryer-chicken-tenders/
        """.trimIndent()

        assertEquals(expected, Recipe.fromString("Test Recipe", recipeString).toString())
    }

    @Test
    fun testCreateNutrition() {
        val expected: Map<String, Quantity> = mapOf(
            Pair("serving", Quantity(1)),
            Pair("calories", Quantity(256)),
            Pair("fat", Quantity(14, Unit.GRAM)),
            Pair("saturated_fat", Quantity(7, Unit.GRAM)),
            Pair("cholesterol", Quantity(84, Unit.MILLIGRAM)),
            Pair("sodium", Quantity(267, Unit.MILLIGRAM )),
            Pair("carbohydrate", Quantity(6, Unit.GRAM)),
            Pair("sugars", Quantity(0)),
            Pair("fiber", Quantity(0)),
            Pair("protein", Quantity(29, Unit.GRAM)),
        )

        val actual = Recipe.fromString("Test Recipe", recipeString)
            .nutrition
            .facts

        assertEquals(expected, actual)
    }
}