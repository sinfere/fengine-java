package com.dix.fengine.common

import com.fasterxml.jackson.core.type.TypeReference
import com.fasterxml.jackson.databind.JavaType
import com.fasterxml.jackson.databind.Module
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.KotlinModule
import com.jayway.jsonpath.*
import com.jayway.jsonpath.spi.json.JacksonJsonProvider
import com.jayway.jsonpath.spi.json.JsonProvider
import com.jayway.jsonpath.spi.mapper.JacksonMappingProvider
import com.jayway.jsonpath.spi.mapper.MappingProvider
import org.slf4j.LoggerFactory
import java.util.*

object CoreJson {
    private val logger = LoggerFactory.getLogger(CoreJson::class.java)

    private val objectMapper = ObjectMapper().registerModule(KotlinModule())

    init {
        Configuration.setDefaults(object : Configuration.Defaults {
            private val jsonProvider: JsonProvider = JacksonJsonProvider()
            private val mappingProvider: MappingProvider = JacksonMappingProvider()
            override fun jsonProvider(): JsonProvider {
                return jsonProvider
            }

            override fun mappingProvider(): MappingProvider {
                return mappingProvider
            }

            override fun options(): Set<Option> {
                return EnumSet.noneOf(Option::class.java)
            }
        })
    }

    fun registerModule(module: Module) {
        objectMapper.registerModule(module)
    }

    fun encode(value: Any?): String? {
        return objectMapper.writeValueAsString(value)
    }

    fun <T> decode(jsonString: String, type: Class<T>?): T? {
        return objectMapper.readValue(jsonString, type)
    }

    fun <T> decode(json: String?, typeReference: TypeReference<T>?): T? {
        if (json == null || json.isEmpty()) return null
        return objectMapper.readValue(json, typeReference)
    }

    fun <T> decode(json: String?, javaType: JavaType?): T? {
        if (json == null || json.isEmpty()) return null
        return objectMapper.readValue(json, javaType)
    }

    fun <T> read(documentContext: DocumentContext, key: String?, type: Class<T>): T? {
        return documentContext.read(key, type)
    }

    fun <T> get(json: String?, key: String?, type: Class<T>): T? {
        return JsonPath.parse(json).read(key, type)
    }

    fun <T> read(documentContext: DocumentContext, key: String?, typeRef: TypeRef<T>): T? {
        return documentContext.read(key, typeRef)
    }

    fun <T> get(json: String?, key: String?, typeRef: TypeRef<T>): T? {
        return JsonPath.parse(json).read(key, typeRef)
    }

    fun <T> read(documentContext: DocumentContext, key: String?, type: Class<T>, defaultValue: T): T? {
        var v = read(documentContext, key, type)
        if (v == null) {
            v = defaultValue
        }
        return v
    }

    fun <T> get(json: String?, key: String?, type: Class<T>, defaultValue: T): T? {
        var v = get(json, key, type)
        if (v == null) {
            v = defaultValue
        }
        return v
    }

    fun <T> read(documentContext: DocumentContext, key: String?, typeRef: TypeRef<T>, defaultValue: T): T? {
        var v = read(documentContext, key, typeRef)
        if (v == null) {
            v = defaultValue
        }
        return v
    }

    fun <T> get(json: String?, key: String?, typeRef: TypeRef<T>, defaultValue: T): T? {
        var v = get(json, key, typeRef)
        if (v == null) {
            v = defaultValue
        }
        return v
    }
}