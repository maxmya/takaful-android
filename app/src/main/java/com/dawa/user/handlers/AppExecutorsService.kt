package com.dawa.user.handlers

import android.os.Handler
import java.util.concurrent.Executor
import java.util.concurrent.Executors.newFixedThreadPool
import java.util.concurrent.Executors.newSingleThreadExecutor

object AppExecutorsService{

     fun diskIO(): Executor {
        return newSingleThreadExecutor()
    }

     fun networkIO(): Executor {
        return newFixedThreadPool(3)
    }

     fun mainThread(): Executor {
        return MainThreadExecutor()
    }

     fun handlerDelayed(runnable: () -> Unit, time: Long) {
        mainThread().execute {
            Handler().postDelayed(runnable, time)
        }
    }

}