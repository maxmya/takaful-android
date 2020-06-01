package com.dawa.user.exceptions

import java.lang.Exception

class ConnectionException constructor(override val message: String) : Exception(message)