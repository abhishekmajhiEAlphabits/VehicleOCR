package com.example.vehicleocr

import android.annotation.SuppressLint
import android.content.Intent
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.ImageView
import android.widget.TextView

class DetailActivity : AppCompatActivity() {

    lateinit var detailsTxtView: TextView
    lateinit var numberImg: ImageView

    @SuppressLint("MissingInflatedId")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_detail)
        detailsTxtView = findViewById(R.id.detailsTxtView)
        numberImg = findViewById(R.id.numberImg)


        val data = intent.getStringExtra("DATA")
        val image = intent.getStringExtra("IMAGE")
        val uri = Uri.parse(image)
        detailsTxtView.text = data
        numberImg.setImageURI(uri)
    }
}