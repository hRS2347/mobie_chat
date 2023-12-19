package com.example.mobilechat.main.history_page

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
import androidx.recyclerview.widget.RecyclerView
import com.example.mobilechat.MyApplication
import com.example.mobilechat.R
import com.example.mobilechat.databinding.FragmentHistoryBinding
import com.example.mobilechat.manager.UserBean
import com.example.mobilechat.manager.main_page.ManagerFragment
import com.example.mobilechat.utils.network.ServerConnector
import com.example.mobilechat.utils.network.beans.UserJSONBean
import com.google.gson.Gson
import okhttp3.Call
import okhttp3.Callback
import okhttp3.Response
import java.io.IOException

class HistoryFragment : Fragment() {
    companion object {
        const val LOAD_HISTORY_SUCCESSFULLY = 1
        const val LOAD_HISTORY_FAILED = 2
        const val DELETE_MSG_SUCCESSFULLY = 3
        const val DELETE_MSG_FAILED = 4
        const val CHECK_USER_SUCCESSFULLY = 5
    }

    private lateinit var bind: FragmentHistoryBinding
    private lateinit var rv: RecyclerView
    private lateinit var adapter: HistoryAdapter
    private lateinit var handler: Handler
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        initData()
    }

    private fun initData() {
        adapter = HistoryAdapter(listOf())
        handler = Handler(Handler.Callback {
            when (it.what) {
                LOAD_HISTORY_SUCCESSFULLY -> {
                    adapter.notifyDataSetChanged()
                }

                LOAD_HISTORY_FAILED -> {
                    Toast.makeText(context, "Load history failed", Toast.LENGTH_SHORT).show()
                }

                DELETE_MSG_SUCCESSFULLY -> {
                    Toast.makeText(context, "Delete message successfully", Toast.LENGTH_SHORT)
                        .show()
                    loadHistory()
                }

                DELETE_MSG_FAILED -> {
                    Toast.makeText(context, "Delete message failed", Toast.LENGTH_SHORT).show()
                }
                CHECK_USER_SUCCESSFULLY -> {

                }
            }
            return@Callback true
        })
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        bind = FragmentHistoryBinding.inflate(layoutInflater)
        rv = bind.rvHistoryList
        rv.layoutManager = LinearLayoutManager(context)
        rv.adapter = this.adapter

        with(adapter) {
            setOnItemClickListener(object : HistoryAdapter.OnItemClickListener {
                override fun onItemClick(position: Int) {
                    MyApplication.instance.sessionId = values[position].sessionId
                    MyApplication.instance.hBean = values[position]
                    Navigation.findNavController(bind.root).navigate(
                        R.id.action_historyFragment_to_chatFragment
                    )
                }

                override fun onDeleteClick() {
                    Toast.makeText(context, "注意，删除需要长按按钮。", Toast.LENGTH_SHORT).show()
                }

                override fun onDeleteLongClick(position: Int) {
                    Log.d("HistoryFragment", "deleteMsg: ${ (values[position].id).toInt().toString()}")
                    ServerConnector.instance.deleteMsg(
                        (values[position].id).toInt().toString(),
                        MyApplication.instance.uid,
                        object : Callback {
                            override fun onFailure(call: Call, e: IOException) {
                                handler.sendEmptyMessage(DELETE_MSG_FAILED)
                            }

                            override fun onResponse(call: Call, response: Response) {
                                handler.sendEmptyMessage(DELETE_MSG_SUCCESSFULLY)
                            }
                        })
                }
            })
        }

        bind.btnManager.visibility = if (MyApplication.instance.isManager) {
            View.VISIBLE

        } else {
            View.GONE
        }
//        if (MyApplication.instance.isManager) {
//            Toast.makeText(context, "欢迎管理员登录", Toast.LENGTH_SHORT).show()
//        }else{
//            Toast.makeText(context, "欢迎用户登录", Toast.LENGTH_SHORT).show()
//        }

        bind.btnManager.setOnClickListener {
            Navigation.findNavController(bind.root).navigate(
                R.id.action_historyFragment_to_managerFragment
            )
        }

        bind.btnNew.setOnClickListener {
            MyApplication.instance.sessionId = null
            MyApplication.instance.hBean = null
            MyApplication.instance.prologue = ""
            Navigation.findNavController(bind.root).navigate(
                R.id.action_historyFragment_to_chatFragment
            )
        }

        bind.btnScene.setOnClickListener {
            MyApplication.instance.sessionId = null
            MyApplication.instance.hBean = null
            Navigation.findNavController(bind.root).navigate(
                R.id.action_historyFragment_to_methodsFragment
            )
        }

        bind.btnShortHand.setOnClickListener {
            MyApplication.instance.sessionId = null
            MyApplication.instance.hBean = null
            Navigation.findNavController(bind.root).navigate(
                R.id.action_historyFragment_to_recordFragment
            )
        }

        bind.btnLogout.setOnClickListener {
            logout()
            //popBackStack()返回上一个fragment
            Navigation.findNavController(bind.root).navigate(
                R.id.action_historyFragment_to_loginFragment
            )
        }

        return bind.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
    }

    override fun onResume() {
        super.onResume()
        loadHistory()
    }



    override fun onDestroy() {
        super.onDestroy()
        handler.removeCallbacksAndMessages(null)
    }

    private fun loadHistory() {
        Log.d("HistoryFragment", "loadHistory")
        handler.postDelayed({
            ServerConnector.instance.loadHistory(object : Callback {
                override fun onFailure(call: Call, e: IOException) {
                    Log.e("HistoryFragment", e.toString())
                    handler.sendEmptyMessage(LOAD_HISTORY_FAILED)
                }

                override fun onResponse(call: Call, response: Response) {
                    try {
                        Gson().fromJson(response.body?.string(), List::class.java)?.let {
                            val list = ArrayList<HistoryBean>()
                            for (i in it) {
                                val item = i as List<*>
                                list.add(
                                    HistoryBean(
                                        item[0] as Double,
                                        item[1] as Double,
                                        item[2] as String,
                                        item[3] as String,
                                        item[4] as String
                                    )
                                )
                            }
                            adapter.values = list
                        }
                        handler.sendEmptyMessage(LOAD_HISTORY_SUCCESSFULLY)
                    } catch (e: Exception) {
                        Log.i("HistoryFragment", e.toString())
                        handler.sendEmptyMessage(LOAD_HISTORY_FAILED)
                    }
                }
            })
        }, 500)
    }

    private fun logout() {
        MyApplication.instance.isManager = false
        ServerConnector.instance.logout(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
            }

            override fun onResponse(call: Call, response: Response) {
            }
        })
    }
}