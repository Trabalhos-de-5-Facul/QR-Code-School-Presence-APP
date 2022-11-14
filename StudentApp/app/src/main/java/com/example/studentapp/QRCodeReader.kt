package com.example.studentapp

import android.Manifest
import android.annotation.SuppressLint
import android.app.Activity
import android.app.ProgressDialog
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothSocket
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.AsyncTask
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.system.Os.socket
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.budiyev.android.codescanner.*
import io.ktor.utils.io.errors.*
import java.io.InputStream
import java.io.OutputStream
import java.util.*


class QRCodeReader : AppCompatActivity() {
    private lateinit var codeScanner: CodeScanner
    var m_bluetoothAdapter: BluetoothAdapter? = null
    private lateinit var m_pairedDevices: Set<BluetoothDevice>
    val REQUEST_ENABLE_BLUETOOTH = 1;
    companion object{
        val m_myUUID: UUID = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB")
        var m_bluetoothSocket: BluetoothSocket? = null
        lateinit var m_progress: ProgressDialog
        lateinit var m_bluetoothAdaptor: BluetoothAdapter
        var m_isConnected: Boolean = false
        lateinit var m_address: String
        var m_OutputStream: OutputStream? = null
        var m_InputStream: InputStream? = null
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_qrcode_reader)
        val httpResponse = intent.getStringExtra(EXTRA_MESSAGE)

