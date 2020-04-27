package com.srsssms

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import kotlinx.android.synthetic.main.activity_menu.*

class ActivityMenu : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_menu)
        btMasukMenu.setOnClickListener {
            val intent = Intent(this@ActivityMenu, MainActivity::class.java)
            startActivity(intent)
        }
        btMonitoring.setOnClickListener {
            val intent = Intent(this@ActivityMenu, ActivityMonitor::class.java)
            startActivity(intent)
        }
    }
}
