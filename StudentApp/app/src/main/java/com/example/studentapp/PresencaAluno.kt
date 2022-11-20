package com.example.studentapp

import android.content.Intent
import android.os.Bundle
import android.widget.ListView
import androidx.appcompat.app.AppCompatActivity
import com.example.studentapp.databinding.ActivityMainBinding
import com.example.studentapp.databinding.ActivityPresencaAlunoBinding
import io.ktor.client.*
import io.ktor.client.engine.cio.*
import io.ktor.client.plugins.json.*
import io.ktor.client.plugins.kotlinx.serializer.*
import io.ktor.client.request.*
import io.ktor.client.statement.*
import kotlinx.android.synthetic.main.activity_presenca_aluno.view.*
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.async
import org.json.JSONObject

class PresencaAluno : AppCompatActivity() {


    private lateinit var binding: ActivityPresencaAlunoBinding
    private lateinit var userArrayList: ArrayList<StudantProfile>
    private var presenceHttp = ""


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityPresencaAlunoBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val httpResponse = intent.getStringExtra(EXTRA_MESSAGE)
        var json = JSONObject(httpResponse)
        presenceHttp = ""
        GlobalScope.async{
            getLoginResponse(
                "http://54.94.139.104:3000/frequenta/"+json["cod"]
            )
        }

        do{
            Thread.sleep(100)
        }while (presenceHttp == "")
        json = JSONObject(presenceHttp)


        val names = arrayOf(

            "Anselmo",
            "Bruno"
        )

        userArrayList = ArrayList()

        for ( i in names.indices){

            val user = StudantProfile(names[i])
            userArrayList.add(user)

        }


        binding.listview.isClickable = true
        binding.listview.adapter = MyAdapter(this,userArrayList)
        binding.listview.setOnItemClickListener { parent, view, position, id ->

            val name = names[position]

            val i = Intent(this, MainActivity::class.java)

            i.putExtra("name",name)

            startActivity(i)

        }

    }

    private suspend fun getLoginResponse(httpString: String) {
        val client = HttpClient(CIO) {
            install(JsonPlugin) {
                serializer = KotlinxSerializer()
            }
        }
        val response: HttpResponse = client.get(httpString)
        presenceHttp = response.bodyAsText()
        client.close()
    }
}