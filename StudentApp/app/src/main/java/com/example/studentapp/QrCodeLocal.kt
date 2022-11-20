package com.example.studentapp

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle

class QrCodeLocal : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_qr_code_local)

        val httpResponse = intent.getStringExtra(EXTRA_MESSAGE)

    }
}