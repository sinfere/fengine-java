package com.dix.fengine.core

import com.dix.fengine.config.RedisBindModule
import com.google.inject.AbstractModule
import com.google.inject.Guice
import com.google.inject.Injector
import io.vertx.core.Vertx
import io.vertx.core.VertxOptions
import io.vertx.core.eventbus.EventBusOptions
import org.slf4j.LoggerFactory
import java.util.concurrent.CountDownLatch

object Core {
    private val logger = LoggerFactory.getLogger("Core")

    lateinit var vertx: Vertx
    private lateinit var injector: Injector

    val bootTime = System.currentTimeMillis()

    private val countDownLatch = CountDownLatch(1)

    private val baseModules = mutableListOf(
            RedisBindModule()
    )

    init {
        System.setProperty("vertx.logger-delegate-factory-class-name", "io.vertx.core.logging.Log4jLogDelegateFactory")
        System.setProperty("vertx.disableDnsResolver", "true")
    }

    fun boot() {
        boot(baseModules)
    }

    fun boot(modules: List<AbstractModule>) {
        CoreConfig.shared()
        logElapsed("load config")

        val eventBusOptions = EventBusOptions()
        eventBusOptions.isClustered = false

        val vertxOptions = VertxOptions()
        vertxOptions.isHAEnabled = false
        // vertxOptions.eventBusOptions = eventBusOptions

        vertx = Vertx.vertx(vertxOptions)
        logElapsed("init vertx")

        injector = Guice.createInjector(modules)
        logElapsed("init injector")
        CoreInjector.boot(modules)
    }


    private fun logElapsed(content: String) {
        logger.info("$content, elapsed ${System.currentTimeMillis() - bootTime}ms")
    }

    fun serve() {
        logElapsed("serve")
        countDownLatch.await()
    }

    fun shutdown() {
        countDownLatch.countDown()
    }
}