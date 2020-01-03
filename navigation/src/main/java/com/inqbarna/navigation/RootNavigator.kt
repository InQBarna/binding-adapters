package com.inqbarna.navigation

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import com.inqbarna.navigation.base.AppRoute
import com.inqbarna.navigation.base.EnhancedNavigator
import com.inqbarna.navigation.base.NavigationHandler
import com.inqbarna.navigation.base.Navigator
import timber.log.Timber

/**
 * @author David García (david.garcia@inqbarna.com)
 * @version 1.0 05/07/2018
 */
class RootNavigator @JvmOverloads constructor(
    private val redirectSupport: NavigationRedirectSupport,
    initialSteps: List<NavigationHandler> = emptyList(),
    private val delegatedNavigator: Navigator? = null,
    responders: List<PermissionResponder> = emptyList()
) : EnhancedNavigator, PermissionEnabledRouting {
    private val steps = initialSteps.toMutableList()
    private val interceptors = initialSteps
            .filterIsInstance(RouteInterceptor::class.java)
            .toMutableList()

    private val permissionResponders: Map<PermissionRequestsKey, PermissionResponder> = responders.associateBy {
        PermissionRequestsKey(it.respondsToPermissions)
    }

    private val navigationResponses = NavigationResponsesImpl()

    override fun responsesManager(): NavigationResponses {
        return delegatedNavigator?.responsesManager() ?: navigationResponses
    }

    fun processPermissionResponse(requestCode: Int, permissions: Array<String>, grantResults: IntArray): Boolean {
        val key = PermissionRequestsKey(permissions)
        val responder = permissionResponders[key] ?: return false

        return responder.delegate.onRequestPermissionsResult(requestCode, permissions, grantResults)
    }

    interface RouteInterceptor {
        fun intercept(destination: AppRoute): AppRoute?
    }

    interface PermissionResponder {
        val delegate: RoutedPermissionsDelegate
        val respondsToPermissions: Array<String>
    }

    fun includeNavigationHandler(step: NavigationHandler) {
        if (!steps.contains(step)) {
            steps += step
        }

        if (step is RouteInterceptor && !interceptors.contains(step)) {
            interceptors += step
        }
    }

    override fun locatePermissionResponder(key: PermissionRequestsKey): PermissionResponder? {
        return when {
            key in permissionResponders -> permissionResponders[key]
            delegatedNavigator is PermissionEnabledRouting -> delegatedNavigator.locatePermissionResponder(key)
            else -> null
        }
    }

    override fun navigateToWithCallback(destination: AppRoute, callback: () -> Unit) {
        // login succeeded, proceed with original destination
        // Ignore redirected targets that don't work properly, after logging that fact
        when {
            destination.needsPermissionResolution() -> {
                val key = PermissionRequestsKey(destination.requiredPermissions!!)
                val responder = locatePermissionResponder(key)
                    ?: throw IllegalArgumentException("Destination requires permissions, but cannot be handled")

                responder.delegate.checkPermissionsForDestination(destination)
            }
            destination.hasPermissionDenied() -> {
                if (destination.requiresResult()) {
                    val intent = with(CancellationMetadata()) {
                        permissionDenied = true
                        Intent().also {
                            writeToIntent(it)
                        }
                    }

                    responsesManager().onActivityResult(
                        destination.requestCode,
                        Activity.RESULT_CANCELED,
                        intent
                    )
                }
            }
            else -> {
                val redirectedTarget = redirectSupport.getPendingRedirections(destination)
                if (redirectedTarget != null) {
                    if (!redirectedTarget.requiresResult()) {
                        // Ignore redirected targets that don't work properly, after logging that fact
                        Timber.e(
                            "Redirected targets need to be callable for result, %s isn't",
                            redirectedTarget
                        )
                    } else {
                        navigateForResult(redirectedTarget) { resultCode, _ ->
                            if (resultCode == Activity.RESULT_OK) {
                                // login succeeded, proceed with original destination
                                navigateTo(destination)
                            }
                        }
                        return
                    }
                }

                val finalDestination = interceptors.fold(destination) { prevDest, interceptor ->
                    interceptor.intercept(prevDest) ?: prevDest
                }

                val selectedStep: Navigator =
                    steps.firstOrNull { step -> step.canProcessDestination(finalDestination) }
                        ?: delegatedNavigator
                        ?: throw IllegalArgumentException("This destination cannot be managed by any navigator")

                selectedStep.navigateTo(finalDestination)

                callback()
            }
        }
    }

    override fun navigateTo(destination: AppRoute) {
        navigateToWithCallback(destination) { }
    }

    class PermissionRequestsKey(permissions: Array<String>) {
        val permissions = permissions.sortedArray()
        override fun equals(other: Any?): Boolean {
            if (this === other) return true
            if (other !is PermissionRequestsKey) return false

            if (!permissions.contentEquals(other.permissions)) return false

            return true
        }

        override fun hashCode(): Int {
            return permissions.contentHashCode()
        }
    }
}

interface PermissionEnabledRouting {
    fun locatePermissionResponder(key: RootNavigator.PermissionRequestsKey): RootNavigator.PermissionResponder?
}

private class NavigationResponsesImpl : NavigationResponses {
    private val callbacks: MutableList<NavigationResponsesCallback> = mutableListOf()

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?): Boolean {
        synchronized(callbacks) {
            callbacks.forEach {
                if (it.onNavigationResult(requestCode, resultCode, data)) {
                    return true
                }
            }
        }
        return false
    }

    override fun addCallback(callback: NavigationResponsesCallback) {
        synchronized(callbacks) {
            callback.takeUnless { callbacks.contains(it) }?.let {
                callbacks.add(it)
            }
        }
    }

    override fun removeCallback(callback: NavigationResponsesCallback) {
        synchronized(callbacks) {
            callbacks -= callback
        }
    }
}

/**
 * @author David García (david.garcia@inqbarna.com)
 * @version 1.0 2019-04-18
 */
interface NavigationRedirectSupport {
    fun getPendingRedirections(appRoute: AppRoute): AppRoute?
}

class VoidRedirectSupport : NavigationRedirectSupport {
    override fun getPendingRedirections(appRoute: AppRoute): AppRoute? = null
}


class CancellationMetadata(private val bundle: Bundle = Bundle()) {
    constructor(intent: Intent) : this(intent.getBundleExtra("com.inqbarna.cancelation-metadata") ?: Bundle())
    var permissionDenied: Boolean
        get() = bundle.getBoolean("permissionDenied", false)
        set(value) = bundle.putBoolean("permissionDenied", value)

    fun writeToIntent(target: Intent) {
        target.putExtra("com.inqbarna.cancelation-metadata", bundle)
    }
}
