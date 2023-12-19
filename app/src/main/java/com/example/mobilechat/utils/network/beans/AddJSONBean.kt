package com.example.mobilechat.utils.network.beans

class AddJSONBean(n: String, p: String, pm: Int) {
    private var users: List<User>

    init {
        users = listOf(User(n, p, pm))
    }

    data class User(
        val username: String,
        val password: String,
        val permission: Int
    )
}