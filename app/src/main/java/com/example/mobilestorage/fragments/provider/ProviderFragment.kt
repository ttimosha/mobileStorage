package com.example.mobilestorage.fragments.provider


import android.os.Bundle
import android.util.Log
import android.view.*
import android.widget.Button
import androidx.fragment.app.Fragment
import androidx.navigation.findNavController
import androidx.recyclerview.widget.RecyclerView
import com.example.mobilestorage.R
import com.example.mobilestorage.adapter.ProvidersAdapter
import com.example.mobilestorage.model.Provider
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase

class ProviderFragment : Fragment() {

    private val TAG = "MyLogger"

    private var providersList: MutableList<Provider> = mutableListOf()
    private val db = Firebase.firestore

    private lateinit var providersAdapter: ProvidersAdapter

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val root = inflater.inflate(R.layout.fragment_provider, container, false)
        setHasOptionsMenu(true)
        val recyclerView = root.findViewById<RecyclerView>(R.id.recycler_view)

        root?.findViewById<Button>(R.id.add_provider)?.setOnClickListener {
            root.findNavController().navigate(R.id.action_provider_to_add_provider)
        }

        val from_where = arguments?.getString("from_where")!!

        providersList.clear()
        providersAdapter = if (arguments?.getString("id") != null) ProvidersAdapter(root.context, providersList, from_where, arguments?.getString("id")!!)
        else ProvidersAdapter(root.context, providersList, from_where)
        recyclerView.adapter = providersAdapter
        providersAdapter.notifyDataSetChanged()

        db.collection("providers").get()
            .addOnSuccessListener { result ->
                for (document in result) {
                    try {
                        providersList.add(
                            Provider(
                                document.id,
                                document.get("name") as String,
                                document.get("INN") as String,
                                document.get("postcode") as String,
                                document.get("address") as String
                            )
                        )
                        Log.w(TAG, document.id)
                    } catch (e: Exception) {}
                }
                providersAdapter.notifyDataSetChanged()
            }
            .addOnFailureListener { exception ->
                Log.w(TAG, "Error getting documents: ", exception)
            }
        return root
    }

}