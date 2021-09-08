package com.example.mobilestorage.fragments.arrival

import android.annotation.SuppressLint
import android.app.AlertDialog
import android.os.Bundle
import android.os.Environment
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.fragment.app.Fragment
import com.example.mobilestorage.R
import com.example.mobilestorage.model.Arrival
import com.example.mobilestorage.model.Product
import com.google.firebase.Timestamp
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import org.apache.poi.ss.usermodel.IndexedColors
import java.text.SimpleDateFormat
import org.apache.poi.xssf.usermodel.XSSFWorkbook
import java.io.File
import java.io.FileOutputStream
import java.lang.Exception

class ArrivalInfoFragment: Fragment() {

    private lateinit var root: View

    private val TAG = "MyLogger"
    private lateinit var id: String
    private lateinit var arrival: Arrival
    private var productsList: MutableList<Product> = mutableListOf()
     @SuppressLint("SimpleDateFormat")
     private val df = SimpleDateFormat("dd/MM/yyyy")

    private val db = Firebase.firestore

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        root = inflater.inflate(R.layout.fragment_arrival_info, container, false)
        setHasOptionsMenu(true)

        id = arguments?.getString("id")!!

        getArrival()

        root.findViewById<Button>(R.id.delete).setOnClickListener {
            onClickDelete()
        }

        root.findViewById<Button>(R.id.createExcelFile).setOnClickListener {
            createExcel()
        }

        return root
    }

    @Suppress("UNCHECKED_CAST")
    @SuppressLint("SetTextI18n")
    private fun getArrival(){
        db.collection("arrivals").document(id).get()
            .addOnSuccessListener { document ->
                arrival = Arrival(document.id,
                    document.get("arrivalDate") as Timestamp,
                    document.get("providerID") as String,
                    document.get("providerName") as String,
                    document.get("listOfProductsID") as MutableList<String>)

                root.findViewById<TextView>(R.id.headText).text = "Приход № ${arrival.id}"
                root.findViewById<TextView>(R.id.provider_data).text = arrival.providerName
                root.findViewById<TextView>(R.id.arrival_date).text = df.format(arrival.arrivalDate.toDate())
                getProducts()
            }
            .addOnFailureListener { exception ->
                Log.w(TAG, "Error getting documents: ", exception)
            }
    }

    private fun getProducts() {
        val namesProductsList: MutableList<String> = mutableListOf()

        for (productId in arrival.listOfProductsID) {
            db.collection("products")
                .document(productId).get()
                .addOnSuccessListener { document ->
                    val productID = document.id
                    val productName = document.get("name") as String
                    productsList.add(Product(productID,
                        productName,
                        document.get("provider") as String,
                        document.get("price") as Long,
                        document.get("amount") as Long,
                        document.get("unit") as String,
                        document.get("total") as Long,
                        document.get("arrival_date") as Timestamp))
                    namesProductsList.add(productName)
                    val listView = root.findViewById<ListView>(R.id.list_view)
                    val listViewAdapter = context?.let { ArrayAdapter<String>(it, android.R.layout.simple_list_item_1, namesProductsList) }
                    listView.adapter = listViewAdapter
        }
                .addOnFailureListener {exception ->
                    Log.w(TAG, "Error getting documents: ", exception)
                }
        }
    }

    private fun onClickDelete(){
        val builder = AlertDialog.Builder(activity)
        builder.setMessage("Вы уверены, что хотите удалить эту запись?")
            .setCancelable(false)
            .setPositiveButton("Да") { dialog, id_ ->
                db.collection("arrivals").document(id)
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

    private fun createExcel() {

        val ArrivalCOLUMNs = arrayOf("Идентификатор", "Дата прибытия" , "Поставщик", "Идентификатор поставщика", "Дата прибытия")
        val ProductCOLUMNs = arrayOf("Идентификатор", "Название", "Количество", "Единица", "Цена за ед.", "Сумма" )
        val workbook = XSSFWorkbook()
        val createHelper = workbook.getCreationHelper()

        val sheet = workbook.createSheet("Приход №${arrival.id}")

        val headerFont = workbook.createFont()
        headerFont.bold = true
        headerFont.color = IndexedColors.BLUE.getIndex()

        val headerCellStyle = workbook.createCellStyle()
        headerCellStyle.setFont(headerFont)

        // Row for Header
        val arrivalRow = sheet.createRow(0)

        // Header
        for (col in ArrivalCOLUMNs.indices) {
            val cell = arrivalRow.createCell(col)
            cell.setCellValue(ArrivalCOLUMNs[col])
            cell.setCellStyle(headerCellStyle)
        }
        var rowIdx = 1
        val row = sheet.createRow(rowIdx++)
        row.createCell(0).setCellValue(arrival.id)
        row.createCell(1).setCellValue(arrival.arrivalDate.toDate())
        row.createCell(2).setCellValue(arrival.providerName)
        row.createCell(3).setCellValue(arrival.providerID)

        rowIdx++
        // Row for Header
        val productsRow = sheet.createRow(rowIdx)

        // Header
        for (col in ProductCOLUMNs.indices) {
            val cell = productsRow.createCell(col)
            cell.setCellValue(ProductCOLUMNs[col])
            cell.setCellStyle(headerCellStyle)
        }

        for (product in productsList) {
            val row = sheet.createRow(rowIdx++)
            row.createCell(0).setCellValue(product.id)
            row.createCell(1).setCellValue(product.name)
            row.createCell(2).setCellValue(product.amount.toDouble())
            row.createCell(3).setCellValue(product.unit)
            row.createCell(4).setCellValue(product.price.toDouble())
            row.createCell(5).setCellValue(product.total.toDouble())
        }
        var dir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS)
        var outFileName = "Приход${arrival.id}.xlsx"
        try {
            var outFile = File(dir, outFileName)
            val fileOut = FileOutputStream(outFile.absolutePath)
            workbook.write(fileOut)
            fileOut.close()
            workbook.close()
            val builder = AlertDialog.Builder(root.context)
            builder.setMessage("Успешно сохранено в папке Загрузки")
                .setCancelable(false)
                .setPositiveButton("ОК") { dialog, id ->
                }
            val alert = builder.create()
            alert.show()
        } catch (e: Exception) {
            val builder = AlertDialog.Builder(root.context)
            builder.setMessage("Произошла ошибка, попробуйте ещё раз")
                .setCancelable(false)
                .setPositiveButton("ОК") { dialog, id ->
                }
            val alert = builder.create()
            alert.show()
        }
    }
}