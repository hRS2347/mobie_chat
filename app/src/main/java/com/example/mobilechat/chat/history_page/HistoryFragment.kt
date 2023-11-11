package com.example.mobilechat.chat.history_page

import android.os.Bundle
import android.os.Handler
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
import com.example.mobilechat.network.ServerConnector
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
    }

    private lateinit var bind: FragmentHistoryBinding
    private lateinit var btnNew: View
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
                    Toast.makeText(context, "Delete message successfully", Toast.LENGTH_SHORT).show()
                    loadHistory()
                }

                DELETE_MSG_FAILED -> {
                    Toast.makeText(context, "Delete message failed", Toast.LENGTH_SHORT).show()
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

                override fun onDeleteLongClick(position: Int) {
                    ServerConnector.instance.deleteMsg(values[position].id.toString(), object : Callback {
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

        if (MyApplication.instance.isManager) {
            bind.btnManager.visibility = View.VISIBLE
            bind.tvRole.text = "Role:Manager"
            bind.btnManager.setOnClickListener {
                Navigation.findNavController(bind.root).navigate(
                    R.id.action_historyFragment_to_managerFragment
                )
            }
        } else {
            bind.btnManager.visibility = View.GONE
            bind.tvRole.text = "Role:User"
        }

        btnNew = bind.cvHistoryNew
        btnNew.setOnClickListener {
            MyApplication.instance.sessionId = null
            MyApplication.instance.hBean = null
            Navigation.findNavController(bind.root).navigate(
                R.id.action_historyFragment_to_methodsFragment
            )
        }

        bind.btnLogout.setOnClickListener{
            logout()
            //popBackStack()返回上一个fragment
            Navigation.findNavController(bind.root).navigate(
                R.id.action_historyFragment_to_loginFragment
            )
        }
        return bind.root
    }

    override fun onResume() {
        super.onResume()
        loadHistory()
    }

    override fun onDestroy() {
        super.onDestroy()
        handler.removeCallbacksAndMessages(null)
        logout()
    }

    private fun loadHistory() {
        handler.postDelayed({
            ServerConnector.instance.loadHistory(object : Callback {
                override fun onFailure(call: Call, e: IOException) {
                    handler.sendEmptyMessage(LOAD_HISTORY_FAILED)
                }

                override fun onResponse(call: Call, response: Response) {
                    try {
                        Gson().fromJson(response.body?.string(), List::class.java)?.let {
                            val list = ArrayList<HistoryBean>()
                            println(it)
                            for (i in it) {
                                val item = i as List<*>
                                list.add(
                                    HistoryBean(
                                        item[0] as Double,
                                        item[1] as String,
                                        item[2] as String,
                                        item[3] as String
                                    )
                                )
                            }
                            adapter.values = list
                        }
                        handler.sendEmptyMessage(LOAD_HISTORY_SUCCESSFULLY)
                    } catch (e: Exception) {
                        e.printStackTrace()
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