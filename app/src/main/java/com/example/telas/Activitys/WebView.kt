package com.example.telas.Activitys.Cursos

import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.webkit.WebChromeClient
import android.webkit.WebSettings
import android.webkit.WebView
import android.webkit.WebViewClient
import android.widget.ProgressBar
import android.widget.TextView
import com.example.telas.R
import java.util.*
import java.lang.Integer.parseInt
import java.lang.Integer.toString

class WebView : AppCompatActivity() {

    private lateinit var webView: WebView
    private lateinit var progresso: ProgressBar
    private lateinit var carregando: TextView
    private lateinit var resultado: TextView
    private lateinit var textoTop: TextView
    private lateinit var textoProgresso: TextView
    private lateinit var textoProgresso2: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_webview)

        val intent = intent
        val valor1 = intent.getStringExtra("valor")
        val valor = parseInt(valor1)

        webView = findViewById(R.id.internetView) as WebView
        progresso = findViewById(R.id.progressBar1) as ProgressBar
        carregando = findViewById(R.id.txt_carregando) as TextView
        resultado = findViewById(R.id.txt_resultado) as TextView
        textoTop = findViewById(R.id.txt_top) as TextView
        textoProgresso = findViewById(R.id.txtProgresso_teste) as TextView
        textoProgresso2 = findViewById(R.id.txtPorcentagem) as TextView

        webView.settings.javaScriptEnabled = true
        webView.settings.javaScriptCanOpenWindowsAutomatically = true
        webView.settings.domStorageEnabled = true
        webView.settings.allowFileAccess = true
        webView.settings.allowFileAccessFromFileURLs = true
        webView.settings.allowUniversalAccessFromFileURLs = true
        webView.settings.allowContentAccess = true

        //otimizando o webView
        webView.settings.setRenderPriority(WebSettings.RenderPriority.HIGH)
        webView.settings.setAppCacheEnabled(true)

        webView.webViewClient = WebViewClient()
       // webView.loadUrl("https://unifor.br/web/graduacao/ciencia-da-computacao")

        val web = when(valor){
            1 -> "https://unifor.br/web/graduacao/ciencia-da-computacao"
            2 -> "https://unifor.br/web/graduacao/analise-e-desenvolvimento-de-sistema"
            3 -> "https://unifor.br/web/graduacao/engenharia-de-controle-e-automacao"
            4 -> "https://unifor.br/web/graduacao/engenharia-da-computacao"
            5 -> "https://unifor.br/"
            else -> "valor invalido"
        }
        webView.loadUrl(web)
    }
    override fun onStart() {
        //Random de frases
        val frases = arrayOf(
                R.string.texto_carregamento1, R.string.texto_carregamento2,
                R.string.texto_carregamento3, R.string.texto_carregamento4,
                R.string.texto_carregamento5, R.string.texto_carregamento6,
                R.string.texto_carregamento7, R.string.texto_carregamento8,
                R.string.texto_carregamento9
        )
        val numero = Random().nextInt(frases.size)
        resultado.setText( frases[numero] )

        //ProgresseBar
        webView.webChromeClient = object : WebChromeClient() {

            override fun onProgressChanged(view: WebView, progress: Int) {
                if (progress < 100 && progresso.visibility == ProgressBar.GONE) {
                    webView.visibility = View.INVISIBLE
                    progresso.visibility = ProgressBar.VISIBLE
                    carregando.visibility = View.VISIBLE
                    textoTop.visibility = View.VISIBLE
                    resultado.visibility = View.VISIBLE
                    textoProgresso.visibility = View.VISIBLE
                    textoProgresso2.visibility = View.VISIBLE
                }
                progresso.setProgress(progress)
                textoProgresso.text = toString(progress)
                if (progress == 100) {
                    webView.visibility = View.VISIBLE
                    progresso.visibility = ProgressBar.INVISIBLE
                    carregando.visibility = View.INVISIBLE
                    textoTop.visibility = View.INVISIBLE
                    resultado.visibility = View.INVISIBLE
                    textoProgresso.visibility = View.INVISIBLE
                    textoProgresso2.visibility = View.INVISIBLE
                }
            }
        }

        super.onStart()
    }

    override fun onBackPressed() {
        if (webView.canGoBack()) {
            webView.goBack()
        } else {
            finish()
            super.onBackPressed()
        }
    }
}
