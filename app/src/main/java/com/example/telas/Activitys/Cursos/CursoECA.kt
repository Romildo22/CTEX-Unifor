package com.example.telas.Activitys.Cursos

import android.content.Intent
import android.os.Bundle
import android.support.v7.widget.Toolbar
import android.widget.TextView
import com.example.telas.R
import com.example.telas.Activitys.BaseActivity


class CursoECA : BaseActivity() {

    private lateinit var Insc: TextView
    private lateinit var Prof: TextView
    private lateinit var Invest: TextView
    private lateinit var Matriz: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_curso_eca)
        //setando a toolbar e colocando o nome
        val toolbar = findViewById(R.id.toolbar) as Toolbar
        toolbar.title = "Feira das Profissões 2019"
        toolbar.titleMarginStart = 45
        setSupportActionBar(toolbar)
        supportActionBar!!.setDisplayHomeAsUpEnabled(true)

        Insc = findViewById(R.id.txtInsc) as TextView

        Insc.setOnClickListener {
            val intent = Intent(this@CursoECA, WebView::class.java)
            intent.putExtra("valor", "3")
            startActivity(intent)
        }

    }
}
