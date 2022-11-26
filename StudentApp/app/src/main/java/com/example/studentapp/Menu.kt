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
import java.sql.Types.NULL

class Menu : AppCompatActivity() {
    private var aulaResponse = "";

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_menu2)
        val httpResponse = intent.getStringExtra(EXTRA_MESSAGE)
        var json = JSONObject(httpResponse)
        var raAluno = json["ra"]

        aulaResponse = ""
        GlobalScope.async{
            getLoginResponse(
                "http://54.94.139.104:3000/alunos/info/"+raAluno
            )
        }

        do{
            Thread.sleep(100)
        }while (aulaResponse == "")

        json = JSONObject(aulaResponse);

        val textWellcome = findViewById<TextView>(R.id.textWc)
        textWellcome.text = "Bem Vindo, "+(((json["aluno"] as JSONArray).get(0) as JSONObject)["nome_aluno"] as String)


        aulaResponse = ""
        GlobalScope.async{
            getLoginResponse(
                "http://54.94.139.104:3000/alunos/"+raAluno
            )
        }

        do{
            Thread.sleep(100)
        }while (aulaResponse == "")

        json = JSONObject(aulaResponse);

        val qrBtn = findViewById<MaterialButton>(R.id.qrCodeBtn)
        val logoutBtn = findViewById<MaterialButton>(R.id.logout_btn)
        val arrayList = ArrayList<Model>()


        if((json["quantidade"] as Int) > 0) {

            arrayList.add(Model(((json["aula"] as JSONArray).get(0) as JSONObject)["nome_disc"] as String))

            val MyAdapter = MyAdapter(arrayList, this)

            recyclerView.layoutManager = LinearLayoutManager(this)
            recyclerView.adapter = MyAdapter

            aulaResponse = ""
            GlobalScope.async{
                getLoginResponse(
                    "http://54.94.139.104:3000/frequenta/aula/"+raAluno+"/"+(((json["aula"] as JSONArray).get(0) as JSONObject)["COD_AULA"].toString())
                )
            }

            do{
                Thread.sleep(100)
            }while (aulaResponse == "")

            json = JSONObject(aulaResponse);

            qrBtn.isEnabled = (((json["frequencia"] as JSONArray).get(0) as JSONObject)["presenca_aluno"] as Number) == 0
            qrBtn.isClickable = (((json["frequencia"] as JSONArray).get(0) as JSONObject)["presenca_aluno"] as Number) == 0

        }
        else{
            arrayList.add(Model("Não há aulas no momento :)"))

            val MyAdapter = MyAdapter(arrayList, this)

            recyclerView.layoutManager = LinearLayoutManager(this)
            recyclerView.adapter = MyAdapter

            qrBtn.isEnabled = false
            qrBtn.isClickable = false

        }

        qrBtn.setOnClickListener{
            val intent = Intent(this, QRCodeReader::class.java).apply {
                putExtra(EXTRA_SECOND_MESSAGE, httpResponse)
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