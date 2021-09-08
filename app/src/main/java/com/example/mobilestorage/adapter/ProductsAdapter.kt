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
import com.example.mobilestorage.model.Product
import java.text.SimpleDateFormat

class ProductsAdapter(private val context: Context,
                      private val list: MutableList<Product>,
) : RecyclerView.Adapter<ProductsAdapter.ViewHolder>() {

    private var space: String = "   "

    class ViewHolder(view: View): RecyclerView.ViewHolder(view) {
        val nameText: TextView = view.findViewById(R.id.textViewName)
        val priceText: TextView = view.findViewById(R.id.textViewSecond)
        val arrDateText: TextView = view.findViewById(R.id.textViewThird)
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
        val product = list[position]
        //val df = SimpleDateFormat("dd/MM/yyyy HH:mm")
        val df = SimpleDateFormat("dd/MM/yyyy")
        var date = df.format(product.arrivalDate.toDate())
        holder.nameText.text = "$space${product.name}"
        holder.priceText.text = "${space}Цена за ед.:  ${product.price} руб., Кол-во: ${product.amount} ${product.unit}"
        holder.arrDateText.text = "${space}Дата прибытия: $date"

        holder.itemView.setOnClickListener {
            val bundle = bundleOf("id" to product.id)
            it.findNavController().navigate(R.id.action_navigation_home_to_productInfo, bundle)
        }
    }
}