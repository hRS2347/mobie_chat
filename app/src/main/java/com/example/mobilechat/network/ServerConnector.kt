package com.example.mobilechat.network

import android.util.Log
import com.example.mobilechat.network.beans.AddJSONBean
import com.example.mobilechat.network.beans.UpdateJSONBean
import com.google.gson.Gson
import okhttp3.Callback
import okhttp3.Cookie
import okhttp3.CookieJar
import okhttp3.FormBody
import okhttp3.HttpUrl
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody

//单例模式，OKHttp3
class ServerConnector {
    companion object {
        val instance = ServerConnector()
        val cookieMap = HashMap<String, List<Cookie>>()
    }

    // 基本的网络请求
    private var client: OkHttpClient = OkHttpClient.Builder().cookieJar(object : CookieJar {
            override fun loadForRequest(url: HttpUrl): List<Cookie> {
                return cookieMap[url.host] ?: ArrayList<Cookie>()
            }

            override fun saveFromResponse(url: HttpUrl, cookies: List<Cookie>) {
                cookieMap[url.host] = cookies
            }

        }).build()

    fun get(url: String, callback: Callback) {
        val request = Request.Builder().url(url).build()
        client.newCall(request).enqueue(callback)
    }

    fun post(url: String, json: String, callback: Callback) {
        val body = json.toRequestBody("application/json; charset=utf-8".toMediaTypeOrNull())
        val request = Request.Builder().url(url).post(body).build()
        client.newCall(request).enqueue(callback)
    }
    //面向业务的网络请求
    /**
     * 登录业务
     * */
    fun login(username: String, password: String, callback: Callback) {
        // 创建请求
        val request = Request.Builder()
            .url(NetConst.BASE_URL + NetConst.LOGIN + "?username=$username&password=$password")
            .post(FormBody.Builder().build()).build()
        // 发送请求
        client.newCall(request).enqueue(callback)
    }

    /**
     * 登出业务
     * */
    fun logout(callback: Callback) {
        val request = Request.Builder().url(NetConst.BASE_URL + NetConst.LOGOUT).get().build()
        client.newCall(request).enqueue(callback)
    }

    /**
     * 获取历史对话表
     * */
    fun loadHistory(callback: Callback) {
        val request = Request.Builder().url(NetConst.BASE_URL + NetConst.LOAD_HISTORY)
            .post(FormBody.Builder().build()).build()
        client.newCall(request).enqueue(callback)
    }

    /**
     * 新建对话,请求一个新的session
     * */
    fun newSession(callback: Callback) {
        val request = Request.Builder().url(NetConst.BASE_URL + NetConst.NEW_SESSION).get().build()
        client.newCall(request).enqueue(callback)
    }

    /**
     * 选中某次对话,请求对话内容
     */
    fun loadContent(msgId: String, callback: Callback) {
        val request =
            Request.Builder().url(NetConst.BASE_URL + NetConst.LOAD_CONTENT + msgId).get().build()
        client.newCall(request).enqueue(callback)
    }

    /**
     * 发送消息
     */
    fun sendMsg(sId: String, question: String, callback: Callback) {
        val request = Request.Builder()
            .url(NetConst.BASE_URL + NetConst.SEND_MSG + sId + "?question=$question").get().build()
        client.newCall(request).enqueue(callback)
    }

    /**
     * cache
     */
    fun cache(sId: String, callback: Callback) {
        val request = Request.Builder().url(NetConst.BASE_URL + NetConst.CACHE + sId).get().build()
        client.newCall(request).enqueue(callback)
    }

    /**
     * delete msg
     */
    fun deleteMsg(sId: String, callback: Callback) {
        val request =
            Request.Builder().url(NetConst.BASE_URL + NetConst.DELETE_MSG + sId).get().build()
        client.newCall(request).enqueue(callback)
    }

    /**
     * load user
     */
    fun loadUser(callback: Callback) {
        val request = Request.Builder().url(NetConst.BASE_URL + NetConst.LOAD_USER).get().build()
        client.newCall(request).enqueue(callback)
    }

    /**
     * add user
     */
    fun addUser(name: String, pwd: String, pm: Double, callback: Callback) {
        val json = Gson().toJson(AddJSONBean(name, pwd, pm.toInt()))
        val request = Request.Builder().url(NetConst.BASE_URL + NetConst.ADD_USER)
            .post(json.toRequestBody("application/json; charset=utf-8".toMediaTypeOrNull())).build()
        client.newCall(request).enqueue(callback)
        Log.e("addUser", "addUser: $json")
    }

    /**
     * delete user
     */
    fun deleteUser(name: String, callback: Callback) {
        val request =
            Request.Builder().url(NetConst.BASE_URL + NetConst.DELETE_USER + name).delete().build()
        client.newCall(request).enqueue(callback)
    }

    /**
     * update user
     */
    fun updateUser(name: String, pwd: String, pm: Double, callback: Callback) {
        try {
            val json = Gson().toJson(UpdateJSONBean(name, pwd, pm.toInt()))
            val request = Request.Builder().url(NetConst.BASE_URL + NetConst.UPDATE_USER + name)
                .put(json.toRequestBody("application/json; charset=utf-8".toMediaTypeOrNull()))
                .build()
            Log.e("updateUser", "updateUser: $json")
            client.newCall(request).enqueue(callback)
        } catch (e: Exception) {
            e.printStackTrace()
        }

    }
}