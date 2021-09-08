package com.example.mobilestorage.fragments.provider

import android.annotation.SuppressLint
import android.app.AlertDialog
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.fragment.app.Fragment
import androidx.navigation.findNavController
import com.example.mobilestorage.R
import com.google.firebase.Timestamp
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import java.text.SimpleDateFormat

class AddProviderFragment : Fragment() {

    private lateinit var root: View

    private val db = Firebase.firestore
    private var timestampDate: Timestamp = Timestamp.now()
    private var id_provider: String = ""

    @SuppressLint("SimpleDateFormat")
    private val df = SimpleDateFormat("dd/MM/yyyy")

    private val TAG = "MyLogger"

    @SuppressLint("SimpleDateFormat")
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        root = inflater.inflate(R.layout.fragment_add_provider, container, false)

        root?.findViewById<Button>(R.id.buttonAdd)?.setOnClickListener {
            onClickAdd(root)
        }

        return root
    }

    private fun onClickAdd(view: View?) {
        val name = view?.findViewById<EditText>(R.id.editTextProviderName)
        val INN = view?.findViewById<EditText>(R.id.editTextProviderINN)
        val postcode = view?.findViewById<EditText>(R.id.editTextProviderPostcode)
        val address = view?.findViewById<TextView>(R.id.editTextProviderAddress)

        if (name?.text.toString().isEmpty()) {
            name?.error = "Пожалуйста, введите название"
            name?.requestFocus()
            return
        }

        if (INN?.text.toString().isEmpty()) {
            INN?.error = "Пожалуйста, введите ИНН"
            INN?.requestFocus()
            return
        }

        if (postcode?.text.toString().isEmpty() || postcode?.text?.count()!=6) {
            postcode?.error = "Пожалуйста, введите индекс"
            postcode?.requestFocus()
            return
        }

        if (address?.text.toString().isEmpty()) {
            address?.error = "Пожалуйста, введите адрес"
            address?.requestFocus()
            return
        }

        val data = hashMapOf(
            "name" to name?.text.toString(),
            "INN" to INN?.text.toString(),
            "postcode" to postcode?.text.toString(),
            "address" to address?.text.toString()
        )

        db.collection("providers")
            .add(data)
            .addOnSuccessListener { documentReference ->
                Log.d(TAG, "DocumentSnapshot written with ID: ${documentReference.id}")
                val builder = AlertDialog.Builder(view?.context)
                builder.setMessage("Успешно сохранено")
                    .setCancelable(false)
                    .setPositiveButton("ОК") { dialog, id ->
                    }
                val alert = builder.create()
                alert.show()
                view.findNavController().popBackStack()
            }
            .addOnFailureListener { e ->
                Log.w(TAG, "Error adding document", e)
                Toast.makeText(activity, "Ошибка. Попробуйте ещё раз",
                    Toast.LENGTH_SHORT).show()
            }
    }

}