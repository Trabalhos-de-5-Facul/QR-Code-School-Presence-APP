package com.example.studentapp

import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.preference.PreferenceManager
import android.util.Log
import android.view.View
import android.widget.TextView
import com.google.android.material.button.MaterialButton
import io.ktor.client.*
import io.ktor.client.call.*
import io.ktor.client.engine.cio.*
import io.ktor.client.plugins.kotlinx.serializer.*
import io.ktor.client.plugins.json.*
import io.ktor.client.request.*
import io.ktor.client.statement.*
import kotlinx.coroutines.CoroutineStart
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.async
import kotlinx.coroutines.launch
import org.json.JSONObject
import java.lang.Thread.sleep
import java.net.URL

const val EXTRA_MESSAGE = "httpResponse"


class UserLogin : AppCompatActivity() {

    val CUSTOM_PREF_NAME = "User_data"

    private var responseHttp = ""
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.user_login_activity)

        val preferences = getSharedPreferences ("PREFERENCE", MODE_PRIVATE);
        val firstTime = preferences.getString("LoginInfo", "FirstTime");
        if(!firstTime.equals("FirstTime")){
            val intent = Intent(this, Menu::class.java).apply {
                putExtra(EXTRA_MESSAGE, firstTime)
            }
            startActivity(intent)
        }

        val username = findViewById<TextView>(R.id.username)
        val password = findViewById<TextView>(R.id.password)
        val loginBtn = findViewById<MaterialButton>(R.id.login_btn)
        //Admin Admin

        loginBtn.setOnClickListener {
            val usernameValue = username.text.toString();
            val passwordValue = password.text.toString();
            responseHttp = ""
            GlobalScope.async{
                getLoginResponse(
                "http://54.94.139.104:3000/alunos/$passwordValue/$usernameValue"
                )
            }

            do{
                sleep(100)
            }while (responseHttp == "")
            val json = JSONObject(responseHttp)
            if(responseHttp != "{\"erro\":\"Email ou Senha incorretos.\"}"){
                //correct
                val editor = preferences.edit();
                editor.putString("LoginInfo",responseHttp);
                editor.apply();
                val intent = Intent(this, Menu::class.java).apply {
                    putExtra(EXTRA_MESSAGE, responseHttp)
                }
                startActivity(intent)
            }else{
                responseHttp = ""
                GlobalScope.async{
                    getLoginResponse(
                        "http://54.94.139.104:3000/professores/$passwordValue/$usernameValue"
                    )
                }

                do{
                    sleep(100)
                }while (responseHttp == "")
                if(responseHttp != "{\"erro\":\"Email ou Senha incorretos.\"}"){
                    //correct
                    val editor = preferences.edit();
                    editor.putString("LoginInfo",responseHttp);
                    editor.apply();
                    val intent = Intent(this, UserLogin::class.java).apply {
                        putExtra(EXTRA_MESSAGE, responseHttp)
                    }
                    startActivity(intent)
                }else {

                }
            }
        }
    }

    private suspend fun getLoginResponse(httpString: String) {
        val client = HttpClient(CIO) {
            install(JsonPlugin) {
                serializer = KotlinxSerializer()
            }
        }
        val response: HttpResponse = client.get(httpString)
        responseHttp = response.bodyAsText()
        client.close()
    }
}