        m_bluetoothAdapter = BluetoothAdapter.getDefaultAdapter()
        if(m_bluetoothAdapter == null){
            Toast.makeText(this, "this device does not support bluetooth", Toast.LENGTH_SHORT).show()
            return
        }
        if(!m_bluetoothAdapter!!.isEnabled){
            val enableBluetoothIntent = Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE)
            startActivityForResult(enableBluetoothIntent, REQUEST_ENABLE_BLUETOOTH)
        }

        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) ==
            PackageManager.PERMISSION_DENIED){
            ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.CAMERA), 123)
        }else{

            pairedDeviceList()
            startScanning()

        }
    }

    private fun startScanning() {
        val scannerView = findViewById<CodeScannerView>(R.id.scanner_view)

        codeScanner = CodeScanner(this, scannerView)

        // Parameters (default values)
        codeScanner.camera = CodeScanner.CAMERA_BACK // or CAMERA_FRONT or specific camera id
        codeScanner.formats = CodeScanner.ALL_FORMATS // list of type BarcodeFormat,
        // ex. listOf(BarcodeFormat.QR_CODE)
        codeScanner.autoFocusMode = AutoFocusMode.SAFE // or CONTINUOUS
        codeScanner.scanMode = ScanMode.SINGLE // or CONTINUOUS or PREVIEW
        codeScanner.isAutoFocusEnabled = true // Whether to enable auto focus or not
        codeScanner.isFlashEnabled = false // Whether to enable flash or not

        // Callbacks
        codeScanner.decodeCallback = DecodeCallback {
            runOnUiThread {
                Toast.makeText(this, "Scan result: ${it.text}", Toast.LENGTH_LONG).show()
            }
        }
        codeScanner.errorCallback = ErrorCallback { // or ErrorCallback.SUPPRESS
            runOnUiThread {
                Toast.makeText(this, "Camera initialization error: ${it.message}",
                    Toast.LENGTH_LONG).show()
            }
        }

        scannerView.setOnClickListener {
            codeScanner.startPreview()
        }
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray){
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == 123){
            if (grantResults[0] == PackageManager.PERMISSION_GRANTED){
                Toast.makeText(this, "Camera permission granted", Toast.LENGTH_SHORT).show()
                startScanning()
            }else{
                Toast.makeText(this, "Camera permission denied", Toast.LENGTH_SHORT).show()
            }
        }
    }

    override fun onResume() {
        super.onResume()
        codeScanner.startPreview()
    }

    override fun onPause() {
        codeScanner.releaseResources()
        super.onPause()
    }

    private fun pairedDeviceList(){
        if (ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.BLUETOOTH_CONNECT
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.BLUETOOTH_CONNECT), 0)
        }
        m_pairedDevices = m_bluetoothAdapter!!.bondedDevices
        val list : ArrayList<BluetoothDevice> = ArrayList()


        var targetDevice : BluetoothDevice? = null
        if(!m_pairedDevices.isEmpty()){
            for(device : BluetoothDevice in m_pairedDevices){
                if(device.address == "98:D3:31:FD:5E:D5"){
                    targetDevice = device
                }
            }
            if(targetDevice != null){
                m_address = targetDevice.address
                ConnectToDevice(this).execute()
            }else{
                Toast.makeText(this, "Device not found", Toast.LENGTH_SHORT).show()
            }

        }else{
            Toast.makeText(this, "No device found", Toast.LENGTH_SHORT).show()
        }
    }

    private class ConnectToDevice(c:Context) : AsyncTask<Void, Void, String>(){
        var workerThread: Thread? = null
        lateinit var readBuffer: ByteArray
        var readBufferPosition = 0
        var isRetrievingFile : Boolean = false;

        @Volatile
        var stopWorker = false


        private var connectSucess: Boolean = true
        private val context:Context

        init{
            this.context = c
        }

        override  fun onPreExecute(){
            super.onPreExecute()
            m_progress = ProgressDialog.show(context, "Connecting", "Please Wait")
        }

        @SuppressLint("MissingPermission")
        override fun doInBackground(vararg params: Void?): String {
            try{
                if(m_bluetoothSocket == null || !m_isConnected){
                    m_bluetoothAdaptor = BluetoothAdapter.getDefaultAdapter()
                    val device: BluetoothDevice = m_bluetoothAdaptor.getRemoteDevice(m_address)
                    m_bluetoothSocket = device!!.createRfcommSocketToServiceRecord(m_myUUID)
                    BluetoothAdapter.getDefaultAdapter().cancelDiscovery()
                    m_bluetoothSocket?.connect()

                }
            }catch (e: IOException){
                connectSucess = false
                e.printStackTrace()
            }
            return ""
        }

        override fun onPostExecute(result: String?){
            super.onPostExecute(result)
            if(!connectSucess){
                Log.i("data", "couldnt connect")
            }else {
                m_isConnected = true

                m_OutputStream = m_bluetoothSocket?.getOutputStream()
                m_InputStream = m_bluetoothSocket?.getInputStream()
                beginListenForData()
            }
            m_progress.dismiss()
        }

        fun beginListenForData(){
            //val handler : Handler//= Handler()
            // Changed following line from 10 (/n newline) to CR (13) to
            val delimiter: Byte = 13 //This is the ASCII code for a newline character

            stopWorker = false
            readBufferPosition = 0
            // only need read buffer to be larger if you're retrieving a file
            readBuffer = ByteArray(50000)
            //readBuffer = ByteArray(1024)
            workerThread = Thread {
                while (!Thread.currentThread().isInterrupted && !stopWorker) {
                    try {
                        val bytesAvailable: Int? = m_InputStream?.available()
                        if (bytesAvailable!! > 0) {
                            Log.i("FirstFrag", "bytesAvail : " + bytesAvailable.toString())
                            val packetBytes = ByteArray(bytesAvailable)
                            val myReader = m_InputStream?.reader()
                            val charArray = CharArray(50)
                            m_InputStream?.read(packetBytes)
                            myReader?.read(charArray)

                            for (i in 0 until bytesAvailable) {
                                val b = packetBytes[i]
                                //Log.i("FirstFrag", "b :" + b.toString())
                                if (b == delimiter) {
                                    val encodedBytes = ByteArray(readBufferPosition)
                                    System.arraycopy(
                                        readBuffer,
                                        0,
                                        encodedBytes,
                                        0,
                                        encodedBytes.size
                                    )
                                    val data = encodedBytes.toString(Charsets.US_ASCII)//  String(encodedBytes, "US-ASCII")
                                    readBufferPosition = 0
                                    if (isRetrievingFile) {
                                        Handler(Looper.getMainLooper()).postDelayed({
                                            Log.i("FirstFrag", "Calling writeFile...")
                                        }, 10)
                                    }
                                    else{
                                        Handler(Looper.getMainLooper()).postDelayed({
                                        }, 10)
                                    }
                                    //handler.post( {  })
                                    Log.i("FirstFrag", data)
                                } else {
                                    readBuffer[readBufferPosition++] = b
                                }
                            }
                        }
                    } catch (ex: IOException) {
                        Log.i("FirstFrag", ex.message.toString())
                        stopWorker = true
                    }
                }
            }
            workerThread!!.start()
        }


    }



    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if(requestCode == REQUEST_ENABLE_BLUETOOTH){
            if(resultCode == Activity.RESULT_OK){
                if(m_bluetoothAdapter!!.isEnabled){
                    Toast.makeText(this, "Bluetooth has been enabled", Toast.LENGTH_SHORT).show()
                }else{
                    Toast.makeText(this, "Bluetooth has been disabled", Toast.LENGTH_SHORT).show()
                }
            } else if(resultCode == Activity.RESULT_CANCELED){
                Toast.makeText(this, "Bluetooth enabling has been canceled", Toast.LENGTH_SHORT).show()
            }
        }
    }
}