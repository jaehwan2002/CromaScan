package com.example.cromascan

import android.graphics.Color
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class HistoryFragment : Fragment() {

    private lateinit var recyclerView: RecyclerView
    private lateinit var adapter: HistoryAdapter
    private val items = mutableListOf<ColorHistoryItem>()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_history, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        recyclerView = view.findViewById(R.id.recycler_history)
        recyclerView.layoutManager = LinearLayoutManager(requireContext())
        adapter = HistoryAdapter(items) { item ->
            navigateToMatch(item)
        }
        recyclerView.adapter = adapter
    }

    override fun onResume() {
        super.onResume()
        items.clear()
        items.addAll(ColorHistoryStorage.loadHistory(requireContext()))
        adapter.notifyDataSetChanged()
    }

    private fun navigateToMatch(item: ColorHistoryItem) {
        val matchArray = intArrayOf(
            item.matchColor1,
            item.matchColor2,
            item.matchColor3
        )

        val bundle = Bundle().apply {
            putInt("baseColor", Color.parseColor(item.baseHex))
            putString("baseHex", item.baseHex)
            putIntArray("matchColors", matchArray)
        }

        findNavController().navigate(
            R.id.action_historyFragment_to_matchFragment,
            bundle
        )
    }
}

class HistoryAdapter(
    private val items: List<ColorHistoryItem>,
    private val onClick: (ColorHistoryItem) -> Unit
) : RecyclerView.Adapter<HistoryAdapter.HistoryViewHolder>() {

    class HistoryViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val textBaseHex: TextView = view.findViewById(R.id.text_base_hex)
        val viewColor1: View = view.findViewById(R.id.view_color_1)
        val viewColor2: View = view.findViewById(R.id.view_color_2)
        val viewColor3: View = view.findViewById(R.id.view_color_3)
        val textDate: TextView = view.findViewById(R.id.text_date)
    }

    private val dateFormat = SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault())

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): HistoryViewHolder {
        val v = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_history, parent, false)
        return HistoryViewHolder(v)
    }

    override fun getItemCount(): Int = items.size

    override fun onBindViewHolder(holder: HistoryViewHolder, position: Int) {
        val item = items[position]

        holder.textBaseHex.text = item.baseHex
        holder.viewColor1.setBackgroundColor(item.matchColor1)
        holder.viewColor2.setBackgroundColor(item.matchColor2)
        holder.viewColor3.setBackgroundColor(item.matchColor3)
        holder.textDate.text = dateFormat.format(Date(item.timestamp))

        holder.itemView.setOnClickListener { onClick(item) }
    }
}
