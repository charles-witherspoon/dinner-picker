package com.wtech.fitness.models

import java.time.Duration
import java.time.LocalDateTime


interface Item

interface Event<T>

interface Log<E> {
    fun add(event: E, insertionTime: LocalDateTime)
    fun getHistory(startingOffset: Int, numRecords: Int): List<E>
}

open class EventLog<E>(var maxCommitWait: Duration = Duration.ofMillis(500), var maxPageSize: Int = 1000): Log<E> {

    private val log: MutableList<E> = mutableListOf()

    override fun add(event: E, insertionTime: LocalDateTime) {
        log.add(event)
    }

    override fun getHistory(startingOffset: Int, numRecords: Int): List<E> {
        return log.subList(startingOffset, startingOffset + numRecords)
    }
}