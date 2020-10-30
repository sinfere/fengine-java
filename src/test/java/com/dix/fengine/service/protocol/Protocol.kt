package com.dix.fengine.service.protocol

import com.dix.codec.bkv.CodecUtil
import com.dix.fengine.app.parse.Parser
import com.google.common.primitives.Bytes
import java.nio.ByteBuffer
import java.util.*
import kotlin.experimental.and

class Protocol : Parser {

    private val frameHead = byteArrayOf(0xFA.toByte(), 0xFB.toByte())
    private val frameTail = byteArrayOf(0xFA.toByte(), 0xBB.toByte())

    override fun parse(buf: ByteArray): Parser.ParseResult {
        if (buf.size < frameHead.size) {
            return Parser.ParseResult(Parser.Result.Incomplete)
        }

        val headPos = Bytes.indexOf(buf, frameHead)
        if (headPos < 0) {
            return Parser.ParseResult(Parser.Result.Invalid, "frame head not found")
        }

        if (buf.size < headPos + 4) {
            return Parser.ParseResult(Parser.Result.Incomplete)
        }

        val buffer = ByteBuffer.wrap(buf)
        val length = buffer.getShort(headPos + 2)
        if (buf.size < headPos + 4 + length) {
            return Parser.ParseResult(Parser.Result.Incomplete)
        }

        if (!buf.copyOfRange(headPos + length + 2, headPos + length + 4).contentEquals(frameTail)) {
            return Parser.ParseResult(Parser.Result.Invalid, "frame tail invalid")
        }

        val partCheckSum = buf[headPos + length + 2 - 1]
        val calculatedChecksum = calculateChecksum(buf.copyOfRange(headPos + 2, headPos + 2 + length - 1))
        if (partCheckSum != calculatedChecksum) {
            return Parser.ParseResult(Parser.Result.Invalid, "invalid checksum")
        }

        val frameBuf = buf.copyOfRange(headPos, headPos + 4 + length)
        val pendingParseBuf = buf.copyOfRange(headPos + 4 + length, buf.size)
        val frame = decode(frameBuf)

        val clientId = frame["device_id"] as? String

        return Parser.ParseResult(Parser.Result.OK, null, frameBuf, pendingParseBuf, frame, clientId)
    }

    fun decode(buf: ByteArray): Map<String, Any> {
        val frame = mutableMapOf<String, Any>()
        val buffer = ByteBuffer.wrap(buf)

        val command = buffer.getShort(4)
        val sn = buffer.getInt(6)
        val type = buffer.get(10).toInt() and 0xFF
        val deviceId = CodecUtil.bytesToHex(buf.copyOfRange(11, 18))

        frame["command"] = command
        frame["sn"] = sn
        frame["type"] = type
        frame["device_id"] = deviceId

        return frame
    }
}

fun calculateChecksum(buf: ByteArray): Byte {
    var sum = 0
    for (b in buf) {
        sum += b.toUByte().toInt()
    }
    return (sum and 0xFF).toByte()
}