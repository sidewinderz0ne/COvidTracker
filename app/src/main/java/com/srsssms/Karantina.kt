package com.srsssms

import android.Manifest
import android.annotation.SuppressLint
import android.app.Activity
import android.content.Intent
import android.content.IntentSender
import android.content.pm.PackageManager
import android.location.Location
import android.net.Uri
import android.os.Bundle
import android.os.Looper
import android.provider.Settings
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import com.android.volley.BuildConfig
import com.android.volley.Response
import com.android.volley.toolbox.StringRequest
import com.android.volley.toolbox.Volley
import com.google.android.gms.common.api.ApiException
import com.google.android.gms.common.api.ResolvableApiException
import com.google.android.gms.location.*
import com.google.android.material.snackbar.Snackbar
import kotlinx.android.synthetic.main.activity_karantina.*
import org.json.JSONException
import org.json.JSONObject
import java.text.DateFormat
import java.text.SimpleDateFormat
import java.util.*

@Suppress("KDocUnresolvedReference")
class Karantina : AppCompatActivity() {

    val url = "https://palmsentry.srs-ssms.com/palmsentry/tracking.php"

    /**
     * [location] */
    var mFusedLocationClient: FusedLocationProviderClient? = null
    var mSettingsClient: SettingsClient? = null
    var mLocationRequest: LocationRequest? = null
    var mLocationSettingsRequest: LocationSettingsRequest? = null
    var mLocationCallback: LocationCallback? = null
    var mCurrentLocation: Location? = null
    var mRequestingLocationUpdates: Boolean? = true
    var mLastUpdateTime: String? = null
    var lat: Double? = null
    var lon: Double? = null

