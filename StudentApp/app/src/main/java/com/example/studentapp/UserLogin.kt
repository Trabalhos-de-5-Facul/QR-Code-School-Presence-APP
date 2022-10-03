package com.example.studentapp

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.TextView
import android.view.View
import android.widget.EditText
import com.google.android.material.button.MaterialButton
const val EXTRA_MESSAGE = "com.example.myfirstapp.MESSAGE"

class UserLogin : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.user_login_activity)

        val username = findViewById<TextView>(R.id.username)
        val password = findViewById<TextView>(R.id.password)
        val loginBtn = findViewById<MaterialButton>(R.id.login_btn)

        //Admin Admin

        loginBtn.setOnClickListener {
            //your implementation goes here
            if(username.text.toString() == "Admin" && password.text.toString() == "Admin"){
                //correct
                val message = "{" + username.text.toString() + "," + password.text.toString() + "}"
                val intent = Intent(this, QRCodeReader::class.java).apply {
                    putExtra(EXTRA_MESSAGE, message)
                }
                startActivity(intent)
            }else{
                //incorrect

            }
        }
    }
}