package com.example.studentapp

import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.BaseAdapter
import android.widget.Button
import android.widget.ListView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import io.ktor.client.*
import io.ktor.client.engine.cio.*
import io.ktor.client.plugins.json.*
import io.ktor.client.plugins.kotlinx.serializer.*
import io.ktor.client.request.*
import io.ktor.client.statement.*
import io.ktor.content.*
import io.ktor.http.*
import io.ktor.util.*
import kotlinx.android.synthetic.main.activity_presenca_aluno.view.*
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.async
import org.json.JSONArray
import org.json.JSONObject

class PresencaAluno : AppCompatActivity() {

    private var presenceHttp = ""
    private var makePresenceHttp = false

    data class Student(val name: String, val ra: Int, var presence : Boolean, val codAula: Int)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_presenca_aluno)
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


        val mylistview = findViewById<ListView>(R.id.listview);
        val btn = findViewById<Button>(R.id.btn);
        mylistview.adapter = StudentList(json, this);
        mylistview.isClickable = true

        btn.setOnClickListener{
            makePresenceHttp = false
            GlobalScope.async{
                val presentStudents : MutableList<Student> = mutableListOf()
                for (i in 0 until mylistview.count) {
                    if((mylistview.getItemAtPosition(i) as Student).presence){
                        presentStudents.add((mylistview.getItemAtPosition(i) as Student))
                    }
                }

                presenceResponse(presentStudents)
            }

            do{
                Thread.sleep(100)
            }while (!makePresenceHttp)

            val intent = Intent(this, MenuProfessor::class.java).apply {

            }
            startActivity(intent)
        }

        mylistview.setOnItemClickListener { parent, view, position, id ->
            val student = (mylistview.getItemAtPosition(position) as Student)
            if(student.presence) {
                view.background = resources.getDrawable(R.drawable.rounded_edittext_lightblue)
                student.presence = false;
            }else {
                view.background = resources.getDrawable(R.drawable.rounded_edittext_lightorange)
                student.presence = true;
            }

        }
    }

    private class StudentList(json: JSONObject, context: Context) : BaseAdapter() {
        private val mContext : Context;
        private val students : MutableList<Student> = mutableListOf()

        init{
            mContext = context;
            for (i in 0 until (json["quantidade"] as Int)) {
                students.add(
                    Student(
                        ((json["alunos"] as JSONArray).get(i) as JSONObject)["nome_aluno"] as String,
                        ((json["alunos"] as JSONArray).get(i) as JSONObject)["RA"] as Int,
                        false,
                        ((json["alunos"] as JSONArray).get(i) as JSONObject)["COD_AULA"] as Int
                    ))
            }
        }

        override fun getItem(position: Int): Any {
            return students[position];
        }



        override fun getItemId(position: Int): Long {
            return students[position].ra.toLong();
        }

        override fun getCount(): Int {
            return students.size;
        }

        override fun getView(position: Int, convertView: View?, parent: ViewGroup?): View {
            val layoutInflater = LayoutInflater.from(mContext)
            val rowMain = layoutInflater.inflate(R.layout.student_row, parent, false)
            val nomeTextView = rowMain.findViewById<TextView>(R.id.nome)
            nomeTextView.text = "Nome: "+ (getItem(position) as Student).name
            val raTextView = rowMain.findViewById<TextView>(R.id.ra)
            raTextView.text = "RA: " + (getItem(position) as Student).ra
            return rowMain
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

    @OptIn(InternalAPI::class)
    private suspend fun presenceResponse(students: List<Student>) {
        makePresenceHttp = false

        for (i in students.indices) {
            val client = HttpClient(CIO) {
                install(JsonPlugin) {
                    serializer = KotlinxSerializer()
                }
            }

            var patchBody = "1/"+students[i].ra.toString()+"/"+students[i].codAula.toString()
            val response: HttpResponse = client.get("http://54.94.139.104:3000/frequenta/$patchBody")
            client.close()

        }
        makePresenceHttp=true
    }
}