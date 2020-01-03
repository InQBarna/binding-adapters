package com.inqbarna.navigation.base

interface NavigationHandler : Navigator {
    fun canProcessDestination(destination: AppRoute): Boolean
}
