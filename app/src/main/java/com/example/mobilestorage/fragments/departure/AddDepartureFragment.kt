package com.example.mobilestorage.fragments.departure

import android.annotation.SuppressLint
import android.app.AlertDialog
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.annotation.RequiresApi
import androidx.fragment.app.Fragment
import androidx.navigation.findNavController
import com.example.mobilestorage.R
import com.example.mobilestorage.model.Product
import com.google.firebase.Timestamp
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import java.text.SimpleDateFormat
import java.util.*

class AddDepartureFragment: Fragment() {

    private lateinit var root: View

    private val db = Firebase.firestore
    private var timestampDate: Timestamp = Timestamp.now()
    private var calendarDate: Date? = null
    private var productsList: MutableList<Product> = mutableListOf()
    private var selectedProduct: Product? = null
    private var mapOfProductsCount: MutableMap<String, Long> = mutableMapOf()
    private var listOfSelectedProducts: MutableList<Product> = mutableListOf()
    private var listOfSelectedProductsToShow: MutableList<String> = mutableListOf()

    @SuppressLint("SimpleDateFormat")
    private val df = SimpleDateFormat("dd/MM/yyyy")

    private val TAG = "MyLogger"

    @SuppressLint("CutPasteId")
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        root = inflater.inflate(R.layout.fragment_add_departure, container, false)
        setHasOptionsMenu(true)

        val listView = root.findViewById<ListView>(R.id.list_view)
        listView.scrollBarSize = 5

        root.findViewById<Button>(R.id.select_date)?.setOnClickListener {
            showMessageDateBox()
        }

        root.findViewById<Button>(R.id.select_product)?.setOnClickListener {
            showMessageConfirmProductBox()
        }

        root.findViewById<TextView>(R.id.arrivalDateAddText).text = df.format(timestampDate.toDate())


        root.findViewById<Button>(R.id.buttonAdd)?.setOnClickListener {
            onClickAdd()
        }

        listView.onItemClickListener = AdapterView.OnItemClickListener { adapterView, _view, position, id ->
            val builder = AlertDialog.Builder(root.context)
            builder.setMessage("Удалить из списка товаров?")
                .setCancelable(false)
                .setPositiveButton("Удалить") { dialog, _id ->
                    listOfSelectedProductsToShow.removeAt(position)
                    val removedProduct = listOfSelectedProducts[position]
                    listOfSelectedProducts.removeAt(position)
                    mapOfProductsCount.remove(removedProduct.id)
                    listView.adapter = context?.let { ArrayAdapter<String>(it, android.R.layout.simple_list_item_1, listOfSelectedProductsToShow) }
                }
                .setNegativeButton("Отмена") { dialog, id ->

                }
            val alert = builder.create()
            alert.show()
        }

