package com.example.telas.Activitys

import android.content.Intent
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.Toast


import com.example.telas.Helper.ConfiguracaoFirebase
import com.example.telas.Model.Usuario
import com.google.firebase.auth.FirebaseAuth
import com.example.telas.MenuLateral.Mapa.Mapa

import android.R
import com.example.telas.Fragments.InicioFragment
import com.google.firebase.auth.FirebaseUser
import java.security.AccessController.getContext


class LoginActivity : AppCompatActivity() {

    private var campoEmail: EditText? = null
    private var campoSenha: EditText? = null
    private var botaoEntrar: Button? = null
    private var progressBar: ProgressBar? = null

    private var usuario: Usuario? = null
    private var autenticacao: FirebaseAuth? = null
    private var textEsqueceuSenha: TextView? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(com.example.telas.R.layout.activity_login)

        inicializarComponentes()

        //Fazer login do usuario
        progressBar!!.visibility = View.GONE
        botaoEntrar!!.setOnClickListener {
            val textoEmail = campoEmail!!.text.toString()
            val textoSenha = campoSenha!!.text.toString()

            if (!textoEmail.isEmpty()) {
                if (!textoSenha.isEmpty()) {

                    usuario = Usuario()
                    usuario!!.email = textoEmail
                    usuario!!.senha = textoSenha
                    validarLogin(usuario)

                } else {
                    Toast.makeText(this@LoginActivity, "Preencha a senha!",
                            Toast.LENGTH_SHORT).show()
                }
            } else {
                Toast.makeText(this@LoginActivity, "Preencha o e-mail!",
                        Toast.LENGTH_SHORT).show()
            }
        }
    }

    override fun onStart(){
        super.onStart()
        verificarUsuarioLogado()
    }

    fun verificarUsuarioLogado() {
        autenticacao = ConfiguracaoFirebase.firebaseAutenticacao
        if (autenticacao!!.getCurrentUser() != null) {
            startActivity(Intent(applicationContext, MainActivity::class.java))
             finish()
        }
    }

    fun validarLogin(usuario: Usuario?) {

        progressBar!!.visibility = View.VISIBLE
        autenticacao = ConfiguracaoFirebase.firebaseAutenticacao

        autenticacao!!.signInWithEmailAndPassword(
                usuario!!.email!!,
                usuario.senha!!
        ).addOnCompleteListener { task ->
            if (task.isSuccessful) {
                progressBar!!.visibility = View.GONE
                startActivity(Intent(applicationContext, MainActivity::class.java))
                finish()
            } else {
                Toast.makeText(this@LoginActivity,
                        "Erro ao fazer login",
                        Toast.LENGTH_SHORT).show()
                progressBar!!.visibility = View.GONE

            }
        }
    }

    fun abrirReset(view: View) {
        val i = Intent(this@LoginActivity, ResetActivity::class.java)
        startActivity(i)
    }


    fun abrirCadastro(view: View) {
        val i = Intent(this@LoginActivity, CadastroActivity::class.java)
        startActivity(i)
    }


    fun inicializarComponentes() {
        campoEmail = findViewById(com.example.telas.R.id.editLoginEmail)
        campoSenha = findViewById(com.example.telas.R.id.editLoginSenha)
        botaoEntrar = findViewById(com.example.telas.R.id.buttonEntrar)
        progressBar = findViewById(com.example.telas.R.id.progressLogin)
        textEsqueceuSenha = findViewById(com.example.telas.R.id.textEsqueceuSenha)

        campoEmail!!.requestFocus()

    }

override fun onBackPressed() {
        finish()
    }

}
