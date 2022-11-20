package com.example.studentapp

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.google.android.material.button.MaterialButton

class MenuProfessor : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_menu_professor)

        val httpResponse = intent.getStringExtra(EXTRA_MESSAGE)
        val qrCodeBtn = findViewById<MaterialButton>(R.id.qrCodeBtn)
        val listBtn = findViewById<MaterialButton>(R.id.ListBtn)
        val logoutBtn = findViewById<MaterialButton>(R.id.logout_btn)

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
}