package com.fireguardedge.app

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

class LogsAdapter(private val items: MutableList<Pair<String,String>>) : RecyclerView.Adapter<LogsAdapter.VH>() {

    class VH(view: View) : RecyclerView.ViewHolder(view) {
        val title: TextView = view.findViewById(R.id.tvLogTitle)
        val time: TextView = view.findViewById(R.id.tvLogTime)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VH {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_log, parent, false)
        return VH(view)
    }

    override fun getItemCount(): Int = items.size

    override fun onBindViewHolder(holder: VH, position: Int) {
        val (t, ts) = items[position]
        holder.title.text = t
        holder.time.text = ts
    }

    fun add(item: Pair<String,String>) {
        items.add(0, item) // newest first
        notifyItemInserted(0)
    }
}