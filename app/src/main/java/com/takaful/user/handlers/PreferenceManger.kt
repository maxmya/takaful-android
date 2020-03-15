package com.takaful.user.handlers

import androidx.preference.PreferenceManager
import com.takaful.user.App
import com.takaful.user.data.UserData

object PreferenceManger : PreferenceTasks {

    private val sPrefs = PreferenceManager.getDefaultSharedPreferences(App.appContext)

    private const val TOKEN_KEY = "token_key"
    private const val USER_PHONE_NUMBER = "user-phone_number"
    private const val USER_FULL_NAME = "user-full-name"

    override fun saveToken(token: String) {
        sPrefs.edit().putString(token, TOKEN_KEY).apply()
    }

    override fun retrieveToken(): String {
        return sPrefs.getString(TOKEN_KEY, "").toString()
    }

    override fun deleteToken() {
        sPrefs.edit().putString("", TOKEN_KEY).apply()
    }

    override fun saveUserData(userData: UserData) {
        sPrefs.edit().putString(userData.fullName, USER_FULL_NAME).apply()
        sPrefs.edit().putString(userData.phone, USER_PHONE_NUMBER).apply()
    }

    override fun retrieveUserData(): UserData {
        val fullName = sPrefs.getString(USER_FULL_NAME, "").toString()
        val phone = sPrefs.getString(USER_PHONE_NUMBER, "").toString()
        return UserData(phone, fullName)
    }

    override fun deleteUserData() {
        sPrefs.edit().putString("", USER_FULL_NAME).apply()
        sPrefs.edit().putString("", USER_PHONE_NUMBER).apply()
    }

}