package com.example.telas.Model

import com.example.telas.Helper.ConfiguracaoFirebase
import com.google.firebase.database.Exclude


class Usuario {

    var id: String? = null
    var nome: String? = null
    var email: String? = null
    @get:Exclude
    var senha: String? = null
    var caminhoFoto: String? = null
    @get:Exclude
    var campoConfirma: String? = null
    var campoEscola: String? = null
    var campoTipoEscola: String? = null
    var campoCidade: String? = null



    fun salvar() {
        val firebaseRef = ConfiguracaoFirebase.firebase
        val usuariosRef = firebaseRef.child("usuarios").child(id!!)
        usuariosRef.setValue(this)
    }





}

