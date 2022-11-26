package com.example.studentapp

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.TextView
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.material.button.MaterialButton
import io.ktor.client.*
import io.ktor.client.engine.cio.*
import io.ktor.client.plugins.json.*
import io.ktor.client.plugins.kotlinx.serializer.*
import io.ktor.client.request.*
import io.ktor.client.statement.*
import kotlinx.android.synthetic.main.activity_menu_professor.*
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.async
import org.json.JSONArray
import org.json.JSONObject

class MenuProfessor : AppCompatActivity() {

    private var profResponse = "";

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_menu_professor)
        val httpResponse = intent.getStringExtra(EXTRA_MESSAGE)
        var json = JSONObject(httpResponse)
        var codProf = json["cod"]

        profResponse = ""
        GlobalScope.async{
            getLoginResponse(
                "http://54.94.139.104:3000/professores/"+codProf
            )
        }

        do{
            Thread.sleep(100)
        }while (profResponse == "")

        json = JSONObject(profResponse);

        val textWellcome = findViewById<TextView>(R.id.textWc)
        textWellcome.text = "Bem Vindo, "+(((json["professor"] as JSONArray).get(0) as JSONObject)["nome_prof"] as String)

        profResponse = ""
        GlobalScope.async{
            getLoginResponse(
                "http://54.94.139.104:3000/professores/aula/"+codProf
            )
        }

        do{
            Thread.sleep(100)
        }while (profResponse == "")

        json = JSONObject(profResponse);

        val qrCodeBtn = findViewById<MaterialButton>(R.id.qrCodeBtn)
        val listBtn = findViewById<MaterialButton>(R.id.ListBtn)
        val logoutBtn = findViewById<MaterialButton>(R.id.logout_btn)

        val arrayList = ArrayList<Model>()

        if((json["quantidade"] as Int) > 0) {
            arrayList.add(Model(((json["aula"] as JSONArray).get(0) as JSONObject)["nome_disc"] as String))

            val MyAdapter = MyAdapter(arrayList, this)

            recyclerView.layoutManager = LinearLayoutManager(this)
            recyclerView.adapter = MyAdapter

            qrCodeBtn.isClickable = true
            qrCodeBtn.isEnabled = true
            listBtn.isClickable = true
            listBtn.isEnabled = true
        }
        else{
            arrayList.add(Model("Não há aulas no momento :("))

            val MyAdapter = MyAdapter(arrayList, this)

            recyclerView.layoutManager = LinearLayoutManager(this)
            recyclerView.adapter = MyAdapter

            qrCodeBtn.isClickable = false
            qrCodeBtn.isEnabled = false
            listBtn.isClickable = false
            listBtn.isEnabled = false
        }


        qrCodeBtn.setOnClickListener{
            val intent = Intent(this, QrCodeLocal::class.java).apply {
                putExtra(EXTRA_MESSAGE, httpResponse)
            }
            startActivity(intent)
        }

        listBtn.setOnClickListener{
            val intent = Intent(this, PresencaAluno::class.java).apply {
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
        profResponse = response.bodyAsText()
        client.close()

    }
}