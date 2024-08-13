package com.example.barcodescanner.feature.tabs.scan

import android.Manifest
import android.app.Activity
import android.content.Intent
import android.graphics.Color
import android.media.MediaPlayer
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageView
import android.widget.SeekBar
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import com.budiyev.android.codescanner.AutoFocusMode
import com.budiyev.android.codescanner.CodeScanner
import com.budiyev.android.codescanner.DecodeCallback
import com.budiyev.android.codescanner.ErrorCallback
import com.budiyev.android.codescanner.ScanMode
import com.example.barcodescanner.R
import com.example.barcodescanner.di.barcodeDatabase
import com.example.barcodescanner.di.barcodeParser
import com.example.barcodescanner.di.permissionsHelper
import com.example.barcodescanner.di.scannerCameraHelper
import com.example.barcodescanner.di.settings
import com.example.barcodescanner.extension.applySystemWindowInsets
import com.example.barcodescanner.extension.equalTo
import com.example.barcodescanner.extension.showError
import com.example.barcodescanner.extension.vibrateOnce
import com.example.barcodescanner.extension.vibrator
import com.example.barcodescanner.feature.barcode.BarcodeActivity
import com.example.barcodescanner.feature.common.dialog.ConfirmBarcodeDialogFragment
import com.example.barcodescanner.feature.tabs.scan.file.ScanBarcodeFromFileActivity
import com.example.barcodescanner.feature.tabs.settings.device.DeviceIdProvider
import com.example.barcodescanner.model.Barcode
import com.example.barcodescanner.network.ApiClient
import com.example.barcodescanner.network.ApiService
import com.example.barcodescanner.usecase.SupportedBarcodeFormats
import com.example.barcodescanner.usecase.save
import com.google.gson.JsonObject
import com.google.zxing.Result
import com.google.zxing.ResultMetadataType
import io.reactivex.Completable
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.rxkotlin.addTo
import io.reactivex.schedulers.Schedulers
import kotlinx.android.synthetic.main.fragment_scan_barcode_from_camera.button_decrease_zoom
import kotlinx.android.synthetic.main.fragment_scan_barcode_from_camera.button_increase_zoom
import kotlinx.android.synthetic.main.fragment_scan_barcode_from_camera.image_view_flash
import kotlinx.android.synthetic.main.fragment_scan_barcode_from_camera.image_view_scan_from_file
import kotlinx.android.synthetic.main.fragment_scan_barcode_from_camera.layout_flash_container
import kotlinx.android.synthetic.main.fragment_scan_barcode_from_camera.layout_scan_from_file_container
import kotlinx.android.synthetic.main.fragment_scan_barcode_from_camera.scanner_view
import kotlinx.android.synthetic.main.fragment_scan_barcode_from_camera.seek_bar_zoom
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.util.concurrent.TimeUnit

class ScanBarcodeFromCameraFragment : Fragment(), ConfirmBarcodeDialogFragment.Listener {

    companion object {
        private val PERMISSIONS = arrayOf(Manifest.permission.CAMERA)
        private const val PERMISSION_REQUEST_CODE = 101
        private const val ZXING_SCAN_INTENT_ACTION = "com.google.zxing.client.android.SCAN"
        private const val CONTINUOUS_SCANNING_PREVIEW_DELAY = 500L
    }

    private val vibrationPattern = arrayOf<Long>(0, 350).toLongArray()
    private val disposable = CompositeDisposable()
    private var maxZoom: Int = 0
    private val zoomStep = 5
    private lateinit var codeScanner: CodeScanner
    private var toast: Toast? = null
    private var lastResult: Barcode? = null
    private var myButton:Button?=null
    private var myTmpButton:Button?=null
    private var courseCode:String?=null
    private var eventTitle:String?=null
    private var roomNumber:String?=null
    private var eventId:String?=null
    //private lateinit var greenTick: ImageView
    private lateinit var mediaPlayer: MediaPlayer
    private lateinit var greenTickImageView: ImageView

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {

        // Inflate the layout for this fragment
        val view = inflater.inflate(R.layout.fragment_scan_barcode_from_camera, container, false)

        myButton = view.findViewById(R.id.button_session_title_text)
        // Optionally, set the button text here

        // Retrieve data from the Bundle
        courseCode = arguments?.getString("CourseCode")
        eventTitle = arguments?.getString("EventTitle")
        roomNumber = arguments?.getString("RoomNumber")
        val time = arguments?.getString("Time")
        eventId=arguments?.getString("EventId")
        val parsedDateTime = LocalDateTime.parse(time)
        // Define a formatter to include AM/PM
        val formatter = DateTimeFormatter.ofPattern("hh:mm a")
        val formattedTime = parsedDateTime.format(formatter)

        myButton?.text="$courseCode $eventTitle ($formattedTime) | Room: $roomNumber"
        myTmpButton=myButton
       // Log.d("From the Scanner Screen",myButton?.text.toString())
        Toast.makeText(requireContext(), myButton?.text.toString(), Toast.LENGTH_LONG).show()
        return view

    }
    private lateinit var greenTick: ImageView
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        greenTickImageView = view.findViewById(R.id.greenTickImageView)

