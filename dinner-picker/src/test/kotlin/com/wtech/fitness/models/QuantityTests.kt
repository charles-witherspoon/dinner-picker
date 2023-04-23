package com.wtech.fitness.models

import com.wtech.fitness.models.quantity.Fraction
import com.wtech.fitness.models.quantity.Quantity
import com.wtech.fitness.models.quantity.Unit
import com.wtech.fitness.models.quantity.decimal.fromDecimal
import com.wtech.fitness.models.quantity.plus
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class  QuantityTests {
    @Test
    fun testFractionEquals() {
        assertEquals(Fraction(0,0), Fraction.EMPTY_FRACTION)
    }

    @Test
    fun testPlus() {

        val expected = Quantity(0, Fraction.EMPTY_FRACTION)
        val actual = expected + expected
        assertEquals(expected, actual)

        val fractionA = Fraction(1, 4)
        assertEquals(
            Quantity(0, fractionA),
            fractionA + Fraction.EMPTY_FRACTION
        )

        val fractionB = Fraction(5, 4)
        assertEquals(
            Quantity(1, Fraction(1, 2)),
            fractionA + fractionB
        )
    }

    @Test
    fun testCreateGroceryList() {
        val ingredients: List<Ingredient> = listOf(
            "1 cup this",
            "1/4 cup should still",
            "1 cup merge",
            "1/2 cup reduced-sodium chicken broth",
            "1 can (14-1/2 ounces) reduced-sodium chicken broth",
            "1/2 cup reduced-sodium chicken broth, divided",
            "2 bacon strips, cooked and crumbled",
        ).map { Ingredient.fromString(it) }

        val expected: Set<String> = setOf(
            "2 1/4 cup chicken broth",
            "2 can reduced-sodium chicken broth",
            "2 bacon strips, cooked and crumbled"
        )

        val groceryList: GroceryList = ingredients.toGroceryList()

        val chickenBroth: List<Ingredient> = listOf(ingredients[0], ingredients[1], ingredients[2])
        groceryList.mergeIngredients(chickenBroth, "chicken broth")

        val reducedSodiumChickenBroth: List<Ingredient> = listOf(ingredients[3], ingredients[4], ingredients[5])
        groceryList.mergeIngredients(reducedSodiumChickenBroth, "reduced-sodium chicken broth")

        assertEquals(expected, groceryList.getIngredientsAsStrings().toSet())
    }

    @Test
    fun testAddQuantitiesWithDifferentUnits() {
        val a = Quantity("1 1/2 cups of premium blend cheese", Unit.TSP)
        val b = Quantity("1 medium apple")
        assertThrows<IllegalArgumentException> {
            a + b
        }
    }

    @Test
    fun testConvertUnits() {
        val teaspoons = Quantity(48, Fraction.EMPTY_FRACTION, Unit.TSP)
        val tablespoons = Quantity(16, Fraction.EMPTY_FRACTION, Unit.TBSP)
        val ounces = Quantity(8, Fraction.EMPTY_FRACTION, Unit.OUNCE)
        val pints = Quantity(0, Fraction(1, 2), Unit.PINT)
        val quarts = Quantity(0, Fraction(1, 4), Unit.QUART)
        val gallons = Quantity(0, Fraction(1, 16), Unit.GALLON)
        val milliliters = Quantity(250, Fraction.EMPTY_FRACTION, Unit.MILLILITER)
        val liters = Quantity(0, Fraction(1, 4), Unit.LITER)

        val quantities: List<Quantity> = listOf(teaspoons, tablespoons, ounces, pints, quarts, gallons, milliliters, liters)
        val cups = Quantity(1, Fraction.EMPTY_FRACTION, Unit.CUP)

        assertTrue(quantities.all { it == cups })
    }

    @Test
    fun testRepeatingDecimalToQuantity() {
        val expected = Quantity(1, Fraction(1, 3))
        val double: Double = 1 + (1.0 / 3.0)
        assertEquals(expected, Quantity.fromDecimal(double, expected.unit))
    }

    @Test
    fun testEndsRepeatingDecimalToQuantity() {
        val expected = Quantity(7, Fraction(1, 12))
        val double: Double = 7 + (1.0 / 12.0)
        assertEquals(expected, Quantity.fromDecimal(double, expected.unit))
    }

    @Test
    fun testRepeatingPatternToQuantity() {
        val expected = Quantity(14, Fraction(142857, 999999))
        val double: Double = 14 + (142857 / 999999.0)
        assertEquals(expected, Quantity.fromDecimal(double, expected.unit))
    }

    @Test
    fun testTerminatingDecimalToQuantity() {
        val expected = Quantity(1, Fraction(1, 4))
        val double: Double = 1 + (1.0 / 4.0)
        assertEquals(expected, Quantity.fromDecimal(double, expected.unit))
    }
}