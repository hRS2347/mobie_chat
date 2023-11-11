package com.example.mobilechat.chat.chat_page

import android.os.Bundle
import android.os.Handler
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.navigation.Navigation
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.mobilechat.MyApplication
import com.example.mobilechat.databinding.FragmentChatBinding
import com.example.mobilechat.network.beans.ChatJSONBean
import com.example.mobilechat.network.ServerConnector
import com.google.gson.Gson
import okhttp3.Call
import okhttp3.Callback
import okhttp3.Response
import java.io.IOException

class ChatFragment : Fragment() {
    companion object {
        const val WRONG = 0
        const val UPDATE = 1
        const val ACCEPT_MSG = 2
    }

    private lateinit var bind: FragmentChatBinding
    private lateinit var adapter: ChatAdapter

    private var list = listOf<ChatBean>()

    private lateinit var handler: Handler
    private var isReceiving = false
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        bind = FragmentChatBinding.inflate(layoutInflater)
        bind.rv.layoutManager = LinearLayoutManager(context)
        adapter = ChatAdapter(list)
        bind.rv.adapter = adapter
        handler = Handler(Handler.Callback {
            when (it.what) {
                WRONG -> {
                    bind.btnChatSubmit.isEnabled = true
                    Toast.makeText(context, "网络错误", Toast.LENGTH_SHORT).show()
                    //返回上一层
                    Navigation.findNavController(requireView()).navigateUp()
                }

                UPDATE -> {
                    adapter.notifyDataSetChanged()
                }

                ACCEPT_MSG -> {
                    val content = it.obj as String
                    if (isReceiving) {
                        if (content == "[DONE]\n\n") {
                            isReceiving = false
                            handler.postDelayed(Runnable { bind.btnChatSubmit.isEnabled = true }, 300)
                        } else adapter.contents[adapter.contents.size - 1].sentence += content.substring(
                            0, content.length - 2
                        )
                    } else {
                        //新增一条消息
                        adapter.contents = adapter.contents + ChatBean(
                            0.0, "", 3.0, ""
                        )
                        isReceiving = true
                        //收起键盘
                        bind.et.clearFocus()

                    }
                    adapter.notifyDataSetChanged()
                    //rv滚动到底部
                    bind.rv.scrollToPosition(adapter.contents.size - 1)
                }
            }
            return@Callback true
        })

        if (MyApplication.instance.hBean != null) {
            ServerConnector.instance.loadContent(MyApplication.instance.hBean!!.id.toString(),
                object : Callback {
                    override fun onFailure(call: Call, e: IOException) {
                        handler.sendEmptyMessage(WRONG)
                    }

                    override fun onResponse(call: Call, response: Response) {
                        try {
                            val content = response.body?.string()
                            Gson().fromJson(content, ChatJSONBean::class.java).let {
                                adapter.contents = it.content_list.map { chat ->
                                    ChatBean(
                                        chat[0] as Double,
                                        chat[1] as String,
                                        chat[2] as Double,
                                        chat[3] as String
                                    )
                                }
                            }
                            handler.sendEmptyMessage(UPDATE)
                            MyApplication.instance.sessionId =
                                MyApplication.instance.hBean?.sessionId
                        } catch (e: Exception) {
                            e.printStackTrace()
                            handler.sendEmptyMessage(WRONG)
                        }
                    }
                })
        } else {
            ServerConnector.instance.newSession(object : Callback {
                override fun onFailure(call: Call, e: IOException) {
                    handler.sendEmptyMessage(WRONG)
                }

                override fun onResponse(call: Call, response: Response) {
                    try {
                        MyApplication.instance.sessionId = response.headers["session_id"]
                        Log.d("ChatFragment", "onResponse: ${MyApplication.instance.sessionId}")
                    } catch (e: Exception) {
                        e.printStackTrace()
                        handler.sendEmptyMessage(WRONG)
                    }
                }

            })
        }
        bind.btnChatSubmit.setOnClickListener {
            val content = bind.et.text.toString()
            if (content.isEmpty()) {
                Toast.makeText(context, "内容不能为空", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            } else {
                bind.btnChatSubmit.isEnabled = false
                //empty edit text
                bind.et.setText("")
                //add to list
                adapter.contents = adapter.contents + ChatBean(
                    0.0, "", 2.0, content
                )
                bind.rv.scrollToPosition(adapter.contents.size - 1)
                //update ui
                adapter.notifyDataSetChanged()
                //send msg
                ServerConnector.instance.sendMsg(MyApplication.instance.sessionId!!,
                    content,
                    object : Callback {
                        override fun onFailure(call: Call, e: IOException) {
                            handler.sendEmptyMessage(WRONG)
                        }

                        override fun onResponse(call: Call, response: Response) {
                            try {
                                // 获取响应流
                                val responseBody = response.body?.byteStream()
                                // 处理事件流数据
                                val buffer = ByteArray(1024)
                                while (true) {
                                    if (responseBody == null) break
                                    val bytesRead = responseBody.read(buffer)
                                    if (bytesRead == -1) break
                                    val eventData = String(buffer, 0, bytesRead)
                                    handler.sendMessage(
                                        handler.obtainMessage(
                                            ACCEPT_MSG, eventData.subSequence(6, eventData.length)
                                        )
                                    )
                                    if (eventData == "data: [DONE]\n\n") break
                                }
                            } catch (e: Exception) {
                                e.printStackTrace()
                                handler.sendEmptyMessage(WRONG)
                            }
                        }
                    })
            }

        }
        return bind.root
    }


    override fun onDestroy() {
        super.onDestroy()
        if (MyApplication.instance.sessionId == null || isReceiving) return
        ServerConnector.instance.cache(MyApplication.instance.sessionId!!,
            object : Callback {
                override fun onFailure(call: Call, e: IOException) {
                }

                override fun onResponse(
                    call: Call, response: Response
                ) {
                }
            })
    }
}
