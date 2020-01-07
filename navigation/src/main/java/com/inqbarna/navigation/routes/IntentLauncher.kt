package com.inqbarna.navigation.routes

import android.app.Activity
import android.app.PendingIntent
import android.content.Intent
import android.os.Bundle
import androidx.core.app.ActivityCompat
import androidx.fragment.app.Fragment

interface IntentLauncher {
    fun launch(intent: Intent, options: Bundle?)
    fun launchForResult(intent: Intent, requestCode: Int, options: Bundle?)
    fun launchPendingIntent(pendingIntent: PendingIntent)
}

internal class ActivityLauncher(private val activity: Activity) :
    IntentLauncher {
    override fun launch(intent: Intent, options: Bundle?) = ActivityCompat.startActivity(activity, intent, options)
    override fun launchForResult(intent: Intent, requestCode: Int, options: Bundle?) {
        ActivityCompat.startActivityForResult(activity, intent, requestCode, options)
    }

    override fun launchPendingIntent(pendingIntent: PendingIntent) {
        pendingIntent.send(activity, 0, null)
    }
}

internal class FragmentLauncher(private val fragment: Fragment) :
    IntentLauncher {
    override fun launch(intent: Intent, options: Bundle?) = fragment.startActivity(intent, options)
    override fun launchForResult(intent: Intent, requestCode: Int, options: Bundle?) {
        fragment.startActivityForResult(intent, requestCode, options)
    }

    override fun launchPendingIntent(pendingIntent: PendingIntent) {
        fragment.context?.let {
            pendingIntent.send(it, 0, null)
        }
    }
}
