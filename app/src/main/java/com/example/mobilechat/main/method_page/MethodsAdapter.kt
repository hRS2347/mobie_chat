package com.example.mobilechat.main.method_page

import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.mobilechat.databinding.FragmentMethodItemBinding

class MethodsAdapter(
    private val values: List<MethodsBean>
) : RecyclerView.Adapter<MethodsAdapter.ViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {

        return ViewHolder(
            FragmentMethodItemBinding.inflate(
                LayoutInflater.from(parent.context),
                parent,
                false
            )
        )

    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = values[position]
        holder.image.setImageResource(item.image)
        holder.title.text = item.title
        holder.content.text = item.content
        holder.itemView.setOnClickListener {
            listener?.onItemClick(position)
        }
    }

    override fun getItemCount(): Int = values.size

    inner class ViewHolder(binding: FragmentMethodItemBinding) :
        RecyclerView.ViewHolder(binding.root) {
        val image = binding.image
        val title: TextView = binding.title
        val content: TextView = binding.content
        override fun toString(): String {
            return super.toString() + " '";
        }
    }

    interface OnItemClickListener {
        fun onItemClick(position: Int)
    }

    private var listener: OnItemClickListener? = null

    fun setOnItemClickListener(listener: OnItemClickListener) {
        this.listener = listener
    }

}