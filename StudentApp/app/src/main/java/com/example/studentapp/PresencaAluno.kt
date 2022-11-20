package com.example.studentapp

import android.content.Intent
import android.os.Bundle
import android.widget.ListView
import androidx.appcompat.app.AppCompatActivity
import com.example.studentapp.databinding.ActivityMainBinding
import com.example.studentapp.databinding.ActivityPresencaAlunoBinding
import kotlinx.android.synthetic.main.activity_presenca_aluno.view.*

class PresencaAluno : AppCompatActivity() {

    private lateinit var binding: ActivityPresencaAlunoBinding
    private lateinit var userArrayList: ArrayList<StudantProfile>



    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityPresencaAlunoBinding.inflate(layoutInflater)
        setContentView(binding.root)


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
}