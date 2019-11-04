package com.example.telas.Sorteio

import android.content.Intent
import android.support.v4.view.ViewPager
import android.os.Bundle
import android.support.v7.widget.Toolbar
import android.view.View
import com.example.sorteio.Activity.Fragment.Passo1Fragment
import com.example.sorteio.Activity.Fragment.Passo2Fragment
import com.example.telas.Activitys.BaseActivity
import com.example.telas.R
import com.ogaclejapan.smarttablayout.SmartTabLayout
import com.ogaclejapan.smarttablayout.utils.v4.FragmentPagerItemAdapter
import com.ogaclejapan.smarttablayout.utils.v4.FragmentPagerItems

class Sorteio : BaseActivity() {

    private var viewPager: ViewPager? = null
    private var smartTabLayout: SmartTabLayout? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_sorteio)

        viewPager = findViewById(R.id.viewPager)
        smartTabLayout = findViewById(R.id.viewPagerTab)

        //setando a toolbar e colocando o nome
        val toolbar = findViewById(R.id.toolbar) as Toolbar
        toolbar.title = "Feira das Profissões 2019"
        toolbar.titleMarginStart = 45
        setSupportActionBar(toolbar)
        supportActionBar!!.setDisplayHomeAsUpEnabled(true)

        //Configurando abas
        val adapter = FragmentPagerItemAdapter(
                supportFragmentManager,
                FragmentPagerItems.with(this)
                        .add("Tutorial", Passo1Fragment::class.java!!)
                        .add("Câmera", Passo2Fragment::class.java!!)
                        .create()
        )
        viewPager!!.adapter = adapter
        smartTabLayout!!.setViewPager(viewPager)
    }
    fun cameraSort(View: View){
        startActivity(Intent(this, Camera::class.java))
    }

    fun abrirVideo(View: View) {
        startActivity(Intent(this, PlayerActivity::class.java))
    }
}