        supportEdgeToEdge()
        setDarkStatusBar()
        initScanner()
        initFlashButton()
        handleScanFromFileClicked()
        handleZoomChanged()
        handleDecreaseZoomClicked()
        handleIncreaseZoomClicked()
        requestPermissions()
        //greenTick = view.findViewById(R.id.green_tick)
        // Initialize the MediaPlayer with the success sound
       mediaPlayer = MediaPlayer.create(requireContext(), R.raw.audio_success)

       // onBarcodeScanSuccess()

    }
    // Method to handle the barcode detection

    fun onBarcodeScannedSuccessfully() {
        greenTickImageView.visibility = View.VISIBLE
        // Optionally animate the appearance
        greenTickImageView.alpha = 0f
        greenTickImageView.animate().alpha(1f).setDuration(500).start()

        // Play success sound if needed
        playSuccessSound()
    }

    private fun playSuccessSound() {
        val mediaPlayer = MediaPlayer.create(requireContext(), R.raw.audio_success)
        mediaPlayer.start()
    }
    override fun onResume() {
        super.onResume()
        if (areAllPermissionsGranted()) {
            initZoomSeekBar()
            codeScanner.startPreview()
        }
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        if (requestCode == PERMISSION_REQUEST_CODE && areAllPermissionsGranted(grantResults)) {
            initZoomSeekBar()
            codeScanner.startPreview()
        }
    }

    override fun onBarcodeConfirmed(barcode: Barcode) {
        handleConfirmedBarcode(barcode)
    }

    override fun onBarcodeDeclined() {
        restartPreview()
    }

    override fun onPause() {
        codeScanner.releaseResources()
        super.onPause()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        setLightStatusBar()
        disposable.clear()
    }

    private fun supportEdgeToEdge() {
        image_view_flash.applySystemWindowInsets(applyTop = true)
        image_view_scan_from_file.applySystemWindowInsets(applyTop = true)
    }

    private fun setDarkStatusBar() {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.M) {
            return
        }

        if (settings.isDarkTheme) {
            return
        }

        requireActivity().window.decorView.apply {
            systemUiVisibility = systemUiVisibility xor View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR
        }
    }

    private fun setLightStatusBar() {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.M) {
            return
        }

        if (settings.isDarkTheme) {
            return
        }

        requireActivity().window.decorView.apply {
            systemUiVisibility = systemUiVisibility or View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR
        }
    }

    private fun initScanner() {
        codeScanner = CodeScanner(requireActivity(), scanner_view).apply {
            camera = if (settings.isBackCamera) {
                CodeScanner.CAMERA_BACK
            } else {
                CodeScanner.CAMERA_FRONT
            }
            autoFocusMode = if (settings.simpleAutoFocus) {
                AutoFocusMode.SAFE
            } else {
                AutoFocusMode.CONTINUOUS
            }
            formats = SupportedBarcodeFormats.FORMATS.filter(settings::isFormatSelected)
            scanMode = ScanMode.SINGLE
            isAutoFocusEnabled = true
            isFlashEnabled = settings.flash
            isTouchFocusEnabled = false
            decodeCallback = DecodeCallback(::handleScannedBarcode)
            errorCallback = ErrorCallback(::showError)
        }
    }

    private fun initZoomSeekBar() {
        scannerCameraHelper.getCameraParameters(settings.isBackCamera)?.apply {
            this@ScanBarcodeFromCameraFragment.maxZoom = maxZoom
            seek_bar_zoom.max = maxZoom
            seek_bar_zoom.progress = zoom
        }
    }

    private fun initFlashButton() {
        layout_flash_container.setOnClickListener {
            toggleFlash()
        }
        image_view_flash.isActivated = settings.flash
    }

    private fun handleScanFromFileClicked() {
        layout_scan_from_file_container.setOnClickListener {
            navigateToScanFromFileScreen()
        }
    }

    private fun handleZoomChanged() {
        seek_bar_zoom.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onStartTrackingTouch(seekBar: SeekBar?) { }
            override fun onStopTrackingTouch(seekBar: SeekBar?) { }

            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                if (fromUser) {
                    codeScanner.zoom = progress
                }
            }
        })
    }

    private fun handleDecreaseZoomClicked() {
        button_decrease_zoom.setOnClickListener {
            decreaseZoom()
        }
    }

    private fun handleIncreaseZoomClicked() {
        button_increase_zoom.setOnClickListener {
            increaseZoom()
        }
    }

    private fun decreaseZoom() {
        codeScanner.apply {
            if (zoom > zoomStep) {
                zoom -= zoomStep
            } else {
                zoom = 0
            }
            seek_bar_zoom.progress = zoom
        }
    }

    private fun increaseZoom() {
        codeScanner.apply {
            if (zoom < maxZoom - zoomStep) {
                zoom += zoomStep
            } else {
                zoom = maxZoom
            }
            seek_bar_zoom.progress = zoom
        }
    }

    private fun handleScannedBarcode(result: Result) {
        if (requireActivity().intent?.action == ZXING_SCAN_INTENT_ACTION) {
            vibrateIfNeeded()
            finishWithResult(result)
            return
        }
        val deviceId = DeviceIdProvider.getDeviceId()
        val barcode = barcodeParser.parseResult(result)
        PostBarcode(barcode, eventId.toString(), roomNumber.toString(),deviceId)
        myButton=myTmpButton;

        /*
        // Hide the tick after 2 seconds (2000 milliseconds)
        Handler(Looper.getMainLooper()).postDelayed({
            myButton?.text=myTmpButton?.text;
        }, 2000)
        */
        if (settings.continuousScanning && result.equalTo(lastResult)) {
            restartPreviewWithDelay(false)
            return
        }

        vibrateIfNeeded()



        when {
            settings.confirmScansManually -> showScanConfirmationDialog(barcode)
            settings.saveScannedBarcodesToHistory || settings.continuousScanning -> saveScannedBarcode(barcode)
            else -> navigateToBarcodeScreen(barcode)
        }

    }

    private fun onBarcodeScanSuccess() {
        greenTick.visibility = View.VISIBLE
// Play the success sound
        mediaPlayer.start()

        // Hide the tick after 2 seconds (2000 milliseconds)
        Handler(Looper.getMainLooper()).postDelayed({
            greenTick.visibility = View.GONE
        }, 5000)
    }
    override fun onDestroy() {
        super.onDestroy()
        // Release the MediaPlayer resources
        if (this::mediaPlayer.isInitialized) {
            mediaPlayer.release()
        }
    }
    //Save Attendance Record in tSMAS
    private fun PostBarcode(barcode:Barcode,eventId:String,roomNumber:String,deviceId:String):Boolean{

       // Inflate the layout for this fragment
       // val view = inflater.inflate(R.layout.fragment_scan_barcode_from_camera, container, false)


       // myButton?.text="This is a test......."

        val apiService = ApiClient.getRetrofitInstance().create(ApiService::class.java)
        val postData = apiService.PostBarcode(
            code = barcode.text,
            eventId = eventId,
            roomNumber = roomNumber,
            deviceId = deviceId
        )
        var successCode=false;
        postData.enqueue(object : Callback<JsonObject> {
            @RequiresApi(Build.VERSION_CODES.O)
            override fun onResponse(
                call: Call<JsonObject>,
                response: Response<JsonObject>
            ) {
               // Log.d("Call started...", "Call to URL state...")
                if (response.isSuccessful) {
                    val session = response.body()
                    //Log.d("tSMAS Response data", session.toString())
                    //Log.d("tSMAS Response", session?.get("success").toString())
                    var sResponse = session?.get("success")?.asBoolean
                    var sResponseText = session?.get("responseText")?.asString
                    // Create a list of Item objects

                    if (sResponse == true) {
                        //val responseText =
                        //Log.d("Attendance Submission:", "Response:" + sResponseText)
                       // showToastCustomMessage("Attendance Submission:"+ sResponseText)
                        //Display success message to the student
                        myButton?.text= sResponseText

                        successCode=true;
                       // onBarcodeScanSuccess()
                        onBarcodeScannedSuccessfully()
                        myButton?.setBackgroundColor(Color.parseColor("#90EE90"))
                    } else {
                        //Attendence record wasn't save
                        //Display error message
                       // Log.d("Attendance Record Response from tSMAS", sResponseText.toString())
                        //Toast.makeText(requireContext(), sResponseText.toString(), Toast.LENGTH_LONG).show()
                      //  handleError(requireContext(), sResponseText.toString())
                        myButton?.text=sResponseText.toString()
                        myButton?.setBackgroundColor(Color.parseColor("#ff8d6d"))
                        successCode=false;
                    }
                } else {
                   // Log.d("No response from server", "No response from service...")
                    //Toast.makeText(requireContext(), "No response", Toast.LENGTH_LONG).show()
                    //handleError(requireContext(), "No response")
                   // showToastCustomMessage("No response from tSMAS server...")
                    myButton?.text="No response from tSMAS server..."
                    myButton?.setBackgroundColor(Color.parseColor("#ff8d6d"))
                    successCode=false;
                }

            }

            override fun onFailure(call: Call<JsonObject>, t: Throwable) {
               // Log.e("MainActivity", "Error: ${t.message}")
                //handleError(requireContext(), t.message.toString())
                //showToastCustomMessage(t.message.toString())
                myButton?.text=t.message.toString()
                myButton?.setBackgroundColor(Color.parseColor("#ff8d6d"))
                successCode=false;
            }
        })
        return successCode;
    }

    private fun handleConfirmedBarcode(barcode: Barcode) {
        when {
            settings.saveScannedBarcodesToHistory || settings.continuousScanning -> saveScannedBarcode(barcode)
            else -> navigateToBarcodeScreen(barcode)
        }
    }

    private fun vibrateIfNeeded() {
        if (settings.vibrate) {
            requireActivity().apply {
                runOnUiThread {
                    applicationContext.vibrator?.vibrateOnce(vibrationPattern)
                }
            }
        }
    }

    private fun showScanConfirmationDialog(barcode: Barcode) {
        val dialog = ConfirmBarcodeDialogFragment.newInstance(barcode)
        dialog.show(childFragmentManager, "")
    }

    private fun saveScannedBarcode(barcode: Barcode) {
        barcodeDatabase.save(barcode, settings.doNotSaveDuplicates)
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe(
                { id ->
                    lastResult = barcode
                    when (settings.continuousScanning) {
                        true -> restartPreviewWithDelay(true)
                        else -> navigateToBarcodeScreen(barcode.copy(id = id))
                    }
                },
                ::showError
            )
            .addTo(disposable)
    }

    private fun restartPreviewWithDelay(showMessage: Boolean) {
        Completable
            .timer(CONTINUOUS_SCANNING_PREVIEW_DELAY, TimeUnit.MILLISECONDS)
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe {
                if (showMessage) {
                    showToast(R.string.fragment_scan_barcode_from_camera_barcode_saved)
                }
                restartPreview()
            }
            .addTo(disposable)
    }

    private fun restartPreview() {
        requireActivity().runOnUiThread {
            codeScanner.startPreview()
        }
    }

    private fun toggleFlash() {
        image_view_flash.isActivated = image_view_flash.isActivated.not()
        codeScanner.isFlashEnabled = codeScanner.isFlashEnabled.not()
    }

    private fun showToast(stringId: Int) {
        toast?.cancel()
        toast = Toast.makeText(requireActivity(), stringId, Toast.LENGTH_SHORT).apply {
            show()
        }
    }
   // private fun showToastCustomMessage(errorText: String) {
    //    Toast.makeText(requireContext(), errorText, Toast.LENGTH_LONG).show()
    //}

    private fun requestPermissions() {
        permissionsHelper.requestNotGrantedPermissions(requireActivity() as AppCompatActivity, PERMISSIONS, PERMISSION_REQUEST_CODE)
    }

    private fun areAllPermissionsGranted(): Boolean {
       return permissionsHelper.areAllPermissionsGranted(requireActivity(), PERMISSIONS)
    }

    private fun areAllPermissionsGranted(grantResults: IntArray): Boolean {
        return permissionsHelper.areAllPermissionsGranted(grantResults)
    }

    private fun navigateToScanFromFileScreen() {
        ScanBarcodeFromFileActivity.start(requireActivity())
    }

    private fun navigateToBarcodeScreen(barcode: Barcode) {
        BarcodeActivity.start(requireActivity(), barcode)
    }

    private fun finishWithResult(result: Result) {
        val intent = Intent()
            .putExtra("SCAN_RESULT", result.text)
            .putExtra("SCAN_RESULT_FORMAT", result.barcodeFormat.toString())

        if (result.rawBytes?.isNotEmpty() == true) {
            intent.putExtra("SCAN_RESULT_BYTES", result.rawBytes)
        }

        result.resultMetadata?.let { metadata ->
            metadata[ResultMetadataType.UPC_EAN_EXTENSION]?.let {
                intent.putExtra("SCAN_RESULT_ORIENTATION", it.toString())
            }

            metadata[ResultMetadataType.ERROR_CORRECTION_LEVEL]?.let {
                intent.putExtra("SCAN_RESULT_ERROR_CORRECTION_LEVEL", it.toString())
            }

            metadata[ResultMetadataType.UPC_EAN_EXTENSION]?.let {
                intent.putExtra("SCAN_RESULT_UPC_EAN_EXTENSION", it.toString())
            }

            metadata[ResultMetadataType.BYTE_SEGMENTS]?.let {
                var i = 0
                @Suppress("UNCHECKED_CAST")
                for (seg in it as Iterable<ByteArray>) {
                    intent.putExtra("SCAN_RESULT_BYTE_SEGMENTS_$i", seg)
                    ++i
                }
            }
        }

        requireActivity().apply {
            setResult(Activity.RESULT_OK, intent)
            finish()
        }
    }
}