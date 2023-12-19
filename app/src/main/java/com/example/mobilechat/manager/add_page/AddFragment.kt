package com.example.mobilechat.manager.add_page

import android.os.Bundle
import android.os.Handler
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.MutableLiveData
import androidx.navigation.Navigation
import com.example.mobilechat.databinding.FragmentAddBinding
import com.example.mobilechat.utils.network.ServerConnector
import okhttp3.Call
import okhttp3.Callback
import okhttp3.Response
import java.io.IOException

class AddFragment : Fragment() {
    companion object {
        const val WRONG = 0
        const val SUCCESS = 1
    }

    private lateinit var handler: Handler
    private lateinit var binding: FragmentAddBinding
    private val role = MutableLiveData<Double>(1.0)
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        handler = Handler(Handler.Callback {
            when (it.what) {
                WRONG -> {
                    Toast.makeText(context, "添加失败", Toast.LENGTH_SHORT).show()
                }
                SUCCESS -> {
                    Toast.makeText(context, "添加成功", Toast.LENGTH_SHORT).show()
                    Navigation.findNavController(binding.root).navigateUp()
                }
            }
            return@Callback false
        })
        binding = FragmentAddBinding.inflate(inflater, container, false)
        binding.btnCancel.setOnClickListener {
            Navigation.findNavController(it).navigateUp()
        }
        binding.roleUp.setOnClickListener {
            role.value = role.value?.plus(1)
        }
        binding.roleDown.setOnClickListener {
            role.value = role.value?.minus(1)
        }
        binding.btnConfirm.setOnClickListener {
            val name = binding.etName.text.toString()
            val pwd = binding.etPwd.text.toString()
            val pm = when(binding.tvRole.text.toString()){
                "管理员" -> 2.0
                "普通用户" -> 1.0
                else -> 0.0
            }
            if (name.isEmpty() || pwd.isEmpty() || (pm !in 0.0..2.0)) {
                Toast.makeText(context, "信息存在错误", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            addUser(name, pwd, pm)
        }

        role.observe(viewLifecycleOwner) {
            binding.tvRole.text = when (it) {
                0.0 -> "封禁用户"
                1.0 -> "普通用户"
                2.0 -> "管理员"
                else -> "未知"
            }
            binding.ivRole.setImageResource(
                when (it) {
                    0.0 -> com.example.mobilechat.R.drawable.danger
                    1.0 -> com.example.mobilechat.R.drawable.user
                    2.0 -> com.example.mobilechat.R.drawable.police
                    else -> com.example.mobilechat.R.drawable.zeus
                }
            )
            binding.roleUp.isEnabled = it < 2.0
            binding.roleDown.isEnabled = it > 0.0
        }




        return binding.root
    }

    private fun addUser(name : String, pwd : String, pm : Double){
        ServerConnector.instance.addUser( name, pwd, pm,object :Callback{
            override fun onFailure(call: Call, e: IOException) {
                handler.sendEmptyMessage(WRONG)
            }

            override fun onResponse(call: Call, response: Response) {
                if (response.code == 200){
                    handler.sendEmptyMessage(SUCCESS)
                }else{
                    handler.sendEmptyMessage(WRONG)
                }
            }

        }
        )
    }

}