package com.example.mobilechat.chat.method_page

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.navigation.Navigation
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.mobilechat.MyApplication
import com.example.mobilechat.R
import com.example.mobilechat.databinding.FragmentMethodBinding

class MethodsFragment : Fragment() {

    private var columnCount = 2

    private lateinit var bind: FragmentMethodBinding
    private var rv: RecyclerView? = null
    private lateinit var manager: RecyclerView.LayoutManager
    private lateinit var adapter: MethodsAdapter
    private lateinit var list: List<MethodsBean>
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        loadData()
    }

    private fun loadData() {
        //TODO:download data from server
        list = listOf(
            MethodsBean("#1", "Normal"),
            MethodsBean("#2", "Normal"),
            MethodsBean("#3", "Normal"),
            MethodsBean("#4", "Normal"),
            MethodsBean("#5", "Normal"),
            MethodsBean("#6", "Normal"),
        )
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        bind = FragmentMethodBinding.inflate(layoutInflater)
        rv = bind.methodList

        manager = when {
            columnCount <= 1 -> LinearLayoutManager(context)
            else -> GridLayoutManager(context, columnCount)
        }
        adapter = MethodsAdapter(list)
        adapter.setOnItemClickListener(object : MethodsAdapter.OnItemClickListener {
            override fun onItemClick(position: Int) {
                Navigation.findNavController(bind.root)
                    .navigate(R.id.action_methodsFragment_to_chatFragment)
            }
        })
        rv?.layoutManager = manager
        rv?.adapter = adapter

        return bind.root
    }

}