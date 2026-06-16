package com.example.kwarta

import android.app.Application
import com.example.kwarta.di.dataModule
import com.example.kwarta.di.repositoryModule
import com.example.kwarta.di.viewModelModule
import org.koin.android.ext.koin.androidContext
import org.koin.core.context.startKoin

class KwartaApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        
        startKoin {
            androidContext(this@KwartaApplication)
            modules(listOf(dataModule, repositoryModule, viewModelModule))
        }
    }
}
