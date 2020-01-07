package com.inqbarna.navigation.navigators

import android.content.Intent
import android.os.Bundle
import com.inqbarna.navigation.base.AppRoute
import com.inqbarna.navigation.base.NavigationRouter
import com.inqbarna.navigation.routes.IntentLauncher
import com.inqbarna.navigation.routes.IntentRoute
import com.inqbarna.navigation.routes.PendingIntentRoute
import com.inqbarna.navigation.routes.SimpleIntentRoute

/**
 * @author David García (david.garcia@inqbarna.com)
 * @version 1.0 07/01/2020
 */
class SimpleIntentRouter(private val intentLauncher: IntentLauncher) : NavigationRouter {

    override fun canProcessDestination(destination: AppRoute): Boolean = destination is SimpleIntentRoute

    override fun navigateTo(destination: AppRoute) {
        when (destination) {
            is IntentRoute -> goToDestination(destination)
            is PendingIntentRoute -> openPendingIntent(destination)
        }
    }

    private fun openPendingIntent(destination: PendingIntentRoute) {
        intentLauncher.launchPendingIntent(destination.pendingIntent)
    }

    private fun goToDestination(destination: IntentRoute) {
        val intent = destination.intent
        intent.putPayload(destination.extras)

        // TODO: transitions?
        val options: Bundle? = null

        if (destination.requiresResult()) {
            intentLauncher.launchForResult(intent, destination.requestCode, options)
        } else {
            intentLauncher.launch(intent, options)
        }
    }

    companion object {
        private const val EXTRA_PAYLOAD = "com.inqbarna.EXTRA_PAYLOAD"
        @JvmStatic
        fun Intent.getPayload(): Bundle = getBundleExtra(EXTRA_PAYLOAD) ?: Bundle()

        @JvmStatic
        fun Intent.putPayload(payload: Bundle) {
            putExtra(EXTRA_PAYLOAD, payload)
        }
    }
}