        return root
    }

    private fun onClickAdd() {
        val address = root.findViewById<EditText>(R.id.editTextAddress)
        if (mapOfProductsCount.isEmpty()) {
            Toast.makeText(activity, "Список на добавление пустой",
                Toast.LENGTH_SHORT).show()
            return
        }
        if (address.text.toString() == "") {
            address.error = "Пожалуйста, введите адресс"
            return
        }

        val data = hashMapOf(
            "departureDate" to timestampDate,
            "addressOfDeparture" to address.text.toString(),
            "mapOfProductsCount" to mapOfProductsCount
        )

        db.collection("departures")
            .add(data)
            .addOnSuccessListener { documentReference ->
                updateProducts(documentReference.id)
            }
            .addOnFailureListener { e ->
                Log.w(TAG, "Error adding document", e)
                Toast.makeText(activity, "Ошибка. Попробуйте ещё раз",
                    Toast.LENGTH_SHORT).show()
            }
    }

    private fun updateProducts(documentReference: String) {
        val mapOfProductsCountSuccess = mutableMapOf<String, Long>()
        mapOfProductsCount.forEach { (key, value) ->
            var product: Product? = null
            listOfSelectedProducts.forEach { if (it.id == key) {
                product = it
                return@forEach
            } }
            val newAmount = product?.amount?.minus(value)
            db.collection("products").document(key)
                .update(
                    mapOf("amount" to newAmount)
                )
                .addOnSuccessListener {
                    mapOfProductsCount.remove(key)
                    mapOfProductsCountSuccess[key] = value
                    if (mapOfProductsCount.isEmpty()) {
                        val builder = AlertDialog.Builder(view?.context)
                        builder.setMessage("Успешно сохранено")
                            .setCancelable(false)
                            .setPositiveButton("ОК") { dialog, id ->
                            }
                        val alert = builder.create()
                        alert.show()
                        root.findNavController().navigate(R.id.action_add_departure_to_navigation_departure)
                    }
                }
                .addOnFailureListener { e ->
                        val builder = AlertDialog.Builder(view?.context)
                        builder.setMessage("Произошла ошибка с товаром №$key. Список товаров в реализации будет обновлен.")
                            .setCancelable(false)
                            .setPositiveButton("ОК") { dialog, id ->
                            }
                        val alert = builder.create()
                        alert.show()
                    db.collection("departures").document(documentReference)
                        .update(mapOf(
                            "mapOfProductsCount" to mapOfProductsCountSuccess
                        ))
                }
        }
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
            messageBoxInstance.dismiss()
        }
    }

    private fun showMessageConfirmProductBox(){
        val messageBoxView = LayoutInflater.from(activity).inflate(R.layout.box_add_product, null)
        val messageBoxBuilder = AlertDialog.Builder(activity).setView(messageBoxView)
        val  messageBoxInstance = messageBoxBuilder.show()

        val editTextAmount = messageBoxView.findViewById<EditText>(R.id.editTextAmount)

        messageBoxView?.findViewById<Button>(R.id.select_product)?.setOnClickListener(){
            showMessageSelectProductBox(messageBoxView)
        }

        messageBoxView?.findViewById<Button>(R.id.ok)?.setOnClickListener {
            val amount = editTextAmount.text.toString()
            if (selectedProduct == null) {
                messageBoxView.findViewById<Button>(R.id.select_product).error = "Пожалуйста, выберите товар"

            } else if (amount == "") {
                editTextAmount.error = "Пожалуйста, введите количество"

            } else if (amount.toLong() > selectedProduct!!.amount) {
                editTextAmount.error = "Вы ввели слишком большое число, максимум ${selectedProduct!!.amount}"

            } else if (selectedProduct!!.id in mapOfProductsCount.keys) {
                editTextAmount.error = "Вы уже добавили этот товар"

            } else {
                //дела
                mapOfProductsCount[selectedProduct!!.id] = amount.toLong()
                listOfSelectedProducts.add(selectedProduct!!)
                listOfSelectedProductsToShow.add("${selectedProduct!!.name}\nКол-во выбрано: $amount")
                root.findViewById<ListView>(R.id.list_view).adapter = context?.let { ArrayAdapter<String>(it, android.R.layout.simple_list_item_1, listOfSelectedProductsToShow) }
                selectedProduct = null
                messageBoxInstance.dismiss()
            }

        }
    }

    private fun showMessageSelectProductBox(view: View){
        val messageBoxView = LayoutInflater.from(activity).inflate(R.layout.box_products, null)
        val messageBoxBuilder = AlertDialog.Builder(activity).setView(messageBoxView)
        val  messageBoxInstance = messageBoxBuilder.show()

        var listProductsToShow = mutableListOf<String>()

        val listView = messageBoxView.findViewById<ListView>(R.id.products_list_view)
        getProducts(listProductsToShow, listView)

        listView.onItemClickListener = AdapterView.OnItemClickListener { adapterView, _view, position, id ->
            selectedProduct = productsList[position]
            view.findViewById<TextView>(R.id.message_box_name).text = adapterView.getItemAtPosition(position) as String
            messageBoxInstance.dismiss()
        }
    }

    private fun getProducts(listProductsToShow: MutableList<String>, listView: ListView ) {
        db.collection("products").get()
            .addOnSuccessListener { result ->
                for (document in result) {
                    try {
                        val name = document.get("name") as String
                        val amount = document.get("amount") as Long
                        productsList.add(
                            Product(
                                document.id,
                                name,
                                document.get("provider") as String,
                                document.get("price") as Long,
                                amount,
                                document.get("unit") as String,
                                document.get("total") as Long,
                                document.get("arrival_date") as Timestamp
                            )
                        )
                        listProductsToShow.add("$name\nКол-во: $amount")
                    } catch (e: Exception) {}
                }
                listView.adapter = context?.let { ArrayAdapter<String>(it, android.R.layout.simple_list_item_1, listProductsToShow) }
            }
            .addOnFailureListener { exception ->
                Log.w(TAG, "Error getting documents: ", exception)
            }
    }
}