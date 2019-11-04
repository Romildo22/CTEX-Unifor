package com.example.telas.Sorteio

import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.widget.MediaController
import android.widget.VideoView

import com.example.telas.R

class PlayerActivity : AppCompatActivity() {

    private var videoView: VideoView? = null


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_player)

        videoView = findViewById(R.id.videoView)

        //Esconder a statusBar e barra de navegação
        val decorView = window.decorView
        //Deixando a tela em FullScreen
        val uiOpcoes = View.SYSTEM_UI_FLAG_HIDE_NAVIGATION or View.SYSTEM_UI_FLAG_FULLSCREEN
        decorView.systemUiVisibility = uiOpcoes

        //Esconder a ActionBar
        //supportActionBar!!.hide()

        //Executar o vídeo
        videoView!!.setMediaController(MediaController(this))
        videoView!!.setVideoPath("android.resource://" + packageName + "/" + R.raw.video1)
        videoView!!.start()
    }
}
