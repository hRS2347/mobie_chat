package com.example.mobilechat.network.beans

class UpdateJSONBean(username: String, password: String, permission: Int) {
    private var user: User? = null

    init {
        user = User(username, password, permission)
    }
    data class User(
        val username: String,
        val password: String,
        val permission: Int
    )
}