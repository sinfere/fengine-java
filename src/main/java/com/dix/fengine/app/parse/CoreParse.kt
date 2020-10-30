package com.dix.fengine.app.parse

import com.dix.codec.bkv.CodecUtil
import com.dix.fengine.app.App
import com.dix.fengine.app.mq.CoreMQ
import com.dix.fengine.core.CoreConfig
import com.dix.fengine.packet.Packet
import com.google.common.base.Strings
import org.slf4j.LoggerFactory
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.Executors
import java.util.concurrent.TimeUnit

object CoreParse : App {
    private val logger = LoggerFactory.getLogger(CoreParse::class.java)

    private val parseExecutors = Executors.newFixedThreadPool(CoreConfig.shared().getInt("parse.runner"))
    private val protocolRegistry: MutableMap<Int, Parser> = ConcurrentHashMap()
    private val pendingParseBytes: MutableMap<String, ByteArray> = ConcurrentHashMap()

    override fun start() {

    }

    override fun stop() {
    }

    fun register(protocol: Int, parser: Parser) {
        protocolRegistry[protocol] = parser
    }

    private fun consume() {
        val queue = CoreMQ.subscribeClientPacketToParse()
        Thread {
            while (true) {
                try {
                    val payload = queue.poll(50, TimeUnit.MILLISECONDS) ?: continue
                    parseExecutors.submit {
                        processPacket(payload)
                    }
                } catch (e: Exception) {
                    logger.error("queue poll exception: {}", e.message)
                    e.printStackTrace()
                }
            }
        }
    }

    fun processPacket(data: Any) {
        try {
            parse(data) { out ->

            }
        } catch (e: Exception) {
            logger.info("parse packet exception: {}", e.message)
            e.printStackTrace()
        }
    }

    fun parse(data: Any, processor: (ParseFrameResult) -> Unit) {
        val payload = CoreMQ.getMessagePayload(data)
        val packet = Packet.unpack(payload)

        // val buf = Unpooled.buffer()
        val pendingParseBuf = getPendingParseBytes(packet) ?: byteArrayOf()
        val buf = concat(pendingParseBuf, packet.buf)

        val remainingBuf = parsePacket(packet, buf, processor)
        setPendingParseBytes(packet, remainingBuf)
    }

    private fun concat(vararg arrays: ByteArray): ByteArray {
        var length = 0
        for (array in arrays) {
            length += array.size
        }
        val result = ByteArray(length)
        var pos = 0
        for (array in arrays) {
            System.arraycopy(array, 0, result, pos, array.size)
            pos += array.size
        }
        return result
    }

    private fun parsePacket(packet: Packet, buf: ByteArray, processor: (ParseFrameResult) -> Unit): ByteArray {
        var protocols = protocolRegistry.keys.sorted()
        if (packet.protocol != 0) {
            protocols = listOf(packet.protocol)
        }

        logger.info("parse: ${CodecUtil.bytesToHex(buf)}")

        var currentBuf = buf.copyOf()

        while (true) {
            if (currentBuf.isEmpty()) {
                break
            }

            var continueParse = false
            var allFail = true

            protocols.forEach { protocol ->
                val parser = protocolRegistry[protocol]!!
                val result = try {
                    parser.parse(currentBuf)
                } catch (e: Exception) {
                    e.printStackTrace()
                    return@forEach
                }

                logger.info("parse result: $result")

                when (result.result) {
                    // 本次解析失败，但仍然可以尝试下一个解析器
                    Parser.Result.Invalid -> {

                    }

                    // 数据不够，等待更多数据
                    Parser.Result.Incomplete -> {
                        allFail = false
                        return currentBuf
                    }

                    // 解析成功，接着解析
                    Parser.Result.OK -> {
                        if (result.frame == null) {
                            logger.error("parse ok but frame is null")
                            return@forEach
                        }

                        if (result.frameBytes == null) {
                            logger.error("parse ok but frame bytes is null")
                            return@forEach
                        }

                        val frame = result.frame
                        val frameBytes = result.frameBytes

                        allFail = false
                        currentBuf = result.pendingParseBytes ?: byteArrayOf()
                        packet.protocol = protocol

                        processor(ParseFrameResult(frame, frameBytes, result.clientId))

                        // 不用继续本循环了，可以继续大循环
                        continueParse = true
                        return@forEach
                    }
                }
            }

            // 没有解析器可以解析成功
            if (allFail) {
                currentBuf = byteArrayOf()
            }

            // 可以直接继续解析，不需要丢弃一个字节（全部解析失败的情况下）
            if (continueParse) {
                continue
            }

            // 尝试以下一个字节为起点解析，前进1
            if (currentBuf.isNotEmpty()) {
                currentBuf = currentBuf.copyOfRange(1, currentBuf.size)
            }
        }

        return currentBuf
    }

    private fun getPendingParseBytes(packet: Packet): ByteArray? {
        val key = packet.sessionId
        if (Strings.isNullOrEmpty(key)) {
            return null
        }

        return pendingParseBytes[key]
    }

    private fun setPendingParseBytes(packet: Packet, buf: ByteArray) {
        val key = packet.sessionId
        if (Strings.isNullOrEmpty(key)) {
            return
        }

        pendingParseBytes[key] = buf
    }

    data class ParseFrameResult(val frame: Any, val frameBytes: ByteArray, val clientId: String?) {
        override fun equals(other: Any?): Boolean {
            if (this === other) return true
            if (javaClass != other?.javaClass) return false

            other as ParseFrameResult

            if (frame != other.frame) return false
            if (!frameBytes.contentEquals(other.frameBytes)) return false
            if (clientId != other.clientId) return false

            return true
        }

        override fun hashCode(): Int {
            var result = frame.hashCode()
            result = 31 * result + frameBytes.contentHashCode()
            result = 31 * result + (clientId?.hashCode() ?: 0)
            return result
        }

        companion object {

        }
    }
}