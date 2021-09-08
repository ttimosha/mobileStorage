package com.example.mobilestorage.fragments.add

import android.annotation.SuppressLint
import android.app.Activity
import android.app.AlertDialog
import android.content.Intent
import android.graphics.Bitmap
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.core.os.bundleOf
import androidx.fragment.app.Fragment
import androidx.navigation.findNavController
import com.example.mobilestorage.R
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

class AddProductFragment : Fragment() {

    private lateinit var root: View

    private val db = Firebase.firestore
    private var timestampDate: Timestamp = Timestamp.now()
    private var id_provider: String = ""
    private var barcode: String = ""


    @SuppressLint("SimpleDateFormat")
    private val df = SimpleDateFormat("dd/MM/yyyy")

    private val TAG = "MyLogger"

    @SuppressLint("SimpleDateFormat")
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        root = inflater.inflate(R.layout.fragment_add_product, container, false)

        if (arguments?.getString("id_provider") != null) {
            id_provider = arguments?.getString("id_provider")!!
            root.findViewById<TextView>(R.id.providerAddText).text = arguments?.getString("name_provider")
        }

        root.findViewById<Button>(R.id.buttonAdd)?.setOnClickListener {
            onClickAdd()
        }

        root.findViewById<TextView>(R.id.arrival_dateAddText).text = df.format(timestampDate.toDate())

        root.findViewById<Button>(R.id.select_provider)?.setOnClickListener {
            val bundle = bundleOf(
                "from_where" to "addProduct")
            root.findNavController().navigate(R.id.action_navigation_add_to_provider, bundle)
        }

        root.findViewById<Button>(R.id.camera).setOnClickListener {
            openGalleryForImage()
        }

        root.findViewById<Button>(R.id.select_date)?.setOnClickListener {
            showMessageDateBox()
        }
        return root
    }

    @SuppressLint("SetTextI18n")
    private fun onClickAdd() {
        val name = root?.findViewById<EditText>(R.id.editTextAddName)
        val price = root?.findViewById<EditText>(R.id.editTextAddPrice)
        val unit = root?.findViewById<EditText>(R.id.editTextAddUnit)
        val amount = root?.findViewById<EditText>(R.id.editTextAddAmount)
        val providerText = root?.findViewById<TextView>(R.id.providerAddText)

        if (name?.text.toString().isEmpty()) {
            name?.error = "Пожалуйста, введите название"
            name?.requestFocus()
            return
        }

        if (price?.text.toString().isEmpty()) {
            price?.error = "Пожалуйста, введите цену"
            price?.requestFocus()
            return
        }

        if (unit?.text.toString().isEmpty()) {
            unit?.error = "Пожалуйста, введите единицу измерения"
            unit?.requestFocus()
            return
        }

        if (amount?.text.toString().isEmpty()) {
            amount?.error = "Пожалуйста, введите количество"
            amount?.requestFocus()
            return
        }

        if (id_provider == "") {
            providerText?.text = "Пожалуйста, выберите поставщика"
            providerText?.requestFocus()
            return
        }

        var total = price?.text.toString().toInt() * amount?.text.toString().toInt()
        root?.findViewById<TextView>(R.id.sumAddText)?.text = "Сумма\n${total}"


        var data = mutableMapOf(
            "name" to name?.text.toString(),
            "provider" to id_provider,
            "amount" to amount?.text.toString().toInt(),
            "unit" to unit?.text.toString(),
            "price" to price?.text.toString().toInt(),
            "total" to total,
            "arrival_date" to timestampDate
        )

        if (barcode!="") data["barcode"] = barcode

        db.collection("products")
            .add(data)
            .addOnSuccessListener { documentReference ->
                Log.d(TAG, "DocumentSnapshot written with ID: ${documentReference.id}")
                val builder = AlertDialog.Builder(root?.context)
                builder.setMessage("Успешно сохранено")
                    .setCancelable(false)
                    .setPositiveButton("ОК") { dialog, id ->
                    }
                val alert = builder.create()
                alert.show()
                fragmentManager?.beginTransaction()?.detach(this)?.attach(this)?.commit()
            }
            .addOnFailureListener { e ->
                Log.w(TAG, "Error adding document", e)
                Toast.makeText(activity, "Ошибка. Попробуйте ещё раз",
                    Toast.LENGTH_SHORT).show()
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
            timestampDate = Timestamp(Date(calendarView.date))
            root.findViewById<TextView>(R.id.arrival_dateAddText)?.text = df.format(timestampDate.toDate())
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
}