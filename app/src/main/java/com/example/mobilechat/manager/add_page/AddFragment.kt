package com.example.mobilechat.manager.add_page

import android.os.Bundle
import android.os.Handler
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.navigation.Navigation
import com.example.mobilechat.databinding.FragmentAddBinding
import com.example.mobilechat.network.ServerConnector
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
        binding.btnConfirm.setOnClickListener {
            val name = binding.etName.text.toString()
            val pwd = binding.etPwd.text.toString()
            val pm = binding.etPm.text.toString().toDouble()
            if (name.isEmpty() || pwd.isEmpty() || (pm !in 0.0..2.0)) {
                Toast.makeText(context, "信息存在错误", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            addUser(name, pwd, pm)
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