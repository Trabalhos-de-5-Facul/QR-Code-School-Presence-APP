package com.example.studentapp

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.TextView
import com.google.android.material.button.MaterialButton
import io.ktor.client.*
import io.ktor.client.engine.cio.*
import io.ktor.client.plugins.json.*
import io.ktor.client.plugins.kotlinx.serializer.*
import io.ktor.client.request.*
import io.ktor.client.statement.*
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.async
import org.json.JSONObject

class Menu : AppCompatActivity() {
    private var aulaResponse = "";

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_menu2)
        val httpResponse = intent.getStringExtra(EXTRA_MESSAGE)
        var json = JSONObject(httpResponse)
        aulaResponse = ""
        GlobalScope.async{
            getLoginResponse(
                "http://54.94.139.104:3000/alunos/"+json["ra"]
            )
        }

        do{
            Thread.sleep(100)
        }while (aulaResponse == "")

        json = JSONObject(aulaResponse);

        val qrBtn = findViewById<MaterialButton>(R.id.qrCodeBtn)
        val logoutBtn = findViewById<MaterialButton>(R.id.logout_btn)

        qrBtn.setOnClickListener{
            val intent = Intent(this, QRCodeReader::class.java).apply {
                putExtra(EXTRA_MESSAGE, httpResponse)
            }
            startActivity(intent)
        }

        logoutBtn.setOnClickListener{
            val preferences = getSharedPreferences ("PREFERENCE", MODE_PRIVATE);
            val editor = preferences.edit();
            editor.putString("LoginInfo","FirstTime");
            editor.apply();
            val intent = Intent(this, UserLogin::class.java).apply {
                putExtra(EXTRA_MESSAGE, httpResponse)
            }
            startActivity(intent)
        }
    }

    private suspend fun getLoginResponse(httpString: String) {
        val client = HttpClient(CIO) {
            install(JsonPlugin) {
                serializer = KotlinxSerializer()
            }
        }
        val response: HttpResponse = client.get(httpString)
        aulaResponse = response.bodyAsText()
        client.close()
    }
}