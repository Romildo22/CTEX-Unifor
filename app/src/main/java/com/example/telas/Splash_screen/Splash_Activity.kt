package com.example.telas.Splash_screen

import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.support.v7.app.AppCompatActivity
import android.view.View
import com.example.telas.MenuLateral.Mapa.Mapa
import com.example.telas.R
import android.view.View.SYSTEM_UI_FLAG_FULLSCREEN
import android.view.View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
import com.example.telas.Activitys.LoginActivity
import com.example.telas.Activitys.MainActivity


class Splash_Activity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_splash_)

        val decorView = window.decorView
            // Esconde tanto a barra de navegação e a barra de status .
        val uiOptions = View.SYSTEM_UI_FLAG_HIDE_NAVIGATION or View.SYSTEM_UI_FLAG_FULLSCREEN
        decorView.systemUiVisibility = uiOptions

        Handler().postDelayed({
            startActivities(arrayOf(Intent(baseContext, LoginActivity::class.java)))
            finish()
        }, 3000)

    }

}
