package com.example.telas.Activitys

import android.content.Intent
import android.support.v4.view.ViewPager
import android.os.Bundle
import com.example.telas.Fragments.CursosFragment
import com.example.telas.Fragments.InicioFragment
import com.example.telas.R
import com.ogaclejapan.smarttablayout.SmartTabLayout
import com.ogaclejapan.smarttablayout.utils.v4.FragmentPagerItemAdapter
import com.ogaclejapan.smarttablayout.utils.v4.FragmentPagerItems
import android.view.View
import android.widget.Toast
import com.example.telas.Activitys.Cursos.CursoADS
import com.example.telas.Activitys.Cursos.CursoCC
import com.example.telas.Activitys.Cursos.CursoEC
import com.example.telas.Activitys.Cursos.CursoECA
import com.example.telas.Sorteio.PlayerActivity
import com.example.telas.Sorteio.Sorteio

class MainActivity : BaseActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        //setando a toolbar e colocando o nome
        setUpToolbar()
        setupNavDrawer()

        //Configurar abas
        val adapter = FragmentPagerItemAdapter(
                supportFragmentManager,
                FragmentPagerItems.with(this)
                        .add(R.string.aba1, InicioFragment::class.java!!)
                        .add(R.string.aba2, CursosFragment::class.java!!)
                        .create()
        )
        val viewPager = findViewById<ViewPager>(R.id.viewPager)
        viewPager.adapter = adapter

        val viewPagerTab = findViewById<SmartTabLayout>(R.id.viewPagerTab)
        viewPagerTab.setViewPager(viewPager)
    }

    //Funções para iniciar as activitys
    fun ActivityCC(v: View) {
        val intent = Intent(this@MainActivity, CursoCC::class.java)
        startActivity(intent)
    }
    fun ActivityADS(v: View) {
        val intent = Intent(this@MainActivity, CursoADS::class.java)
        startActivity(intent)
    }
    fun ActivityEC(v: View) {
        val intent = Intent(this@MainActivity, CursoEC::class.java)
        startActivity(intent)
    }
    fun ActivityECA(v: View) {
        val intent = Intent(this@MainActivity, CursoECA::class.java)
        startActivity(intent)
    }
    fun ActivitySoteio(v: View) {
        val intent = Intent(this@MainActivity, Sorteio::class.java)
        startActivity(intent)
    }

    fun PlayerActivity(v: View) {
        val intent = Intent(this@MainActivity, PlayerActivity::class.java)
        startActivity(intent)
    }
}
