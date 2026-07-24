package com.toolly.foundation

fun interface ToollyClock {
    fun nowEpochMillis(): Long
}

fun interface OpaqueIdGenerator {
    /**
     * Returns a lower-case UUID string without embedding user or provider data.
     */
    fun newId(): String
}
