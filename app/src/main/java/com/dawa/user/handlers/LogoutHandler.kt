package com.dawa.user.handlers

object LogoutHandler {

    fun doLogout() {
        PreferenceManagerService.deleteToken()
        PreferenceManagerService.deleteUserData()
    }

}