package com.feibao.catvalve

import android.app.Application
import com.feibao.catvalve.util.DeviceUtil
import com.tencent.mmkv.MMKV


class App : Application() {

    override fun onCreate() {
        super.onCreate()
        MMKV.initialize(this)
        DeviceUtil.initialize(this)

    }
}