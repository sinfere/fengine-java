package com.dix.fengine.core

import com.google.inject.AbstractModule
import com.google.inject.Guice
import com.google.inject.Injector
import com.google.inject.Key
import com.google.inject.name.Names

object CoreInjector {
    private lateinit var injector: Injector

    fun boot(modules: List<AbstractModule>) {
        injector = Guice.createInjector(modules)
    }

    fun <T> getInstance(type: Class<T>?): T? {
        return injector.getInstance(type)
    }

    fun <T> getInstance(type: Class<T>?, name: String?): T? {
        return injector.getInstance(Key.get(type, Names.named(name)))
    }
}