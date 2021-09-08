package com.example.mobilestorage.fragments.home.product

import android.annotation.SuppressLint
import android.app.Activity
import android.app.AlertDialog
import android.content.Intent
import android.net.Uri
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
import com.example.mobilestorage.model.Provider
import com.google.firebase.Timestamp
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.google.mlkit.vision.barcode.Barcode
import com.google.mlkit.vision.barcode.BarcodeScannerOptions
import com.google.mlkit.vision.barcode.BarcodeScanning
import com.google.mlkit.vision.common.InputImage
import java.lang.Exception
import java.text.SimpleDateFormat
import java.util.*

class ProductInfoFragment: Fragment() {

    private lateinit var root: View

    private lateinit var id: String
    private lateinit var product: Product
    private lateinit var provider: Provider
    private var idProvider: String = ""
    private var nameProvider: String = ""
    private val db = Firebase.firestore
    private lateinit var timestampDate: Timestamp
    private var barcode: String = ""

    @SuppressLint("SimpleDateFormat")
    private val df = SimpleDateFormat("dd/MM/yyyy")

    private val TAG = "MyLogger"

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        root = inflater.inflate(R.layout.fragment_product_info, container, false)

        id = arguments?.getString("id")!!

        if (arguments?.getString("id_provider") != null) {
            idProvider = arguments?.getString("id_provider")!!
            nameProvider = arguments?.getString("name_provider")!!
            root.findViewById<TextView>(R.id.provider_data).text = nameProvider
        }
        getProduct()

        root.findViewById<Button>(R.id.edit).setOnClickListener {
            onClickEdit()
        }
        root.findViewById<Button>(R.id.delete).setOnClickListener {
            onClickDelete()
        }
        root.findViewById<Button>(R.id.editConf).setOnClickListener {
            onClickEditConfirm()
        }

        root.findViewById<Button>(R.id.select_dateEdit).setOnClickListener {
            showMessageDateBox()
        }

        root.findViewById<Button>(R.id.editCancel).setOnClickListener {
            root.findViewById<TextView>(R.id.provider_data).text = provider.name
            updateEditUI(false)
        }

        root.findViewById<Button>(R.id.camera).setOnClickListener {
            openGalleryForImage()
        }

        root.findViewById<Button>(R.id.select_providerEdit)?.setOnClickListener {
            val bundle = bundleOf(
                "from_where" to "editProduct",
                "id" to id
            )
            root.findNavController().navigate(R.id.action_productInfo_to_provider, bundle)
        }

