package com.srsssms

import android.app.IntentService
import android.content.Intent

class ReminderIntentService: IntentService(ReminderIntentService::class.simpleName) {

    override fun onHandleIntent(intent: Intent?) {
        Reminder().executeTask()
    }

}