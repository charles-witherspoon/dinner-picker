package com.wtech.fitness.models

import kotlinx.coroutines.delay
import kotlinx.coroutines.runBlocking
import org.junit.jupiter.api.Test
import java.time.Duration
import java.time.LocalDateTime

class LogTests {

    @Test
    fun `test update log`(): Nothing = runBlocking {
        val log: EventLog<String> = EventLog(Duration.ofMillis(10), 5)

        log.add("a", LocalDateTime.now())
        delay(15)

        log.add("b", LocalDateTime.now())
        delay(15)

        TODO()
    }
}