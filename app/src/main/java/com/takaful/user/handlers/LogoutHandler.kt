package com.takaful.user.handlers

object LogoutHandler {

    fun doLogout() {
        PreferenceManger.deleteToken()
        PreferenceManger.deleteUserData()
    }

}