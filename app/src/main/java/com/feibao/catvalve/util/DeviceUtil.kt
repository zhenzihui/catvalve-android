package com.feibao.catvalve.util

import android.Manifest
import android.annotation.SuppressLint
import android.app.Activity
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothManager
import android.bluetooth.BluetoothSocket
import android.companion.AssociationRequest
import android.companion.BluetoothDeviceFilter
import android.companion.CompanionDeviceManager
import android.content.Context
import android.content.Intent
import android.content.IntentSender
import android.content.pm.PackageManager
import android.util.Log
import androidx.core.app.ActivityCompat
import com.feibao.catvalve.R
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.IOException

const val REQUEST_ENABLE_BT = 11
const val DEVICE_SELECTED = 12

enum class ConnStatus(val desc: Int) {
    UNPAIRED(R.string.conn_unpair), CONNECTED(R.string.conn_connected), DISCONNECTED(R.string.conn_disconnected), CHECKING(
        R.string.conn_checking
    )
}

val deviceFilter: BluetoothDeviceFilter by lazy {
    BluetoothDeviceFilter.Builder()
//        .setNamePattern(Pattern.compile(LocalData.DEVICE_NAME))
        .build()
}
val pairReq by lazy {
    AssociationRequest.Builder().addDeviceFilter(deviceFilter).setSingleDevice(false).build()
}

class DeviceUtil {


    companion object {
        private var _dh: DeviceHelper? = null
        val deviceHelper get() = _dh!!

        // 申请蓝牙权限
        private fun requestBTPermission(ac: Activity) {
            ActivityCompat.requestPermissions(
                ac,
                arrayOf(Manifest.permission.BLUETOOTH_CONNECT),
                REQUEST_ENABLE_BT
            )
        }

        fun showNearbyDevices(context: Context) {
            val mgn =
                context.getSystemService(Context.COMPANION_DEVICE_SERVICE) as CompanionDeviceManager
            mgn.associate(pairReq, object : CompanionDeviceManager.Callback() {
                override fun onFailure(error: CharSequence?) {
                    "failed search bt".print()
                }

                override fun onDeviceFound(chooserLauncher: IntentSender) {
                    (context as Activity).startIntentSenderForResult(
                        chooserLauncher,
                        DEVICE_SELECTED,
                        null,
                        0,
                        0,
                        0
                    )
                }

            }, null)
        }

        private fun isBluetoothPermitted(ac: Activity): Boolean {
            return ActivityCompat.checkSelfPermission(
                ac,
                Manifest.permission.BLUETOOTH_CONNECT
            ) == PackageManager.PERMISSION_GRANTED

        }

        @SuppressLint("MissingPermission")
        fun turnOnBT(ac: Activity) {
            if (isBluetoothPermitted(ac)) {
                if (!deviceHelper.adaptor.isEnabled) {
                    val enableBtIntent = Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE)
                    ac.startActivityForResult(enableBtIntent, REQUEST_ENABLE_BT)
                }
            } else {
                requestBTPermission(ac)
            }
        }

        @SuppressLint("MissingPermission")
        fun connectDevice(address: String) {
            val btDevice = deviceHelper.adaptor.getRemoteDevice(address)

            CoroutineScope(Dispatchers.IO).launch {
                deviceHelper.socket = btDevice.createRfcommSocketToServiceRecord(LocalData.sppUUID)
                    .apply {
                        runCatching {
                            connect()
                        }.onFailure {
                            deviceHelper.connectionStatus = ConnStatus.DISCONNECTED
                        }
                    }
            }

        }


        @Synchronized
        fun initialize(context: Context) {
            if (_dh != null) {
                return
            }
            _dh = DeviceHelper(context.getSystemService(BluetoothManager::class.java))
        }
    }
}

@SuppressLint("MissingPermission")
class DeviceHelper(manager: BluetoothManager) {
    val adaptor = manager.adapter!!
    var socket: BluetoothSocket? = null
    var connectionStatus: ConnStatus = ConnStatus.CHECKING

    var deviceListener: DeviceListener? = null

    private suspend fun callStatusChange() {
        withContext(Dispatchers.Main) {
            deviceListener?.onConnStatusChanged(connectionStatus)
        }
    }

    init {

        CoroutineScope(Dispatchers.IO).launch {
            while (true) {
                if(LocalData.deviceAddr.isNullOrBlank()) {
                    connectionStatus = ConnStatus.UNPAIRED
                }
                "bond state: ${socket?.remoteDevice?.bondState}".print()

                if (socket?.isConnected != true) {
                    connectionStatus = ConnStatus.DISCONNECTED
                }
                if (socket?.isConnected == true && connectionStatus != ConnStatus.CONNECTED) {
                    connectionStatus = ConnStatus.CONNECTED
                }

                "current status: $connectionStatus".print()
                callStatusChange()

                if (socket?.isConnected != true) {
                    runCatching {

                        socket?.connect()
                    }.onFailure {
                        connectionStatus = ConnStatus.DISCONNECTED
                    }
                }
                Thread.sleep(5000)

            }
        }
    }
}

interface DeviceListener {
    fun onConnStatusChanged(connStatus: ConnStatus)

}

fun String.print() {
    Log.d("BT", this)
}
