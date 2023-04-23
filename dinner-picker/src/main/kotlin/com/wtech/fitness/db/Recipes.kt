package com.wtech.fitness.db

import org.jetbrains.exposed.dao.id.IntIdTable
import org.jetbrains.exposed.sql.*

object Recipes : IntIdTable() {
    val name: Column<String> = varchar("name", 100)
}

data class Recipe(val id: Int, val name: String)

class RecipeDAO() {
    fun addRecipe(name: String): Int {
        return Recipes.insertAndGetId {
            it[this.name] = name
        }.value
    }

    fun getRecipe(id: Int): Recipe {
        return Recipes.select { Recipes.id eq id }
            .firstOrNull()
            ?.run {
                Recipe(this[Recipes.id].value, this[Recipes.name])
            } ?: error("")
    }
}
