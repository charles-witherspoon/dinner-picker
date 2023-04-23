package com.wtech.fitness.db

import org.jetbrains.exposed.sql.transactions.transaction
import org.junit.jupiter.api.Test
import kotlin.test.assertEquals

class RecipeTests: PGTests() {

    @Test
    fun testCreateRecipe() {
        transaction {
            val name = "test-recipe"
            val dao = RecipeDAO()
            val id = dao.addRecipe(name)

            val expected = Recipe(id, name)
            val actual = dao.getRecipe(id)

            assertEquals(expected, actual)
        }
    }
}