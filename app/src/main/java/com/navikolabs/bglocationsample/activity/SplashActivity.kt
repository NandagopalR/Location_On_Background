package com.navikolabs.bglocationsample.activity

import android.os.Bundle
import android.os.Handler
import android.os.Looper
import com.navikolabs.bglocationsample.R

class SplashActivity : BaseActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_splash)

        Handler(Looper.getMainLooper()).postDelayed({
            startActivity(
                if (appPreference.isLoggedIn()) {
                    MainActivity.getCallingIntent(this)
                } else {
                    LoginActivity.getCallingIntent(this)
                }
            )
            finish()
        }, 2000)
    }

}