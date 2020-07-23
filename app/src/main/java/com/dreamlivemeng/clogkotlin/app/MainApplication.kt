package com.dreamlivemeng.clogkotlin.app

import android.app.Application
import com.dreamlivemeng.cloglibrary.CollectLog

class MainApplication : Application() {

    override fun onCreate() {
        super.onCreate()
        CollectLog.getInstance().init(this)
    }
}