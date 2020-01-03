package com.inqbarna.navigation.base

import com.inqbarna.navigation.NavigationResponses

/**
 * @author David García (david.garcia@inqbarna.com)
 * @version 1.0 20/06/2018
 */
interface Navigator {
    fun navigateTo(destination: AppRoute)
    fun responsesManager(): NavigationResponses { throw UnsupportedOperationException("This method is not implemented, typically you would implement this in root navigation") }
}

interface EnhancedNavigator : Navigator {
    fun navigateToWithCallback(destination: AppRoute, callback: () -> Unit)
}

