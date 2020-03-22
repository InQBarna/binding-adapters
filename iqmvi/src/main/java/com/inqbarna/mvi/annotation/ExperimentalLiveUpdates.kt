package com.inqbarna.mvi.annotation

@Retention(AnnotationRetention.BINARY)
@RequiresOptIn("This for bottom up updates behavior is not very elegant", RequiresOptIn.Level.WARNING)
annotation class ExperimentalLiveUpdates
