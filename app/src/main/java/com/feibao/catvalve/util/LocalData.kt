package com.feibao.catvalve.util

import com.tencent.mmkv.MMKV
import java.util.UUID

val kv by lazy { MMKV.defaultMMKV() }

class LocalData {

    companion object {
        const val DEVICE_NAME = "valve"
        var deviceAddr: String?
            set(v) {
                kv.encode("addr", v)
            }
            get() = kv.decodeString("addr")

        val sppUUID: UUID = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB")
    }
}