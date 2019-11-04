package com.example.telas.MenuLateral.Mapa

import android.Manifest
import android.app.AlertDialog
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Color
import android.location.Address
import android.location.Geocoder
import android.location.Location
import android.location.LocationListener
import android.location.LocationManager
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import android.support.v4.app.ActivityCompat
import android.support.v7.widget.Toolbar
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.LinearLayout
import android.widget.SearchView
import android.widget.Toast
import com.example.telas.Activitys.BaseActivity

import com.google.android.gms.common.ConnectionResult
import com.google.android.gms.common.GoogleApiAvailability
import com.google.android.gms.common.api.GoogleApiClient
import com.google.android.gms.location.LocationServices
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.BitmapDescriptorFactory
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.Circle
import com.google.android.gms.maps.model.CircleOptions
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions
import com.google.android.gms.maps.model.PolygonOptions

import java.io.IOException
import java.util.ArrayList
import java.util.Locale

import com.example.telas.MenuLateral.Mapa.Constants.Constants.ERROR_DIALOG_REQUEST
import com.example.telas.MenuLateral.Mapa.Constants.Constants.PERMISSIONS_REQUEST_ENABLE_GPS
import com.example.telas.R

class Mapa : BaseActivity(), OnMapReadyCallback, GoogleApiClient.OnConnectionFailedListener, GoogleApiClient.ConnectionCallbacks {

