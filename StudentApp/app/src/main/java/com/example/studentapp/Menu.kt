package com.example.studentapp

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.TextView
import com.google.android.material.button.MaterialButton

class Menu : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_menu2)
        val httpResponse = intent.getStringExtra(EXTRA_MESSAGE)
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
}