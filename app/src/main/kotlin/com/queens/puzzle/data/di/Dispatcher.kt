package com.queens.puzzle.data.di

import javax.inject.Qualifier

/** The dispatchers the app injects rather than hardcoding, so tests can supply their own. */
enum class QueensDispatcher { Default, IO }

@Qualifier
@Retention(AnnotationRetention.RUNTIME)
annotation class Dispatcher(val dispatcher: QueensDispatcher)
