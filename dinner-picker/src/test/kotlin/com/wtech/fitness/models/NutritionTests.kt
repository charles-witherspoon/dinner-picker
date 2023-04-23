package com.wtech.fitness.models

import com.wtech.fitness.models.quantity.Quantity
import com.wtech.fitness.models.quantity.Unit
import com.wtech.fitness.plugins.User
import kotlinx.coroutines.delay
import kotlinx.coroutines.runBlocking
import org.junit.jupiter.api.Test
import java.time.Duration
import java.time.LocalDateTime
import kotlin.test.assertEquals
import kotlin.test.assertNotEquals

class NutritionTests {

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

    @Test
    fun `get consumption events in window`() = runBlocking {
        val windowStart = LocalDateTime.now()

        val foodInWindow = Consumption(Food(potatoNutrition), LocalDateTime.now())
        val foodOutOfWindow = Consumption(Food(bananaNutrition), windowStart - Duration.ofMillis(1))

        val foodHistory = ConsumptionLog(listOf(
            foodOutOfWindow,
            foodInWindow
        ))

        val expected: EventLog<Consumption> = ConsumptionLog(listOf(foodInWindow))
        assertEquals(expected, foodHistory.refreshHistory(windowStart, windowStart + Duration.ofMillis(1)))
    }


    @Test
    fun testGetNutritionLevels() {
        val foodHistory = ConsumptionLog(listOf(
            Consumption(Food(bananaNutrition), LocalDateTime.now()),
            Consumption(Food(potatoNutrition), LocalDateTime.now()),
        ))
        val user = User("user", 30, foodHistory)
        val expected: Map<String, Quantity> = mapOf(
            Pair("potassium", Quantity(1042))
        )
        assertEquals(expected, user.nutritionLevels)
    }

    @Test
    fun testCheckNutritionLevelsAfterEating() {
        val user = User("user", 30, ConsumptionLog(
            listOf(
                Consumption(Food(bananaNutrition), LocalDateTime.now())
            )
        ))

        val levelsBeforeEating = user.nutritionLevels

        user.ate(Food(potatoNutrition), LocalDateTime.now())
        val levelsAfterEating = user.nutritionLevels

        assertNotEquals(levelsBeforeEating, levelsAfterEating)

        val currentLevels = user.nutritionLevels
        assertEquals(levelsAfterEating, currentLevels)
    }

//    @Test
//    fun ``() {
//
//    }

    @Test
    fun testCheckNutritionLevelsAfterTimeElapsed() = runBlocking {
        val aSecondBeforeExit = LocalDateTime.now() - Duration.ofDays(1) + Duration.ofSeconds(1)
        val foodHistory = ConsumptionLog(listOf(
            Consumption(Food(bananaNutrition), aSecondBeforeExit),
            Consumption(Food(potatoNutrition), LocalDateTime.now())
        ))
        val user = User("user", 30, foodHistory)
        var expected: Map<String, Quantity> = mapOf(
            Pair("potassium", Quantity(1042))
        )
        assertEquals(expected, user.nutritionLevels)
        delay(1000)

        expected = mapOf(
            Pair("potassium", Quantity(620))
        )
        assertEquals(expected, user.nutritionLevels)
    }

    @Test
    fun testGetMealSatisfyingNutritionRequirements() {
        // nutrient, target, limit
        val nutritionRequirements: List<Nutrition> = listOf(
            Nutrition(
                mapOf(
                    Pair("potassium", Quantity(1040))
                )
            )
        )
        val now = LocalDateTime.of(2023, 6, 7, 0, 0)
        val expected = Meal(listOf(Food(bananaNutrition), Food(potatoNutrition)))
        val actual: Meal = Meal.satisfying(nutritionRequirements)
        assertEquals(expected, actual)
    }

    @Test
    fun testSuggestFood() {
        TODO()
    }

    @Test
    fun testFoundationFoodToFood() {
        TODO()
    }
}