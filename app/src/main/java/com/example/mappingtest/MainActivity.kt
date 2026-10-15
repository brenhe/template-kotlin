package com.example.mappingtest

import android.os.Bundle
import android.view.View
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val titleView = findViewById<TextView>(R.id.title)
        val resultView = findViewById<TextView>(R.id.text_result)

        titleView.text = "Monthly Excel inputs"

        try {
            val file = getMonthlyInputFile()

            if (!file.exists()) {
                resultView.visibility = View.VISIBLE
                resultView.text = "No file found: ${file.name}\nUse columns: object, value, date"
                return
            }

            val rows = ExcelInputParser().parse(FileInputStream(file))

            if (rows.isEmpty()) {
                resultView.visibility = View.VISIBLE
                resultView.text = "File read successfully, but no valid rows were found."
                return
            }

            resultView.visibility = View.VISIBLE
            resultView.text = rows.joinToString(separator = "\n") { row ->
                "object=${row.objectName} | value=${row.value} | date=${row.date}"
            }
        } catch (e: Exception) {
            resultView.visibility = View.VISIBLE
            resultView.text = "Excel error: ${e.message}"
        }
    }

    private fun getMonthlyInputFile(): File {
        val outputFile = File(filesDir, "monthly_inputs.xlsx")

        if (outputFile.exists()) {
            return outputFile
        }

        val assetStream = assets.open("monthly_inputs.xlsx")
        val outputStream = FileOutputStream(outputFile)

        assetStream.use { input ->
            outputStream.use { output ->
                input.copyTo(output)
            }
        }

        return outputFile
    }
}