package com.bahri.lovelypos

import android.app.Application
import com.bahri.lovelypos.di.appModule
import org.koin.android.ext.koin.androidContext
import org.koin.android.ext.koin.androidLogger
import org.koin.core.context.startKoin

class POSApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        startKoin {
            androidLogger()
            androidContext(this@POSApplication)
            modules(appModule)
        }
    }
}