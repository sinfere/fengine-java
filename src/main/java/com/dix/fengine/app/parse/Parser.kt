package com.dix.fengine.app.parse

interface Parser {
    fun parse(buf: ByteArray): ParseResult

    enum class Result(val value: Int) {
        OK(1),
        Invalid(2),
        Incomplete(3),
    }

    data class ParseResult(
            val result: Result,
            val errorMessage: String? = null,
            val frameBytes: ByteArray? = null,
            val pendingParseBytes: ByteArray? = null,
            val frame: Any? = null,
            val clientId: String? = null
    )
}