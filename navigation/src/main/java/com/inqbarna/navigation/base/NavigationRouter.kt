package com.inqbarna.navigation.base

interface NavigationRouter : Navigator {
    fun canProcessDestination(destination: AppRoute): Boolean
}
