package com.example.mobilechat.manager.main_page

import android.text.Editable
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.mobilechat.databinding.FragmentUserItemBinding
import com.example.mobilechat.manager.UserBean

class ManagerAdapter(
    var users: List<UserBean>,
    private val listener: OnItemClickListener
) : RecyclerView.Adapter<ManagerAdapter.ViewHolder>(
) {
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
        return users.size
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val user = users[position]
        holder.name.text = Editable.Factory.getInstance().newEditable(user.username)
        holder.pwd.text = Editable.Factory.getInstance().newEditable(user.password)
        holder.role.text = Editable.Factory.getInstance().newEditable(user.permission.toString())
        holder.delete.setOnClickListener {
            listener.onDeleteClick(position)
        }
        holder.update.setOnClickListener {
            listener.onUpdateClick(position, holder.name.text.toString(), holder.pwd.text.toString(), holder.role.text.toString().toDouble())
        }
    }

    inner class ViewHolder(binding: FragmentUserItemBinding): RecyclerView.ViewHolder(binding.root) {
        val name = binding.tvName
        val pwd = binding.tvPwd
        val role = binding.tvRole
        val delete = binding.btnDelete
        val update = binding.btnUpdate
    }

    interface OnItemClickListener {
        fun onDeleteClick(position: Int)
        fun onUpdateClick(position: Int, name: String, pwd: String, role: Double)
    }
}