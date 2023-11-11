package com.example.mobilechat.network
//存放网络请求的常量和配置
class NetConst {
    companion object {
        var BASE_URL = "http:172.26.97.37:5000"
        const val LOGIN = "/auth/login"
        const val LOGOUT = "/auth/logout"

        const val LOAD_HISTORY = "/gpt/loadHistory"
        const val NEW_SESSION = "/gpt/session_id"
        const val LOAD_CONTENT = "/gpt/content/"
        const val SEND_MSG = "/gpt/"
        const val CACHE = "/gpt/cache/"
        const val DELETE_MSG = "/gpt/del/"

        const val LOAD_USER = "/manage/user"
        const val ADD_USER = "/manage/user"
        const val DELETE_USER = "/manage/user/"
        const val UPDATE_USER = "/manage/user/"
    }
}