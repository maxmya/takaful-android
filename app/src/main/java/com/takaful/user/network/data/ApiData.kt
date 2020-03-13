package com.takaful.user.network.data

/*
  This kotlin file can hold an infinite number of data classes
  so wy we need to separate them into different files they all
  for one business cases
 */

data class UserRegisterRequest(
    val username: String,
    val password: String,
    val phone: String,
    val fullName: String,
    val pictureUrl: String
)

data class UserRegisterResponse(
    val success: Boolean,
    val message: String
)

data class UserTokenRequest(
    val username: String,
    val password: String
)

data class TokenResponse(
    val success: Boolean,
    val jwtToken: String
)
