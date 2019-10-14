package com.example.wisatajogja.Activity

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.ProgressBar
import androidx.annotation.NonNull
import androidx.appcompat.app.AppCompatActivity
import com.example.wisatajogja.Connection.RestAdapter
import com.example.wisatajogja.Connection.callbacks.CallbackDevice
import com.example.wisatajogja.Data.SharedPref
import com.example.wisatajogja.R
import com.example.wisatajogja.Utils.PermissionUtil
import com.example.wisatajogja.Utils.Tools
import retrofit2.Call
import retrofit2.Response
import java.util.*

class ActivitySplashScreen : AppCompatActivity() {

    private var sharedPref: SharedPref? = null
    private var parent_view: View? = null
    private var progressBar: ProgressBar? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_splash)
        parent_view = findViewById(R.id.parent_view)
        progressBar = findViewById(R.id.progressBar) as ProgressBar

        sharedPref = SharedPref(this)
        Tools.initImageLoader(applicationContext)
        parent_view!!.setBackgroundColor(sharedPref!!.getThemeColorInt())

        // permission checker for android M or higher
        if (Tools.needRequestPermission()) {
            val permission = PermissionUtil.getDeniedPermission(this)
            if (permission.size != 0) {
                requestPermissions(permission, 200)
            } else {
                initGcmData()
            }
        } else {
            initGcmData()
        }

        // for system bar in lollipop
        Tools.systemBarLolipop(this)
    }

    private fun startActivityMainDelay() {
        // Show splash screen for 2 seconds
        val task = object : TimerTask() {
            override fun run() {
                val intent = Intent(baseContext, MainActivity::class.java)
                startActivity(intent)
                finish() // kill current activity
            }
        }
        Timer().schedule(task, 2000)
    }

    private fun initGcmData() {
        if (sharedPref!!.isGcmRegIdEmpty() && Tools.cekConnection(this)) {
            prepareDeviceInfo()
        } else if (sharedPref!!.isOpenAppCounterReach() && Tools.cekConnection(this)) {
            registerDeviceToServer(Tools.getDeviceInfo(this))
        } else {
            startActivityMainDelay()
        }
    }

    private fun prepareDeviceInfo() {
        progressBar!!.setVisibility(View.VISIBLE)
        Tools.obtainGcmRegId(this, object : Tools.CallbackRegId {
            override fun onSuccess(result: DeviceInfo) {
                // start registration to server
                if (Tools.cekConnection(applicationContext)) {
                    registerDeviceToServer(result)
                } else {
                    startActivityMainDelay()
                }
            }

            override fun onError() {
                startActivityMainDelay()
            }
        })
    }

    private fun registerDeviceToServer(deviceInfo: DeviceInfo) {
        progressBar!!.setVisibility(View.VISIBLE)
        // register gcm
        val callback = RestAdapter.createShortAPI().registerDevice(deviceInfo)
        callback.enqueue(object : retrofit2.Callback<CallbackDevice> {
            override fun onResponse(
                call: Call<CallbackDevice>,
                response: Response<CallbackDevice>
            ) {
                val resp = response.body()
                if (resp!!.status.equals("success")) {
                    sharedPref!!.setOpenAppCounter(0)
                }
                startActivityMainDelay()
            }

            override fun onFailure(call: Call<CallbackDevice>, t: Throwable) {
                startActivityMainDelay()
            }
        })
    }

    override fun onRequestPermissionsResult(requestCode: Int, @NonNull permissions: Array<String>, @NonNull grantResults: IntArray) {
        if (requestCode == 200) {
            for (perm in permissions) {
                val rationale = shouldShowRequestPermissionRationale(perm)
                sharedPref!!.setNeverAskAgain(perm, !rationale)
            }
            initGcmData()
        } else {
            super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        }
    }

}
