package com.dix.fengine.app.mq

import com.dix.fengine.app.App
import java.util.concurrent.*

object InnerMQ : App, MQ {
    private val subscriptions = ConcurrentHashMap<String, BlockingQueue<Any>>()

    private fun ensureQueue(channel: String): BlockingQueue<Any> {
        val queue = subscriptions[channel] ?: LinkedBlockingQueue<Any>()
        subscriptions[channel] = queue
        return queue
    }

    override fun pub(channel: String, bs: ByteArray) {
        val queue = ensureQueue(channel)
        queue.offer(bs, 1000, TimeUnit.MILLISECONDS)
    }

    override fun sub(channel: String, queueName: String): BlockingQueue<Any> {
        return ensureQueue(channel)
    }

    override fun start() {
    }

    override fun stop() {
    }

}

private operator fun <K, V> ConcurrentHashMap<K, V>.set(channel: K, value: V) {
    put(channel, value)
}
