package com.example.studentapp

import android.content.Intent
import android.graphics.Bitmap
import android.graphics.Point
import android.os.Bundle
import android.text.TextUtils
import android.view.Display
import android.view.WindowManager
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.Toast
import androidmads.library.qrgenearator.QRGContents
import androidmads.library.qrgenearator.QRGEncoder
import androidx.appcompat.app.AppCompatActivity
import com.example.studentapp.EXTRA_MESSAGE
import com.example.studentapp.R
import io.ktor.client.*
import io.ktor.client.engine.cio.*
import io.ktor.client.plugins.json.*
import io.ktor.client.plugins.kotlinx.serializer.*
import io.ktor.client.request.*
import io.ktor.client.statement.*
import kotlinx.android.synthetic.main.activity_qr_code_local.*
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.async
import org.json.JSONObject
import org.json.JSONArray


class QrCodeLocal : AppCompatActivity() {

    // on below line we are creating a variable
    // for our image view, edit text and a button.
    lateinit var qrIV: ImageView
    lateinit var BackBtn: Button

    // on below line we are creating
    // a variable for bitmap
    lateinit var bitmap: Bitmap

    // on below line we are creating
    // a variable for qr encoder.
    lateinit var qrEncoder: QRGEncoder

    private var QrHttp = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_qr_code_local)
        val httpResponse = intent.getStringExtra(EXTRA_MESSAGE)
        var json = JSONObject(httpResponse)
        QrHttp = ""
        GlobalScope.async{
            getQrCodeResponse(
                "http://54.94.139.104:3000/professores/aula/"+json["cod"]

            )
        }

        do{
            Thread.sleep(100)
        }while (QrHttp == "")
        json = JSONObject(QrHttp)

        // on below line we are
        // initializing our all variables.
        qrIV = findViewById(R.id.idIVQrcode)
        BackBtn = findViewById(R.id.BackBtn)

        // on below line we are checking if msg edit text is empty or not.

        // on below line we are getting service for window manager
        val windowManager: WindowManager = getSystemService(WINDOW_SERVICE) as WindowManager

        // on below line we are initializing a
        // variable for our default display
        val display: Display = windowManager.defaultDisplay

        // on below line we are creating a variable
        // for point which is use to display in qr code
        val point: Point = Point()
        display.getSize(point)

        // on below line we are getting
        // height and width of our point
        val width = point.x
        val height = point.y

        // on below line we are generating
        // dimensions for width and height
        var dimen = if (width < height) width else height
        dimen = dimen * 3 / 4

            // on below line we are initializing our qr encoder
            qrEncoder = QRGEncoder((((json["aula"] as JSONArray).get(0).toString())), null, QRGContents.Type.TEXT, dimen)

            // on below line we are running a try
            // and catch block for initialing our bitmap

            try {
                // on below line we are
                // initializing our bitmap
                bitmap = qrEncoder.getBitmap(0)

                // on below line we are setting
                // this bitmap to our image view
                qrIV.setImageBitmap(bitmap)
            } catch (e: Exception) {
                // on below line we
                // are handling exception
                e.printStackTrace()
            }


        // on below line we are adding on click
        // listener for our generate QR button.
        BackBtn.setOnClickListener {
            val intent = Intent(this, MenuProfessor::class.java).apply {
                putExtra(EXTRA_MESSAGE, httpResponse)
            }
            startActivity(intent)
        }
    }

    private suspend fun getQrCodeResponse(httpString: String) {
        val client = HttpClient(CIO) {
            install(JsonPlugin) {
                serializer = KotlinxSerializer()
            }
        }
        val response: HttpResponse = client.get(httpString)
        QrHttp = response.bodyAsText()
        client.close()
    }
}