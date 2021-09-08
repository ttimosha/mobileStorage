package com.example.mobilestorage.adapter

import android.annotation.SuppressLint
import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.core.os.bundleOf
import androidx.navigation.findNavController
import androidx.recyclerview.widget.RecyclerView
import com.example.mobilestorage.R
import com.example.mobilestorage.model.Arrival
import java.text.SimpleDateFormat

class ArrivalsAdapter(private val context: Context,
                      private val list: MutableList<Arrival>
                      ): RecyclerView.Adapter<ArrivalsAdapter.ViewHolder>() {

    private var space: String = "   "

    class ViewHolder(view: View): RecyclerView.ViewHolder(view) {
        val providerText: TextView = view.findViewById(R.id.textViewName)
        val arrDateText: TextView = view.findViewById(R.id.textViewSecond)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.cell, parent, false)
        return ViewHolder(view)
    }

    override fun getItemCount(): Int {
        return list.count()
    }

    @SuppressLint("SetTextI18n", "SimpleDateFormat")
    override fun onBindViewHolder(holder: ArrivalsAdapter.ViewHolder, position: Int) {
        val arrival = list[position]
        val df = SimpleDateFormat("dd/MM/yyyy")
        val date = df.format(arrival.arrivalDate.toDate())
        holder.providerText.text = "${space}Поставщик: ${arrival.providerName}"
        holder.arrDateText.text = "${space}Дата прибытия: $date"

        holder.itemView.setOnClickListener {
            var bundle = bundleOf("id" to arrival.id)
            it.findNavController().navigate(R.id.action_navigation_arrival_to_arrival_info, bundle)
        }
    }
}