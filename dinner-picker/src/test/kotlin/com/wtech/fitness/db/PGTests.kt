package com.wtech.fitness.db

import org.jetbrains.exposed.sql.Database
import org.jetbrains.exposed.sql.SchemaUtils
import org.jetbrains.exposed.sql.deleteAll
import org.jetbrains.exposed.sql.transactions.transaction
import org.junit.jupiter.api.BeforeEach
import org.postgresql.ds.PGSimpleDataSource

abstract class PGTests {

    private val dataSource = PGSimpleDataSource().apply {
        user = "workoutapp"
        password = "ppatuokrow"
        databaseName = "workout-app"
        portNumbers = intArrayOf(5433)
    }

    private val database = Database.connect(dataSource)

    @BeforeEach
    fun resetDBs() {
        val tables = listOf(Recipes)
        transaction {
            tables.forEach {
                SchemaUtils.createMissingTablesAndColumns(it)
                it.deleteAll()
            }
        }
    }
}