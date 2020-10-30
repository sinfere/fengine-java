package com.dix.fengine.app.connect

import com.dix.fengine.app.App
import io.vertx.core.AbstractVerticle
import io.vertx.core.Future
import io.vertx.core.Promise
import javax.inject.Inject

class ConnectApp @Inject constructor(
) : App, AbstractVerticle() {

    override fun start() {
        val future = Future.future<Void> { promise -> start(promise) }
    }

    override fun start(startFuture: Promise<Void>) {
        try {


            startFuture.complete()
        } catch (ex: Exception) {
            startFuture.fail(ex)
        }
    }

    override fun stop() {
        val future = Future.future<Void> { promise -> stop(promise) }
    }

    override fun stop(stopFuture: Promise<Void>) {
        try {
            stopFuture.complete()
        } catch (ex: java.lang.Exception) {
            stopFuture.fail(ex)
        }
    }

}