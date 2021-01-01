package com.navikolabs.bglocationsample.activity

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.View
import com.navikolabs.bglocationsample.R

class LoginActivity : BaseActivity() {

    companion object {
        fun getCallingIntent(context: Context): Intent {
            return Intent(context, LoginActivity::class.java)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)
    }

    fun onLoginClicked(view: View) {
        appPreference.setLoggedIn(true)
        startActivity(MainActivity.getCallingIntent(this))
        finish()
    }

}