package com.dawa.user.handlers

import androidx.preference.PreferenceManager
import com.dawa.user.App
import com.dawa.user.data.UserData

object PreferenceManger : PreferenceTasks {

    private val preferences = PreferenceManager.getDefaultSharedPreferences(App.appContext)

    private const val TOKEN_KEY = "token_key"
    private const val USER_PHONE_NUMBER = "user-phone_number"
    private const val USER_FULL_NAME = "user-full-name"
    private const val USER_PROFILE_PICTURE = "user-profile_picture"


    override fun saveToken(token: String) {
        preferences.edit().putString(TOKEN_KEY, token).apply()
    }

    override fun retrieveToken(): String {
        return preferences.getString(TOKEN_KEY, "").toString()
    }

    override fun deleteToken() {
        preferences.edit().putString(TOKEN_KEY, "").apply()
    }

    override fun saveUserData(userData: UserData) {
        preferences.edit().putString(USER_FULL_NAME, userData.fullName).apply()
        preferences.edit().putString(USER_PHONE_NUMBER, userData.phone).apply()
        preferences.edit().putString(USER_PROFILE_PICTURE, userData.imageUrl).apply()
    }

    override fun retrieveUserData(): UserData {
        val fullName = preferences.getString(USER_FULL_NAME, "").toString()
        val phone = preferences.getString(USER_PHONE_NUMBER, "").toString()
        val imageUrl = preferences.getString(USER_PROFILE_PICTURE, "").toString()
        return UserData(phone, fullName, imageUrl)
    }

    override fun deleteUserData() {
        preferences.edit().putString(USER_FULL_NAME, "").apply()
        preferences.edit().putString(USER_PHONE_NUMBER, "").apply()
        preferences.edit().putString(USER_PROFILE_PICTURE, "").apply()
    }

}