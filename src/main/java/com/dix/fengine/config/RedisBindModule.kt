package com.dix.fengine.config

import com.dix.fengine.core.CoreConfig
import com.google.common.base.Strings
import com.google.inject.AbstractModule
import com.google.inject.Provides
import org.slf4j.LoggerFactory
import redis.clients.jedis.JedisPool
import redis.clients.jedis.JedisPoolConfig
import redis.clients.jedis.Protocol
import javax.inject.Singleton

class RedisBindModule : AbstractModule() {
    private val logger = LoggerFactory.getLogger(RedisBindModule::class.java)

    @Provides
    @Singleton
    fun provideJedisPool(): JedisPool {
        val host = CoreConfig.shared().getString(CoreConfig.RedisHost)
        val port = CoreConfig.shared().getInt(CoreConfig.RedisPort)
        var password: String? = CoreConfig.shared().getString(CoreConfig.RedisPassword)
        val poolMaxActive = CoreConfig.shared().getInt(CoreConfig.RedisPoolMaxActive)

        password = if (Strings.isNullOrEmpty(password)) null else password

        val jedisPoolConfig = JedisPoolConfig()
        jedisPoolConfig.maxTotal = poolMaxActive
        return JedisPool(jedisPoolConfig, host, port, Protocol.DEFAULT_TIMEOUT, password)
    }


}