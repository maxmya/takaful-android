package com.takaful.user.handlers

import android.os.Handler
import java.util.concurrent.Executor
import java.util.concurrent.Executors.newFixedThreadPool
import java.util.concurrent.Executors.newSingleThreadExecutor

object AppExecutorsClient : AppExecutorsService {

    override fun diskIO(): Executor {
        return newSingleThreadExecutor()
    }

    override fun networkIO(): Executor {
        return newFixedThreadPool(3)
    }

    override fun mainThread(): Executor {
        return MainThreadExecutor()
    }

    override fun handlerDelayed(runnable: () -> Unit, time: Long) {
        mainThread().execute {
            Handler().postDelayed(runnable, time)
        }
    }

}