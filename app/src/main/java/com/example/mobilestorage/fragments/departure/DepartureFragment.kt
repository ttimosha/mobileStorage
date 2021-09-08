package com.example.mobilestorage.fragments.departure

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
import com.example.mobilestorage.adapter.DepartureAdapter
import com.example.mobilestorage.model.Departure
import com.google.firebase.Timestamp
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase

class DepartureFragment: Fragment() {

    private lateinit var root: View

    private val TAG = "MyLogger"

    private var departuresList: MutableList<Departure> = mutableListOf()
    private val db = Firebase.firestore
    private lateinit var departureAdapter: DepartureAdapter

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        root = inflater.inflate(R.layout.fragment_departure, container, false)
        setHasOptionsMenu(true)
        val recyclerView = root.findViewById<RecyclerView>(R.id.recycler_view)

        root.findViewById<Button>(R.id.addDeparture).setOnClickListener {
            root.findNavController().navigate(R.id.action_navigation_departure_to_add_departure)
        }

        departuresList.clear()
        departureAdapter = DepartureAdapter(root.context, departuresList)
        recyclerView.adapter = departureAdapter
        departureAdapter.notifyDataSetChanged()

        getArrivals()

        return root
    }

    private fun getArrivals() {
        db.collection("departures").get()
            .addOnSuccessListener { result ->
                for (document in result) {
                    try {
                        departuresList.add(
                            Departure(
                                document.id,
                                document.get("departureDate") as Timestamp,
                                document.get("addressOfDeparture") as String,
                                document.get("mapOfProductsCount") as MutableMap<String, Long>
                            )
                        )
                        departuresList.sortByDescending { it.departureDate }
                        departureAdapter.notifyDataSetChanged()
                    } catch (e: Exception) {}
                }
            }
            .addOnFailureListener { exception ->
                Log.w(TAG, "Error getting documents: ", exception)
            }
    }}