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
import com.example.mobilestorage.model.Departure
import java.text.SimpleDateFormat

class DepartureAdapter(private val context: Context,
                       private val list: MutableList<Departure>
): RecyclerView.Adapter<DepartureAdapter.ViewHolder>() {

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
    override fun onBindViewHolder(holder: DepartureAdapter.ViewHolder, position: Int) {
        val departure = list[position]
        val df = SimpleDateFormat("dd/MM/yyyy")
        val date = df.format(departure.departureDate.toDate())
        holder.providerText.text = "${space}Адрес: ${departure.addressOfDeparture}"
        holder.arrDateText.text = "${space}Дата прибытия: $date"

        holder.itemView.setOnClickListener {
            val bundle = bundleOf("id" to departure.id)
            it.findNavController().navigate(R.id.action_navigation_departure_to_departure_info, bundle)
        }
    }
}