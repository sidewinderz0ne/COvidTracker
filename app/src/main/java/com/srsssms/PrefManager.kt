package com.srsssms

import android.content.Context
import android.content.SharedPreferences

class PrefManager(_context: Context) {
    var pref: SharedPreferences
    var editor: SharedPreferences.Editor
    // shared pref mode
    var privateMode = 0

    var logged: Boolean
        get() = pref.getBoolean(LOGGED, false)
        set(loggedin) {
            editor.putBoolean(LOGGED, loggedin)
            editor.commit()
        }

    var name: String?
        get() = pref.getString(NAME, "")
        set(sureName) {
            editor.putString(NAME, sureName)
            editor.commit()
        }

    var imei: String?
        get() = pref.getString(IMEI, "")
        set(imeis) {
            editor.putString(IMEI, imeis)
            editor.commit()
        }

    var alamat: String?
        get() = pref.getString(ALAMAT, "")
        set(alam) {
            editor.putString(ALAMAT, alam)
            editor.commit()
        }

    companion object {
        // Shared preferences file name
        private const val PREF_NAME = "sulungresearch"
        private const val IS_FIRST_TIME_LAUNCH = "IsFirstTimeLaunch"
        private const val LOGGED = "Logged"
        private const val NAME = "Name"
        private const val IMEI = "IMEI"
        private const val ALAMAT = "Alamat"
    }

    init {
        pref = _context.getSharedPreferences(PREF_NAME, privateMode)
        editor = pref.edit()
    }
}