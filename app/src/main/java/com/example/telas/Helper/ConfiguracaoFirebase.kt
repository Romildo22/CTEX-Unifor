package com.example.telas.Helper

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase

object ConfiguracaoFirebase {

    private var referenciaFirebase: DatabaseReference? = null
    private var referenciaAutenticacao: FirebaseAuth? = null

    //retorna a referencia do database
    val firebase: DatabaseReference
        get() {
            if (referenciaFirebase == null) {
                referenciaFirebase = FirebaseDatabase.getInstance().reference
            }
            return this!!.referenciaFirebase!!

        }


    //retorna a instancia do FirebaseAuth
    val firebaseAutenticacao: FirebaseAuth
        get() {
            if (referenciaAutenticacao == null) {
                referenciaAutenticacao = FirebaseAuth.getInstance()
            }
            return this!!.referenciaAutenticacao!!
        }
}
