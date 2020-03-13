package com.takaful.user

import android.app.Application
import android.content.SharedPreferences
import androidx.annotation.UiThread
import androidx.preference.PreferenceManager
import com.takaful.user.utils.AppExecutors
import com.takaful.user.utils.MainThreadExecutor
import java.util.concurrent.Executors.newFixedThreadPool
import java.util.concurrent.Executors.newSingleThreadExecutor


class App : Application() {

    companion object {

        // const and global assignable
        var TOKEN = ""

        const val TOKEN_KEY = "token_key"
        const val USERNAME = "username"

        // late-init dependencies
        lateinit var sPrefs: SharedPreferences

        lateinit var appExecutors: AppExecutors

        lateinit var username: String

    }


    override fun onCreate() {
        super.onCreate()
        sPrefs = PreferenceManager.getDefaultSharedPreferences(this)
        TOKEN = sPrefs.getString(TOKEN_KEY, "").toString()
        appExecutors = AppExecutors(
            newSingleThreadExecutor(),
            newFixedThreadPool(3),
            MainThreadExecutor()
        )

        username = sPrefs.getString(USERNAME, "").toString()
    }

}