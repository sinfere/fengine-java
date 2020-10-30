package com.dix.fengine.app.mq

import com.dix.fengine.app.App
import com.dix.fengine.core.CoreConfig
import java.util.concurrent.BlockingQueue

object CoreMQ : App, MQ {
    private val vendor = CoreConfig.shared().getString("mq.vendor")
    private val prefix = CoreConfig.shared().getString("mq.prefix")

    private fun mq(): MQ {
        return when (vendor) {
            "inner" -> InnerMQ
            else -> throw Exception("unsupported mq vendor[$vendor]")
        }
    }

    override fun start() {
        mq().start()
    }

    override fun stop() {
        mq().stop()
    }

    override fun pub(channel: String, bs: ByteArray) {
        mq().pub(channel, bs)
    }

    override fun sub(channel: String, queueName: String): BlockingQueue<Any> {
        return mq().sub(channel, queueName)
    }

    fun subscribeClientPacketToParse(queueName: String = ""): BlockingQueue<Any> {
        return sub(getChannelNameOfClientPacketToParse(), queueName)
    }

    fun subscribeClientPacketToProcess(queueName: String = ""): BlockingQueue<Any> {
        return sub(getChannelNameOfClientPacketToProcess(), queueName)
    }


    fun publishClientPacketToParse(bs: ByteArray) {
        pub(getChannelNameOfClientPacketToParse(), bs)
    }

    fun publishClientPacketToProcess(bs: ByteArray) {
        pub(getChannelNameOfClientPacketToProcess(), bs)
    }



    fun getChannelNameOfClientPacketToParse(): String {
        return "_.$prefix.channel.client.packet.parse"
    }

    fun getChannelNameOfClientPacketToProcess(): String {
        return "_.$prefix.channel.client.packet.process"
    }

    fun getChannelNameOfClientPacketToPersist(): String {
        return "_.$prefix.channel.client.packet.persist"
    }

    fun getChannelNameOfServerPacket(): String {
        return "_.$prefix.channel.server.packet.write"
    }

    fun getChannelNameOfServerPacket(serverId: String): String {
        return "_.$prefix.channel.server.$serverId.packet.write"
    }

    fun getChannelNameOfClientFrameToRelay(): String {
        return "$prefix.channel.frame.relay"
    }

    fun getChannelNameOfClientEvent(): String {
        return "$prefix.channel.event"
    }

    fun getMessagePayload(data: Any): ByteArray {
        return when(data) {
            is ByteArray -> data
            else -> throw Exception("unsupported payload")
        }
    }
}