        return root
    }


    private fun getProduct() {
        db.collection("products").document(id).get()
            .addOnSuccessListener { document ->
                product =  Product(document.id,
                    document.get("name") as String,
                    document.get("provider") as String,
                    document.get("price") as Long,
                    document.get("amount") as Long,
                    document.get("unit") as String,
                    document.get("total") as Long,
                    document.get("arrival_date") as Timestamp)
                timestampDate = product.arrivalDate
                try {
                    barcode = document.get("barcode") as String
                    root.findViewById<TextView>(R.id.barcode).text = barcode
                } catch (e: Exception) {
                    Log.w(TAG, "Error getting documents: ", e)
                }
                if (idProvider != "") {
                    updateEditUI(true)
                } else {
                    idProvider = product.provider
                }
                getProvider()
            }
            .addOnFailureListener { exception ->
                Log.w(TAG, "Error getting documents: ", exception)
            }

    }

    private fun getProvider() {
        db.collection("providers").document(product.provider).get()
            .addOnSuccessListener { document ->
                try {
                    provider = Provider(document.id,
                        document.get("name") as String,
                        document.get("INN") as String,
                        document.get("postcode") as String,
                        document.get("address") as String)
                    if (nameProvider != "") else nameProvider = provider.name
                    updateUI()
                } catch (e: Exception) {}
            }
            .addOnFailureListener { exception ->
                Log.w(TAG, "Error getting documents: ", exception)
            }
    }

    private fun onClickEdit(){
        updateEditUI(true)
    }

    private fun onClickEditConfirm() {
        var total = root.findViewById<EditText>(R.id.editTextEditPrice)?.text.toString().toInt() * root.findViewById<EditText>(R.id.editTextEditAmount)?.text.toString().toInt()
        var data = mutableMapOf(
            "name" to root.findViewById<EditText>(R.id.editTextEditName)?.text.toString(),
            "price" to root.findViewById<EditText>(R.id.editTextEditPrice)?.text.toString().toLong(),
            "amount" to root.findViewById<EditText>(R.id.editTextEditAmount)?.text.toString().toLong(),
            "util" to root.findViewById<EditText>(R.id.editTextEditUnit)?.text.toString(),
            "total" to total,
            "arrival_date" to timestampDate,
            "provider" to idProvider
        )
        if (barcode!="") {
            data["barcode"] = barcode
        }
        db.collection("products").document(id)
            .update(data as Map<String, Any>)
            .addOnSuccessListener {
                Log.d(TAG, "DocumentSnapshot successfully updated!")
                Toast.makeText(
                    activity, "Успешно",
                    Toast.LENGTH_SHORT
                ).show()
                idProvider = ""
                nameProvider = ""
                getProduct()
                updateEditUI(false)
            }
            .addOnFailureListener { e ->
                Log.w(TAG, "Error updating document", e)
                Toast.makeText(
                    activity, "Ошибка. Попробуйте ещё раз",
                    Toast.LENGTH_SHORT
                ).show()
            }
    }

    private fun onClickDelete(){
        val builder = AlertDialog.Builder(activity)
        builder.setMessage("Вы уверены, что хотите удалить эту запись?")
            .setCancelable(false)
            .setPositiveButton("Да") { dialog, id_ ->
                db.collection("products").document(id)
                    .delete()
                    .addOnSuccessListener {
                        Log.d(TAG, "DocumentSnapshot successfully deleted!")
                        Toast.makeText(
                            activity, "Успешно",
                            Toast.LENGTH_SHORT
                        ).show()
                        activity?.supportFragmentManager?.popBackStack()
                    }
                    .addOnFailureListener { e ->
                        Log.w(TAG, "Error deleting document", e)
                        Toast.makeText(
                            activity, "Ошибка. Попробуйте ещё раз",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
            }
            .setNegativeButton("Нет") { dialog, id_ ->
                dialog.dismiss()
            }
        val alert = builder.create()
        alert.show()
    }

    @SuppressLint("SimpleDateFormat")
    private fun showMessageDateBox(){
        //Inflate the dialog as custom view
        val messageBoxView = LayoutInflater.from(activity).inflate(R.layout.box_date, null)

        //AlertDialogBuilder
        val messageBoxBuilder = AlertDialog.Builder(activity).setView(messageBoxView)

        //show dialog
        val  messageBoxInstance = messageBoxBuilder.show()
        val calendar = Calendar.getInstance()

        val calendarView = messageBoxView.findViewById<CalendarView>(R.id.calendarView)
        calendarView.date = timestampDate.seconds*1000
        calendarView.setOnDateChangeListener { view, year, month, dayOfMonth ->
            // set the calendar date as calendar view selected date
            calendar.set(year, month, dayOfMonth)
            calendarView.date = calendar.timeInMillis
        }

        //set Listener
        messageBoxView?.findViewById<Button>(R.id.ok_date)?.setOnClickListener(){
            timestampDate = Timestamp(Date(calendarView.date))
            root.findViewById<TextView>(R.id.arrival_date_dataEdit)?.text = df.format(timestampDate.toDate())
            messageBoxInstance.dismiss()
        }
    }

    val REQUEST_CODE = 100
    private fun openGalleryForImage() {
        val intent = Intent(Intent.ACTION_PICK)
        intent.type = "image/*"
        startActivityForResult(intent, REQUEST_CODE)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (resultCode == Activity.RESULT_OK && requestCode == REQUEST_CODE && data != null){
            val uri = data?.data
            scanBarcode(uri)
        }
    }

    private fun scanBarcode(uri: Uri?) {
        val options = BarcodeScannerOptions.Builder()
            .setBarcodeFormats(
                Barcode.FORMAT_QR_CODE,
                Barcode.FORMAT_AZTEC)
            .build()
        val image = InputImage.fromFilePath(context, uri)
        val scanner = BarcodeScanning.getClient()
        // Or, to specify the formats to recognize:
        // val scanner = BarcodeScanning.getClient(options)
        scanner.process(image)
            .addOnSuccessListener { barcodes ->
                if (barcodes.isEmpty()) {
                    Toast.makeText(activity, "Код для сканирования не найден",
                        Toast.LENGTH_SHORT).show()
                } else {
                    barcode = barcodes[0].displayValue
                    root.findViewById<TextView>(R.id.barcode).text = barcode
                }
            }
            .addOnFailureListener {e ->
                Log.w(TAG, "Error", e)
            }

    }

    @SuppressLint("SetTextI18n", "SimpleDateFormat")
    private fun updateUI() {
        root.findViewById<TextView>(R.id.headText)?.text = "Товар № $id"
        root.findViewById<TextView>(R.id.name_data)?.text = product.name
        root.findViewById<TextView>(R.id.price_data)?.text = product.price.toString()
        root.findViewById<TextView>(R.id.unit_data)?.text = product.unit
        root.findViewById<TextView>(R.id.amount_data)?.text = product.amount.toString()
        root.findViewById<TextView>(R.id.total_data)?.text = product.total.toString()
        root.findViewById<TextView>(R.id.arrival_date_dataEdit)?.text = df.format(timestampDate.toDate())
        root.findViewById<TextView>(R.id.provider_data).text = nameProvider
    }

    @SuppressLint("CutPasteId")
    private fun updateEditUI(edit: Boolean) {
        if (edit) {
            root.findViewById<Button>(R.id.edit)?.visibility = Button.INVISIBLE
            root.findViewById<Button>(R.id.delete)?.visibility = Button.INVISIBLE
            root.findViewById<Button>(R.id.editConf)?.visibility = Button.VISIBLE
            root.findViewById<Button>(R.id.editCancel)?.visibility = Button.VISIBLE
            root.findViewById<Button>(R.id.select_dateEdit)?.visibility = Button.VISIBLE
            root.findViewById<Button>(R.id.select_providerEdit)?.visibility = Button.VISIBLE
            root.findViewById<Button>(R.id.camera)?.visibility = Button.VISIBLE

            root.findViewById<TextView>(R.id.editTextEditName)?.visibility = EditText.VISIBLE
            root.findViewById<TextView>(R.id.editTextEditName)?.text = product.name
            root.findViewById<TextView>(R.id.name_data)?.visibility = TextView.INVISIBLE

            root.findViewById<TextView>(R.id.editTextEditPrice)?.visibility = EditText.VISIBLE
            root.findViewById<TextView>(R.id.editTextEditPrice)?.text = product.price.toString()
            root.findViewById<TextView>(R.id.price_data)?.visibility = TextView.INVISIBLE

            root.findViewById<TextView>(R.id.editTextEditUnit)?.visibility = EditText.VISIBLE
            root.findViewById<TextView>(R.id.editTextEditUnit)?.text = product.unit
            root.findViewById<TextView>(R.id.unit_data)?.visibility = TextView.INVISIBLE

            root.findViewById<TextView>(R.id.editTextEditAmount)?.visibility = EditText.VISIBLE
            root.findViewById<TextView>(R.id.editTextEditAmount)?.text = product.amount.toString()
            root.findViewById<TextView>(R.id.amount_data)?.visibility = TextView.INVISIBLE


        } else {
            root.findViewById<Button>(R.id.edit)?.visibility = Button.VISIBLE
            root.findViewById<Button>(R.id.delete)?.visibility = Button.VISIBLE
            root.findViewById<Button>(R.id.editConf)?.visibility = Button.INVISIBLE
            root.findViewById<Button>(R.id.editCancel)?.visibility = Button.INVISIBLE
            root.findViewById<Button>(R.id.select_dateEdit)?.visibility = Button.INVISIBLE
            root.findViewById<Button>(R.id.select_providerEdit)?.visibility = Button.INVISIBLE
            root.findViewById<Button>(R.id.camera)?.visibility = Button.INVISIBLE

            root.findViewById<TextView>(R.id.editTextEditName)?.visibility = EditText.INVISIBLE
            root.findViewById<TextView>(R.id.name_data)?.visibility = TextView.VISIBLE

            root.findViewById<TextView>(R.id.editTextEditPrice)?.visibility = EditText.INVISIBLE
            root.findViewById<TextView>(R.id.price_data)?.visibility = TextView.VISIBLE

            root.findViewById<TextView>(R.id.editTextEditUnit)?.visibility = EditText.INVISIBLE
            root.findViewById<TextView>(R.id.unit_data)?.visibility = TextView.VISIBLE

            root.findViewById<TextView>(R.id.editTextEditAmount)?.visibility = EditText.INVISIBLE
            root.findViewById<TextView>(R.id.amount_data)?.visibility = TextView.VISIBLE
        }
    }
}