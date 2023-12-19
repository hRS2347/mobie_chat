package com.example.mobilechat.main.method_page

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
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
        list = listOf(
            MethodsBean(
                R.drawable.mental_health,
                getString(R.string.title_mental),
                getString(R.string.content_mental),
                getString(R.string.prologue_mental)
            ),
            MethodsBean(
                R.drawable.school,
                getString(R.string.title_academic),
                getString(R.string.content_academic),
                getString(R.string.prologue_academic)
            ),
            MethodsBean(
                R.drawable.dog,
                getString(R.string.title_dog),
                getString(R.string.content_dog),
                getString(R.string.prologue_dog)
            ),
            MethodsBean(
                R.drawable.chef,
                getString(R.string.title_chef),
                getString(R.string.content_chef),
                getString(R.string.prologue_chef)
            ),
            MethodsBean(
                R.drawable.marketing,
                getString(R.string.title_marketing),
                getString(R.string.content_marketing),
                getString(R.string.prologue_marketing)
            ),
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
                MyApplication.instance.prologue = list[position].prologue
                Navigation.findNavController(bind.root)
                    .navigate(R.id.action_methodsFragment_to_chatFragment)
            }
        })
        rv?.layoutManager = manager
        rv?.adapter = adapter

        return bind.root
    }

}