
package com.example.mappingtest

import android.os.Bundle
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val titleView = findViewById<TextView>(R.id.title)
        val resultView = findViewById<TextView>(R.id.text_result)

        titleView.text = "Mapping Test"
        resultView.text = "App initialized successfully."

        val crashButton = Button(this)
        crashButton.text = "Test Crash"

        crashButton.setOnClickListener {
            throw RuntimeException("Test Crash")
        }

        addContentView(
            crashButton,
            ViewGroup.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
            )
        )
    }
}
