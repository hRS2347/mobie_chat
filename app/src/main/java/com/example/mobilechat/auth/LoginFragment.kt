package com.example.mobilechat.auth

import android.os.Bundle
import android.os.Handler
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.navigation.Navigation
import com.example.mobilechat.MyApplication
import com.example.mobilechat.R
import com.example.mobilechat.databinding.FragmentLoginBinding
import com.example.mobilechat.network.NetConst
import com.example.mobilechat.network.ServerConnector
import org.json.JSONObject

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
                    MyApplication.instance.sharePref.setUserName(bind.etLoginName.text.toString())
                    MyApplication.instance.sharePref.setPassword(bind.etLoginPwd.text.toString())
                    Navigation.findNavController(bind.root)
                        .navigate(R.id.action_loginFragment_to_historyFragment)
                }

                LOGIN_FAILED -> {
                    Toast.makeText(context, "Login failed", Toast.LENGTH_SHORT).show()
                }
            }
            return@Callback true
        })
        return bind.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        MyApplication.instance.sharePref.getUserName()?.let {
            bind.etLoginName.setText(it)
        }
        MyApplication.instance.sharePref.getPassword()?.let {
            bind.etLoginPwd.setText(it)
        }
        bind.btnLoginLogin.setOnClickListener {
            resetIP()
            val username = bind.etLoginName.text.toString()
            val password = bind.etLoginPwd.text.toString()
            if (username.isNotEmpty() && password.isNotEmpty()) {
                connector.login(username, password, object : okhttp3.Callback {
                    override fun onFailure(call: okhttp3.Call, e: java.io.IOException) {
                    }

                    override fun onResponse(call: okhttp3.Call, response: okhttp3.Response) {
                        try {
                            val jsonObject = response.body?.string()?.let { it1 -> JSONObject(it1) }
                            val code = jsonObject?.getInt("status")
                            if (code == 200) {
                                if (username == "root")
                                    MyApplication.instance.isManager = true
                                handler.sendEmptyMessage(LOGIN_SUCCESSFULLY)
                            } else {
                                handler.sendEmptyMessage(LOGIN_FAILED)
                            }
                        } catch (e: Exception) {
                            handler.sendEmptyMessage(LOGIN_FAILED)
                        }
                    }
                })
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
}