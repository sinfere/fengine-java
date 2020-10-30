package com.dix.fengine.app.mq

import com.dix.fengine.app.App
import java.util.concurrent.BlockingQueue

interface MQ : App {
    fun pub(channel: String, bs: ByteArray)
    fun sub(channel: String, queueName: String): BlockingQueue<Any>
}