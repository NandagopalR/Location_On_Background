package com.navikolabs.bglocationsample.activity

import android.Manifest
import android.R
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.ServiceConnection
import android.content.pm.PackageManager
import android.os.Bundle
import android.os.IBinder
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import com.google.android.material.snackbar.Snackbar
import com.navikolabs.bglocationsample.data.AppPreference
import com.navikolabs.bglocationsample.service.LocationUpdateService

abstract class BaseActivity : AppCompatActivity() {
    private val TAG = BaseActivity::class.java.simpleName

    lateinit var appPreference: AppPreference

    val REQUEST_PERMISSIONS_REQUEST_CODE = 34
    private var mService: LocationUpdateService? = null
    private var mBound = false
    private val mServiceConnection: ServiceConnection = object : ServiceConnection {
        override fun onServiceConnected(name: ComponentName, service: IBinder) {
            val binder: LocationUpdateService.LocalBinder =
                service as LocationUpdateService.LocalBinder
            if (mService == null) {
                mService = binder.service
                startLocationUpdateService()
            } else {
                mService = binder.service
            }
            mBound = true
        }

        override fun onServiceDisconnected(name: ComponentName) {
            mService = null
            mBound = false
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        appPreference = AppPreference(this)
    }

    override fun onStart() {
        super.onStart()

        if (appPreference.isLoggedIn()) {
            bindService(
                Intent(this, LocationUpdateService::class.java), mServiceConnection,
                Context.BIND_AUTO_CREATE
            )
            if (!checkPermissions()) {
                requestPermissions()
            } else {
                startLocationUpdateService()
            }
        }
    }

    protected open fun startLocationUpdateService() {
        if (mService != null && appPreference.isLoggedIn()) {
            mService!!.requestLocationUpdates()
        }
    }

    protected open fun stopLocationUpdates() {
        appPreference.setLoggedIn(false)
        if (mService != null) {
            mService!!.removeLocationUpdates()
        }
    }

    override fun onStop() {
        if (mBound) {
            // Unbind from the service. This signals to the service that this activity is no longer
            // in the foreground, and the service can respond by promoting itself to a foreground
            // service.
            unbindService(mServiceConnection)
            mBound = false
        }
        super.onStop()
    }

    protected open fun checkPermissions(): Boolean {
        return PackageManager.PERMISSION_GRANTED == ActivityCompat.checkSelfPermission(
            this,
            Manifest.permission.ACCESS_FINE_LOCATION
        )
    }

    protected open fun requestPermissions() {
        val shouldProvideRationale =
            ActivityCompat.shouldShowRequestPermissionRationale(
                this,
                Manifest.permission.ACCESS_FINE_LOCATION
            )

        // Provide an additional rationale to the user. This would happen if the user denied the
        // request previously, but didn't check the "Don't ask again" checkbox.
        if (shouldProvideRationale) {
            Log.i(
                TAG,
                "Displaying permission rationale to provide additional context."
            )
            Snackbar.make(
                findViewById(android.R.id.content),
                "Location permission is needed for core functionality",
                Snackbar.LENGTH_INDEFINITE
            ).setAction(R.string.ok) { view ->
                // Request permission
                ActivityCompat.requestPermissions(
                    this@BaseActivity,
                    arrayOf(Manifest.permission.ACCESS_FINE_LOCATION),
                    REQUEST_PERMISSIONS_REQUEST_CODE
                )
            }
                .show()
        } else {
            Log.i(TAG, "Requesting permission")
            // Request permission. It's possible this can be auto answered if device policy
            // sets the permission in a given state or the user denied the permission
            // previously and checked "Never ask again".
            ActivityCompat.requestPermissions(
                this@BaseActivity,
                arrayOf(Manifest.permission.ACCESS_FINE_LOCATION),
                REQUEST_PERMISSIONS_REQUEST_CODE
            )
        }
    }

}