package com.example.mobilechat.manager.main_page

import android.content.Context
import android.content.res.ColorStateList
import android.graphics.Color
import android.text.Editable
import android.util.Log
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.content.res.ResourcesCompat
import androidx.recyclerview.widget.RecyclerView
import com.example.mobilechat.R
import com.example.mobilechat.databinding.FragmentUserItemBinding
import com.example.mobilechat.manager.UserBean

class ManagerAdapter(
    var users: List<UserBean>,
    val context: Context,
) : RecyclerView.Adapter<ManagerAdapter.ViewHolder>(
) {
    private lateinit var listener: OnItemClickListener
    fun setOnItemClickListener(listener: OnItemClickListener) {
        this.listener = listener
    }
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return ViewHolder(
            FragmentUserItemBinding.inflate(
                LayoutInflater.from(parent.context),
                parent,
                false
            )
        )

    }

    override fun getItemCount(): Int {
        return users.size+1
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        if (position == users.size) {
            holder.root.visibility = ViewGroup.INVISIBLE
            //set high 100dp
            val params = holder.root.layoutParams
            params.height = 200
            return
        }else{
            holder.root.layoutParams.height = ViewGroup.LayoutParams.WRAP_CONTENT
            holder.root.visibility = ViewGroup.VISIBLE
        }

        val user = users[position]
        holder.name.text = Editable.Factory.getInstance().newEditable(user.username)
        holder.pwd.text = Editable.Factory.getInstance().newEditable(user.password)
        holder.role.text = when (user.permission) {
            0.0 -> "封禁用户"
            1.0 -> "普通用户"
            2.0 -> "管理员"
            else -> "未知"
        }
        holder.iv.setImageResource(
            when (user.permission) {
                0.0 -> R.drawable.danger
                1.0 -> R.drawable.user
                2.0 -> R.drawable.police
                else -> R.drawable.zeus
            }
        )
        holder.update.setOnClickListener {
            listener.onUpdateClick(
                position,
                holder.name.text.toString(),
                holder.pwd.text.toString(),
                when (holder.role.text.toString()) {
                    "封禁用户" -> 0.0
                    "普通用户" -> 1.0
                    "管理员" -> 2.0
                    else -> 0.0
                }
            )
        }
        when (user.permission) {
            0.0 -> {
                holder.up.isEnabled = true
                //turn gray
                holder.down.backgroundTintList = ColorStateList.valueOf(Color.GRAY)

                holder.down.isEnabled = false
                holder.up.backgroundTintList =
                    ResourcesCompat.getColorStateList(
                        context.resources,
                        R.color.sl_color, null
                    )
            }

            1.0 -> {
                holder.up.isEnabled = true
                holder.down.isEnabled = true
                holder.down.backgroundTintList =
                    ResourcesCompat.getColorStateList(
                        context.resources,
                        R.color.sl_color, null
                    )
                holder.up.backgroundTintList =
                    ResourcesCompat.getColorStateList(
                        context.resources,
                        R.color.sl_color, null
                    )
            }

            2.0 -> {
                holder.up.isEnabled = false
                holder.down.isEnabled = true
                holder.down.backgroundTintList =
                    ResourcesCompat.getColorStateList(
                        context.resources,
                        R.color.sl_color, null
                    )
                holder.up.backgroundTintList = ColorStateList.valueOf(Color.GRAY)
            }
        }
        holder.delete.setOnLongClickListener {
            listener.onDeleteLongClick(position)
            true
        }
        holder.delete.setOnClickListener {
            listener.onDeleteClick()
        }
        holder.up.setOnClickListener {
            listener.onUpClick(position)
            Log.d("up", "up")
        }
        holder.down.setOnClickListener {
            listener.onDownClick(position)
        }

    }

    inner class ViewHolder(binding: FragmentUserItemBinding) :
        RecyclerView.ViewHolder(binding.root) {
        val name = binding.tvName
        val pwd = binding.tvPwd
        val role = binding.tvRole
        val delete = binding.btnDelete
        val update = binding.btnUpdate
        val iv = binding.ivRole
        val up = binding.roleUp
        val down = binding.roleDown
        val root = binding.root
    }

    interface OnItemClickListener {
        fun onDeleteLongClick(position: Int)
        fun onDeleteClick()
        fun onUpdateClick(position: Int, name: String, pwd: String, role: Double)
        fun onUpClick(position: Int)
        fun onDownClick(position: Int)
    }
}