    private var map: GoogleMap? = null
    private var map2: GoogleMap? = null
    private var mGoogleApiClient: GoogleApiClient? = null
    private var locationManager: LocationManager? = null
    private var locationListener: LocationListener? = null
    private var polygonOptions = PolygonOptions()
    private val circleOptions = CircleOptions()
    private var editDestino: SearchView? = null
    var circleAp: Circle? = null
    private var btnMapa: Button? = null
    private var voltar: Button? = null
    private var enderecoDestino: String? = null
    private var linearLayoutDestino: LinearLayout? = null
    private var status = false
    private val PERMISSION_CODE= 1000
    private lateinit var opcoes: MutableList<String>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_maps)

        val toolbar = findViewById(R.id.toolbar) as Toolbar
        toolbar.title = "Feira das Profissões 2019"
        toolbar.titleMarginStart = 45
        setSupportActionBar(toolbar)
        supportActionBar!!.setDisplayHomeAsUpEnabled(true)

        inicializarComponentes()
        array()
        selecaoLocais()
    }

    //botão de ir para o local
    fun irParaLocal() {
        if (!status) {
            enderecoDestino = editDestino!!.query.toString()
            if (enderecoDestino != " " || enderecoDestino != null) {
                var i = 0
                    while (opcoes.size > i) {
                        if (enderecoDestino == opcoes[i]) {
                            selecaoLocais()
                        }
                        i++
                    }
            } else if (enderecoDestino != " " || enderecoDestino != null) {
                val addressDestino = recuperarEndereco(enderecoDestino)
                if (addressDestino != null) {
                    val destino = Destino()
                    destino.cidade = addressDestino.adminArea
                    destino.cep = addressDestino.postalCode
                    destino.bairro = addressDestino.subLocality
                    destino.rua = addressDestino.thoroughfare
                    destino.numero = addressDestino.featureName
                    destino.latitude = addressDestino.latitude.toString()
                    destino.longitude = addressDestino.longitude.toString()

                    val mensagem = StringBuilder()
                    mensagem.append("Nome do local: " + enderecoDestino)
                    mensagem.append("\nCidade: " + destino.cidade!!)
                    mensagem.append("\nRua: " + destino.rua!!)
                    mensagem.append("\nBairro: " + destino.bairro!!)
                    mensagem.append("\nNúmero: " + destino.numero!!)
                    mensagem.append("\nCep: " + destino.cep!!)

                    val builder = AlertDialog.Builder(this)
                            .setTitle("Confirme seu endereço!")
                            .setMessage(mensagem)
                            .setPositiveButton("Confirmar") { dialog, which ->
                                val localDestino = LatLng(addressDestino.latitude, addressDestino.longitude)
                                map2!!.addMarker(MarkerOptions().position(localDestino).title("Seu Destino"))
                                val cameraPosition = CameraPosition.Builder().target(localDestino).zoom(16f).bearing(84f).build()
                                val update = CameraUpdateFactory.newCameraPosition(cameraPosition)
                                map2!!.animateCamera(update, object : GoogleMap.CancelableCallback {
                                    override fun onFinish() {}

                                    override fun onCancel() {}
                                }) // o Zoom varia de 2.0 a 21.0

                                status = true
                                linearLayoutDestino!!.visibility = View.GONE
                                voltar!!.visibility = View.VISIBLE
                                locais()
                            }.setNegativeButton("Cancelar") { dialog, which -> }
                    val dialog = builder.create()
                    dialog.show()
                }
            } else {
                Toast.makeText(this, "Informe o endereço de destino!", Toast.LENGTH_SHORT).show()
            }
        } else {
            deleteCircles()
            status = false
            linearLayoutDestino!!.visibility = View.VISIBLE
            voltar!!.visibility = View.GONE
        }
    }

    private fun recuperarEndereco(endereco: String?): Address? {
        val geocoder = Geocoder(this, Locale.getDefault())
        try {
            val listaEnderecos = geocoder.getFromLocationName(endereco, 1)
            if (listaEnderecos != null && listaEnderecos.size > 0) {
                return listaEnderecos[0]
            }
        } catch (e: IOException) {
            e.printStackTrace()
        }

        return null
    }

    //Retornar a criação do mapa
    override fun onMapReady(googleMap: GoogleMap?) {
        map = googleMap
        map2 = googleMap
        mGoogleApiClient!!.connect()

        userLocation()
        deleteCircles()

        val Unifor = LatLng(-3.768994, -38.479675)
        val BlocoA = LatLng(-3.770989, -38.481197) ; val BlocoS = LatLng(-3.766795, -38.479783)
        val BlocoB = LatLng(-3.770659, -38.481366) ; val BlocoT = LatLng(-3.767605, -38.480239)
        val BlocoC = LatLng(-3.769721, -38.481234) ; val BlocoZ = LatLng(-3.769269, -38.474417)
        val BlocoD = LatLng(-3.770470, -38.480477) ; val BlocoCC = LatLng(-3.769199, -38.479685)
        val BlocoE = LatLng(-3.770220, -38.481553) ; val BlocoBibli = LatLng(-3.768947, -38.480581)
        val BlocoF = LatLng(-3.771602, -38.478084) ; val BlocoPref = LatLng(-3.770579, -38.479149)
        val BlocoH = LatLng(-3.767953, -38.480542) ; val BlocoDAE = LatLng(-3.768985, -38.481205)
        val BlocoI = LatLng(-3.769784, -38.479694) ; val Nami = LatLng(-3.771970, -38.480071)
        val BlocoJ = LatLng(-3.770036, -38.479407) ; val ginasio = LatLng(-3.769736, -38.477200)
        val Blocok = LatLng(-3.769582, -38.478810) ; val atletismo = LatLng(-3.768777, -38.475438)
        val BlocoL = LatLng(-3.770945, -38.478835)
        val BlocoM = LatLng(-3.768848, -38.478686)
        val BlocoN = LatLng(-3.768067, -38.479177)
        val BlocoO = LatLng(-3.770612, -38.477507)
        val BlocoP = LatLng(-3.767738, -38.479317)
        val BlocoQ = LatLng(-3.767431, -38.479476)
        val BlocoR = LatLng(-3.767136, -38.479643)

        //verifica se foram dadas as permissões de acesso
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            locationManager!!.requestLocationUpdates(
                    LocationManager.GPS_PROVIDER,
                    5000,
                    10f,
                    locationListener
            )
            /*map.addMarker(new MarkerOptions().position(Unifor).title("Unifor").icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_AZURE) // marcador com a cor azul
                                    // o defaultMarker é para representar o marcador padrão, mas pode ser substituido
                    ));*/
            map!!.addMarker(MarkerOptions().position(BlocoA).title("Bloco A").icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_BLUE)))
            map!!.addMarker(MarkerOptions().position(BlocoB).title("Bloco B").icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_BLUE)))
            map!!.addMarker(MarkerOptions().position(BlocoC).title("Bloco C").icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_BLUE)))
            map!!.addMarker(MarkerOptions().position(BlocoD).title("Bloco D").icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_BLUE)))
            map!!.addMarker(MarkerOptions().position(BlocoE).title("Bloco E").icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_BLUE)))
            map!!.addMarker(MarkerOptions().position(BlocoF).title("Bloco F").icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_BLUE)))
            map!!.addMarker(MarkerOptions().position(BlocoH).title("Bloco H").icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_BLUE)))
            map!!.addMarker(MarkerOptions().position(BlocoI).title("Bloco I").icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_BLUE)))
            map!!.addMarker(MarkerOptions().position(BlocoJ).title("Bloco J").icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_BLUE)))
            map!!.addMarker(MarkerOptions().position(Blocok).title("Bloco K").icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_BLUE)))
            map!!.addMarker(MarkerOptions().position(BlocoL).title("Bloco L").icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_BLUE)))
            map!!.addMarker(MarkerOptions().position(BlocoM).title("Bloco M").icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_BLUE)))
            map!!.addMarker(MarkerOptions().position(BlocoN).title("Bloco N").icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_BLUE)))
            map!!.addMarker(MarkerOptions().position(BlocoO).title("Bloco O").icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_BLUE)))
            map!!.addMarker(MarkerOptions().position(BlocoP).title("Bloco P").icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_BLUE)))
            map!!.addMarker(MarkerOptions().position(BlocoQ).title("Bloco Q").icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_BLUE)))
            map!!.addMarker(MarkerOptions().position(BlocoR).title("Bloco R").icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_BLUE)))
            map!!.addMarker(MarkerOptions().position(BlocoS).title("Bloco S").icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_BLUE)))
            map!!.addMarker(MarkerOptions().position(BlocoT).title("Bloco T").icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_BLUE)))
            map!!.addMarker(MarkerOptions().position(BlocoZ).title("Bloco Z").icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_BLUE)))
            map!!.addMarker(MarkerOptions().position(BlocoCC).title("Centro de Convivência (CC)").icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_YELLOW)))
            map!!.addMarker(MarkerOptions().position(BlocoBibli).title("Biblioteca").icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_YELLOW)))
            map!!.addMarker(MarkerOptions().position(BlocoDAE).title("Central Administrativa DAE").icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_YELLOW)))
            map!!.addMarker(MarkerOptions().position(BlocoPref).title("Prefeitura").icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_YELLOW)))
            map!!.addMarker(MarkerOptions().position(Nami).title("Núcleo de Atenção Medica Integrada (Nami)").icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_YELLOW)))
            map!!.addMarker(MarkerOptions().position(ginasio).title("Ginásio Poliesportivo").icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_YELLOW)))
            map!!.addMarker(MarkerOptions().position(atletismo).title("Estádio de Atletismo").icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_YELLOW)))

            val cameraPosition = CameraPosition.Builder().target(Unifor).zoom(16f).bearing(85f).build()
            val update = CameraUpdateFactory.newCameraPosition(cameraPosition)
            map!!.animateCamera(update, object : GoogleMap.CancelableCallback {
                override fun onFinish() {}

                override fun onCancel() {}
            }) // o Zoom varia de 2.0 a 21.0
            //Criando a area da Unifor no mapa
            polygonOptions = PolygonOptions()
            polygonOptions.add(LatLng(-3.771358, -38.481788)) // lateral direita proxima a placa e a saida bloco D
            polygonOptions.add(LatLng(-3.766598, -38.482260)) // lateral esquerda próximo ao centro de eventos
            polygonOptions.add(LatLng(-3.766438, -38.480437)) // final do centro de eventos e inicio do estacionamento
            polygonOptions.add(LatLng(-3.766212, -38.478118))
            polygonOptions.add(LatLng(-3.765961, -38.476666)) // final lateral do estacionamento
            polygonOptions.add(LatLng(-3.765059, -38.476657))
            polygonOptions.add(LatLng(-3.766244, -38.473797))
            polygonOptions.add(LatLng(-3.766200, -38.473826))
            polygonOptions.add(LatLng(-3.767952, -38.473804))
            polygonOptions.add(LatLng(-3.768333, -38.473108))
            polygonOptions.add(LatLng(-3.769225, -38.473748))
            polygonOptions.add(LatLng(-3.770206, -38.474361))
            polygonOptions.add(LatLng(-3.770773, -38.474604))
            polygonOptions.add(LatLng(-3.770928, -38.476818))//
            polygonOptions.add(LatLng(-3.770972, -38.477144))
            polygonOptions.add(LatLng(-3.772349, -38.476995))
            polygonOptions.add(LatLng(-3.772454, -38.478387))
            polygonOptions.add(LatLng(-3.771688, -38.478442))
            polygonOptions.add(LatLng(-3.771820, -38.479789))// proximo a escola e o nami
            polygonOptions.add(LatLng(-3.772564, -38.479745))
            polygonOptions.add(LatLng(-3.772658, -38.480772))
            polygonOptions.add(LatLng(-3.771479, -38.480871))
            polygonOptions.add(LatLng(-3.771275, -38.480860))
            polygonOptions.add(LatLng(-3.771330, -38.481788))
            polygonOptions.add(LatLng(-3.770851, -38.481827))//fim e inicio
            polygonOptions.strokeWidth(3f)
            map!!.addPolygon(polygonOptions)
            map!!.isMyLocationEnabled = true
            deleteCircles()
        }
    }

    fun permissao() {

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (checkSelfPermission(Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_DENIED) {
                //permission was not enable
                val permission = arrayOf(Manifest.permission.ACCESS_FINE_LOCATION)
                //show popup to request permission
                requestPermissions(permission, PERMISSION_CODE)
            }

        }
    }
    fun solicit(requestCode: Int, permissions: Array<out String>, grantResults: IntArray){
        when (requestCode) {
            PERMISSION_CODE -> {
                if (grantResults.size > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    //permission from popup was granted
                    if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED){
                    val enableGpsIntent = Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS)
                    startActivityForResult(enableGpsIntent, PERMISSIONS_REQUEST_ENABLE_GPS)
                    locationManager!!.requestLocationUpdates(
                            LocationManager.GPS_PROVIDER,
                            3000,
                            10f,
                            locationListener)
                    }
                } else {
                    //permission from popup was denied
                    Toast.makeText(this, "Permissao negada", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<String>,
                                            grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)

        solicit(requestCode, permissions, grantResults);

        //solicitando permissões pela primeira vez
        for (permissaoResultado in grantResults) {
            //permission denied (negada)
            if (permissaoResultado == PackageManager.PERMISSION_DENIED) {
                //alerta
                alertaValidacaoPermissao()
            } else {
                if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
                    val enableGpsIntent = Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS)
                    startActivityForResult(enableGpsIntent, PERMISSIONS_REQUEST_ENABLE_GPS)
                    locationManager!!.requestLocationUpdates(
                            LocationManager.GPS_PROVIDER,
                            3000,
                            10f,
                            locationListener
                    )
                }
            }
        }
    }

    private fun alertaValidacaoPermissao() {
        val builder = AlertDialog.Builder(this)
        builder.setTitle("Permissões Negadas")
        builder.setMessage("Para utilizar o app é necessário aceitar as permissões")
        builder.setNegativeButton("Confirmar") { dialog, which -> finish() }
        builder.setPositiveButton("Ativar Localicação") { dialog, which ->
            val enableGpsIntent = Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS)
            startActivityForResult(enableGpsIntent, PERMISSIONS_REQUEST_ENABLE_GPS)
            onMapReady(map)
        }
        val dialog = builder.create()
        dialog.show()
    }

    fun verServicesOK(): Boolean {
        Log.d(TAG, "verServicesOK: verificar a versão do google services")
        val validar = GoogleApiAvailability.getInstance().isGooglePlayServicesAvailable(this@Mapa)
        if (validar == ConnectionResult.SUCCESS) {
            Log.d(TAG, "verServicesOK: Google play services ok")
            return true
        } else if (GoogleApiAvailability.getInstance().isUserResolvableError(validar)) {
            Log.d(TAG, "verServicesOK: Ocorreu um erro")
            val dialog = GoogleApiAvailability.getInstance().getErrorDialog(this@Mapa, validar, ERROR_DIALOG_REQUEST)
            dialog.show()
        } else {
            Toast.makeText(this, "Você não pode acessar o mapa", Toast.LENGTH_SHORT).show()
        }
        return false
    }

    private fun inicializarComponentes() {
        //autenticacao = ConfiguracaoFirebase.getFirebaseAutenticacao();
        //autenticacao.signOut();
        //iniciando os componentes
        editDestino = findViewById(R.id.editDestino)
        btnMapa = findViewById(R.id.btnMapa)
        linearLayoutDestino = findViewById(R.id.linearLayoutDestino)
        voltar = findViewById(R.id.btnVoltar)
        voltar!!.visibility = View.GONE

        btnMapa!!.setOnClickListener { irParaLocal() }
        voltar!!.setOnClickListener {
            onMapReady(map)
            status = false
            linearLayoutDestino!!.visibility = View.VISIBLE
            voltar!!.visibility = View.GONE
        }
        //Validar permissões
        permissao()
        //Mapa fragment
        val mapFragment = supportFragmentManager
                .findFragmentById(R.id.map) as SupportMapFragment?
        mapFragment!!.getMapAsync(this)
        // Configurar o objeto GoogleApiClient
        mGoogleApiClient = GoogleApiClient.Builder(this)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .addApi(LocationServices.API)
                .build()
        //Verificar serviços
        verServicesOK()

    }

    override fun onStart() {
        super.onStart()
        //mGoogleApiClient!!.connect()
        userLocation()
    }

    override fun onStop() {
        super.onStop()
        mGoogleApiClient!!.disconnect()
    }

    override fun onConnected(bundle: Bundle?) {
        Toast.makeText(this, "Conectado ao Google Play services!", Toast.LENGTH_SHORT).show()
    }

    override fun onConnectionFailed(connectionResult: ConnectionResult) {
        Toast.makeText(this, "Erro ao conectar: $connectionResult", Toast.LENGTH_SHORT).show()
    }

    override fun onConnectionSuspended(i: Int) {
        Toast.makeText(this, "Conexão Pausada (Conexão interrompida)", Toast.LENGTH_SHORT).show()
    }

    fun userLocation() {
        //obj responsavel por gerenciar a localização do user
        locationManager = this.getSystemService(Context.LOCATION_SERVICE) as LocationManager
        locationListener = object : LocationListener {
            override fun onLocationChanged(location: Location) {
                val latitude = location.latitude
                val longitude = location.longitude
                // para limpar os marcadores antes de criar novos
                map!!.clear()
                val localUser = LatLng(latitude, longitude)
                map!!.addMarker(MarkerOptions().position(localUser).title("Meu Local").icon(
                        BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_GREEN)))
                val cameraPosition = CameraPosition.Builder().target(localUser).zoom(15f).bearing(0f).build()
                val update = CameraUpdateFactory.newCameraPosition(cameraPosition)
                map!!.animateCamera(update, object : GoogleMap.CancelableCallback {
                    override fun onFinish() {}

                    override fun onCancel() {}
                }) // o Zoom varia de 2.0 a 21.0
                map!!.clear()
                locais()
                map!!.addPolygon(polygonOptions)

                //circle.fillColor(Color.argb(128,0,191,255)); // cor DeepSkyBlue
            }

            override fun onStatusChanged(provider: String, status: Int, extras: Bundle) {}

            override fun onProviderEnabled(provider: String) {}

            override fun onProviderDisabled(provider: String) {}
        }
        deleteCircles()
    }

    protected fun locais() {
        val BlocoA = LatLng(-3.770989, -38.481197)
        val BlocoS = LatLng(-3.766795, -38.479783)
        val BlocoB = LatLng(-3.770659, -38.481366)
        val BlocoT = LatLng(-3.767605, -38.480239)
        val BlocoC = LatLng(-3.769721, -38.481234)
        val BlocoZ = LatLng(-3.769269, -38.474417)
        val BlocoD = LatLng(-3.770470, -38.480477)
        val BlocoCC = LatLng(-3.769199, -38.479685)
        val BlocoE = LatLng(-3.770220, -38.481553)
        val BlocoBibli = LatLng(-3.768947, -38.480581)
        val BlocoF = LatLng(-3.771602, -38.478084)
        val BlocoPref = LatLng(-3.770579, -38.479149)
        val BlocoH = LatLng(-3.767953, -38.480542)
        val BlocoDAE = LatLng(-3.768985, -38.481205)
        val BlocoI = LatLng(-3.769784, -38.479694)
        val Nami = LatLng(-3.771970, -38.480071)
        val BlocoJ = LatLng(-3.770036, -38.479407)
        val ginasio = LatLng(-3.769736, -38.477200)
        val Blocok = LatLng(-3.769582, -38.478810)
        val atletismo = LatLng(-3.768777, -38.475438)
        val BlocoL = LatLng(-3.770945, -38.478835)
        val BlocoM = LatLng(-3.768848, -38.478686)
        val BlocoN = LatLng(-3.768067, -38.479177)
        val BlocoO = LatLng(-3.770612, -38.477507)
        val BlocoP = LatLng(-3.767738, -38.479317)
        val BlocoQ = LatLng(-3.767431, -38.479476)
        val BlocoR = LatLng(-3.767136, -38.479643)

        /*map.addMarker(new MarkerOptions().position(Unifor).title("Unifor").icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_AZURE) // marcador com a cor azul
                                    // o defaultMarker é para representar o marcador padrão, mas pode ser substituido
                    ));*/
        map!!.addMarker(MarkerOptions().position(BlocoA).title("Bloco A").icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_BLUE)))
        map!!.addMarker(MarkerOptions().position(BlocoB).title("Bloco B").icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_BLUE)))
        map!!.addMarker(MarkerOptions().position(BlocoC).title("Bloco C").icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_BLUE)))
        map!!.addMarker(MarkerOptions().position(BlocoD).title("Bloco D").icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_BLUE)))
        map!!.addMarker(MarkerOptions().position(BlocoE).title("Bloco E").icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_BLUE)))
        map!!.addMarker(MarkerOptions().position(BlocoF).title("Bloco F").icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_BLUE)))
        map!!.addMarker(MarkerOptions().position(BlocoH).title("Bloco H").icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_BLUE)))
        map!!.addMarker(MarkerOptions().position(BlocoI).title("Bloco I").icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_BLUE)))
        map!!.addMarker(MarkerOptions().position(BlocoJ).title("Bloco J").icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_BLUE)))
        map!!.addMarker(MarkerOptions().position(Blocok).title("Bloco K").icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_BLUE)))
        map!!.addMarker(MarkerOptions().position(BlocoL).title("Bloco L").icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_BLUE)))
        map!!.addMarker(MarkerOptions().position(BlocoM).title("Bloco M").icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_BLUE)))
        map!!.addMarker(MarkerOptions().position(BlocoN).title("Bloco N").icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_BLUE)))
        map!!.addMarker(MarkerOptions().position(BlocoO).title("Bloco O").icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_BLUE)))
        map!!.addMarker(MarkerOptions().position(BlocoP).title("Bloco P").icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_BLUE)))
        map!!.addMarker(MarkerOptions().position(BlocoQ).title("Bloco Q").icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_BLUE)))
        map!!.addMarker(MarkerOptions().position(BlocoR).title("Bloco R").icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_BLUE)))
        map!!.addMarker(MarkerOptions().position(BlocoS).title("Bloco S").icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_BLUE)))
        map!!.addMarker(MarkerOptions().position(BlocoT).title("Bloco T").icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_BLUE)))
        map!!.addMarker(MarkerOptions().position(BlocoZ).title("Bloco Z").icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_BLUE)))
        map!!.addMarker(MarkerOptions().position(BlocoCC).title("Centro de Convivência (CC)").icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_YELLOW)))
        map!!.addMarker(MarkerOptions().position(BlocoBibli).title("Biblioteca").icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_YELLOW)))
        map!!.addMarker(MarkerOptions().position(BlocoDAE).title("Central Administrativa DAE").icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_YELLOW)))
        map!!.addMarker(MarkerOptions().position(BlocoPref).title("Prefeitura").icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_YELLOW)))
        map!!.addMarker(MarkerOptions().position(Nami).title("Núcleo de Atenção Medica Integrada (Nami)").icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_YELLOW)))
        map!!.addMarker(MarkerOptions().position(ginasio).title("Ginásio Poliesportivo").icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_YELLOW)))
        map!!.addMarker(MarkerOptions().position(atletismo).title("Estádio de Atletismo").icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_YELLOW)))

        deleteCircles()
    }

    private fun array() {
        opcoes = ArrayList()

        opcoes.add("Bloco A")
        opcoes.add("BlocoA")
        opcoes.add("blocoa")
        opcoes.add("bloco a")
        opcoes.add("bloco A")
        opcoes.add("Bloco a")
        opcoes.add("Bloco B")
        opcoes.add("BlocoB")
        opcoes.add("blocob")
        opcoes.add("bloco b")
        opcoes.add("bloco B")
        opcoes.add("Bloco b")
        opcoes.add("Bloco C")
        opcoes.add("BlocoC")
        opcoes.add("blococ")
        opcoes.add("bloco c")
        opcoes.add("bloco C")
        opcoes.add("Bloco c")
        opcoes.add("Bloco D")
        opcoes.add("BlocoD")
        opcoes.add("blocod")
        opcoes.add("bloco d")
        opcoes.add("bloco D")
        opcoes.add("Bloco d")
        opcoes.add("Bloco E")
        opcoes.add("BlocoE")
        opcoes.add("blocoe")
        opcoes.add("bloco e")
        opcoes.add("bloco E")
        opcoes.add("Bloco e")
        opcoes.add("Bloco F")
        opcoes.add("BlocoF")
        opcoes.add("blocof")
        opcoes.add("bloco f")
        opcoes.add("bloco F")
        opcoes.add("Bloco f")
        opcoes.add("Bloco H")
        opcoes.add("BlocoH")
        opcoes.add("blocoh")
        opcoes.add("bloco h")
        opcoes.add("bloco H")
        opcoes.add("Bloco h")
        opcoes.add("Bloco I")
        opcoes.add("BlocoI")
        opcoes.add("blocoi")
        opcoes.add("bloco i")
        opcoes.add("bloco I")
        opcoes.add("Bloco i")
        opcoes.add("Bloco J")
        opcoes.add("BlocoJ")
        opcoes.add("blocoj")
        opcoes.add("bloco j")
        opcoes.add("bloco J")
        opcoes.add("Bloco j")
        opcoes.add("Bloco K")
        opcoes.add("BlocoK")
        opcoes.add("blocok")
        opcoes.add("bloco k")
        opcoes.add("bloco K")
        opcoes.add("Bloco k")
        opcoes.add("Bloco L")
        opcoes.add("BlocoL")
        opcoes.add("blocol")
        opcoes.add("bloco l")
        opcoes.add("bloco L")
        opcoes.add("Bloco l")
        opcoes.add("Bloco M")
        opcoes.add("BlocoM")
        opcoes.add("blocom")
        opcoes.add("bloco m")
        opcoes.add("bloco M")
        opcoes.add("Bloco m")
        opcoes.add("Bloco N")
        opcoes.add("BlocoN")
        opcoes.add("blocon")
        opcoes.add("bloco n")
        opcoes.add("bloco N")
        opcoes.add("Bloco n")
        opcoes.add("Bloco O")
        opcoes.add("BlocoO")
        opcoes.add("blocoo")
        opcoes.add("bloco o")
        opcoes.add("bloco O")
        opcoes.add("Bloco o")
        opcoes.add("Bloco P")
        opcoes.add("BlocoP")
        opcoes.add("blocop")
        opcoes.add("bloco p")
        opcoes.add("bloco P")
        opcoes.add("Bloco p")
        opcoes.add("Bloco Q")
        opcoes.add("BlocoQ")
        opcoes.add("blocoq")
        opcoes.add("bloco q")
        opcoes.add("bloco Q")
        opcoes.add("Bloco q")
        opcoes.add("Bloco R")
        opcoes.add("BlocoR")
        opcoes.add("blocor")
        opcoes.add("bloco r")
        opcoes.add("bloco R")
        opcoes.add("Bloco r")
        opcoes.add("Bloco S")
        opcoes.add("BlocoS")
        opcoes.add("blocos")
        opcoes.add("bloco s")
        opcoes.add("bloco S")
        opcoes.add("Bloco s")
        opcoes.add("Bloco T")
        opcoes.add("BlocoT")
        opcoes.add("blocot")
        opcoes.add("bloco t")
        opcoes.add("bloco T")
        opcoes.add("Bloco t")
        opcoes.add("Bloco Z")
        opcoes.add("BlocoZ")
        opcoes.add("blocoz")
        opcoes.add("bloco z")
        opcoes.add("bloco Z")
        opcoes.add("Bloco z")
        opcoes.add("Centro de Convivência")
        opcoes.add("Centro de convivencia")
        opcoes.add("centro de convivencia")
        opcoes.add("centro de convivência")
        opcoes.add("CC")
        opcoes.add("cc")
        opcoes.add("Biblioteca")
        opcoes.add("biblioteca")
        opcoes.add("Prefeitura")
        opcoes.add("prefeitura")
        opcoes.add("Central Administrativa")
        opcoes.add("central administrativa")
        opcoes.add("DAE")
        opcoes.add("dae")
        opcoes.add("Nami")
        opcoes.add("nami")
        opcoes.add("Nucleo de atendimento")
        opcoes.add("nucleo de atendimento")
        opcoes.add("Nucleo de Atendimento")
        opcoes.add("Núcleo de atendimento")
        opcoes.add("núcleo de atendimento")
        opcoes.add("Núcleo de Atendimento")
        opcoes.add("ginasio")
        opcoes.add("Ginasio")
        opcoes.add("ginásio")
        opcoes.add("Ginásio")
        opcoes.add("Ginásio Poliesportivo")
        opcoes.add("ginásio poliesportivo")
        opcoes.add("atletismo")
        opcoes.add("Atletismo")
        opcoes.add("Estádio de Atletismo")
        opcoes.add("estádio de atletismo")
        opcoes.add("Estádio")
        opcoes.add("estádio")
        opcoes.add("Estadio")
        opcoes.add("estadio")
    }

    protected fun selecaoLocais() {

        if (enderecoDestino == "Bloco A" || enderecoDestino == "BlocoA" || enderecoDestino == "bloco a" || enderecoDestino == "blocoa" ||
                enderecoDestino == "Bloco a" || enderecoDestino == "bloco A") {
            val BlocoA = LatLng(-3.770989, -38.481197)
            map!!.addMarker(MarkerOptions().position(BlocoA).title("Bloco A").icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_BLUE)))
            val cameraPosition = CameraPosition.Builder().target(BlocoA).zoom(17f).bearing(83f).build()
            val update = CameraUpdateFactory.newCameraPosition(cameraPosition)

            status = true
            linearLayoutDestino!!.visibility = View.GONE
            voltar!!.visibility = View.VISIBLE

            map!!.animateCamera(update, object : GoogleMap.CancelableCallback {
                override fun onFinish() {
                    circleOptions.center(BlocoA)
                    circleOptions.radius(12.0)
                    circleOptions.strokeWidth(5f)
                    circleOptions.strokeColor(Color.argb(255, 0, 0, 255))
                    circleOptions.fillColor(Color.argb(55, 0, 255, 0))
                    map!!.addCircle(circleOptions)

                    circleAp = map!!.addCircle(circleOptions)

                }

                override fun onCancel() {}
            })
            deleteCircles()
        }
        if (enderecoDestino == "Bloco B" || enderecoDestino == "BlocoB" || enderecoDestino == "bloco b" || enderecoDestino == "blocob" ||
                enderecoDestino == "Bloco b" || enderecoDestino == "bloco B") {
            val BlocoB = LatLng(-3.770659, -38.481366)
            map!!.addMarker(MarkerOptions().position(BlocoB).title("Bloco B").icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_BLUE)))
            val cameraPosition = CameraPosition.Builder().target(BlocoB).zoom(17f).bearing(83f).build()
            val update = CameraUpdateFactory.newCameraPosition(cameraPosition)

            status = true
            linearLayoutDestino!!.visibility = View.GONE
            voltar!!.visibility = View.VISIBLE

            map!!.animateCamera(update, object : GoogleMap.CancelableCallback {
                override fun onFinish() {
                    circleOptions.center(BlocoB)
                    circleOptions.radius(12.0)
                    circleOptions.strokeWidth(5f)
                    circleOptions.strokeColor(Color.argb(255, 0, 0, 255))
                    circleOptions.fillColor(Color.argb(55, 0, 255, 0))
                    map!!.addCircle(circleOptions)

                    circleAp = map!!.addCircle(circleOptions)
                }

                override fun onCancel() {}
            })
        }
        if (enderecoDestino == "Bloco C" || enderecoDestino == "BlocoC" || enderecoDestino == "bloco c" || enderecoDestino == "blococ" ||
                enderecoDestino == "Bloco c" || enderecoDestino == "bloco C") {
            val BlocoC = LatLng(-3.769721, -38.481234)
            map!!.addMarker(MarkerOptions().position(BlocoC).title("Bloco C").icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_BLUE)))
            val cameraPosition = CameraPosition.Builder().target(BlocoC).zoom(17f).bearing(83f).build()
            val update = CameraUpdateFactory.newCameraPosition(cameraPosition)
            map!!.animateCamera(update, object : GoogleMap.CancelableCallback {
                override fun onFinish() {
                    circleOptions.center(BlocoC)
                    circleOptions.radius(12.0)
                    circleOptions.strokeWidth(5f)
                    circleOptions.strokeColor(Color.argb(255, 0, 0, 255))
                    circleOptions.fillColor(Color.argb(55, 0, 255, 0))
                    map!!.addCircle(circleOptions)
                    circleAp = map!!.addCircle(circleOptions)
                }

                override fun onCancel() {}
            })
            status = true
            linearLayoutDestino!!.visibility = View.GONE
            voltar!!.visibility = View.VISIBLE
        }
        if (enderecoDestino == "Bloco D" || enderecoDestino == "BlocoD" || enderecoDestino == "bloco d" || enderecoDestino == "blocod" ||
                enderecoDestino == "Bloco d" || enderecoDestino == "bloco D") {
            val BlocoD = LatLng(-3.770470, -38.480477)
            map!!.addMarker(MarkerOptions().position(BlocoD).title("Bloco D").icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_BLUE)))
            val cameraPosition = CameraPosition.Builder().target(BlocoD).zoom(17f).bearing(83f).build()
            val update = CameraUpdateFactory.newCameraPosition(cameraPosition)
            map!!.animateCamera(update, object : GoogleMap.CancelableCallback {
                override fun onFinish() {
                    circleOptions.center(BlocoD)
                    circleOptions.radius(12.0)
                    circleOptions.strokeWidth(5f)
                    circleOptions.strokeColor(Color.argb(255, 0, 0, 255))
                    circleOptions.fillColor(Color.argb(55, 0, 255, 0))
                    map!!.addCircle(circleOptions)
                }

                override fun onCancel() {}
            })
            circleAp = map!!.addCircle(circleOptions)
            status = true
            linearLayoutDestino!!.visibility = View.GONE
            voltar!!.visibility = View.VISIBLE
        }
        if (enderecoDestino == "Bloco E" || enderecoDestino == "BlocoE" || enderecoDestino == "bloco e" || enderecoDestino == "blocoe" ||
                enderecoDestino == "Bloco e" || enderecoDestino == "bloco E") {
            val BlocoE = LatLng(-3.770220, -38.481553)
            map!!.addMarker(MarkerOptions().position(BlocoE).title("Bloco E").icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_BLUE)))
            val cameraPosition = CameraPosition.Builder().target(BlocoE).zoom(17f).bearing(83f).build()
            val update = CameraUpdateFactory.newCameraPosition(cameraPosition)
            map!!.animateCamera(update, object : GoogleMap.CancelableCallback {
                override fun onFinish() {

                    circleOptions.center(BlocoE)
                    circleOptions.radius(12.0)
                    circleOptions.strokeWidth(5f)
                    circleOptions.strokeColor(Color.argb(255, 0, 0, 255))
                    circleOptions.fillColor(Color.argb(55, 0, 255, 0))
                    map!!.addCircle(circleOptions)
                }

                override fun onCancel() {}
            })
            circleAp = map!!.addCircle(circleOptions)
            status = true
            linearLayoutDestino!!.visibility = View.GONE
            voltar!!.visibility = View.VISIBLE
        }
        if (enderecoDestino == "Bloco F" || enderecoDestino == "BlocoF" || enderecoDestino == "bloco f" || enderecoDestino == "blocof" ||
                enderecoDestino == "Bloco f" || enderecoDestino == "bloco F") {
            val BlocoF = LatLng(-3.771602, -38.478084)
            map!!.addMarker(MarkerOptions().position(BlocoF).title("Bloco F").icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_BLUE)))
            val cameraPosition = CameraPosition.Builder().target(BlocoF).zoom(17f).bearing(83f).build()
            val update = CameraUpdateFactory.newCameraPosition(cameraPosition)
            map!!.animateCamera(update, object : GoogleMap.CancelableCallback {
                override fun onFinish() {

                    circleOptions.center(BlocoF)
                    circleOptions.radius(12.0)
                    circleOptions.strokeWidth(5f)
                    circleOptions.strokeColor(Color.argb(255, 0, 0, 255))
                    circleOptions.fillColor(Color.argb(55, 0, 255, 0))
                    map!!.addCircle(circleOptions)
                }

                override fun onCancel() {}
            })
            circleAp = map!!.addCircle(circleOptions)
            status = true
            linearLayoutDestino!!.visibility = View.GONE
            voltar!!.visibility = View.VISIBLE
        }
        if (enderecoDestino == "Bloco H" || enderecoDestino == "BlocoH" || enderecoDestino == "bloco h" || enderecoDestino == "blocoh" ||
                enderecoDestino == "Bloco h" || enderecoDestino == "bloco H") {
            val BlocoH = LatLng(-3.767953, -38.480542)
            map!!.addMarker(MarkerOptions().position(BlocoH).title("Bloco H").icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_BLUE)))
            val cameraPosition = CameraPosition.Builder().target(BlocoH).zoom(17f).bearing(83f).build()
            val update = CameraUpdateFactory.newCameraPosition(cameraPosition)
            map!!.animateCamera(update, object : GoogleMap.CancelableCallback {
                override fun onFinish() {

                    circleOptions.center(BlocoH)
                    circleOptions.radius(12.0)
                    circleOptions.strokeWidth(5f)
                    circleOptions.strokeColor(Color.argb(255, 0, 0, 255))
                    circleOptions.fillColor(Color.argb(55, 0, 255, 0))
                    map!!.addCircle(circleOptions)
                }

                override fun onCancel() {}
            })
            status = true
            linearLayoutDestino!!.visibility = View.GONE
            voltar!!.visibility = View.VISIBLE
        }
        if (enderecoDestino == "Bloco I" || enderecoDestino == "BlocoI" || enderecoDestino == "bloco i" || enderecoDestino == "blocoi" ||
                enderecoDestino == "Bloco i" || enderecoDestino == "bloco I") {
            val BlocoI = LatLng(-3.769784, -38.479694)
            map!!.addMarker(MarkerOptions().position(BlocoI).title("Bloco I").icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_BLUE)))
            val cameraPosition = CameraPosition.Builder().target(BlocoI).zoom(17f).bearing(83f).build()
            val update = CameraUpdateFactory.newCameraPosition(cameraPosition)
            map!!.animateCamera(update, object : GoogleMap.CancelableCallback {
                override fun onFinish() {

                    circleOptions.center(BlocoI)
                    circleOptions.radius(12.0)
                    circleOptions.strokeWidth(5f)
                    circleOptions.strokeColor(Color.argb(255, 0, 0, 255))
                    circleOptions.fillColor(Color.argb(55, 0, 255, 0))
                    map!!.addCircle(circleOptions)
                    circleAp = map!!.addCircle(circleOptions)
                }

                override fun onCancel() {}
            })

            status = true
            linearLayoutDestino!!.visibility = View.GONE
            voltar!!.visibility = View.VISIBLE
        }
        if (enderecoDestino == "Bloco J" || enderecoDestino == "BlocoJ" || enderecoDestino == "bloco j" || enderecoDestino == "blocoj" ||
                enderecoDestino == "Bloco j" || enderecoDestino == "bloco J") {
            val BlocoJ = LatLng(-3.770036, -38.479407)
            map!!.addMarker(MarkerOptions().position(BlocoJ).title("Bloco J").icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_BLUE)))
            val cameraPosition = CameraPosition.Builder().target(BlocoJ).zoom(17f).bearing(83f).build()
            val update = CameraUpdateFactory.newCameraPosition(cameraPosition)
            map!!.animateCamera(update, object : GoogleMap.CancelableCallback {
                override fun onFinish() {

                    circleOptions.center(BlocoJ)
                    circleOptions.radius(12.0)
                    circleOptions.strokeWidth(5f)
                    circleOptions.strokeColor(Color.argb(255, 0, 0, 255))
                    circleOptions.fillColor(Color.argb(55, 0, 255, 0))
                    map!!.addCircle(circleOptions)
                    circleAp = map!!.addCircle(circleOptions)
                }

                override fun onCancel() {}
            })

            status = true
            linearLayoutDestino!!.visibility = View.GONE
            voltar!!.visibility = View.VISIBLE
        }
        if (enderecoDestino == "Bloco K" || enderecoDestino == "BlocoK" || enderecoDestino == "bloco k" || enderecoDestino == "blocok" ||
                enderecoDestino == "Bloco k" || enderecoDestino == "bloco K") {
            val Blocok = LatLng(-3.769582, -38.478810)
            map!!.addMarker(MarkerOptions().position(Blocok).title("Bloco K").icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_BLUE)))
            val cameraPosition = CameraPosition.Builder().target(Blocok).zoom(17f).bearing(83f).build()
            val update = CameraUpdateFactory.newCameraPosition(cameraPosition)
            map!!.animateCamera(update, object : GoogleMap.CancelableCallback {
                override fun onFinish() {

                    circleOptions.center(Blocok)
                    circleOptions.radius(12.0)
                    circleOptions.strokeWidth(5f)
                    circleOptions.strokeColor(Color.argb(255, 0, 0, 255))
                    circleOptions.fillColor(Color.argb(55, 0, 255, 0))
                    map!!.addCircle(circleOptions)
                    circleAp = map!!.addCircle(circleOptions)
                }

                override fun onCancel() {}
            })

            status = true
            linearLayoutDestino!!.visibility = View.GONE
            voltar!!.visibility = View.VISIBLE
        }
        if (enderecoDestino == "Bloco L" || enderecoDestino == "BlocoL" || enderecoDestino == "bloco l" || enderecoDestino == "blocol" ||
                enderecoDestino == "Bloco l" || enderecoDestino == "bloco L") {
            val BlocoL = LatLng(-3.770945, -38.478835)
            map!!.addMarker(MarkerOptions().position(BlocoL).title("Bloco L").icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_BLUE)))
            val cameraPosition = CameraPosition.Builder().target(BlocoL).zoom(17f).bearing(83f).build()
            val update = CameraUpdateFactory.newCameraPosition(cameraPosition)
            map!!.animateCamera(update, object : GoogleMap.CancelableCallback {
                override fun onFinish() {

                    circleOptions.center(BlocoL)
                    circleOptions.radius(12.0)
                    circleOptions.strokeWidth(5f)
                    circleOptions.strokeColor(Color.argb(255, 0, 0, 255))
                    circleOptions.fillColor(Color.argb(55, 0, 255, 0))
                    map!!.addCircle(circleOptions)
                    circleAp = map!!.addCircle(circleOptions)
                }

                override fun onCancel() {}
            })

            status = true
            linearLayoutDestino!!.visibility = View.GONE
            voltar!!.visibility = View.VISIBLE
        }
        if (enderecoDestino == "Bloco M" || enderecoDestino == "BlocoM" || enderecoDestino == "bloco m" || enderecoDestino == "blocom" ||
                enderecoDestino == "Bloco m" || enderecoDestino == "bloco M") {
            val BlocoM = LatLng(-3.768848, -38.478686)
            map!!.addMarker(MarkerOptions().position(BlocoM).title("Bloco M").icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_BLUE)))
            val cameraPosition = CameraPosition.Builder().target(BlocoM).zoom(17f).bearing(83f).build()
            val update = CameraUpdateFactory.newCameraPosition(cameraPosition)
            map!!.animateCamera(update, object : GoogleMap.CancelableCallback {
                override fun onFinish() {

                    circleOptions.center(BlocoM)
                    circleOptions.radius(12.0)
                    circleOptions.strokeWidth(5f)
                    circleOptions.strokeColor(Color.argb(255, 0, 0, 255))
                    circleOptions.fillColor(Color.argb(55, 0, 255, 0))
                    map!!.addCircle(circleOptions)
                    circleAp = map!!.addCircle(circleOptions)
                }

                override fun onCancel() {}
            })

            status = true
            linearLayoutDestino!!.visibility = View.GONE
            voltar!!.visibility = View.VISIBLE
        }
        if (enderecoDestino == "Bloco N" || enderecoDestino == "BlocoN" || enderecoDestino == "bloco n" || enderecoDestino == "blocon" ||
                enderecoDestino == "Bloco n" || enderecoDestino == "bloco N") {
            val BlocoN = LatLng(-3.768067, -38.479177)
            map!!.addMarker(MarkerOptions().position(BlocoN).title("Bloco N").icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_BLUE)))
            val cameraPosition = CameraPosition.Builder().target(BlocoN).zoom(17f).bearing(83f).build()
            val update = CameraUpdateFactory.newCameraPosition(cameraPosition)
            map!!.animateCamera(update, object : GoogleMap.CancelableCallback {
                override fun onFinish() {

                    circleOptions.center(BlocoN)
                    circleOptions.radius(12.0)
                    circleOptions.strokeWidth(5f)
                    circleOptions.strokeColor(Color.argb(255, 0, 0, 255))
                    circleOptions.fillColor(Color.argb(55, 0, 255, 0))
                    map!!.addCircle(circleOptions)
                    circleAp = map!!.addCircle(circleOptions)
                }

                override fun onCancel() {}
            })

            status = true
            linearLayoutDestino!!.visibility = View.GONE
            voltar!!.visibility = View.VISIBLE
        }
        if (enderecoDestino == "Bloco O" || enderecoDestino == "BlocoO" || enderecoDestino == "bloco o" || enderecoDestino == "blocoo" ||
                enderecoDestino == "Bloco o" || enderecoDestino == "bloco O") {
            val BlocoO = LatLng(-3.770612, -38.477507)
            map!!.addMarker(MarkerOptions().position(BlocoO).title("Bloco O").icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_BLUE)))
            val cameraPosition = CameraPosition.Builder().target(BlocoO).zoom(17f).bearing(83f).build()
            val update = CameraUpdateFactory.newCameraPosition(cameraPosition)
            map!!.animateCamera(update, object : GoogleMap.CancelableCallback {
                override fun onFinish() {

                    circleOptions.center(BlocoO)
                    circleOptions.radius(12.0)
                    circleOptions.strokeWidth(5f)
                    circleOptions.strokeColor(Color.argb(255, 0, 0, 255))
                    circleOptions.fillColor(Color.argb(55, 0, 255, 0))
                    map!!.addCircle(circleOptions)
                    circleAp = map!!.addCircle(circleOptions)
                }

                override fun onCancel() {}
            })

            status = true
            linearLayoutDestino!!.visibility = View.GONE
            voltar!!.visibility = View.VISIBLE
        }
        if (enderecoDestino == "Bloco P" || enderecoDestino == "BlocoP" || enderecoDestino == "bloco p" || enderecoDestino == "blocop" ||
                enderecoDestino == "Bloco p" || enderecoDestino == "bloco P") {
            val BlocoP = LatLng(-3.767738, -38.479317)
            map!!.addMarker(MarkerOptions().position(BlocoP).title("Bloco P").icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_BLUE)))
            val cameraPosition = CameraPosition.Builder().target(BlocoP).zoom(17f).bearing(83f).build()
            val update = CameraUpdateFactory.newCameraPosition(cameraPosition)
            map!!.animateCamera(update, object : GoogleMap.CancelableCallback {
                override fun onFinish() {

                    circleOptions.center(BlocoP)
                    circleOptions.radius(12.0)
                    circleOptions.strokeWidth(5f)
                    circleOptions.strokeColor(Color.argb(255, 0, 0, 255))
                    circleOptions.fillColor(Color.argb(55, 0, 255, 0))
                    map!!.addCircle(circleOptions)
                    circleAp = map!!.addCircle(circleOptions)
                }

                override fun onCancel() {}
            })

            status = true
            linearLayoutDestino!!.visibility = View.GONE
            voltar!!.visibility = View.VISIBLE
        }
        if (enderecoDestino == "Bloco Q" || enderecoDestino == "BlocoQ" || enderecoDestino == "bloco q" || enderecoDestino == "blocoq" ||
                enderecoDestino == "Bloco q" || enderecoDestino == "bloco Q") {
            val BlocoQ = LatLng(-3.767431, -38.479476)
            map!!.addMarker(MarkerOptions().position(BlocoQ).title("Bloco Q").icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_BLUE)))
            val cameraPosition = CameraPosition.Builder().target(BlocoQ).zoom(17f).bearing(83f).build()
            val update = CameraUpdateFactory.newCameraPosition(cameraPosition)
            map!!.animateCamera(update, object : GoogleMap.CancelableCallback {
                override fun onFinish() {

                    circleOptions.center(BlocoQ)
                    circleOptions.radius(12.0)
                    circleOptions.strokeWidth(5f)
                    circleOptions.strokeColor(Color.argb(255, 0, 0, 255))
                    circleOptions.fillColor(Color.argb(55, 0, 255, 0))
                    map!!.addCircle(circleOptions)
                    circleAp = map!!.addCircle(circleOptions)
                }

                override fun onCancel() {}
            })

            status = true
            linearLayoutDestino!!.visibility = View.GONE
            voltar!!.visibility = View.VISIBLE
        }
        if (enderecoDestino == "Bloco R" || enderecoDestino == "BlocoR" || enderecoDestino == "bloco r" || enderecoDestino == "blocor" ||
                enderecoDestino == "Bloco r" || enderecoDestino == "bloco R") {
            val BlocoR = LatLng(-3.767136, -38.479643)
            map!!.addMarker(MarkerOptions().position(BlocoR).title("Bloco R").icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_BLUE)))
            val cameraPosition = CameraPosition.Builder().target(BlocoR).zoom(17f).bearing(83f).build()
            val update = CameraUpdateFactory.newCameraPosition(cameraPosition)
            map!!.animateCamera(update, object : GoogleMap.CancelableCallback {
                override fun onFinish() {

                    circleOptions.center(BlocoR)
                    circleOptions.radius(12.0)
                    circleOptions.strokeWidth(5f)
                    circleOptions.strokeColor(Color.argb(255, 0, 0, 255))
                    circleOptions.fillColor(Color.argb(55, 0, 255, 0))
                    map!!.addCircle(circleOptions)
                    circleAp = map!!.addCircle(circleOptions)
                }

                override fun onCancel() {}
            })

            status = true
            linearLayoutDestino!!.visibility = View.GONE
            voltar!!.visibility = View.VISIBLE
        }
        if (enderecoDestino == "Bloco S" || enderecoDestino == "BlocoS" || enderecoDestino == "bloco s" || enderecoDestino == "blocos" ||
                enderecoDestino == "Bloco s" || enderecoDestino == "bloco S") {
            val BlocoS = LatLng(-3.766795, -38.479783)
            map!!.addMarker(MarkerOptions().position(BlocoS).title("Bloco S").icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_BLUE)))
            val cameraPosition = CameraPosition.Builder().target(BlocoS).zoom(17f).bearing(83f).build()
            val update = CameraUpdateFactory.newCameraPosition(cameraPosition)
            map!!.animateCamera(update, object : GoogleMap.CancelableCallback {
                override fun onFinish() {

                    circleOptions.center(BlocoS)
                    circleOptions.radius(12.0)
                    circleOptions.strokeWidth(5f)
                    circleOptions.strokeColor(Color.argb(255, 0, 0, 255))
                    circleOptions.fillColor(Color.argb(55, 0, 255, 0))
                    map!!.addCircle(circleOptions)
                    circleAp = map!!.addCircle(circleOptions)
                }

                override fun onCancel() {}
            })

            status = true
            linearLayoutDestino!!.visibility = View.GONE
            voltar!!.visibility = View.VISIBLE
        }
        if (enderecoDestino == "Bloco T" || enderecoDestino == "BlocoT" || enderecoDestino == "bloco t" || enderecoDestino == "blocot" ||
                enderecoDestino == "Bloco t" || enderecoDestino == "bloco T") {
            val BlocoT = LatLng(-3.767605, -38.480239)
            map!!.addMarker(MarkerOptions().position(BlocoT).title("Bloco T").icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_BLUE)))

            val cameraPosition = CameraPosition.Builder().target(BlocoT).zoom(17f).bearing(83f).build()
            val update = CameraUpdateFactory.newCameraPosition(cameraPosition)
            map!!.animateCamera(update, object : GoogleMap.CancelableCallback {
                override fun onFinish() {

                    circleOptions.center(BlocoT)
                    circleOptions.radius(12.0)
                    circleOptions.strokeWidth(5f)
                    circleOptions.strokeColor(Color.argb(255, 0, 0, 255))
                    circleOptions.fillColor(Color.argb(55, 0, 255, 0))
                    map!!.addCircle(circleOptions)
                    circleAp = map!!.addCircle(circleOptions)
                }

                override fun onCancel() {}
            })

            status = true
            linearLayoutDestino!!.visibility = View.GONE
            voltar!!.visibility = View.VISIBLE
        }
        if (enderecoDestino == "Bloco Z" || enderecoDestino == "BlocoZ" || enderecoDestino == "bloco z" || enderecoDestino == "blocoz" ||
                enderecoDestino == "Bloco z" || enderecoDestino == "bloco Z") {
            val BlocoZ = LatLng(-3.769269, -38.474417)
            map!!.addMarker(MarkerOptions().position(BlocoZ).title("Bloco Z").icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_BLUE)))

            val cameraPosition = CameraPosition.Builder().target(BlocoZ).zoom(17f).bearing(83f).build()
            val update = CameraUpdateFactory.newCameraPosition(cameraPosition)
            map!!.animateCamera(update, object : GoogleMap.CancelableCallback {
                override fun onFinish() {

                    circleOptions.center(BlocoZ)
                    circleOptions.radius(12.0)
                    circleOptions.strokeWidth(5f)
                    circleOptions.strokeColor(Color.argb(255, 0, 0, 255))
                    circleOptions.fillColor(Color.argb(55, 0, 255, 0))
                    map!!.addCircle(circleOptions)
                    circleAp = map!!.addCircle(circleOptions)
                }

                override fun onCancel() {}
            })

            status = true
            linearLayoutDestino!!.visibility = View.GONE
            voltar!!.visibility = View.VISIBLE
        }
        if (enderecoDestino == "Centro de Convivência" || enderecoDestino == "Centro de convivencia" || enderecoDestino == "centro de convivencia" || enderecoDestino == "CC" || enderecoDestino == "cc") {
            val BlocoCC = LatLng(-3.769199, -38.479685)
            map!!.addMarker(MarkerOptions().position(BlocoCC).title("Centro de Convivência (CC)").icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_YELLOW)))

            val cameraPosition = CameraPosition.Builder().target(BlocoCC).zoom(17f).bearing(83f).build()
            val update = CameraUpdateFactory.newCameraPosition(cameraPosition)
            map!!.animateCamera(update, object : GoogleMap.CancelableCallback {
                override fun onFinish() {

                    circleOptions.center(BlocoCC)
                    circleOptions.radius(12.0)
                    circleOptions.strokeWidth(5f)
                    circleOptions.strokeColor(Color.argb(255, 0, 0, 255))
                    circleOptions.fillColor(Color.argb(55, 0, 255, 0))
                    map!!.addCircle(circleOptions)
                    circleAp = map!!.addCircle(circleOptions)
                }

                override fun onCancel() {}
            })

            status = true
            linearLayoutDestino!!.visibility = View.GONE
            voltar!!.visibility = View.VISIBLE
        }
        if (enderecoDestino == "Biblioteca" || enderecoDestino == "biblioteca") {
            val BlocoBibli = LatLng(-3.768947, -38.480581)
            map!!.addMarker(MarkerOptions().position(BlocoBibli).title("Biblioteca").icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_YELLOW)))

            val cameraPosition = CameraPosition.Builder().target(BlocoBibli).zoom(17f).bearing(83f).build()
            val update = CameraUpdateFactory.newCameraPosition(cameraPosition)
            map!!.animateCamera(update, object : GoogleMap.CancelableCallback {
                override fun onFinish() {

                    circleOptions.center(BlocoBibli)
                    circleOptions.radius(12.0)
                    circleOptions.strokeWidth(5f)
                    circleOptions.strokeColor(Color.argb(255, 0, 0, 255))
                    circleOptions.fillColor(Color.argb(55, 0, 255, 0))
                    map!!.addCircle(circleOptions)
                    circleAp = map!!.addCircle(circleOptions)
                }

                override fun onCancel() {}
            })

            status = true
            linearLayoutDestino!!.visibility = View.GONE
            voltar!!.visibility = View.VISIBLE
        }
        if (enderecoDestino == "Prefeitura" || enderecoDestino == "prefeitura") {
            val BlocoPref = LatLng(-3.770579, -38.479149)
            map!!.addMarker(MarkerOptions().position(BlocoPref).title("Prefeitura").icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_YELLOW)))

            val cameraPosition = CameraPosition.Builder().target(BlocoPref).zoom(17f).bearing(83f).build()
            val update = CameraUpdateFactory.newCameraPosition(cameraPosition)
            map!!.animateCamera(update, object : GoogleMap.CancelableCallback {
                override fun onFinish() {

                    circleOptions.center(BlocoPref)
                    circleOptions.radius(12.0)
                    circleOptions.strokeWidth(5f)
                    circleOptions.strokeColor(Color.argb(255, 0, 0, 255))
                    circleOptions.fillColor(Color.argb(55, 0, 255, 0))
                    map!!.addCircle(circleOptions)
                    circleAp = map!!.addCircle(circleOptions)
                }

                override fun onCancel() {}
            })

            status = true
            linearLayoutDestino!!.visibility = View.GONE
            voltar!!.visibility = View.VISIBLE
        }
        if (enderecoDestino == "Central Administrativa" || enderecoDestino == "central administrativa" || enderecoDestino == "DAE" || enderecoDestino == "dae") {
            val BlocoDAE = LatLng(-3.768985, -38.481205)
            map!!.addMarker(MarkerOptions().position(BlocoDAE).title("Central Administrativa DAE").icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_YELLOW)))

            val cameraPosition = CameraPosition.Builder().target(BlocoDAE).zoom(17f).bearing(83f).build()
            val update = CameraUpdateFactory.newCameraPosition(cameraPosition)
            map!!.animateCamera(update, object : GoogleMap.CancelableCallback {
                override fun onFinish() {

                    circleOptions.center(BlocoDAE)
                    circleOptions.radius(12.0)
                    circleOptions.strokeWidth(5f)
                    circleOptions.strokeColor(Color.argb(255, 0, 0, 255))
                    circleOptions.fillColor(Color.argb(55, 0, 255, 0))
                    map!!.addCircle(circleOptions)
                    circleAp = map!!.addCircle(circleOptions)
                }

                override fun onCancel() {}
            })
            status = true
            linearLayoutDestino!!.visibility = View.GONE
            voltar!!.visibility = View.VISIBLE
        }
        if (enderecoDestino == "Nucleo de atendimento" || enderecoDestino == "nucleo de atendimento" || enderecoDestino == "Nucleo de Atendimento" ||
                enderecoDestino == "Nami" || enderecoDestino == "nami") {
            val Nami = LatLng(-3.771970, -38.480071)
            map!!.addMarker(MarkerOptions().position(Nami).title("Núcleo de Atenção Medica Integrada (Nami)").icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_YELLOW)))

            val cameraPosition = CameraPosition.Builder().target(Nami).zoom(17f).bearing(83f).build()
            val update = CameraUpdateFactory.newCameraPosition(cameraPosition)
            map!!.animateCamera(update, object : GoogleMap.CancelableCallback {
                override fun onFinish() {

                    circleOptions.center(Nami)
                    circleOptions.radius(12.0)
                    circleOptions.strokeWidth(5f)
                    circleOptions.strokeColor(Color.argb(255, 0, 0, 255))
                    circleOptions.fillColor(Color.argb(55, 0, 255, 0))
                    map!!.addCircle(circleOptions)
                    circleAp = map!!.addCircle(circleOptions)
                }

                override fun onCancel() {}
            })
            status = true
            linearLayoutDestino!!.visibility = View.GONE
            voltar!!.visibility = View.VISIBLE
        }
        if (enderecoDestino == "Ginásio Poliesportivo" || enderecoDestino == "ginásio poliesportivo" || enderecoDestino == "Ginásio" || enderecoDestino == "ginásio" ||
                enderecoDestino == "Ginasio" || enderecoDestino == "ginasio") {
            val ginasio = LatLng(-3.769736, -38.477200)
            map!!.addMarker(MarkerOptions().position(ginasio).title("Ginásio Poliesportivo").icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_YELLOW)))

            val cameraPosition = CameraPosition.Builder().target(ginasio).zoom(17f).bearing(83f).build()
            val update = CameraUpdateFactory.newCameraPosition(cameraPosition)
            map!!.animateCamera(update, object : GoogleMap.CancelableCallback {
                override fun onFinish() {

                    circleOptions.center(ginasio)
                    circleOptions.radius(12.0)
                    circleOptions.strokeWidth(5f)
                    circleOptions.strokeColor(Color.argb(255, 0, 0, 255))
                    circleOptions.fillColor(Color.argb(55, 0, 255, 0))
                    map!!.addCircle(circleOptions)
                    circleAp = map!!.addCircle(circleOptions)
                }

                override fun onCancel() {}
            })

            status = true
            linearLayoutDestino!!.visibility = View.GONE
            voltar!!.visibility = View.VISIBLE
        }
        if (enderecoDestino == "Estádio de Atletismo" || enderecoDestino == "estádio de atletismo" || enderecoDestino == "estádio" || enderecoDestino == "Estádio" ||
                enderecoDestino == "atletismo" || enderecoDestino == "Atletismo") {
            val atletismo = LatLng(-3.768777, -38.475438)
            map!!.addMarker(MarkerOptions().position(atletismo).title("Estádio de Atletismo").icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_YELLOW)))

            val cameraPosition = CameraPosition.Builder().target(atletismo).zoom(17f).bearing(83f).build()
            val update = CameraUpdateFactory.newCameraPosition(cameraPosition)
            map!!.animateCamera(update, object : GoogleMap.CancelableCallback {
                override fun onFinish() {

                    circleOptions.center(atletismo)
                    circleOptions.radius(12.0)
                    circleOptions.strokeWidth(5f)
                    circleOptions.strokeColor(Color.argb(255, 0, 0, 255))
                    circleOptions.fillColor(Color.argb(55, 0, 255, 0))
                    map!!.addCircle(circleOptions)
                    circleAp = map!!.addCircle(circleOptions)
                }

                override fun onCancel() {}
            })
            status = true
            linearLayoutDestino!!.visibility = View.GONE
            voltar!!.visibility = View.VISIBLE
        }

    }

    fun deleteCircles() {
        if (circleAp != null) {
            circleAp!!.remove()
            //deleteCircles();
        }
    }

    companion object {
        private val TAG = "Mapa"
    }
    override fun onBackPressed() {
        finish()
    }

}