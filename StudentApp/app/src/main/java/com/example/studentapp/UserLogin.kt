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
import com.example.studentapp.UserLogin.PreferenceHelper.customPreference
import com.example.studentapp.UserLogin.PreferenceHelper.userId
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

const val EXTRA_MESSAGE = "com.example.myfirstapp.MESSAGE"


class UserLogin : AppCompatActivity() {

    val CUSTOM_PREF_NAME = "User_data"

    private var responseHttp = ""
    override fun onCreate(savedInstanceState: Bundle?) {

        val prefs = customPreference(this, CUSTOM_PREF_NAME)

        super.onCreate(savedInstanceState)
        setContentView(R.layout.user_login_activity)

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
                sleep(1000)
            }while (responseHttp == "")
            val json = JSONObject(responseHttp)
            if(responseHttp != "{\"erro\":\"Email ou Senha incorretos.\"}"){
                //correct

                prefs.userId = usernameValue
                val intent = Intent(this, QRCodeReader::class.java).apply {
                    putExtra(EXTRA_MESSAGE, responseHttp)
                }
                startActivity(intent)
            }else{
                //incorrect

            }
        }
    }

    object PreferenceHelper {

        val USER_ID = "USER_ID"

        fun customPreference(context: Context, name: String): SharedPreferences = context.getSharedPreferences(name, Context.MODE_PRIVATE)

        inline fun SharedPreferences.editMe(operation: (SharedPreferences.Editor) -> Unit) {
            val editMe = edit()
            operation(editMe)
            editMe.apply()
        }

        var SharedPreferences.userId
            get() = getString(USER_ID, "")
            set(value) {
                editMe {
                    it.putString(USER_ID, value)
                }
            }

        var SharedPreferences.clearValues
            get() = { }
            set(value) {
                editMe {
                    it.clear()
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