    @SuppressLint("HardwareIds")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_karantina)
        val prefMan = PrefManager(this)

        updateValuesFromBundle(savedInstanceState)
        mFusedLocationClient = LocationServices.getFusedLocationProviderClient(this)
        mSettingsClient = LocationServices.getSettingsClient(this)

        btDaftar.setOnClickListener {
            if (etNama.text.isEmpty() || etAlamat.text.isEmpty()){
                Toast.makeText(this, "Mohon isi kolom nama dan alamat!!", Toast.LENGTH_SHORT).show()
            } else {
                val tm = Settings.Secure.getString(this.contentResolver, Settings.Secure.ANDROID_ID)
                    prefMan.name = etNama.text.toString()
                    prefMan.logged = true
                    prefMan.alamat = etAlamat.text.toString()
                    prefMan.imei = tm
                val intent = Intent(this@Karantina, Karantina::class.java)
                startActivity(intent)
            }
        }

        Toast.makeText(this, prefMan.logged.toString(), Toast.LENGTH_SHORT).show()
        if (prefMan.logged){
            etNama.visibility = View.GONE
            etAlamat.visibility = View.GONE
            btDaftar.visibility = View.GONE
            createLocationCallback()
            createLocationRequest()
            buildLocationSettingsRequest()
            mRequestingLocationUpdates = true
        }
    }

    fun execute(){
        startLocationUpdates()
    }

    override fun onBackPressed() {
        val intent = Intent(this@Karantina, ActivityMenu::class.java)
        startActivity(intent)
    }

    private fun checkRegister(namaLengkap: String, imei: String, latitude: Double, longitude: Double,
                              waktu: String, alamat: String) {
        val strReq: StringRequest = object : StringRequest(Method.POST, url, Response.Listener { response ->
            try {
                val jObj = JSONObject(response)
                val success = jObj.getInt(Db().TAG_SUCCESS)
                if (success == 1) {
                    Toast.makeText(this@Karantina, "sukses", Toast.LENGTH_LONG).show()
                } else{
                    Toast.makeText(applicationContext,
                        jObj.getString(success.toString()), Toast.LENGTH_LONG).show()
                }
            } catch (e: JSONException) {
                /*Toast.makeText(this@MainActivity, "error response", Toast.LENGTH_LONG).show()*/
                }
        }, Response.ErrorListener { error ->
            /*Toast.makeText(this@MainActivity, "error ga terkirim", Toast.LENGTH_LONG).show()*/
        }) {
            override fun getParams(): Map<String, String> {
                val params: MutableMap<String, String> = HashMap()
                params[Db().TAG_NAMA] = namaLengkap
                params[Db().TAG_IMEI] = imei
                params[Db().TAG_WAKTU] = waktu
                params[Db().TAG_LATITUDE] = latitude.toString()
                params[Db().TAG_LONGITUDE] = longitude.toString()
                params[Db().TAG_ALAMAT] = alamat
                return params
            }
        }
        // Adding request to request queue
        val queue = Volley.newRequestQueue(this)
        queue.add(strReq)
    }

    private fun updateValuesFromBundle(savedInstanceState: Bundle?) {
        if (savedInstanceState != null) {
            if (savedInstanceState.keySet().contains(KEY_REQUESTING_LOCATION_UPDATES)) {
                mRequestingLocationUpdates = savedInstanceState.getBoolean(
                        KEY_REQUESTING_LOCATION_UPDATES
                )
            }
            if (savedInstanceState.keySet().contains(KEY_LOCATION)) {
                mCurrentLocation = savedInstanceState.getParcelable(KEY_LOCATION)
            }
            if (savedInstanceState.keySet().contains(KEY_LAST_UPDATED_TIME_STRING)) {
                mLastUpdateTime = savedInstanceState.getString(KEY_LAST_UPDATED_TIME_STRING)
            }
            updateUI()
        }
    }

    private fun createLocationRequest() {
        mLocationRequest = LocationRequest()
        mLocationRequest!!.interval = UPDATE_INTERVAL_IN_MILLISECONDS
        mLocationRequest!!.fastestInterval = FASTEST_UPDATE_INTERVAL_IN_MILLISECONDS
        mLocationRequest!!.priority = LocationRequest.PRIORITY_HIGH_ACCURACY
    }
    private fun createLocationCallback() {
        mLocationCallback = object : LocationCallback() {
            override fun onLocationResult(locationResult: LocationResult) {
                super.onLocationResult(locationResult)
                mCurrentLocation = locationResult.lastLocation
                mLastUpdateTime = DateFormat.getTimeInstance().format(Date())
                updateLocationUI()
            }
        }
    }
    private fun buildLocationSettingsRequest() {
        val builder = LocationSettingsRequest.Builder()
        builder.addLocationRequest(mLocationRequest!!)
        mLocationSettingsRequest = builder.build()
    }
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        when (requestCode) {
            REQUEST_CHECK_SETTINGS -> when (resultCode) {
                Activity.RESULT_OK -> Log.i(
                        TAG,
                        "User agreed to make required location settings changes."
                )
                Activity.RESULT_CANCELED -> {
                    Log.i(TAG, "User chose not to make required location settings changes.")
                    mRequestingLocationUpdates = false
                    updateUI()
                }
            }
        }
    }
    private fun startLocationUpdates() {
        mSettingsClient!!.checkLocationSettings(mLocationSettingsRequest)
                .addOnSuccessListener(this@Karantina) {
                    Log.i(TAG, "All location settings are satisfied.")
                    mFusedLocationClient!!.requestLocationUpdates(
                            mLocationRequest,
                            mLocationCallback, Looper.myLooper()
                    )
                    updateUI()
                }
                .addOnFailureListener(this@Karantina) { e ->
                    when ((e as ApiException).statusCode) {
                        LocationSettingsStatusCodes.RESOLUTION_REQUIRED -> {
                            Log.i(
                                    TAG, "Location settings are not satisfied. Attempting to upgrade " +
                                    "location settings "
                            )
                            try {
                                val rae = e as ResolvableApiException
                                rae.startResolutionForResult(this@Karantina, REQUEST_CHECK_SETTINGS)
                            } catch (sie: IntentSender.SendIntentException) {
                                Log.i(TAG, "PendingIntent unable to execute request.")
                            }
                        }
                        LocationSettingsStatusCodes.SETTINGS_CHANGE_UNAVAILABLE -> {
                            val errorMessage = "Location settings are inadequate, and cannot be " +
                                    "fixed here. Fix in Settings."
                            Log.e(TAG, errorMessage)
                            Toast.makeText(this@Karantina, errorMessage, Toast.LENGTH_LONG).show()
                            mRequestingLocationUpdates = false
                        }
                    }
                    updateUI()
                }
    }

    private fun updateUI() {
        updateLocationUI()
    }

    @Suppress("DEPRECATION")
    @SuppressLint("SetTextI18n", "SimpleDateFormat")
    private fun updateLocationUI() {
        val prefManager = PrefManager(this)
        if (mCurrentLocation != null) {
            val dateFormatted = SimpleDateFormat("yyyy-M-dd hh:mm:ss").format(Calendar.getInstance().time)
            lat = String.format(Locale.ENGLISH, "%s",
                    mCurrentLocation!!.latitude).toDouble()
            lon = String.format(Locale.ENGLISH, "%s",
                    mCurrentLocation!!.longitude).toDouble()
            checkRegister(prefManager.name!!, prefManager.imei!!, lat!!, lon!!, dateFormatted, prefManager.alamat!!)
            Log.d("checkregister", "name:${prefManager.name} || imei:${prefManager.imei} || lat:$lat || lon:$lon || date:$dateFormatted || alamat:${prefManager.alamat} ")
        }
    }

    private fun stopLocationUpdates() {
        if (!mRequestingLocationUpdates!!) {
            Log.d(TAG, "stopLocationUpdates: updates never requested, no-op.")
            return
        }
        mFusedLocationClient!!.removeLocationUpdates(mLocationCallback)
                .addOnCompleteListener(this) {
                    mRequestingLocationUpdates = false
                }
    }

    public override fun onResume() {
        super.onResume()
        if (PrefManager(this).logged){
            if (!mRequestingLocationUpdates!! && checkPermissions()) {
                startLocationUpdates()
            } else if (!checkPermissions()) {
                requestPermissions()
            }
            updateUI()
        }
    }

    override fun onPause() {
        super.onPause()/*
        stopLocationUpdates()*/
    }

    public override fun onSaveInstanceState(savedInstanceState: Bundle) {
        savedInstanceState.putBoolean(
                KEY_REQUESTING_LOCATION_UPDATES,
                mRequestingLocationUpdates!!
        )
        savedInstanceState.putParcelable(KEY_LOCATION, mCurrentLocation)
        savedInstanceState.putString(KEY_LAST_UPDATED_TIME_STRING, mLastUpdateTime)
        super.onSaveInstanceState(savedInstanceState)
    }

    @Suppress("SameParameterValue")
    private fun showSnackbar(
            mainTextStringId: Int, actionStringId: Int,
            listener: View.OnClickListener
    ) {
        Snackbar.make(
                findViewById(android.R.id.content),
                mainTextStringId.toString(),
                Snackbar.LENGTH_INDEFINITE
        )
                .setAction(getString(actionStringId), listener).show()
    }

    private fun checkPermissions(): Boolean {
        val permissionState = ActivityCompat.checkSelfPermission(
                this@Karantina,
                Manifest.permission.ACCESS_FINE_LOCATION
        )
        if (permissionState == PackageManager.PERMISSION_GRANTED) {
            startLocationUpdates()
        }
        return permissionState == PackageManager.PERMISSION_GRANTED
    }

    private fun requestPermissions() {
        val shouldProvideRationale = ActivityCompat.shouldShowRequestPermissionRationale(
                this@Karantina,
                Manifest.permission.ACCESS_FINE_LOCATION
        )
        if (shouldProvideRationale) {
            Log.i(TAG, "Displaying permission rationale to provide additional context.")
            ActivityCompat.requestPermissions(
                    this@Karantina, arrayOf(Manifest.permission.ACCESS_FINE_LOCATION),
                    REQUEST_PERMISSIONS_REQUEST_CODE
            )
        } else {
            Log.i(TAG, "Requesting permission")
            ActivityCompat.requestPermissions(
                    this@Karantina, arrayOf(Manifest.permission.ACCESS_FINE_LOCATION),
                    REQUEST_PERMISSIONS_REQUEST_CODE
            )
        }
    }

    override fun onRequestPermissionsResult(
            requestCode: Int, permissions: Array<String>,
            grantResults: IntArray
    ) {
        Log.i(TAG, "onRequestPermissionResult")
        if (requestCode == REQUEST_PERMISSIONS_REQUEST_CODE) {
            if (grantResults.isEmpty()) {
                Log.i(TAG, "User interaction was cancelled.")
            } else if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                if (mRequestingLocationUpdates!!) {
                    Log.i(
                            TAG,
                            "Permission granted, updates requested, starting location updates"
                    )
                    startLocationUpdates()
                }
            } else {
                showSnackbar(R.string.permission_denied_explanation,
                        R.string.settings, View.OnClickListener {
                    val intent = Intent()
                    intent.action = Settings.ACTION_APPLICATION_DETAILS_SETTINGS
                    val uri = Uri.fromParts(
                            "package",
                            BuildConfig.APPLICATION_ID, null
                    )
                    intent.data = uri
                    intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
                    startActivity(intent)
                })
            }
        }
    }

    companion object {
        private val TAG = Karantina::class.java.simpleName
        private const val REQUEST_PERMISSIONS_REQUEST_CODE = 34
        private const val REQUEST_CHECK_SETTINGS = 0x1
        private const val UPDATE_INTERVAL_IN_MILLISECONDS: Long = 10000
        private const val FASTEST_UPDATE_INTERVAL_IN_MILLISECONDS = UPDATE_INTERVAL_IN_MILLISECONDS / 2
        private const val KEY_REQUESTING_LOCATION_UPDATES = "requesting-location-updates"
        private const val KEY_LOCATION = "location"
        private const val KEY_LAST_UPDATED_TIME_STRING = "last-updated-time-string"
    }
}
