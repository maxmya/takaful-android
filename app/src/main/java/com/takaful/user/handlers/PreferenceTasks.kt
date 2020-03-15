package com.takaful.user.handlers

import com.takaful.user.data.UserData


interface PreferenceTasks {
    fun saveToken(token: String)
    fun retrieveToken(): String
    fun deleteToken()
    fun saveUserData(userData: UserData)
    fun retrieveUserData(): UserData
    fun deleteUserData()
}


