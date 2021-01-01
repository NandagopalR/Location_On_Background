package com.navikolabs.bglocationsample.data

import android.content.Context
import android.content.SharedPreferences
import android.preference.PreferenceManager

class AppPreference(context: Context) {

    companion object {
        private const val PREF_IS_LOGGED_IN = "PREF_IS_LOGGED_IN"
    }

    private var mSharedPreferences: SharedPreferences? = null

    init {
        mSharedPreferences = PreferenceManager.getDefaultSharedPreferences(context)
    }

    fun isLoggedIn(): Boolean {
        return mSharedPreferences?.getBoolean(PREF_IS_LOGGED_IN, false)!!
    }

    fun setLoggedIn(isLoggedIn: Boolean) {
        mSharedPreferences?.edit()?.putBoolean(PREF_IS_LOGGED_IN, isLoggedIn)!!.apply()
    }

}