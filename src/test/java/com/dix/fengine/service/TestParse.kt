package com.dix.fengine.service

import com.dix.codec.bkv.CodecUtil
import com.dix.fengine.app.parse.CoreParse
import com.dix.fengine.packet.Packet
import com.dix.fengine.service.protocol.Protocol
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

class TestParse {

    @BeforeEach
    fun init() {
        CoreParse.register(1, Protocol())
    }

    private fun encodePacket(hex: String): ByteArray {
        val buf = CodecUtil.hexToBytes(hex)
        return Packet(System.currentTimeMillis(), 0, System.currentTimeMillis(), "", "1", "", 0, buf).encode()
    }

    @Test
    fun testParse() {
        var successCount = 0

        CoreParse.parse(encodePacket("fafb001600150000000000995039211414050002011b01bafabb")) {
            successCount++
        }

        Assertions.assertEquals(1, successCount)

        CoreParse.parse(encodePacket("fafb001600150000000000")) {
            successCount++
        }

        Assertions.assertEquals(1, successCount)

        CoreParse.parse(encodePacket("995039211414050002011b01bafabb")) {
            successCount++
        }

        Assertions.assertEquals(2, successCount)

        CoreParse.parse(encodePacket("fafb001600150000000000995039211414050002011b01bafabbfafb001600150000000000995039211414050002011b01bafabb")) {
            successCount++
        }

        Assertions.assertEquals(4, successCount)

        CoreParse.parse(encodePacket("aafafb001600150000000000995039211414050002011b01bafabbdd0032fafb001600150000000000995039211414050002011b01bafabb")) {
            successCount++
        }

        Assertions.assertEquals(6, successCount)

    }
}