package com.example.mobilestorage.fragments.home

import android.annotation.SuppressLint
import android.app.AlertDialog
import android.os.Bundle
import android.util.Log
import android.view.*
import android.widget.*
import androidx.fragment.app.Fragment
import androidx.navigation.findNavController
import androidx.recyclerview.widget.RecyclerView
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import com.example.mobilestorage.R
import com.example.mobilestorage.adapter.ProductsAdapter
import com.example.mobilestorage.model.Product
import com.google.firebase.Timestamp
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import java.text.SimpleDateFormat
import java.util.*

class HomeFragment : Fragment() {

    private lateinit var root: View

    private val TAG = "MyLogger"

    private var productsList: MutableList<Product> = mutableListOf()
    private val db = Firebase.firestore
    private var filterDate: Date? = null
    private var filterProviderName: String = ""
    private var filterProviderNameMap: MutableMap<String, String> = mutableMapOf()

    private lateinit var productsAdapter: ProductsAdapter

    @SuppressLint("ResourceType")
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        root = inflater.inflate(R.layout.fragment_home, container, false)
        setHasOptionsMenu(true)
        val recyclerView = root.findViewById<RecyclerView>(R.id.recycler_view)

        root.findViewById<SwipeRefreshLayout>(R.id.swipeToRefresh).setOnRefreshListener {
            fragmentManager?.beginTransaction()?.detach(this)?.attach(this)?.commit()
            root.findViewById<SwipeRefreshLayout>(R.id.swipeToRefresh).isRefreshing = false
        }

        productsList.clear()
        productsAdapter = ProductsAdapter(root.context, productsList)
        recyclerView.adapter = productsAdapter
        productsAdapter.notifyDataSetChanged()

        getProducts()

