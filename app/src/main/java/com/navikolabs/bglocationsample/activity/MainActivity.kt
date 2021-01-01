package com.navikolabs.bglocationsample.activity

import android.app.Activity
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.IntentSender.SendIntentException
import android.content.pm.PackageManager
import android.location.LocationManager
import android.net.Uri
import android.os.Bundle
import android.provider.Settings
import android.util.Log
import android.view.View
import android.widget.Toast
import com.google.android.gms.common.api.GoogleApiClient
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.LocationSettingsRequest
import com.google.android.gms.location.LocationSettingsStatusCodes
import com.google.android.material.snackbar.Snackbar
import com.navikolabs.bglocationsample.BuildConfig
import com.navikolabs.bglocationsample.R
import kotlinx.android.synthetic.main.activity_main.*

class MainActivity : BaseActivity() {

    companion object {
        fun getCallingIntent(context: Context): Intent {
            return Intent(context, MainActivity::class.java)
        }

        const val REQUEST_LOCATION = "REQUEST_LOCATION"
        const val REQUEST_LATITUDE = "REQUEST_LATITUDE"
        const val REQUEST_LONGITUDE = "REQUEST_LONGITUDE"
        const val REQUEST_CODE_LOCATION = 1450
    }

    lateinit var locationReceiver: BroadcastReceiver
    lateinit var googleApiClient: GoogleApiClient

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        locationReceiver = LocationReceiver()
    }

    fun onLogoutClicked(view: View) {
        appPreference.setLoggedIn(false)
        stopLocationUpdates()
        startActivity(LoginActivity.getCallingIntent(this))
        finish()
    }

    override fun onStart() {
        super.onStart()
        registerReceiver(locationReceiver, IntentFilter(REQUEST_LOCATION))
    }

    override fun onStop() {
        super.onStop()
        unregisterReceiver(locationReceiver)
    }

    private fun applyLocationDataToViews(latitude: Double?, longitude: Double?) {
        tv_latitude.text = "Latitude : ${latitude.toString()}"
        tv_longitude.text = "Longitude : ${longitude.toString()}"
    }

    inner class LocationReceiver : BroadcastReceiver() {
        override fun onReceive(
            context: Context,
            intent: Intent
        ) {
            if (intent.action == REQUEST_LOCATION) {
                val latitude = intent.getDoubleExtra(REQUEST_LATITUDE, 0.toDouble())
                val longitude = intent.getDoubleExtra(REQUEST_LONGITUDE, 0.toDouble())
                applyLocationDataToViews(latitude, longitude)
            }
        }
    }

    private fun buildGoogleApiClient() {
        googleApiClient = GoogleApiClient.Builder(this)
            .addApi(LocationServices.API)
            .addConnectionCallbacks(object : GoogleApiClient.ConnectionCallbacks {
                override fun onConnected(bundle: Bundle?) {}
                override fun onConnectionSuspended(i: Int) {
                    googleApiClient.connect()
                }
            })
            .addOnConnectionFailedListener { connectionResult ->
                Log.d(
                    "Location error",
                    "Location error " + connectionResult.errorCode
                )
            }.build()
        googleApiClient.connect()
        val manager =
            this.getSystemService(Context.LOCATION_SERVICE) as LocationManager
        if (!manager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
            enableLocation()
        }
    }

    private fun enableLocation() {
        val locationRequest = LocationRequest.create()
        locationRequest.priority = LocationRequest.PRIORITY_HIGH_ACCURACY
        locationRequest.interval = 30 * 1000.toLong()
        locationRequest.fastestInterval = 5 * 1000.toLong()
        val builder = LocationSettingsRequest.Builder()
            .addLocationRequest(locationRequest)
        builder.setAlwaysShow(true)
        val result =
            LocationServices.SettingsApi.checkLocationSettings(
                googleApiClient,
                builder.build()
            )
        result.setResultCallback { result ->
            val status = result.status
            when (status.statusCode) {
                LocationSettingsStatusCodes.RESOLUTION_REQUIRED -> try {
                    // Show the dialog by calling startResolutionForResult(),
                    // and check the result in onActivityResult().
                    status.startResolutionForResult(
                        this,
                        REQUEST_CODE_LOCATION
                    )
                } catch (e: SendIntentException) {
                    // Ignore the error.
                    e.printStackTrace()
                }
            }
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == REQUEST_CODE_LOCATION) {
            if (resultCode == Activity.RESULT_CANCELED) {
                Toast.makeText(this, "Request Cancelled", Toast.LENGTH_SHORT).show()
            } else if (resultCode == Activity.RESULT_OK) {
                startLocationUpdateService()
            }
        }
    }

    /**
     * Callback received when a permissions request has been completed.
     */
    override fun onRequestPermissionsResult(
        requestCode: Int, permissions: Array<String?>,
        grantResults: IntArray
    ) {
        if (requestCode == REQUEST_PERMISSIONS_REQUEST_CODE) {
            when {
                grantResults.isEmpty() -> {
                    // If user interaction was interrupted, the permission request is cancelled and you
                    // receive empty arrays.

                }
                grantResults[0] == PackageManager.PERMISSION_GRANTED -> {
                    // Permission was granted.
                    //                mService.requestLocationUpdates();
                    buildGoogleApiClient()
                }
                else -> {
                    // Permission denied.
                    Snackbar.make(
                        findViewById(android.R.id.content),
                        R.string.permission_denied_explanation,
                        Snackbar.LENGTH_INDEFINITE
                    )
                        .setAction(
                            "Settings"
                        ) { view: View? ->
                            // Build intent that displays the App settings screen.
                            val intent = Intent()
                            intent.action = Settings.ACTION_APPLICATION_DETAILS_SETTINGS
                            val uri = Uri.fromParts(
                                "package",
                                BuildConfig.APPLICATION_ID, null
                            )
                            intent.data = uri
                            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
                            startActivity(intent)
                        }
                        .show()
                }
            }
        }
    }

}