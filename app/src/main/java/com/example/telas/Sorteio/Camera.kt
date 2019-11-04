package com.example.telas.Sorteio

import android.Manifest
import android.app.Activity
import android.content.ContentValues
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.net.Uri
import android.os.Build
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.provider.MediaStore
import android.view.View
import android.widget.ImageView
import android.widget.Toast

import com.example.telas.R
import kotlinx.android.synthetic.main.fragment_passo2.*

import java.io.FileNotFoundException
import android.os.Environment.getExternalStorageDirectory

import android.os.Environment
import android.widget.Button
import java.io.File


class Camera : AppCompatActivity() {

    private val IMAGE_CAPTURE_CODE= 1001
    private val PERMISSION_CODE= 1000
    var imagem_uri: Uri? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.fragment_passo2)

        capture_btn.setOnClickListener {
            //Se o sistema operacional for Marshmallow ou superior, precisamos solicitar permissão em tempo de execução
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                if (checkSelfPermission(Manifest.permission.CAMERA) ==
                        PackageManager.PERMISSION_DENIED ||
                        checkSelfPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE) ==
                        PackageManager.PERMISSION_DENIED
                ) {
                    //a permissão não foi ativada
                    val permission = arrayOf(Manifest.permission.CAMERA, Manifest.permission.WRITE_EXTERNAL_STORAGE)
                    //mostre o popup para pedir a permissão
                    requestPermissions(permission, PERMISSION_CODE)
                } else {
                    openCamera()
                }
            } else {
                //permissão já concedida
                openCamera()
            }
        }
    }
    private fun openCamera() {
        val values = ContentValues()

        values.put(MediaStore.Images.Media.TITLE, "Nova foto")
        values.put(MediaStore.Images.Media.DESCRIPTION, "Da câmera")
        imagem_uri = contentResolver.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values)
        val cameraIntent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
        cameraIntent.putExtra(MediaStore.EXTRA_OUTPUT, imagem_uri)

        startActivityForResult(cameraIntent, IMAGE_CAPTURE_CODE)

        buttonCompartilhar.setOnClickListener {

            val dir = File(getExternalStorageDirectory(), "FolderName")
            val imgFile = File(dir, "0.png")
            val intent = Intent()
            intent.type = "image/png image/jpeg"
            intent.action = Intent.ACTION_SEND
            intent.putExtra(Intent.EXTRA_STREAM, Uri.parse("content://$imgFile"))
            intent.putExtra(Intent.EXTRA_TEXT, "<---MY TEXT--->.")
            intent.setPackage("com.instagram.android")
            intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            try {
                startActivity(Intent.createChooser(intent, "Por favor selecione o aplicativo: "))
            } catch (ex: android.content.ActivityNotFoundException) {
                Toast.makeText(this, "Por favor instale o Instagram", Toast.LENGTH_LONG).show()
            }
        }
    }

    //handle permission result
    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        //chamado quando o usuário pressiona ALLOW ou DENY frim Permission Request Popup
        when (requestCode) {
            PERMISSION_CODE -> {
                if (grantResults.size > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    //permissão do popup foi concedida
                    openCamera()
                } else {
                    //permissão do popup foi negada
                    Toast.makeText(this, "Permissao negada", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        //chamado quando a imagem foi capturada da intenção da câmera
        if(resultCode == Activity.RESULT_OK){
            //definir imagem capturada para image_view
            image_view.setImageURI(imagem_uri)
        }
    }

    override fun onBackPressed() {
        Toast.makeText(this@Camera,"Imagem salva na pasta : Pictures, na galeria",Toast.LENGTH_SHORT).show()
        finish()
    }
}
