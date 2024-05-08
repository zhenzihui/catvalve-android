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
import androidx.lifecycle.lifecycleScope
import com.feibao.catvalve.CameraValveActivity
import com.feibao.catvalve.R
import com.feibao.catvalve.databinding.ActivityBtBinding
import com.feibao.catvalve.util.ConnStatus
import com.feibao.catvalve.util.DEVICE_SELECTED
import com.feibao.catvalve.util.DeviceListener
import com.feibao.catvalve.util.DeviceUtil
import com.feibao.catvalve.util.LocalData
import com.feibao.catvalve.util.REQUEST_ENABLE_BT
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class BtActivity : AppCompatActivity() {

    private var _bd: ActivityBtBinding? = null
    val bd get() = _bd!!

    val btUtil = DeviceUtil.deviceHelper


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
        setContentView(bd.root)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
        DeviceUtil.turnOnBT(this)
        setButtons()
        vm.connStatus.observe(this) {
            bd.connStateText.setText(it.desc)
        }

        bd.bindDeviceButton.setOnClickListener {
            if (vm.connStatus.value==ConnStatus.UNPAIRED) {
                DeviceUtil.showNearbyDevices(this)
            } else {
                DeviceUtil.connectDevice(LocalData.deviceAddr!!)
            }
        }
        //尝试链接蓝牙
        LocalData.deviceAddr?.let {
            DeviceUtil.connectDevice(it)
        }


    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)

        if (requestCode == REQUEST_ENABLE_BT) {
            DeviceUtil.turnOnBT(this)
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
        if (requestCode == DEVICE_SELECTED && resultCode == Activity.RESULT_OK) {
            val deviceToPair: BluetoothDevice? =
                data?.getParcelableExtra(CompanionDeviceManager.EXTRA_DEVICE)

            val address = deviceToPair!!.address
            LocalData.deviceAddr = address
            DeviceUtil.connectDevice(address)

        }

    }

    fun setButtons() {
        vm.connStatus.observe(this) {
            when (it) {


                ConnStatus.CONNECTED -> {
                    bd.bindDeviceButton.visibility = View.INVISIBLE
                    bd.startServiceButton.visibility = View.VISIBLE
                    bd.setScheduleButton.visibility = View.VISIBLE
                }
                ConnStatus.UNPAIRED -> {
                    bd.bindDeviceButton.visibility = View.VISIBLE
                    bd.startServiceButton.visibility = View.INVISIBLE
                    bd.setScheduleButton.visibility = View.INVISIBLE
                }
               else -> {
                    bd.bindDeviceButton.visibility = View.INVISIBLE
                    bd.startServiceButton.visibility = View.INVISIBLE
                    bd.setScheduleButton.visibility = View.INVISIBLE
                }
            }
        }


    }
}


class BtViewModel : ViewModel() {

    init {
        DeviceUtil.deviceHelper.deviceListener = object : DeviceListener {
            override fun onConnStatusChanged(it: ConnStatus) {
                connStatus.value = it
            }

        }

    }



    val connStatus = MutableLiveData<ConnStatus>().apply { value = ConnStatus.CHECKING }


}