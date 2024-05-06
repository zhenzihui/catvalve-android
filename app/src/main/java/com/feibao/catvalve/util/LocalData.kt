package com.feibao.catvalve.util

import com.tencent.mmkv.MMKV

val kv by lazy { MMKV.defaultMMKV() }
class LocalData {

    companion object {
        var deviceAddr: String set(v) { kv.encode("addr", v) }
            get() = kv.decodeString("addr")?:""
    }
}