package com.dix.fengine.packet

import com.dix.codec.bkv.BKV
import java.lang.Exception

open class Packet(
        val id: Long,
        val type: Int,
        val time: Long,
        val serverId: String,
        val sessionId: String,
        val clientId: String,
        var protocol: Int,
        val buf: ByteArray
) {
    fun pack(): ByteArray {
        val bkv = BKV()
        bkv.add(keyId, id)
        bkv.add(keyType, type)
        bkv.add(keyTime, time)
        bkv.add(keyServerId, serverId)
        bkv.add(keySessionId, sessionId)
        bkv.add(keyClientId, clientId)
        bkv.add(keyProtocol, protocol)
        bkv.add(keyBuf, buf)
        return bkv.pack()
    }

    companion object {
        private const val keyId = 1L
        private const val keyType = 2L
        private const val keyTime = 3L
        private const val keyServerId = 4L
        private const val keySessionId = 5L
        private const val keyClientId = 6L
        private const val keyProtocol = 7L
        private const val keyBuf = 8L

        fun unpack(data: ByteArray): Packet {
            val result = BKV.unpack(data)
            val bkv = result.bkv
            val id = bkv.getNumberValue(keyId) ?: throw Exception("field[id] required")
            val type = bkv.getNumberValue(keyType) ?: throw Exception("field[type] required")
            val time = bkv.getNumberValue(keyTime) ?: throw Exception("field[time] required")
            val serverId = bkv.getStringValue(keyServerId) ?: throw Exception("field[server_id] required")
            val sessionId = bkv.getStringValue(keySessionId) ?: throw Exception("field[session_id] required")
            val clientId = bkv.getStringValue(keyClientId) ?: throw Exception("field[client_id] required")
            val protocol = bkv.getNumberValue(keyProtocol) ?: throw Exception("field[protocol] required")
            val buf = bkv.get(keyBuf)?.value ?: throw Exception("field[buf] required")
            return Packet(id, type.toInt(), time, serverId, sessionId, clientId, protocol.toInt(), buf)
        }

        fun encodeFramePacket(packet: Packet, buf: ByteArray): Packet {
            return Packet(packet.id, PacketType.Frame.value, packet.time, packet.serverId, packet.sessionId, packet.clientId, packet.protocol, buf)
        }
    }
}

enum class PacketType(val value: Int) {
    Raw(0),
    Frame(1),
    Event(2),
    Command(3),
}

/**
 * @param frameBuf client frame source bytes
 * @param frameBytes encoded frame object after parse, normally json
 */
class ProcessFramePayload(val frameBuf: ByteArray, val frameBytes: ByteArray, val clientId: String?) {
    fun pack(): ByteArray {
        val bkv = BKV()
        bkv.add(keyFrameBuf, frameBuf)
        bkv.add(keyFrameBytes, frameBytes)
        if (clientId != null) {
            bkv.add(keyClientId, clientId)
        }
        return bkv.pack()
    }

    companion object {
        private const val keyFrameBuf = 1L
        private const val keyFrameBytes = 2L
        private const val keyClientId = 3L

        fun unpack(data: ByteArray): ProcessFramePayload {
            val result = BKV.unpack(data)
            val bkv = result.bkv
            val frameBuf = bkv.get(keyFrameBuf)?.value ?: throw Exception("field[frame_buf] required")
            val frameBytes = bkv.get(keyFrameBytes)?.value ?: throw Exception("field[frame_bytes] required")
            val clientId = bkv.getStringValue(keyClientId)
            return ProcessFramePayload(frameBuf, frameBytes, clientId)
        }
    }
}