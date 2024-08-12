package com.example.barcodescanner.feature.tabs.session

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.TextView
import com.example.barcodescanner.R
import com.example.barcodescanner.extension.applySystemWindowInsets
import com.example.barcodescanner.feature.BaseActivity
import com.example.barcodescanner.feature.tabs.scan.ScanBarcodeFromCameraFragment
import kotlinx.android.synthetic.main.activity_device_id.root_view
import kotlinx.android.synthetic.main.activity_device_id.toolbar

class SessionErrorActivity: BaseActivity() {
    companion object {
        fun start(context: Context) {
            val intent = Intent(context, SessionErrorActivity::class.java)
            context.startActivity(intent)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_session_error)
        // Get the error message passed from the previous activity
        val errorMessage = intent.getStringExtra("ERROR_MESSAGE")
        // Set the error message to a TextView
        val errorTextView = findViewById<TextView>(R.id.errorTextView)
        errorTextView.text = errorMessage
        root_view.applySystemWindowInsets(applyTop = true, applyBottom = true)


        // Set toolbar navigation click listener
       toolbar.setNavigationOnClickListener { finish() }

        // Set the click listener for the retry button
        val retryButton = findViewById<Button>(R.id.retryButton)
        retryButton.setOnClickListener {
            onRetryButtonClicked(it)
        }
    }

    fun onRetryButtonClicked(view: View) {
        val fragment = SessionFragment()
        // Create a Bundle to pass data

        //Check if fragment is added to activity
        // Perform the fragment transaction
        supportFragmentManager.beginTransaction()
            .replace(
                R.id.layout_fragment_container,
                fragment
            ) // Replace with the ID of the container in the activity's layout
            .addToBackStack(null) // Optional: Add to the back stack to enable back navigation
            .commit()
    }


}