package com.example.mobilechat.auth

import android.graphics.Color
import android.os.Bundle
import android.os.Handler
import android.text.Editable
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.navigation.Navigation
import com.example.mobilechat.MyApplication
import com.example.mobilechat.R
import com.example.mobilechat.databinding.FragmentLoginBinding
import com.example.mobilechat.main.history_page.HistoryFragment
import com.example.mobilechat.manager.UserBean
import com.example.mobilechat.utils.network.NetConst
import com.example.mobilechat.utils.network.ServerConnector
import com.example.mobilechat.utils.network.beans.UserJSONBean
import com.google.android.material.transition.MaterialContainerTransform
import com.google.gson.Gson
import okhttp3.Call
import okhttp3.Response
import org.json.JSONObject
import java.io.IOException

class LoginFragment : Fragment() {
    companion object {
        const val LOGIN_SUCCESSFULLY: Int = 1
        const val LOGIN_FAILED: Int = 2
    }

    private lateinit var bind: FragmentLoginBinding
    private lateinit var connector: ServerConnector
    private lateinit var handler: Handler

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        bind = FragmentLoginBinding.inflate(layoutInflater)
        connector = ServerConnector.instance
        handler = Handler(Handler.Callback {
            when (it.what) {
                LOGIN_SUCCESSFULLY -> {
                    checkUser()
                    MyApplication.instance.sharePref.setUserName(bind.etName.text.toString())
                    MyApplication.instance.sharePref.setPassword(bind.etPwd.text.toString())
                    Navigation.findNavController(bind.root)
                        .navigate(R.id.action_loginFragment_to_historyFragment)
                }

                LOGIN_FAILED -> {
                    Toast.makeText(context, "Login failed", Toast.LENGTH_SHORT).show()
                }
            }
            bind.btnLoginLogin.visibility = View.VISIBLE
            bind.progressBar.visibility = View.INVISIBLE
            return@Callback true
        })
        return bind.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        bind.progressBar.visibility = View.INVISIBLE
        bind.btnLoginLogin.visibility = View.VISIBLE
        bind.etIp.text = Editable.Factory.getInstance()
            .newEditable(MyApplication.instance.sharePref.getServerIP())

        MyApplication.instance.sharePref.getUserName()?.let {
            bind.etName.setText(it)
        }
        MyApplication.instance.sharePref.getPassword()?.let {
            bind.etPwd.setText(it)
        }
        bind.btnLoginLogin.setOnClickListener {
            val transform = MaterialContainerTransform().apply {
                startView = bind.btnLoginLogin
                endView = bind.progressBar
                duration = 700
                scrimColor = Color.TRANSPARENT
            }
            sharedElementEnterTransition = transform
            bind.btnLoginLogin.visibility = View.INVISIBLE
            bind.progressBar.visibility = View.VISIBLE
            bind.progressBar.animate()
            resetIP()
            val username = bind.etName.text.toString()
            val password = bind.etPwd.text.toString()
            try {
                if (username.isNotEmpty() && password.isNotEmpty()) {
                    connector.login(username, password, object : okhttp3.Callback {
                        override fun onFailure(call: okhttp3.Call, e: java.io.IOException) {
                            handler.sendEmptyMessage(LOGIN_FAILED)
                            Log.d("LoginFragment", "onFailure: $e")
                        }

                        override fun onResponse(
                            call: okhttp3.Call,
                            response: okhttp3.Response
                        ) {
                            try {
                                val json = JSONObject(response.body?.string())
                                val code = json.getInt("status")
                                Log.d("LoginFragment", "onResponse: $code")
                                if (code == 200) {
                                    MyApplication.instance.user = username
                                    MyApplication.instance.pwd = password
                                    handler.sendEmptyMessage(LOGIN_SUCCESSFULLY)
                                } else {
                                    handler.sendEmptyMessage(LOGIN_FAILED)
                                }
                            } catch (e: Exception) {
                                Log.d("LoginFragment", "onResponse: $e")
                                handler.sendEmptyMessage(LOGIN_FAILED)
                            }
                        }
                    })
                }
            } catch (e: Exception) {
                handler.sendEmptyMessage(LOGIN_FAILED)
            }
        }
    }

    private fun resetIP() {
        var ip = bind.etIp.text.toString()
        if (ip.isNotEmpty()) {
            MyApplication.instance.sharePref.setServerIP(ip)
        }
        ip = MyApplication.instance.sharePref.getServerIP() ?: "172.26.97.37"
        NetConst.BASE_URL = "http://$ip:5000"
    }

    private fun checkUser() {
        Log.d("HistoryFragment", "checkUser")
        ServerConnector.instance.loadUser(object : okhttp3.Callback {
            override fun onFailure(call: Call, e: IOException) {
                Log.e("HistoryFragment", e.toString())
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
                    for (i in userBeans) {
                        if (i.username == MyApplication.instance.user
                            && i.password == MyApplication.instance.pwd
                        ) {
                            MyApplication.instance.uid = i.id.toString()
                            if (i.permission == 2.0) {
                                MyApplication.instance.isManager = true
                                Log.d("HistoryFragment", "isManager")
                                return
                            }
                        }
                    }
                    MyApplication.instance.isManager = false
                } catch (e: Exception) {
                    e.printStackTrace()
                    MyApplication.instance.isManager = false
                }
            }

        })
    }
}