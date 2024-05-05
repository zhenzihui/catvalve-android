package com.feibao.catvalve.activity

import android.content.Intent
import android.os.Bundle
import android.os.Debug
import android.util.Log
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.feibao.catvalve.CameraValveActivity
import com.feibao.catvalve.R
import com.feibao.catvalve.databinding.ActivityBtBinding
import com.feibao.catvalve.util.BluetoothUtil
import com.feibao.catvalve.util.REQUEST_ENABLE_BT
import java.util.logging.Logger

class BtActivity : AppCompatActivity() {

    private var _bd: ActivityBtBinding? = null
    val bd get() = _bd!!

    private var _btUtil: BluetoothUtil? = null
    private val btUtil get() = _btUtil!!
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

    }
}