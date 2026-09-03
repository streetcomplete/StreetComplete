package de.westnordost.streetcomplete

import org.koin.core.context.startKoin

// called from iOSApp.swift
fun initKoin() {
    val koinApp = startKoin {
        modules(iosModule, commonModule)
    }
    val koin = koinApp.koin
    koin.get<ApplicationInitializer>().initialize()
}
