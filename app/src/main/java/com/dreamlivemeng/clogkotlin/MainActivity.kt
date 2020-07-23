package com.dreamlivemeng.clogkotlin

import android.os.Bundle
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        var tvConfirm:TextView = findViewById(R.id.tv_confirm)
        tvConfirm.setOnClickListener {
            var int = 1 / 0
        }
    }
}