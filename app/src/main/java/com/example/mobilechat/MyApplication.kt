package com.example.mobilechat

import android.app.Application
import com.example.mobilechat.main.history_page.HistoryBean
import com.example.mobilechat.db.share.ShareHelper
import kotlinx.coroutines.newSingleThreadContext
import java.util.concurrent.Executor

class MyApplication:Application() {
    companion object{
        lateinit var instance: MyApplication
    }

    lateinit var sharePref : ShareHelper
    var sessionId : String? = null
    var hBean : HistoryBean? = null
    var context = this
    var isManager = false
    var prologue = ""
    var user = ""
    var pwd = ""
    var uid = ""

    override fun onCreate() {
        super.onCreate()
        instance = this
        sharePref = ShareHelper(this)
    }
}