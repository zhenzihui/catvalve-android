package com.feibao.catvalve.activity

import android.app.Activity
import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothSocket
import android.companion.CompanionDeviceManager
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.feibao.catvalve.CameraValveActivity
import com.feibao.catvalve.R
import com.feibao.catvalve.databinding.ActivityBtBinding
import com.feibao.catvalve.util.BluetoothUtil
import com.feibao.catvalve.util.DEVICE_SELECTED
import com.feibao.catvalve.util.LocalData
import com.feibao.catvalve.util.REQUEST_ENABLE_BT

class BtActivity : AppCompatActivity() {

    private var _bd: ActivityBtBinding? = null
    val bd get() = _bd!!

    private var _btUtil: BluetoothUtil? = null
    private val btUtil get() = _btUtil!!

    val vm: BtViewModel by viewModels()
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        _bd = ActivityBtBinding.inflate(layoutInflater)



        bd.startServiceButton.setOnClickListener {
            Intent(this, CameraValveActivity::class.java)
                .apply {
                    startActivity(this)
                }
        }
        _btUtil = BluetoothUtil(this)
        setContentView(bd.root)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
        btUtil.turnOnBT()
        setButtons()
        bd.bindDeviceButton.setOnClickListener {
            if(vm.bleAddr.value.isNullOrBlank()) {
                btUtil.showPairDevice()
            } else {
                btUtil.connectDevice(LocalData.deviceAddr!! ) {
                    vm.socket.value = it
                    vm.bleAddr.value = LocalData.deviceAddr
                }
            }
        }
        //尝试链接蓝牙
        vm.connectBle(btUtil)



    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)

        if (requestCode == REQUEST_ENABLE_BT) {
            btUtil.turnOnBT()
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == REQUEST_ENABLE_BT) {
            Log.d("BT", "turned on result $resultCode")
            if (resultCode == 0) {
                Intent(this, MalfunctionActivity::class.java)
                    .apply {
                        startActivity(this)
                        finish()
                    }

            }
        }

        //选择蓝牙设备
        if(requestCode == DEVICE_SELECTED && resultCode == Activity.RESULT_OK) {
            val deviceToPair: BluetoothDevice? =
                data?.getParcelableExtra(CompanionDeviceManager.EXTRA_DEVICE)

            val address = deviceToPair!!.address
            LocalData.deviceAddr = address
            vm.bleAddr.value = address
            vm.connectBle(btUtil)

        }

    }
    fun setButtons() {
        vm.socket.observe(this) {
            if(it == null || !it.isConnected) {
                bd.bindDeviceButton.visibility = View.VISIBLE
                bd.startServiceButton.visibility = View.INVISIBLE
                bd.setScheduleButton.visibility = View.INVISIBLE
            } else {
                bd.bindDeviceButton.visibility = View.INVISIBLE
                bd.startServiceButton.visibility = View.VISIBLE
                bd.setScheduleButton.visibility = View.VISIBLE
            }
        }


    }
}


class BtViewModel: ViewModel() {
    //连接中
    val connecting = MutableLiveData<Boolean>().apply { value=false }

    val socket = MutableLiveData<BluetoothSocket>().apply { value=null }
    val bleAddr = MutableLiveData<String>().apply { value=LocalData.deviceAddr }


    fun connectBle(util: BluetoothUtil) {
        val addr = bleAddr.value
        connecting.value = true

        if (addr.isNullOrBlank()) {
            util.showPairDevice()
            return
        }

        util.connectDevice(addr, {_ -> connecting.value = false}) {
            connecting.value = false
            socket.value = it
        }
    }

}