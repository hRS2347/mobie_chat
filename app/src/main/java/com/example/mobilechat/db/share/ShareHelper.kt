package com.example.mobilechat.db.share

import android.content.Context
import android.content.SharedPreferences

class ShareHelper(val context: Context){
    //sharePreference
    private val preferences = context.getSharedPreferences("mobileChat", Context.MODE_PRIVATE)
    //get
    fun get(key: String, default: String): String? {
        return preferences.getString(key, default)
    }
    //set
    fun set(key: String, value: String) {
        preferences.edit().putString(key, value).apply()
    }
    //getUserName
    fun getUserName(): String? {
        return get("username", "")
    }
    //setUserName
    fun setUserName(username: String) {
        set("username", username)
    }

    //getPassword
    fun getPassword(): String? {
        return get("password", "")
    }
    //setPassword
    fun setPassword(password: String) {
        set("password", password)
    }

    //getServerIP
    fun getServerIP(): String? {
        return get("serverIP", "")
    }
    //setServerIP
    fun setServerIP(serverIP: String) {
        set("serverIP", serverIP)
    }
}