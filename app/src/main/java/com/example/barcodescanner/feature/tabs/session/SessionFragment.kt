package com.example.barcodescanner.feature.tabs.session

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.LinearLayout
import androidx.fragment.app.Fragment
import com.example.barcodescanner.R
import com.example.barcodescanner.extension.applySystemWindowInsets
import com.example.barcodescanner.feature.barcode.BarcodeActivity
import com.example.barcodescanner.feature.tabs.create.CreateBarcodeFragment
import com.example.barcodescanner.feature.tabs.history.BarcodeHistoryFragment
import com.example.barcodescanner.feature.tabs.scan.ScanBarcodeFromCameraFragment
import com.example.barcodescanner.feature.tabs.settings.SettingsFragment
import io.reactivex.disposables.CompositeDisposable
import kotlinx.android.synthetic.main.fragment_settings.app_bar_layout

class SessionFragment : Fragment(){
    private val disposable = CompositeDisposable()

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_sessions, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        supportEdgeToEdge()
// Fetch data and create buttons
        setupButtons(view)
    }

    override fun onResume() {
        super.onResume()
    }


    override fun onDestroyView() {
        super.onDestroyView()
        disposable.clear()
    }

    fun supportEdgeToEdge() {
        app_bar_layout.applySystemWindowInsets(applyTop = true)
    }
    private fun setupButtons(view: View) {
        // Assuming you have a LinearLayout in your fragment_sessions.xml with id 'linearLayout'
        val linearLayout = view.findViewById<LinearLayout>(R.id.linearLayout)
        // Check if the LinearLayout is properly referenced
        if (linearLayout == null) {
            Log.e("SessionFragment", "LinearLayout not found in the view hierarchy")
            return
        }
        // Simulating data fetched from a database
        val data = fetchDataFromDatabase()

        // Dynamically create buttons based on the number of rows fetched
        for (item in data) {
            val button = Button(context)
            button.text = item.name // Assuming 'item' has a 'name' property
            button.layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            )
            // Add button to the LinearLayout
            linearLayout.addView(button)
            // Set onClickListener if needed
            button.setOnClickListener {
                // Handle button click
                Log.d("Button Click", "Button clicked")
                // Display a Toast message when the button is clicked
                // Toast.makeText(this, "Button clicked!", Toast.LENGTH_SHORT).show()
                // Create an intent to start the new activity
                //val intent = Intent(this,CreateBarcodeActivity::class.java)
                //startActivity(intent)
                //val scannerFragment= ScanBarcodeFromCameraFragment()
                //setContentView(R.layout.activity_barcode)
                // Create an Intent to start the new Activity
                //val intent = Intent(requireActivity(), BarcodeActivity::class.java)
                // startActivity(intent)
                // Create an instance of the new fragment

                showBarcodeScanner("CS220","Lecture","CS202","12.50PM",2024,2,button.text.toString())

            }
        }
    }
    private fun showBarcodeScanner(courseCode:String,eventTitle:String,roomNumber:String,time:String,year:Int,semester:Int,eventId:String){
        val fragment = ScanBarcodeFromCameraFragment()
        // Create a Bundle to pass data
        val bundle = Bundle().apply {
            putString("CourseCode",courseCode)
            putString("EventTitle",eventTitle)
            putString("RoomNumber",roomNumber)
            putString("Time",time)
            putString("ButtonText",eventId)
        }
        fragment.arguments = bundle

        //Check if fragment is added to activity
        if (isAdded) {
            // Perform the fragment transaction
            requireActivity().supportFragmentManager.beginTransaction()
                .replace(
                    R.id.layout_fragment_container,
                    fragment
                ) // Replace with the ID of the container in the activity's layout
                .addToBackStack(null) // Optional: Add to the back stack to enable back navigation
                .commit()
            //val myButton = fragment.findViewById<Button>(R.id.button_session_title_text)
            // Inflate the layout for this fragment
            // val view = inflater.inflate(R.layout.fragment_scan_barcode_from_camera, container, false)
            fragment.displaySessionInformation(courseCode,eventTitle,roomNumber,time,eventId)
        }else {
            Log.e("SessionFragment", "SessionFragment is not attached to an activity")
        }


    }
    private fun showFragment(bottomItemId: Int) {
        val fragment = when (bottomItemId) {
            R.id.item_session -> SessionFragment()
            R.id.item_scan -> ScanBarcodeFromCameraFragment()
            R.id.item_create -> CreateBarcodeFragment()
            R.id.item_history -> BarcodeHistoryFragment()
            R.id.item_settings -> SettingsFragment()
            else -> null
        }
      //  fragment?.apply(::replaceFragment)
    }
/*
    private fun replaceFragment(fragment: Fragment) {
        supportFragmentManager.beginTransaction()
            .replace(R.id.layout_fragment_container, fragment)
            .setReorderingAllowed(true)
            .commit()
    }*/
    // Example function to simulate fetching data from a database
    private fun fetchDataFromDatabase(): List<Item> {
        // Replace this with your actual database query logic
        return listOf(
            Item("Button 1"),
            Item("Button 2"),
            Item("Button 3")
        )
    }

    // Data class representing an item fetched from the database
    data class Item(val name: String)
}