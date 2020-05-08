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
        set(logged) {
            editor.putBoolean(LOGGED, logged)
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

    var latA: String?
        get() = pref.getString(LATA, null)
        set(lata) {
            editor.putString(LATA, lata)
            editor.commit()
        }

    var lonA: String?
        get() = pref.getString(LONA, null)
        set(lona) {
            editor.putString(LONA, lona)
            editor.commit()
        }

    var userID: String?
        get() = pref.getString(USERID, null)
        set(user) {
            editor.putString(USERID, user)
            editor.commit()
        }

    companion object {
        // Shared preferences file name
        private const val PREF_NAME = "covidtracker"
        private const val LOGGED = "logged"
        private const val USERID = "user_id"
        private const val NAME = "name"
        private const val IMEI = "deviceid"
        private const val ALAMAT = "alamat"
        private const val LATA = "latitude_a"
        private const val LONA = "longitude_a"
    }

    init {
        pref = _context.getSharedPreferences(PREF_NAME, privateMode)
        editor = pref.edit()
    }
}