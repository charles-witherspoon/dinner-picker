package com.wtech.fitness.plugins

import com.wtech.fitness.models.Food
import com.wtech.fitness.models.ConsumptionLog
import com.wtech.fitness.models.quantity.Quantity
import kotlinx.coroutines.Dispatchers
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq
import org.jetbrains.exposed.sql.transactions.experimental.newSuspendedTransaction
import org.jetbrains.exposed.sql.transactions.transaction
import java.time.LocalDateTime


class User(val name: String, val age: Int, foodHistory: ConsumptionLog) {

    private val _foodHistory = foodHistory

    val foodHistory: ConsumptionLog get() {
        return _foodHistory.apply {
            refreshHistory(LocalDateTime.now(), LocalDateTime.now())
        }
    }

    val nutritionLevels: Map<String, Quantity> get() = foodHistory.nutrition

    fun ate(food: Food, ingestedAt: LocalDateTime) {
        foodHistory.add(food, ingestedAt)
    }
}
class UserService(private val database: Database) {
    object Users : Table() {
        val id = integer("id").autoIncrement()
        val name = varchar("name", length = 50)
        val age = integer("age")

        override val primaryKey = PrimaryKey(id)
    }

    init {
        transaction(database) {
            SchemaUtils.create(Users)
        }
    }

    suspend fun <T> dbQuery(block: suspend () -> T): T =
        newSuspendedTransaction(Dispatchers.IO) { block() }

    suspend fun create(user: User): Int = dbQuery {
        Users.insert {
            it[name] = user.name
            it[age] = user.age
        }[Users.id]
    }

    suspend fun read(id: Int): User? {
        return dbQuery {
            Users.select { Users.id eq id }
                .map { User(it[Users.name], it[Users.age], ConsumptionLog(listOf())) }
                .singleOrNull()
        }
    }

    suspend fun update(id: Int, user: User) {
        dbQuery {
            Users.update({ Users.id eq id }) {
                it[name] = user.name
                it[age] = user.age
            }
        }
    }

    suspend fun delete(id: Int) {
        dbQuery {
            Users.deleteWhere { Users.id.eq(id) }
        }
    }
}