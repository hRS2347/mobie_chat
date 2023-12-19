package com.example.mobilechat.main.history_page

data class HistoryBean(val id:Double, var role:Double,var topic:String, val time:String, val sessionId:String){
    //overwirte all params constructor
    init {
        val len = 16
        //if len of topic>10, then cut it and add "..."
        if(topic.length>len){
            topic=topic.substring(0,len)+"..."
        }
    }
}