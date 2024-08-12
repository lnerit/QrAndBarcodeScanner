package com.example.barcodescanner.feature.tabs.session

import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.LinearLayout
import androidx.annotation.RequiresApi
import androidx.fragment.app.Fragment
import com.example.barcodescanner.R
import com.example.barcodescanner.extension.applySystemWindowInsets
import com.example.barcodescanner.feature.tabs.scan.ScanBarcodeFromCameraFragment
import com.example.barcodescanner.feature.tabs.settings.device.DeviceIdProvider
import com.example.barcodescanner.network.ApiClient
import com.example.barcodescanner.network.ApiService
import com.google.gson.JsonArray
import com.google.gson.JsonElement
import com.google.gson.JsonObject
import com.google.gson.JsonParser
import io.reactivex.disposables.CompositeDisposable
import kotlinx.android.synthetic.main.fragment_settings.app_bar_layout
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

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
        fetchDataFromDatabase(linearLayout)

    }
    private fun showBarcodeScanner(
        courseCode:String,
        eventTitle:String,
        roomNumber:String,
        time:String,
        year: Int?,
        semester: Int?,
        eventId:String){
        val fragment = ScanBarcodeFromCameraFragment()
        // Create a Bundle to pass data
        val bundle = Bundle().apply {
            putString("CourseCode",courseCode)
            putString("EventTitle",eventTitle)
            putString("RoomNumber",roomNumber)
            putString("Time",time)
            putString("EventId",eventId)
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
        }else {
            Log.e("SessionFragment", "SessionFragment is not attached to an activity")
            handleError(requireContext(), "SessionFragment is not attached to an activity")
        }
    }

    // Example function to simulate fetching data from a database
    private fun fetchDataFromDatabase(linearLayout:LinearLayout): List<Item> {
        // Replace this with your actual database query logic
        val deviceId = DeviceIdProvider.getDeviceId()  // Get device ID from the singleton
        Log.d("DeviceId:", deviceId.toString())
        val apiService = ApiClient.getRetrofitInstance().create(ApiService::class.java)
        /*
        val call = apiService.PostBarcode(
            code = "23301913",
            eventId = "c8347c02-cec5-4e8c-8c40-b4dcf03f12d5",
            roomNumber = "MCS202",
            deviceId = "A2C78A9A862DACF9"
        )*/
        val callDevice = apiService.ScanBarcodeMobile(
            deviceId = "A2C78A9A862DACF9"
        )
        // Function to fetch all items using coroutines
            val itemList: MutableList<Item> = mutableListOf()

                callDevice.enqueue(object : Callback<JsonObject> {
                    @RequiresApi(Build.VERSION_CODES.O)
                    override fun onResponse(
                        callDevice: Call<JsonObject>,
                        response: Response<JsonObject>
                    ) {
                        Log.d("Call started...", "Call to URL state...")
                        if (response.isSuccessful) {
                            val sessions = response.body()
                            Log.d("tSMAS Response data", sessions.toString())
                            Log.d("tSMAS Response", sessions?.get("success").toString())
                            var sResponse = sessions?.get("success")?.asBoolean;
                            var sResponseText = sessions?.get("responseText")?.asString;
                            // Create a list of Item objects


                            if (sResponse == true) {
                                val rows = listOf(sessions?.get("sessions"))
                                Log.d("Session Listing", "Response:" + rows)
                                if (rows.size > 0) {
                                    for (row in rows) {
                                        //Log.d("", row?.asJsonObject.toString())
                                        //val jsonString = "[{\"isCurrentSession\":true,\"deviceId\":\"A2C78A9A862DACF9\",\"id\":\"1bbac0bf-86cb-45c2-87ca-9e4aada85624\",\"academicYear\":2024,\"semester\":2,\"courseCode\":\"CS220\",\"venue\":\"MCS202\",\"duration\":1.0,\"eventDescription\":\"Lecture\",\"activate\":true,\"eventDate\":\"2024-08-13T01:10:00\",\"particulars\":null}]"

                                        val jsonElement: JsonElement =
                                            JsonParser().parse(row.toString())
                                        Log.d("jsonElement", jsonElement.toString())
                                        if (jsonElement.isJsonArray) {
                                            val jsonArray: JsonArray = jsonElement.asJsonArray
                                            Log.d("jsonArray", jsonArray.toString())
                                            for (jsonObj in jsonArray) {
                                                if (jsonObj.isJsonObject) {
                                                    val jsonObject = jsonObj.asJsonObject
                                                    Log.d("jsonObject", jsonObj.toString())
                                                    Log.d(
                                                        "courseCode",
                                                        jsonObject.get("courseCode").toString()
                                                    )
                                                    // Now you can access the fields of the JSON object
                                                    // Safely check for null and existence of the key
                                                    // Initialize courseCode with a default value
                                                    var courseCode: String = "Unknown"
                                                    var eventTitle: String = "Unknown"
                                                    var roomNumber: String = "Unknown"
                                                    var time: String = "Unknown"
                                                    var year: Int = 0
                                                    var semester: Int = 0
                                                    var eventId: String = "Unknown"

                                                    if (jsonObject.has("courseCode")) {
                                                        val courseCodeElement =
                                                            jsonObject.get("courseCode")

                                                        // Check if the value is not null
                                                        if (!courseCodeElement.isJsonNull) {
                                                            courseCode = courseCodeElement.asString
                                                            Log.d("courseCode", courseCode)
                                                        } else {
                                                            Log.e("Error", "Course code is null")
                                                        }
                                                    } else {
                                                        Log.e("Error", "Course code key is missing")
                                                    }
                                                    //val courseCode = jsonObject.get("courseCode").asString

                                                    //val eventTitle = jsonObject.get("eventTitle").asString
                                                    if (jsonObject.has("eventDescription")) {
                                                        val eventTitleElement = jsonObject.get("eventDescription")

                                                        // Check if the value is not null
                                                        if (!eventTitleElement.isJsonNull) {
                                                            eventTitle = eventTitleElement.asString
                                                            Log.d("eventTitle", eventTitle)
                                                        } else {
                                                            Log.e(
                                                                "Error",
                                                                "eventTitle code is null"
                                                            )
                                                        }
                                                    } else {
                                                        Log.e("Error", "eventTitle  key is missing")
                                                    }
                                                    //val roomNumber = jsonObject.get("roomNumber").asString

                                                    if (jsonObject.has("venue")) {
                                                        val roomNumberElement =
                                                            jsonObject.get("venue")

                                                        // Check if the value is not null
                                                        if (!roomNumberElement.isJsonNull) {
                                                            roomNumber = roomNumberElement.asString
                                                            Log.d("roomNumberElement", roomNumber)
                                                        } else {
                                                            Log.e(
                                                                "Error",
                                                                "roomNumber code is null"
                                                            )
                                                        }
                                                    } else {
                                                        Log.e("Error", "roomNumber  key is missing")
                                                    }

                                                    //val time = jsonObject.get("time").asString

                                                    if (jsonObject.has("eventDate")) {
                                                        val timeElement =
                                                            jsonObject.get("eventDate")

                                                        // Check if the value is not null
                                                        if (!timeElement.isJsonNull) {
                                                            time = timeElement.asString
                                                            Log.d("timeElement", time)
                                                        } else {
                                                            Log.e("Error", "time is null")
                                                        }
                                                    } else {
                                                        Log.e("Error", "time  key is missing")
                                                    }
                                                    //val eventId = jsonObject.get("eventId").asString


                                                    if (jsonObject.has("id")) {
                                                        val eventIdElement = jsonObject.get("id")

                                                        // Check if the value is not null
                                                        if (!eventIdElement.isJsonNull) {
                                                            eventId = eventIdElement.asString
                                                            Log.d("eventId", eventId)
                                                        } else {
                                                            Log.e("Error", "eventId is null")
                                                        }
                                                    } else {
                                                        Log.e("Error", "eventId  key is missing")
                                                    }

                                                    //val year = jsonObject.get("year").asInt


                                                    if (jsonObject.has("academicYear")) {
                                                        val yearElement =
                                                            jsonObject.get("academicYear")

                                                        // Check if the value is not null
                                                        if (!yearElement.isJsonNull) {
                                                            year = yearElement.asInt
                                                            Log.d("academicYear", year.toString())
                                                        } else {
                                                            Log.e("Error", "year is null")
                                                        }
                                                    } else {
                                                        Log.e("Error", "year  key is missing")
                                                    }
                                                    // val semester = jsonObject.get("semester").asInt
                                                    if (jsonObject.has("semester")) {
                                                        val semesterElement =
                                                            jsonObject.get("semester")

                                                        // Check if the value is not null
                                                        if (!semesterElement.isJsonNull) {
                                                            semester = semesterElement.asInt
                                                            Log.d("semester", semester.toString())
                                                        } else {
                                                            Log.e("Error", "semester is null")
                                                        }
                                                    } else {
                                                        Log.e("Error", "semester  key is missing")
                                                    }
                                                    // Process the object as needed
                                                    val item = Item(
                                                        courseCode = courseCode.toString(),
                                                        eventTitle = eventTitle.toString(),
                                                        roomNumber = roomNumber.toString(),
                                                        time = time.toString(),
                                                        eventId = eventId.toString(),
                                                        year = year,  // Convert string to Int
                                                        semester = semester // Convert string to Int
                                                    )

                                                    val button = Button(context).apply {
                                                        textSize = 18f
                                                        setBackgroundColor(Color.parseColor("#90EE90")) // Light green color
                                                        setTextColor(Color.BLACK) // Optional: Set text color to black for contrast
                                                        //setBackgroundColor(0xFF6200EE.toInt()) // Set background color using hex (purple in this case)
                                                        //setTextColor(0xFFFFFFFF.toInt()) // Set text color (white)
                                                        gravity = Gravity.CENTER
                                                    }

                                                    //val timeOnly = item.time.split("T")[1]
                                                    val parsedDateTime = LocalDateTime.parse(item.time)
                                                    // Define a formatter to include AM/PM
                                                    val formatter = DateTimeFormatter.ofPattern("hh:mm a")
                                                    val formattedTime = parsedDateTime.format(formatter)

                                                    button.text = item.courseCode +" " + item.eventTitle +" "+ formattedTime + "| Room:"+item.roomNumber  //"$item.courseCode $item.eventTitle ($item.time) | Room: $item.roomNumber"
                                                    button.layoutParams = LinearLayout.LayoutParams(
                                                        LinearLayout.LayoutParams.MATCH_PARENT,
                                                        LinearLayout.LayoutParams.WRAP_CONTENT
                                                    ).apply {
                                                        setMargins(16, 16, 16, 16) // Set margins (left, top, right, bottom)
                                                    }
                                                    // Add button to the LinearLayout
                                                    linearLayout.addView(button)
                                                    // Set onClickListener if needed
                                                    button.setOnClickListener {
                                                        // Handle button click
                                                        //Log.d("Session Item", "$item.courseCode $item.eventTitle ($item.time) | Room: $item.roomNumber | Text: $item.buttonText")

                                                        showBarcodeScanner(item.courseCode,item.eventTitle,item.roomNumber,item.time,item.year,item.semester,item.eventId)
                                                    }
                                                }
                                            }

                                        } else {
                                            // Handle the case where it's not a JSON array
                                            Log.d("tSMAS Response111", "it's not a JSON array")
                                            handleError(
                                                requireContext(),
                                                "Data not in correct format JSON Array"
                                            )
                                        }
                                    }
                                    // Populate the list using a for loop
                                } else {
                                    Log.d("tSMAS Response", sResponseText.toString())
                                    //Toast.makeText(requireContext(), sResponseText.toString(), Toast.LENGTH_LONG).show()
                                    handleError(requireContext(), sResponseText.toString())
                                }

                            } else {
                                //Device is not authorized
                                //Redirect to an error page
                                Log.d("tSMAS Response", sResponseText.toString())
                                //Toast.makeText(requireContext(), sResponseText.toString(), Toast.LENGTH_LONG).show()
                                handleError(requireContext(), sResponseText.toString())
                            }
                        } else {
                            Log.d("No Response", "No response...")
                            //Toast.makeText(requireContext(), "No response", Toast.LENGTH_LONG).show()
                            handleError(requireContext(), "No response")
                        }

                    }

                    override fun onFailure(call: Call<JsonObject>, t: Throwable) {
                        Log.e("MainActivity", "Error: ${t.message}")
                        handleError(requireContext(), t.message.toString())
                    }

                })

            /*
        return listOf(
            Item1("Button 1"),
        )*/
            Log.d("Sessions from database:", itemList.toString())
            return itemList

    }
    data class Item1(
        val name:String
    )
    // Data class representing an item fetched from the database
    data class Item(
        val courseCode: String,
        val eventTitle: String,
        val roomNumber: String,
        var time:String,
        val eventId:String,
        val year: Int?,
        var semester: Int?
        )
    fun handleError(context: Context, errorMessage: String) {
        val intent = Intent(context, SessionErrorActivity::class.java).apply {
            putExtra("ERROR_MESSAGE", errorMessage)
        }
        context.startActivity(intent)
    }
}