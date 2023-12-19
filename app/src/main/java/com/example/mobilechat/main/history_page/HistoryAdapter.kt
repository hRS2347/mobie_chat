package com.example.mobilechat.main.history_page

import androidx.recyclerview.widget.RecyclerView
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.TextView

import com.example.mobilechat.databinding.FragmentHistoryItemBinding

class HistoryAdapter(
    var values: List<HistoryBean>
) : RecyclerView.Adapter<HistoryAdapter.ViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {

        return ViewHolder(
            FragmentHistoryItemBinding.inflate(
                LayoutInflater.from(parent.context),
                parent,
                false
            )
        )

    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = values[position]
        holder.idView.text = item.time
        holder.contentView.text = item.topic
    }

    override fun getItemCount(): Int = values.size

    inner class ViewHolder(binding: FragmentHistoryItemBinding) :
        RecyclerView.ViewHolder(binding.root) {
        val idView: TextView = binding.itemNumber
        val contentView: TextView = binding.content
        init {
            binding.root.setOnClickListener {
                listener?.onItemClick(bindingAdapterPosition)
            }
            binding.btnDelete.setOnLongClickListener{
                listener?.onDeleteLongClick(bindingAdapterPosition)
                true
            }
            binding.btnDelete.setOnClickListener {
                listener?.onDeleteClick()
            }
        }
        override fun toString(): String {
            return super.toString() + " '" + contentView.text + "'"
        }
    }

    interface OnItemClickListener {
        fun onItemClick(position: Int)
        fun onDeleteClick()
        fun onDeleteLongClick(position:Int)
    }

    private var listener: OnItemClickListener? = null

    fun setOnItemClickListener(listener: OnItemClickListener) {
        this.listener = listener
    }

}