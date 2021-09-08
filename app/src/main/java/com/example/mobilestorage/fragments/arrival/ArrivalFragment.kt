package com.example.mobilestorage.fragments.arrival

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import androidx.fragment.app.Fragment
import androidx.navigation.findNavController
import androidx.recyclerview.widget.RecyclerView
import com.example.mobilestorage.R
import com.example.mobilestorage.adapter.ArrivalsAdapter
import com.example.mobilestorage.model.Arrival
import com.google.firebase.Timestamp
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase

class ArrivalFragment: Fragment() {

    private lateinit var root: View

    private val TAG = "MyLogger"

    private var arrivalsList: MutableList<Arrival> = mutableListOf()
    private val db = Firebase.firestore
    private lateinit var arrivalsAdapter: ArrivalsAdapter

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        root = inflater.inflate(R.layout.fragment_arrival, container, false)
        setHasOptionsMenu(true)
        val recyclerView = root.findViewById<RecyclerView>(R.id.recycler_view)

        root.findViewById<Button>(R.id.addArrival).setOnClickListener {
            root.findNavController().navigate(R.id.action_navigation_arrival_to_add_arrival)
        }

        arrivalsList.clear()
        arrivalsAdapter = ArrivalsAdapter(root.context, arrivalsList)
        recyclerView.adapter = arrivalsAdapter
        arrivalsAdapter.notifyDataSetChanged()

        getArrivals()

        return root
    }

    private fun getArrivals() {
        db.collection("arrivals").get()
            .addOnSuccessListener { result ->
                for (document in result) {
                    try {
                        arrivalsList.add(
                            Arrival(
                                document.id,
                                document.get("arrivalDate") as Timestamp,
                                document.get("providerID") as String,
                                document.get("providerName") as String,
                                document.get("listOfProductsID") as MutableList<String>
                            )
                        )
                        arrivalsList.sortByDescending { it.arrivalDate }
                        arrivalsAdapter.notifyDataSetChanged()
                    } catch (e: Exception) {}
                }
            }
            .addOnFailureListener { exception ->
                Log.w(TAG, "Error getting documents: ", exception)
            }
    }
}