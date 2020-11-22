package com.example.zesty.activities

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Button
import android.widget.Toast
import com.example.zesty.R

class OrderPlaced : AppCompatActivity() {

    lateinit var btnOk:Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_order_placed)

        btnOk = findViewById(R.id.btnOk)

        btnOk.setOnClickListener {
            startActivity(Intent(this@OrderPlaced,MainActivity::class.java))
            finishAffinity()
        }
    }

    override fun onBackPressed() {
        Toast.makeText(this,"Please click OK",Toast.LENGTH_SHORT).show()
    }
}