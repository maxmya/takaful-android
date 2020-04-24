package com.dawa.user.handlers

import java.util.concurrent.Executor


interface AppExecutorsService {

    fun diskIO(): Executor

    fun networkIO(): Executor

    fun mainThread(): Executor

    fun handlerDelayed(runnable: () -> Unit, time: Long)

}