        return root
    }

    private fun getProducts() {
        db.collection("products").get()
            .addOnSuccessListener { result ->
                for (document in result) {
                    try {
                        productsList.add(
                            Product(
                                document.id,
                                document.get("name") as String,
                                document.get("provider") as String,
                                document.get("price") as Long,
                                document.get("amount") as Long,
                                document.get("unit") as String,
                                document.get("total") as Long,
                                document.get("arrival_date") as Timestamp
                            )
                        )
                    } catch (e: Exception) {}
                }
                productsList.sortByDescending { it.arrivalDate }
                productsAdapter.notifyDataSetChanged()
            }
            .addOnFailureListener { exception ->
                Log.w(TAG, "Error getting documents: ", exception)
            }
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        inflater.inflate(R.menu.app_bar, menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.action_sort -> {
                showMessageSortBox()
                true
            }
            R.id.action_account -> {
                root.findNavController()?.navigate(R.id.action_navigation_home_to_navigation_user)
                true
            }
            R.id.action_filter-> {
                showMessageFilterBox()
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    private fun showMessageSortBox(){
        val messageBoxView = LayoutInflater.from(activity).inflate(R.layout.box_sort, null)
        val messageBoxBuilder = AlertDialog.Builder(activity).setView(messageBoxView)
        val  messageBoxInstance = messageBoxBuilder.show()

        messageBoxView?.findViewById<Button>(R.id.ok_sort)?.setOnClickListener(){
            when (messageBoxView.findViewById<RadioGroup>(R.id.radio_group_sort).checkedRadioButtonId) {
                R.id.dateOldToNew -> {
                    productsList.sortBy { it.arrivalDate }
                    productsAdapter.notifyDataSetChanged()
                }
                R.id.dateNewToOld -> {
                    productsList.sortByDescending { it.arrivalDate }
                    productsAdapter.notifyDataSetChanged()
                }
                R.id.priceLowtoHigh -> {
                    productsList.sortBy { it.price }
                    productsAdapter.notifyDataSetChanged()
                }
                R.id.priceHighToLow -> {
                    productsList.sortByDescending { it.price }
                    productsAdapter.notifyDataSetChanged()
                }
            }
            messageBoxInstance.dismiss()
        }
    }

    private fun showMessageFilterBox(){
        val messageBoxView = LayoutInflater.from(activity).inflate(R.layout.box_filter, null)
        val messageBoxBuilder = AlertDialog.Builder(activity).setView(messageBoxView)
        val  messageBoxInstance = messageBoxBuilder.show()

        messageBoxView?.findViewById<Button>(R.id.filerDateButton)?.setOnClickListener {
            showMessageDateBox(messageBoxView)
        }

        messageBoxView?.findViewById<Button>(R.id.filerProviderButton)?.setOnClickListener {
            showMessageProvidersBox(messageBoxView)
        }

        messageBoxView?.findViewById<Button>(R.id.ok_filter)?.setOnClickListener(){
            val priceB = messageBoxView.findViewById<EditText>(R.id.editTextFilterPriceBigger).text.toString().toInt()
            val priceL = messageBoxView.findViewById<EditText>(R.id.editTextFilterPriceLess).text.toString().toInt()

            if (priceB!=0){
                val filteredList = productsList.filter { it.price > priceB} as MutableList
                productsList.clear()
                productsList.addAll(filteredList)
                productsAdapter.notifyDataSetChanged()
            }
            if (priceL!=0){
                val filteredList = productsList.filter { it.price < priceL} as MutableList
                productsList.clear()
                productsList.addAll(filteredList)
                productsAdapter.notifyDataSetChanged()
            }
            if (filterProviderName!=""){
                val filteredList = productsList.filter { it.provider == filterProviderNameMap[filterProviderName] } as MutableList
                productsList.clear()
                productsList.addAll(filteredList)
                productsAdapter.notifyDataSetChanged()
            }
            if (filterDate!=null){
                val filteredList = productsList.filter { it.arrivalDate.toDate().year == filterDate!!.year}
                    .filter { it.arrivalDate.toDate().month == filterDate!!.month }
                    .filter { it.arrivalDate.toDate().day == filterDate!!.day } as MutableList
                productsList.clear()
                productsList.addAll(filteredList)
                productsAdapter.notifyDataSetChanged()
            }
            messageBoxInstance.dismiss()
        }
    }

    @SuppressLint("SimpleDateFormat", "SetTextI18n", "ResourceType")
    private fun showMessageProvidersBox(view: View){
        val messageBoxView = LayoutInflater.from(activity).inflate(R.layout.box_providers, null)
        val messageBoxBuilder = AlertDialog.Builder(activity).setView(messageBoxView)
        val  messageBoxInstance = messageBoxBuilder.show()

        val listProviders: MutableList<String> = mutableListOf()
        val listView = messageBoxView.findViewById<ListView>(R.id.providersFilterList)

        db.collection("providers").get()
            .addOnSuccessListener { result ->
                for (document in result) {
                    val name = document.get("name") as String
                    listProviders.add(name)
                    filterProviderNameMap[name] = document.id
                }
                listView.adapter = context?.let { ArrayAdapter<String>(it, android.R.layout.simple_list_item_1, listProviders) }
            }
            .addOnFailureListener { exception ->
                Log.w(TAG, "Error getting documents: ", exception)
            }

        listView.onItemClickListener = AdapterView.OnItemClickListener { adapterView, _view, position, id ->
            filterProviderName = adapterView.getItemAtPosition(position) as String
            view.findViewById<TextView>(R.id.providerFilterTextView)?.text = "Поставщик:\n$filterProviderName"
            messageBoxInstance.dismiss()
        }

    }


    @SuppressLint("SimpleDateFormat", "SetTextI18n")
    private fun showMessageDateBox(view: View){
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
            filterDate = Date(calendarView.date)
            val df = SimpleDateFormat("dd/MM/yyyy")
            view.findViewById<TextView>(R.id.dateFilterTextView)?.text = "Дата прибытия:\n${df.format(filterDate)}"
            messageBoxInstance.dismiss()
        }
    }
}