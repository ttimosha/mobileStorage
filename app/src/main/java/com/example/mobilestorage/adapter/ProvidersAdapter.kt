package com.example.mobilestorage.adapter

import android.annotation.SuppressLint
import android.content.Context
import android.os.Build
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.annotation.RequiresApi
import androidx.core.os.bundleOf
import androidx.navigation.findNavController
import androidx.recyclerview.widget.RecyclerView
import com.example.mobilestorage.R
import com.example.mobilestorage.model.Provider

class ProvidersAdapter(private val context: Context,
                      private val list: MutableList<Provider>,
                       private val from_where: String,
                       private val id: String = ""
) : RecyclerView.Adapter<ProvidersAdapter.ViewHolder>() {

    private var space: String = "   "

    class ViewHolder(view: View): RecyclerView.ViewHolder(view) {
        val nameText: TextView = view.findViewById(R.id.textViewName)
        val postcodeText: TextView = view.findViewById(R.id.textViewSecond)
        val INNText: TextView = view.findViewById(R.id.textViewThird)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.cell, parent, false)
        return ViewHolder(view)
    }

    override fun getItemCount(): Int {
        return list.count()
    }

    @RequiresApi(Build.VERSION_CODES.O)
    @SuppressLint("SetTextI18n", "SimpleDateFormat")
    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val provider = list[position]
        holder.nameText.text = "$space${provider.name}\n$space${provider.address}"
        holder.postcodeText.text = "$space${provider.postcode}"
        holder.INNText.text = "${space}ИНН ${provider.INN}"

        val bundle = bundleOf(
            "id_provider" to provider.id,
            "name_provider" to provider.name,
            "id" to id
        )
        holder.itemView.setOnClickListener {
            if (from_where == "addProduct") {
                it.findNavController().navigate(R.id.action_provider_to_navigation_add, bundle)
            } else if (from_where == "editProduct") {
                it.findNavController().navigate(R.id.action_provider_to_productInfo, bundle)
            } else if (from_where == "addArrival") {
                it.findNavController().navigate(R.id.action_provider_to_add_arrival, bundle)
            }
        }
    }
}