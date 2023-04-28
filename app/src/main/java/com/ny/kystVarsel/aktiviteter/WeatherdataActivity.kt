package com.ny.kystVarsel.aktiviteter
import android.Manifest
import android.annotation.SuppressLint
import android.app.Notification
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.icu.text.SimpleDateFormat
import android.location.Location
import android.location.LocationListener
import android.location.LocationManager
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.*
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.cardview.widget.CardView
import androidx.core.app.ActivityCompat
import androidx.core.app.NotificationCompat
import androidx.core.content.ContextCompat
import androidx.drawerlayout.widget.DrawerLayout
import androidx.navigation.findNavController
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.navigateUp
import androidx.navigation.ui.setupActionBarWithNavController
import androidx.navigation.ui.setupWithNavController
import com.github.kittinunf.fuel.Fuel
import com.github.kittinunf.fuel.coroutines.awaitString
import com.google.android.gms.maps.model.LatLng
import com.google.android.material.navigation.NavigationView
import com.ny.kystVarsel.R
import com.ny.kystVarsel.dataClasses.Info
import com.ny.kystVarsel.dataClasses.Sted
import com.ny.kystVarsel.primarParser.TempParser
import kotlinx.android.synthetic.main.activity_lagret.*
import kotlinx.android.synthetic.main.sted.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.util.*


@Suppress("BlockingMethodInNonBlockingContext", "DEPRECATION")
class WeatherdataActivity : AppCompatActivity(), LocationListener {

    private lateinit var appBarConfiguration: AppBarConfiguration
    var bundle: Bundle? = null
    private lateinit var locationManager: LocationManager
    private val locationPermissionCode = 2

    private lateinit var cardViewDato: CardView
    private lateinit var cardViewWeatherData: CardView
    private lateinit var cardViewWeatherSymbol: CardView
    private lateinit var cardViewBoatBilde: CardView
    private lateinit var cardViewTemperatur: CardView

    private lateinit var tekstIkkeTilgang: TextView
    private lateinit var temperatur: TextView
    private lateinit var symbolBilde: ImageView
    private lateinit var tilgang: Button
    private var myList:ArrayList<Info?>? = null

    private lateinit var skydekkeData: TextView
    private lateinit var fuktighetData: TextView
    private lateinit var taakeData: TextView
    private lateinit var vindstyrkeData: TextView

    private lateinit var cartBadge: TextView
    private lateinit var imageView: ImageView

    private lateinit var dateTimeDisplay: TextView

