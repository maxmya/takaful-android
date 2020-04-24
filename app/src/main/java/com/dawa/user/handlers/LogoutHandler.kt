package com.dawa.user.handlers

object LogoutHandler {

    fun doLogout() {
        PreferenceManger.deleteToken()
        PreferenceManger.deleteUserData()
    }

}