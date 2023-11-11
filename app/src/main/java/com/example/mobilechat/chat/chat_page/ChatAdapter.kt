package com.example.mobilechat.chat.chat_page

import androidx.recyclerview.widget.RecyclerView
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.content.res.AppCompatResources
import com.example.mobilechat.MyApplication
import com.example.mobilechat.R

import com.example.mobilechat.databinding.FragmentChatItemBinding
import io.noties.markwon.Markwon

class ChatAdapter(
    var contents: List<ChatBean>
) : RecyclerView.Adapter<ChatAdapter.ViewHolder>() {
    companion object{
        //markwon
        val markwon = Markwon.builder(MyApplication.instance.context).build()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {

        return ViewHolder(
            com.example.mobilechat.databinding.FragmentChatItemBinding.inflate(
                LayoutInflater.from(parent.context),
                parent,
                false
            )
        )

    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = contents[position]
        if (item.role==3.0){
            holder.itemView.background = AppCompatResources.getDrawable(MyApplication.instance,R.drawable.bg_bot_chat)
        }else{
            holder.itemView.background = AppCompatResources.getDrawable(MyApplication.instance,R.drawable.bg_human_chat)
        }
        markwon.setMarkdown(holder.contentView, item.sentence)
    }

    override fun getItemCount(): Int = contents.size

    inner class ViewHolder(binding: FragmentChatItemBinding) :
        RecyclerView.ViewHolder(binding.root) {
        val iv: ImageView = binding.iv
        val contentView: TextView = binding.tvContent
    }
}