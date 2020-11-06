package com.github.b1412.api.service

import org.springframework.security.core.userdetails.User


interface SecurityFilter {

    fun currentUser(): User

    fun query(method: String, requestURI: String): Map<String, String>

}
