package com.example.mobilechat.manager.main_page

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
import com.example.mobilechat.R
import com.example.mobilechat.databinding.FragmentManagerBinding
import com.example.mobilechat.manager.UserBean
import com.example.mobilechat.utils.network.ServerConnector
import com.example.mobilechat.utils.network.beans.UserJSONBean
import com.google.gson.Gson
import okhttp3.Call
import okhttp3.Callback
import okhttp3.Response
import java.io.IOException

class ManagerFragment : Fragment() {
    companion object {
        const val WRONG = 0
        const val SUCCESS = 1
        const val SUCCESS_DELETE = 2
        const val CHANGE = 3
    }

    private lateinit var handler: Handler
    private lateinit var adapter: ManagerAdapter
    private var users = ArrayList<UserBean>()
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val binding = FragmentManagerBinding.inflate(inflater, container, false)
        binding.recyclerView.layoutManager = LinearLayoutManager(context)
        adapter = ManagerAdapter(
            users, requireContext()
        )
        binding.recyclerView.adapter = adapter
        handler = Handler(Handler.Callback {
            when (it.what) {
                WRONG -> {
                    Toast.makeText(context, "加载错误", Toast.LENGTH_SHORT).show()
                    Navigation.findNavController(binding.root)
                        .navigateUp()
                }
                CHANGE -> {
                    adapter.notifyItemChanged(it.arg1)
                }
                SUCCESS -> {
                    adapter.notifyDataSetChanged()
                }

                SUCCESS_DELETE -> {
                    loadUsers()
                }
            }
            return@Callback true
        })

        adapter.setOnItemClickListener(object : ManagerAdapter.OnItemClickListener {
            override fun onDeleteLongClick(position: Int) {
                ServerConnector.instance.deleteUser(
                    users[position].username,
                    object : Callback {
                        override fun onFailure(call: Call, e: IOException) {
                            handler.sendEmptyMessage(WRONG)
                        }

                        override fun onResponse(call: Call, response: Response) {
                            try {
                                if (response.code != 200) {
                                    handler.sendEmptyMessage(WRONG)
                                    return
                                }
                                handler.sendEmptyMessage(SUCCESS_DELETE)
                            } catch (e: Exception) {
                                e.printStackTrace()
                                handler.sendEmptyMessage(WRONG)
                            }
                        }

                    })
            }

            override fun onDeleteClick() {
                Toast.makeText(context, "长按删除", Toast.LENGTH_SHORT).show()
            }

            override fun onUpdateClick(position: Int, n: String, p: String, pm: Double) {
                ServerConnector.instance.updateUser(
                    n, p, pm,
                    object : Callback {
                        override fun onFailure(call: Call, e: IOException) {
                            handler.sendEmptyMessage(WRONG)
                        }

                        override fun onResponse(call: Call, response: Response) {
                            try {
                                if (response.code != 200) {
                                    handler.sendEmptyMessage(WRONG)
                                    return
                                }
                                handler.sendEmptyMessage(SUCCESS_DELETE)
                            } catch (e: Exception) {
                                e.printStackTrace()
                                handler.sendEmptyMessage(WRONG)
                            }
                        }

                    })
            }

            override fun onUpClick(position: Int) {
                users[position].permission += 1
                handler.sendMessage(handler.obtainMessage(CHANGE, position, 0))
            }

            override fun onDownClick(position: Int) {
                if (users[position].permission==2.0) {
                    Toast.makeText(context, "无权修改管理员，请通知超级管理员在后台修改。", Toast.LENGTH_SHORT).show()
                    return
                }
                users[position].permission -= 1
                handler.sendMessage(handler.obtainMessage(CHANGE, position, 0))
            }
        })

        binding.btnNew.setOnClickListener {
            Navigation.findNavController(binding.root)
                .navigate(R.id.action_managerFragment_to_addFragment)
        }
        return binding.root
    }

    override fun onResume() {
        super.onResume()
        loadUsers()
    }

    private fun loadUsers() {
        handler.postDelayed({
            ServerConnector.instance.loadUser(object : Callback {
                override fun onFailure(call: Call, e: IOException) {
                    handler.sendEmptyMessage(WRONG)
                }

                override fun onResponse(call: Call, response: Response) {
                    try {
                        val userBeans =
                            Gson().fromJson(response.body?.string(), UserJSONBean::class.java)
                                .users.map {
                                    UserBean(
                                        it[0] as Double,
                                        it[1].toString(),
                                        it[2].toString(),
                                        it[3] as Double
                                    )
                                }
                        users = userBeans as ArrayList<UserBean>
                        adapter.users = users
                        handler.sendEmptyMessage(SUCCESS)
                    } catch (e: Exception) {
                        e.printStackTrace()
                        handler.sendEmptyMessage(WRONG)
                    }
                }

            })
        }, 300)
    }

}