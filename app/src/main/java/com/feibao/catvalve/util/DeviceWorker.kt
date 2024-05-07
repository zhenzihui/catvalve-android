package com.feibao.catvalve.util

import android.content.Context
import androidx.work.Worker
import androidx.work.WorkerParameters
import org.greenrobot.eventbus.EventBus


class DeviceWorker(private val context: Context, params: WorkerParameters): Worker(context, params) {

    private val eventBus = EventBus.getDefault().apply {
        register(context)
    }

    override fun doWork(): Result {


        return Result.success()


    }

    override fun onStopped() {
        super.onStopped()
        EventBus.getDefault().unregister(context)
    }



    fun handleConnection() {



    }
}