    @RequiresApi(Build.VERSION_CODES.N)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_weatherdata)

        setView()

        val weatherdataKnapp : ImageButton = findViewById(R.id.weatherdataKnapp)
        weatherdataKnapp.setImageResource(R.drawable.ic_weatherdata_knapp_clicked)

        supportActionBar?.setHomeAsUpIndicator(R.drawable.ic_meny)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.setDisplayShowTitleEnabled(false)

        bundle = intent.getBundleExtra("Bundle")
        myList = bundle?.getParcelableArrayList<Info?>("key")

        if (myList?.size !=0){
            cartBadge.visibility = View.VISIBLE
            cartBadge.text = myList?.size.toString()
        }

        sjekkLokasjon()
    }

    @SuppressLint("SimpleDateFormat")
    @RequiresApi(Build.VERSION_CODES.N)
    private fun setView() {
        val toolbar: Toolbar = findViewById(R.id.toolbar)
        setSupportActionBar(toolbar)

        val drawerLayout: DrawerLayout = findViewById(R.id.drawer_layout)
        val navView: NavigationView = findViewById(R.id.nav_view)
        val navController = findNavController(R.id.nav_host_fragment)

        cardViewBoatBilde = findViewById(R.id.boatBilde)
        cardViewDato = findViewById(R.id.dato)
        cardViewTemperatur = findViewById(R.id.temperatur)
        cardViewWeatherData = findViewById(R.id.weatherData)
        cardViewWeatherSymbol = findViewById(R.id.weatherSymbol)

        skydekkeData = findViewById(R.id.skyDekkeData)
        fuktighetData = findViewById(R.id.fuktighetData)
        taakeData = findViewById(R.id.taakeData)
        vindstyrkeData = findViewById(R.id.vindstyrkeData)

        tekstIkkeTilgang = findViewById(R.id.tekst_ikke_tilgang)
        temperatur = findViewById(R.id.temp)
        symbolBilde = findViewById(R.id.symbolBilde)
        tilgang = findViewById(R.id.tilgangKnapp)
        cartBadge = findViewById(R.id.cart_badge)
        imageView = findViewById(R.id.baat)

        val c = Calendar.getInstance().time
        println("Current time => $c")

        val df = SimpleDateFormat("EEE, MMM d, ''yy", Locale.getDefault())
        val formattedDate = df.format(c)

        dateTimeDisplay = findViewById(R.id.dateTimeDisplay)
        dateTimeDisplay.text = formattedDate

        appBarConfiguration = AppBarConfiguration(setOf(R.id.nav_fareSymboler, R.id.nav_nodetater, R.id.nav_lagetAv, R.id.nav_hjem), drawerLayout)
        setupActionBarWithNavController(navController, appBarConfiguration)
        navView.setupWithNavController(navController)
    }

    private fun sjekkLokasjon() {
        val permission:String = Manifest.permission.ACCESS_FINE_LOCATION

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) { //sjekker om SDK version er større lik 23
            when { //sjekker videre om vi har permisjon
                ContextCompat.checkSelfPermission(applicationContext, permission) == PackageManager.PERMISSION_GRANTED -> {
                    hentLokasjon() //hvis ja hent lokasjonen
                }
                shouldShowRequestPermissionRationale(permission) -> visDialogBoks()
                else -> { //spør for permisjon i gjen
                    ActivityCompat.requestPermissions(this, arrayOf(permission), 101)
                }//check for permisson
            }
        }

    }
    //Source: https://www.tutorialspoint.com/how-to-get-the-current-gps-location-programmatically-on-android-using-kotlin
    @SuppressLint("MissingPermission") //sjekker allerede om vi har fått tilgang på lokasjon før denne blir kalt på
    private fun hentLokasjon() {
        locationManager = getSystemService(Context.LOCATION_SERVICE) as LocationManager
        if ((ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED)) {
            ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.ACCESS_FINE_LOCATION), locationPermissionCode)
        }
        locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 5000, 5f, this)
    }
    override fun onLocationChanged(location: Location) {
        hentData(location)
    }

    override fun onStatusChanged(provider: String?, status: Int, extras: Bundle?) {
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        @SuppressLint("SetTextI18n")
        fun innerCheck() {
            if (grantResults.isEmpty() || grantResults[0] != PackageManager.PERMISSION_GRANTED) {
                Toast.makeText(
                        applicationContext,
                        "Vi fikk ikke tilgang til din lokasjon",
                        Toast.LENGTH_SHORT
                ).show()
                tilgang.visibility = View.VISIBLE
                tekstIkkeTilgang.visibility = View.VISIBLE

                cardViewBoatBilde.visibility = View.INVISIBLE
                cardViewDato.visibility = View.INVISIBLE
                cardViewTemperatur.visibility = View.INVISIBLE
                cardViewWeatherData.visibility = View.INVISIBLE
                cardViewWeatherSymbol.visibility = View.INVISIBLE


                tilgang.setOnClickListener{
                    val permission:String = Manifest.permission.ACCESS_FINE_LOCATION
                    ActivityCompat.requestPermissions(this, arrayOf(permission), 101)
                }
            } else {
                Toast.makeText(
                        applicationContext,
                        "Du ga oss tilgang til din lokasjon",
                        Toast.LENGTH_SHORT
                ).show()

                hentLokasjon()

                cardViewBoatBilde.visibility = View.VISIBLE
                cardViewDato.visibility = View.VISIBLE
                cardViewTemperatur.visibility = View.VISIBLE
                cardViewWeatherData.visibility = View.VISIBLE
                cardViewWeatherSymbol.visibility = View.VISIBLE

                tilgang.visibility = View.INVISIBLE
                tekstIkkeTilgang.visibility = View.INVISIBLE

            }
        }
        innerCheck()
    }



    @SuppressLint("SetTextI18n")
    private fun hentData(lokasjon: Location){
        val symboler: HashMap<String, Int> = lagHasMap()
        val link = "https://in2000-apiproxy.ifi.uio.no/weatherapi/locationforecast/2.0/classic.xml?lat=" +lokasjon.latitude +"&lon=" + lokasjon.longitude

        CoroutineScope(Dispatchers.IO).launch{
            val response = Fuel.get(link).awaitString()
            val inputStream = response.byteInputStream()
            var temp:String? = null
            var skyDekke:String? = null
            var taake: String? = null
            var fuktighet:String? = null
            var vindStyrke: String? = null

            var symbolNr: String? = null
            var symbolKode: String? = null

            val info = TempParser().parse(inputStream)
            val timeList = info?.time

            if (timeList != null) {
                val location = timeList[0].location
                temp = location?.temperature?.value
                skyDekke = location?.cloudiness?.percent
                taake = location?.fog?.percent
                fuktighet = location?.humidity?.value
                vindStyrke = location?.windSpeed?.mps

                val lokasjon2 = timeList[1].location
                symbolNr = lokasjon2?.symbol?.number
                symbolKode = lokasjon2?.symbol?.code
            }
            runOnUiThread { //plott informasjon om værdata hos bruker
                val tempTekst = "$temp°"
                val tempTittel = temperatur.text

                temperatur.text = "$tempTittel $tempTekst"

                skydekkeData.text = skyDekke
                fuktighetData.text = fuktighet
                val taakeTekst = "$taake%"
                taakeData.text = taakeTekst
                vindstyrkeData.text = vindStyrke

                val symbolWeather = symbolKode + symbolNr

                imageView.setImageResource(R.drawable.gladbaat)

                val bilde = symboler[symbolWeather]
                if (bilde != null) {
                    symbolBilde.setImageResource(bilde)
                }
            }
        }
    }

    private fun lagHasMap(): HashMap<String, Int>{
        val symboler: HashMap<String, Int> = HashMap()

        symboler["clearsky_day1"] = R.drawable.day1
        symboler["clearsky_midnight1"] = R.drawable.midnight1
        symboler["clearsky_night1"] = R.drawable.night1
        symboler["cloudy4"] = R.drawable.cloudy4
        symboler["fair_day2"] = R.drawable.day2
        symboler["fair_midnight2"] = R.drawable.midnight2
        symboler["fair_night2"] = R.drawable.night2
        symboler["fog15"] = R.drawable.fog15
        symboler["heavyrain10"] = R.drawable.heavyrain10
        symboler["heavyrainandthunder11"] = R.drawable.heavyrainandthunder11
        symboler["heavyrainshowers_day41"] = R.drawable.day41
        symboler["heavyrainshowers_midnight41"] = R.drawable.midnight41
        symboler["heavyrainshowers_night41"] = R.drawable.night41
        symboler["heavyrainsandthunder_day25"] = R.drawable.day25
        symboler["heavyrainsandthunder_midnight25"] = R.drawable.midnight25
        symboler["heavyrainsandthunder_night25"] = R.drawable.night25
        symboler["heavysleet48"] = R.drawable.heavysleet48
        symboler["heavysleetandthunder32"] = R.drawable.heavysleetandthunder32
        symboler["heavysleetshowers_day43"] = R.drawable.day43
        symboler["heavysleetshowers_midnight43"] = R.drawable.midnight43
        symboler["heavysleetshowers_night43"] = R.drawable.night43
        symboler["heavysleetshowersandthunder_day27"] = R.drawable.day27
        symboler["heavysleetshowersandthunder_midnight27"] = R.drawable.midnight27
        symboler["heavysleetshowersandthunder_night27"] = R.drawable.night27
        symboler["heavysnow50"] = R.drawable.heavysnow50
        symboler["heavysnowandthunder34"] = R.drawable.heavysnowandthunder34
        symboler["heavysnowshowers_day45"] = R.drawable.day45
        symboler["heavysnowshowers_midnight45"] = R.drawable.midnight45
        symboler["heavysnowshowers_night45"] = R.drawable.night45
        symboler["heavysnowshowersandthunder_day29"] = R.drawable.day29
        symboler["heavysnowshowersandthunder_midnight29"] = R.drawable.midnight29
        symboler["heavysnowshowersandthunder_night29"] = R.drawable.night29
        symboler["lightrain46"] = R.drawable.lightrain46
        symboler["lightrainandthunder30"] = R.drawable.lightrainandthunder30
        symboler["lightrainshowers_day40"] = R.drawable.day40
        symboler["lightrainshowers_midnight40"] = R.drawable.midnight40
        symboler["lightrainshowers_night40"] = R.drawable.night40
        symboler["lightrainshowersandthunder_day24"] = R.drawable.day24
        symboler["lightrainshowersandthunder_midnight24"] = R.drawable.midnight24
        symboler["lightrainshowersandthunder_night24"] = R.drawable.night24
        symboler["lightsleet47"] = R.drawable.lightsleet47
        symboler["lightsleetandthunder31"] = R.drawable.lightsleetandthunder31
        symboler["lightsleetshowers_day42"] = R.drawable.day42
        symboler["lightsleetshowers_midnight42"] = R.drawable.midnight42
        symboler["lightsleetshowers_night42"] = R.drawable.night42
        symboler["lightsnow49"] = R.drawable.lightsnow49
        symboler["lightsnowandthunder33"] = R.drawable.lightsnowandthunder33
        symboler["lightsnowshowers_day44"] = R.drawable.day44
        symboler["lightsnowshowers_midnight44"] = R.drawable.midnight44
        symboler["lightsnowshowers_night44"] = R.drawable.night44
        symboler["lightssleetshowersandthunder_day26"] = R.drawable.day26
        symboler["lightssleetshowersandthunder_midnight26"] = R.drawable.midnight26
        symboler["lightssleetshowersandthunder_night26"] = R.drawable.night26
        symboler["lightssnowshowersandthunder_day28"] = R.drawable.day28
        symboler["lightssnowshowersandthunder_midnight28"] = R.drawable.midnight28
        symboler["lightssnowshowersandthunder_night28"] = R.drawable.night28
        symboler["partlycloudy_day3"] = R.drawable.day3
        symboler["partlycloudy_midnight3"] = R.drawable.midnight3
        symboler["partlycloudy_night3"] = R.drawable.night3
        symboler["rain9"] = R.drawable.rain9
        symboler["rainandthunder22"] = R.drawable.rainandthunder22
        symboler["rainshowers_day5"] = R.drawable.day5
        symboler["rainshowers_midnight5"] = R.drawable.midnight5
        symboler["rainshowers_night5"] = R.drawable.night5
        symboler["rainshowersandthunder_day6"] = R.drawable.day6
        symboler["rainshowersandthunder_midnight6"] = R.drawable.midnight6
        symboler["rainshowersandthunder_night6"] = R.drawable.night6
        symboler["sleet12"] = R.drawable.sleet12
        symboler["sleetandthunder23"] = R.drawable.sleetandthunder23
        symboler["sleetshowers_day7"] = R.drawable.day7
        symboler["sleetshowers_midnight7"] = R.drawable.midnight7
        symboler["sleetshowers_night7"] = R.drawable.night7
        symboler["sleetshowersandthunder_day20"] = R.drawable.day20
        symboler["sleetshowersandthunder_midnight20"] = R.drawable.midnight20
        symboler["sleetshowersandthunder_night20"] = R.drawable.night20
        symboler["snow13"] = R.drawable.snow13
        symboler["snowandthunder14"] = R.drawable.snowandthunder14
        symboler["snowshowers_day8"] = R.drawable.day8
        symboler["snowshowers_midnight8"] = R.drawable.midnight8
        symboler["snowshowers_night8"] = R.drawable.night8
        symboler["snowshowersandthunder_day21"] = R.drawable.day21
        symboler["snowshowersandthunder_midnight21"] = R.drawable.midnight21
        symboler["snowshowersandthunder_night21"] = R.drawable.night21

        return symboler
    }

    private fun visDialogBoks(){
        val builder = AlertDialog.Builder(this)
        builder.apply {
            setMessage("For å bruke denne funksjonen trenger vi permisjon til å hente din lokasjon")
            setTitle("Lokasjon trengs")
            setPositiveButton("OK") { _, _ ->
                ActivityCompat.requestPermissions(this@WeatherdataActivity, arrayOf(Manifest.permission.ACCESS_FINE_LOCATION), 101)
            }
        }
        val dialog = builder.create()
        dialog.show()
    }

    fun restartActivity(view: View) {
        var myIntent = intent
        when (view.id) {
            R.id.kartKnapp -> {
                myIntent = Intent(this, MainActivity::class.java)
            }
            R.id.farerKnapp -> {
                myIntent = Intent(this, FarerActivity::class.java)
            }
            R.id.lagretKnapp -> {
                myIntent = Intent(this, LagretActivity::class.java)
            }
            R.id.weatherdataKnapp -> {
                myIntent = Intent(this, WeatherdataActivity::class.java)
            }
        }

        val stedListe = bundle?.getParcelableArrayList<Sted?>("list")


        val koor = bundle?.getParcelable<LatLng>("markerKoor")

        bundle?.putParcelable("markerKoor", koor)
        bundle?.putParcelableArrayList("list", stedListe)
        bundle?.putParcelableArrayList("key", myList)
        myIntent.putExtra("Bundle", bundle)

        myIntent.flags = Intent.FLAG_ACTIVITY_NO_ANIMATION
        startActivity(myIntent)
    }

    override fun onSupportNavigateUp(): Boolean {
        val navController = findNavController(R.id.nav_host_fragment)
        return navController.navigateUp(appBarConfiguration) || super.onSupportNavigateUp()
    }

    @SuppressLint("MissingPermission")
    private fun addNotification() {
        val builder = NotificationCompat.Builder(this)
                .setSmallIcon(R.drawable.ic_alert)
                .setContentTitle("Du er i nærheten av en fare!")
                .setContentText("Pass på deg selv!")
                .setDefaults(Notification.DEFAULT_SOUND)
                .setPriority(NotificationCompat.PRIORITY_MAX)

        val notificationIntent = Intent(this, MainActivity::class.java)
        val contentIntent = PendingIntent.getActivity(this, 0, notificationIntent,
                PendingIntent.FLAG_ONE_SHOT)
        builder.setContentIntent(contentIntent)

        // Add as notification
        val manager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        manager.notify(0, builder.build())
    }

}