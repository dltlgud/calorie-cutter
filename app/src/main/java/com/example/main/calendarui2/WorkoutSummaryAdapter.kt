package com.example.main.calendarui2

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.BaseAdapter
import android.widget.ImageButton
import android.widget.TextView
import com.example.main.R
import android.content.Context // ✅ 이걸로 바꿔줘


class WorkoutSummaryAdapter(
    private val context: Context,
    private var items: MutableList<String>,
    private val onDelete: (Int) -> Unit
) : BaseAdapter() {

    override fun getCount(): Int = items.size
    override fun getItem(position: Int): Any = items[position]
    override fun getItemId(position: Int): Long = position.toLong()

    override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
        val view = convertView ?: LayoutInflater.from(context)
            .inflate(R.layout.item_summary_row, parent, false)

        val tvItem = view.findViewById<TextView>(R
            .id.tvSummaryItem)
        val btnDelete = view.findViewById<ImageButton>(R.id.btnDelete)

        tvItem.text = items[position]

        btnDelete.setOnClickListener {
            onDelete(position)
        }

        return view
    }
}
