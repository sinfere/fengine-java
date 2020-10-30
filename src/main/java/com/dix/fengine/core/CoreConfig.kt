package com.dix.fengine.core

import com.typesafe.config.Config
import com.typesafe.config.ConfigFactory

object CoreConfig {
    const val HttpPort = "http.port"

    const val Http2Port = "http2.port"
    const val Http2KeyPath = "http2.key-path"
    const val Http2CertPath = "http2.cert-path"

    const val DBDriverClassName = "db.driver-class-name"
    const val DBUrl = "db.url"
    const val DBUsername = "db.username"
    const val DBPassword = "db.password"
    const val DBPoolMaxActive = "db.pool.max-active"

    const val RedisKeyPrefix = "redis.key-prefix"
    const val RedisHost = "redis.host"
    const val RedisPort = "redis.port"
    const val RedisPassword = "redis.password"
    const val RedisDataBase = "redis.database"
    const val RedisPoolMaxActive = "redis.pool.max-active"

    private val instance = ConfigFactory.defaultApplication()
            .withFallback(ConfigFactory.defaultOverrides())
            .withFallback(ConfigFactory.load("app.conf"))
            .withFallback(ConfigFactory.defaultReference())

    fun shared(): Config {
        return instance
    }

}