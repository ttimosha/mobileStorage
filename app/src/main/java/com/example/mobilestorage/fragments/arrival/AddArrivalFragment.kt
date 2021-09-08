package com.example.mobilestorage.fragments.arrival

import android.annotation.SuppressLint
import android.app.AlertDialog
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.core.os.bundleOf
import androidx.fragment.app.Fragment
import androidx.navigation.findNavController
import com.example.mobilestorage.R
import com.example.mobilestorage.model.Product
import com.google.firebase.Timestamp
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import java.text.SimpleDateFormat
import java.util.*

class AddArrivalFragment: Fragment() {

    private lateinit var root: View

    private val db = Firebase.firestore
    private var timestampDate: Timestamp = Timestamp.now()
    private var calendarDate: Date? = timestampDate.toDate()
    private var idProvider: String = ""
    private var nameProvider: String = ""
    private var productsList: MutableList<Product> = mutableListOf()
    private val namesProductsList: MutableMap<String, String> = mutableMapOf()

    @SuppressLint("SimpleDateFormat")
    private val df = SimpleDateFormat("dd/MM/yyyy")

    private val TAG = "MyLogger"

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        root = inflater.inflate(R.layout.fragment_add_arrival, container, false)
        setHasOptionsMenu(true)

        if (arguments?.getString("id_provider") != null) {
            idProvider = arguments?.getString("id_provider")!!
            nameProvider = arguments?.getString("name_provider")!!
            root.findViewById<TextView>(R.id.providerAddText).text = nameProvider
            getProducts()
        }

        root.findViewById<Button>(R.id.select_date)?.setOnClickListener {
            showMessageDateBox()
        }

        root.findViewById<TextView>(R.id.arrivalDateAddText).text = df.format(timestampDate.toDate())

        root.findViewById<Button>(R.id.select_provider)?.setOnClickListener {
            val bundle = bundleOf(
                "from_where" to "addArrival")
            root.findNavController().navigate(R.id.action_add_arrival_to_provider, bundle)
        }

        root.findViewById<Button>(R.id.buttonAdd)?.setOnClickListener {
            onClickAdd()
        }

        return root
    }

    @SuppressLint("SimpleDateFormat")
    private fun showMessageDateBox(){
        val messageBoxView = LayoutInflater.from(activity).inflate(R.layout.box_date, null)
        val messageBoxBuilder = AlertDialog.Builder(activity).setView(messageBoxView)
        val  messageBoxInstance = messageBoxBuilder.show()

        val calendar = Calendar.getInstance()

        var calendarView = messageBoxView.findViewById<CalendarView>(R.id.calendarView)
        calendarView.setOnDateChangeListener { view, year, month, dayOfMonth ->
            calendar.set(year, month, dayOfMonth)
            calendarView.date = calendar.timeInMillis
        }

        messageBoxView?.findViewById<Button>(R.id.ok_date)?.setOnClickListener(){
            calendarDate = Date(calendarView.date)
            timestampDate = Timestamp(calendarDate!!)
            root.findViewById<TextView>(R.id.arrivalDateAddText)?.text = df.format(timestampDate.toDate())
            getProducts()
            messageBoxInstance.dismiss()
        }
    }

    private fun getProducts() {
        if (idProvider == "") {
            return
        }
        if (calendarDate == null) {
            return
        }

        db.collection("products")
            .whereEqualTo("provider", idProvider).get()
            .addOnSuccessListener { result ->
                for (document in result) {
                    productsList.add(Product(document.id,
                        document.get("name") as String,
                        document.get("provider") as String,
                        document.get("price") as Long,
                        document.get("amount") as Long,
                        document.get("unit") as String,
                        document.get("total") as Long,
                        document.get("arrival_date") as Timestamp))
                }

                productsList = productsList.filter { it.arrivalDate.toDate().year == calendarDate!!.year}
                    .filter { it.arrivalDate.toDate().month == calendarDate!!.month }
                    .filter { it.arrivalDate.toDate().day == calendarDate!!.day } as MutableList


                productsList.forEach { namesProductsList[it.name] = it.id }
                val listView = root.findViewById<ListView>(R.id.list_view)
                listView.adapter = context?.let { ArrayAdapter<String>(it, android.R.layout.simple_list_item_1, namesProductsList.keys.toList()) }

                listView.onItemClickListener = AdapterView.OnItemClickListener { adapterView, _view, position, id ->
                    val builder = AlertDialog.Builder(root?.context)
                    builder.setMessage("Удалить из прихода?")
                        .setCancelable(false)
                        .setPositiveButton("Удалить") { dialog, _id ->
                            val key = adapterView.getItemAtPosition(position) as String
                            namesProductsList.remove(key)
                            listView.adapter = context?.let { ArrayAdapter<String>(it, android.R.layout.simple_list_item_1, namesProductsList.keys.toList()) }
                        }
                        .setNegativeButton("Отмена") { dialog, id ->

                        }
                    val alert = builder.create()
                    alert.show()
                }
            }
            .addOnFailureListener { exception ->
                Log.w(TAG, "Error getting documents: ", exception)
            }
    }

    private fun onClickAdd() {
        val providerText = root?.findViewById<TextView>(R.id.providerAddText)
        if (idProvider == "") {
            providerText?.text = "Пожалуйста, выберите поставщика"
            providerText?.requestFocus()
            return
        }
        if (productsList.size == 0) {
            Toast.makeText(
                activity, "Список товаров пустой",
                Toast.LENGTH_SHORT
            ).show()
            return
        }

        val data = hashMapOf(
            "providerID" to idProvider,
            "providerName" to nameProvider,
            "arrivalDate" to timestampDate,
            "listOfProductsID" to namesProductsList.values.toList()
        )

        db.collection("arrivals").add(data)
            .addOnSuccessListener { documentReference ->

            val builder = AlertDialog.Builder(view?.context)
            builder.setMessage("Успешно сохранено")
                .setCancelable(false)
                .setPositiveButton("ОК") { dialog, id ->
                }
            val alert = builder.create()
            alert.show()
            root?.findNavController().navigate(R.id.action_add_arrival_to_navigation_arrival)
        }
            .addOnFailureListener { e ->
                Log.w(TAG, "Error adding document", e)
                Toast.makeText(activity, "Ошибка. Попробуйте ещё раз",
                    Toast.LENGTH_SHORT).show()
            }
    }

}