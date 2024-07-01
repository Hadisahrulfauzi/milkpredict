package com.example.milkpredict

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Button
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.example.milkpredict.ui.MainMenuActivity

class MainActivity : AppCompatActivity(), View.OnClickListener {

    private lateinit var btnIntent : Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        btnIntent = findViewById(R.id.btnPindah)
        btnIntent.setOnClickListener(this)
    }

    override fun onClick(v: View?) {
        if (v != null) {
            when (v.id) {
                R.id.btnPindah -> run {
                    val intentBiasa = Intent(this@MainActivity, MainMenuActivity::class.java)
                    startActivity(intentBiasa)
                }
            }
        }
    }
}
