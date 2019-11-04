package com.example.telas.Activitys

import android.content.Intent
import android.support.design.widget.NavigationView
import android.support.v4.view.GravityCompat
import android.support.v4.widget.DrawerLayout
import android.support.v7.widget.Toolbar
import android.view.MenuItem
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import com.example.telas.R
import com.example.telas.Activitys.Cursos.WebView
import com.example.telas.Helper.ConfiguracaoFirebase
import com.example.telas.MenuLateral.Mapa.Mapa
import com.example.telas.MenuLateral.Sobre.Sobre
import com.example.telas.Model.Usuario
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import kotlinx.android.synthetic.main.activity_sobre.*

open class BaseActivity : livroandroid.lib.activity.BaseActivity() {

    protected var drawerLayout: DrawerLayout? = null
    private var autenticacao: FirebaseAuth? = null
    private var usuario: FirebaseUser? = null

    private val permissoes = arrayOf(android.Manifest.permission.ACCESS_FINE_LOCATION)

    // Configura a Toolbar
    protected fun setUpToolbar() {
        val toolbar = findViewById(R.id.toolbar) as Toolbar
        if (toolbar != null) {
            toolbar.title = "Feira das Profissões 2019"
            toolbar.titleMarginStart = 40
            setSupportActionBar(toolbar)
        }
    }

    // Configura o nav drawer
    protected fun setupNavDrawer() {
        // Drawer Layout
        val actionBar = getSupportActionBar()

        // Ícone do menu do Nav Drawer
        actionBar!!.setHomeAsUpIndicator(R.drawable.ic_menu)
        actionBar.setDisplayHomeAsUpEnabled(true)

        drawerLayout = findViewById<DrawerLayout>(R.id.drawer_layout)

        val navigationView = findViewById<NavigationView>(R.id.nav_view)

        //Verificação do drawerLayout
        if (navigationView != null && drawerLayout != null) {
            // Atualiza a imagem e os textos do header
            setNavViewValues(navigationView, R.string.idNome, R.string.idEmail, R.drawable.icon_unifor)
            // Trata o evento de clique no menu
            navigationView.setNavigationItemSelectedListener { menuItem ->
                // Seleciona a linha
                //menuItem.setChecked(true);
                // Fecha o menu
                drawerLayout!!.closeDrawers()
                // Trata o evento do menu
                onNavDrawerItemSelected(menuItem)
                true
            }
        }
    }

    // Trata o evento do menu lateral
    private fun onNavDrawerItemSelected(menuItem: MenuItem) {
        when (menuItem.itemId) {

            R.id.btnSite -> {
                val intent = Intent(context, WebView::class.java)
                intent.putExtra("valor", "5")
                startActivity(intent)
            }
            R.id.btnMapaMenu -> {
                val intent = Intent(context, Mapa::class.java)
                //intent.putExtra("valor", "5")
                startActivity(intent)
            }
            R.id.btnSobre -> {
                val intent = Intent(context, Sobre::class.java)
                //intent.putExtra("valor", "5")
                startActivity(intent)
            }
            R.id.btnSair -> { toast("Sessão sendo encerrada")
                val intent = Intent(context, LoginActivity::class.java)
                deslogarUsuario()
                startActivity(intent)
            }
        }
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            android.R.id.home ->
                // Trata o clique no botão que abre o menu
                if (drawerLayout != null) {
                    openDrawer()
                    return true
                }
        }
        return super.onOptionsItemSelected(item)
    }

    // Abre o menu lateral
    protected fun openDrawer() {
        if (drawerLayout != null) {
            drawerLayout!!.openDrawer(GravityCompat.START)
        }
    }

    // Fecha o menu lateral
    protected fun closeDrawer() {
        drawerLayout?.closeDrawer(GravityCompat.START)
    }

    // Atualiza os dados do header do Navigation View
    // recebendo NOME,EMAIL e IMAGEM
    fun setNavViewValues(navView: NavigationView, nome: Int, email: Int, img: Int) {
        val headerView = navView.getHeaderView(0)
        val tNome = headerView.findViewById(R.id.tNome) as TextView
        val tEmail = headerView.findViewById(R.id.tEmail) as TextView
        val imgView = headerView.findViewById(R.id.img) as ImageView
        tNome.setText(nome)
        tEmail.setText(email)
        imgView.setImageResource(img)
    }

    fun deslogarUsuario() {
        autenticacao = ConfiguracaoFirebase.firebaseAutenticacao
        try {
            autenticacao?.signOut()
            finish()
        } catch (e: Exception) {
            e.printStackTrace()
        }

    }
}
