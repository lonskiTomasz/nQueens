package com.queens.puzzle.data.di

import javax.inject.Qualifier

enum class AppDispatcher { Default, IO }

@Qualifier
@Retention(AnnotationRetention.RUNTIME)
annotation class Dispatcher(val dispatcher: AppDispatcher)
