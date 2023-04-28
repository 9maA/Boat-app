package com.ny.kystVarsel.aktiviteter

import android.content.Intent
import android.location.Location
import android.location.LocationManager
import android.os.Bundle
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.drawerlayout.widget.DrawerLayout
import androidx.navigation.findNavController
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.navigateUp
import androidx.navigation.ui.setupActionBarWithNavController
import androidx.navigation.ui.setupWithNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.room.Room
import com.github.kittinunf.fuel.Fuel
import com.github.kittinunf.fuel.coroutines.awaitString
import com.google.android.gms.maps.model.LatLng
import com.google.android.material.navigation.NavigationView
import com.ny.kystVarsel.R
import com.ny.kystVarsel.adaptere.LagretAdapter
import com.ny.kystVarsel.dataBase.AppDatabase
import com.ny.kystVarsel.dataBase.StedDAO
import com.ny.kystVarsel.dataClasses.Info
import com.ny.kystVarsel.dataClasses.OceanForecast
import com.ny.kystVarsel.dataClasses.Sted
import com.ny.kystVarsel.dataClasses.StedInfo
import com.ny.kystVarsel.primarParser.OceanParser
import com.ny.kystVarsel.primarParser.TempParser
import kotlinx.android.synthetic.main.spinner_baatklasse_design.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.io.InputStream
import java.util.*


//Aktivitet for å vise lagrede steder
@Suppress("BlockingMethodInNonBlockingContext")
class LagretActivity : AppCompatActivity(), AdapterView.OnItemSelectedListener {

    //Globare variabler
    private lateinit var appBarConfiguration: AppBarConfiguration
    private lateinit var recyclerview: RecyclerView
    private lateinit var viewAdapder: RecyclerView.Adapter<*>
    private lateinit var viewManager: RecyclerView.LayoutManager
    private lateinit var steder: MutableList<Sted?>
    private lateinit var stedDAO: StedDAO
    private lateinit var infoSteder: MutableList<StedInfo?>
    private lateinit var cartBadge: TextView

    private var stedListe: ArrayList<Sted>? = null
    var bundle: Bundle? = null
    private var myList:ArrayList<Info?>? = null

    private lateinit var textView: TextView
    private lateinit var steg: TextView
    private lateinit var steg1: TextView
    private lateinit var steg2: TextView
    private lateinit var steg3: TextView
    private lateinit var stegBilde: ImageView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_lagret)

        //Endrer farge til knappen for å vise at aktiviteten kjører
        val lagretKnapp: ImageButton = findViewById(R.id.lagretKnapp)
        lagretKnapp.setImageResource(R.drawable.ic_lagret_knapp_clicked)

        //Setter opp hovedfunksjonene
        setView()
        //Henter databasen
        initDatabase()

        //Henter liste med farer
        bundle = intent.getBundleExtra("Bundle")
        myList = bundle?.getParcelableArrayList<Info?>("key")

        if (myList?.size != 0) { //display antall farer over fareknappen hvis str ikke er 0
            cartBadge.visibility = View.VISIBLE
            cartBadge.text = myList?.size.toString()
        }
        0.displaySted()
    }

    //Setter view
    private fun setView() {
        //Setter toolbaren til custom toolbar
        val toolbar: Toolbar = findViewById(R.id.toolbar)
        setSupportActionBar(toolbar)

        //Setter sammen drawerlayout for hamburgermeny
        val drawerLayout: DrawerLayout = findViewById(R.id.drawer_layout)
        val navView: NavigationView = findViewById(R.id.nav_view)
        val navController = findNavController(R.id.nav_host_fragment)
        cartBadge = findViewById(R.id.cart_badge)
        appBarConfiguration = AppBarConfiguration(setOf(R.id.nav_fareSymboler, R.id.nav_nodetater, R.id.nav_lagetAv, R.id.nav_hjem), drawerLayout)
        setupActionBarWithNavController(navController, appBarConfiguration)
        navView.setupWithNavController(navController)

        //Henter textviewet for å vise at det ikke er noen lagrede
        //Henter tekstview med alle steg man skal følge dersom du ikke har noen lagrede
        textView = findViewById(R.id.ingenLagrede)
        steg = findViewById(R.id.steg)
        steg1 = findViewById(R.id.steg1)
        steg2 = findViewById(R.id.steg2)
        steg3 = findViewById(R.id.steg3)
        stegBilde = findViewById(R.id.stegBilde)

        //Setter opp hamburgermenyen til toolbaren
        supportActionBar?.setHomeAsUpIndicator(R.drawable.ic_meny)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.setDisplayShowTitleEnabled(false)
    }

    //Henter databasen
    private fun initDatabase() {
        //Source: https://developer.android.com/training/data-storage/room
        //Henter database
        val db = Room.databaseBuilder(applicationContext, AppDatabase::class.java, "lagrede-steder")
            .allowMainThreadQueries()
            .build()

        stedDAO = db.stedDao() //hent databasen
    }

    //Viser et sted som er lagret
    private fun Int.displaySted() {
      
        stedListe = bundle?.getParcelableArrayList("list") //hent listen med steder
      
        //Lager nye lister
        steder = mutableListOf()
        infoSteder = mutableListOf()

        //Legger til elementer i stedslisten
        if (stedListe != null) { //gjør om til mutablelist
            for (i in stedListe!!){
                steder.add(i)
            }
        }
        sjekkIngenLagrede(steder.size) //sjekk om vi har lagrede farer

        //Kjører i coroutines for å gjøre API-kall

        CoroutineScope(Dispatchers.IO).launch {
            infoSteder = gjorAPI(steder)

            runOnUiThread{ //gir info til adapter
                viewManager = LinearLayoutManager(this@LagretActivity)
                viewAdapder = LagretAdapter(infoSteder, this@LagretActivity, this@displaySted)

                recyclerview = findViewById<RecyclerView>(R.id.recyclerView).apply {
                    layoutManager = viewManager
                    adapter = viewAdapder
                }
            }
        }
    }

    //Sjekker om det er noen lagrede steder
    fun sjekkIngenLagrede(antall: Int): Boolean{
        //Gjør textviewene synlige
        if(antall == 0) {
            stegBilde.visibility = View.VISIBLE
            textView.visibility = View.VISIBLE
            steg.visibility = View.VISIBLE
            steg1.visibility = View.VISIBLE
            steg2.visibility = View.VISIBLE
            steg3.visibility = View.VISIBLE

            //Source: https://developer.android.com/reference/android/app/Activity#invalidateOptionsMenu()
            //Sier at menyen har endret seg, derfor må den lages på nytt
            invalidateOptionsMenu()

            return true
        }
        return false
    }

    fun fjernSted(t: String): Boolean { //kalles på av adapter når søppel kasse på cardview blir trykket på
        val ind = finnSted(t)
        val stedFjernes = stedListe?.get(ind)
        stedListe?.removeAt(ind)
        if (stedFjernes != null) {
            stedDAO.delete(stedFjernes) //fjren fra databasen
            stedListe?.size?.let {
                sjekkIngenLagrede(it) } //sjekk om ingen lagrede igjen
        }
        else {
            return false
        }
        return true
    }

    //Finner et sted i listen
    private fun finnSted(t: String):Int{
        if (stedListe?.size != 0){
            for(i in stedListe?.indices!!){
                if (stedListe?.get(i)?.tittel==t) return i
            }
        }
        return -1
    }

    //Gjør api kall
    private suspend fun gjorAPI(steder: MutableList<Sted?>): MutableList<StedInfo?>{
        val infoFerdig = mutableListOf<StedInfo?>()

        for (s in steder) {
            var bolgeHoyde: String? = null
            var vindRetning: String? = null
            var vindStyrke: String? = null

            //Henter apiene
            val oceanList = mutableListOf<OceanForecast>()
            val oceanForcast = "https://in2000-apiproxy.ifi.uio.no/weatherapi/oceanforecast/0.9/?lat=" + s?.lat + "&lon=" + s?.lng
            val locationForcast = "https://in2000-apiproxy.ifi.uio.no/weatherapi/locationforecast/2.0/classic?lat=" + s?.lat + "&lon=" + s?.lng

            //Kjører response
            val responseOcean = Fuel.get(oceanForcast).awaitString()
            val responseLocation = Fuel.get(locationForcast).awaitString()

            //Inputstream
            val inputStream1: InputStream = responseOcean.byteInputStream()
            val inputStream2: InputStream = responseLocation.byteInputStream()

            //Kjører med egen parser
            val product = TempParser().parse(inputStream2)
            oceanList.addAll(OceanParser().parse(inputStream1))

            val timeList = product?.time

            if (timeList != null) { //hent første "værmelding", den er nærmest real time data
                val lokasjon = timeList[0].location
                vindRetning = lokasjon?.windDirection?.deg
                vindStyrke = lokasjon?.windSpeed?.mps
            }

            if (oceanList.isNotEmpty()){ //sjekker om oceanforecast gir data
                bolgeHoyde = oceanList[0].significantTotalWaveHeight?.content
            }

            var nearFare: MutableList<Info?> //liste med nærme farer til lokasjonen

            val minLagrede = Location(LocationManager.GPS_PROVIDER) //lager objekt med lokasjonen til lagert sted
            s?.lat?.toDouble()?.let { minLagrede.latitude = it }
            s?.lng?.toDouble()?.let { minLagrede.longitude = it }

            nearFare = sjekkFarer(minLagrede)

            val stedObjekt = StedInfo(s?.tittel, bolgeHoyde, vindRetning, vindStyrke, nearFare) //legger objektet i listen
            infoFerdig.add(stedObjekt)
        }

        return infoFerdig.sortedBy { it?.nearmeFarer?.size}.reversed().toMutableList()
    }

    private fun sjekkFarer(lagret: Location): MutableList<Info?>{
        val nearFare = mutableListOf<Info?>()

        if (myList!= null&&myList?.size != 0){ //sjekker om det er farer og sammenligne mot
            for (i in myList!!){ //loop gjennom farer
                val fare = Location(LocationManager.GPS_PROVIDER) //lag objekt
                var poly = i?.area?.polygon //hent polygon til faren
                poly = poly?.trim() //fjern ekstra mellomrom
                val splittetMellom = poly?.split(" ")
                //format er lat,lng lat2,lng2 osv..
                //så splitter først for å få koordinatene hver for seg

                if (splittetMellom != null) {
                    for (kord in splittetMellom){ //loop gjennom alle koordinater til den faren vi er på
                        val splittet = kord.split(",") //splitt for å skille mellom lat og lng

                        try { //prøv å cast til flytttall, og prøv å sette koordinatene til fare lokasjon
                            fare.latitude = splittet[0].toDouble()
                            fare.longitude = splittet[1].toDouble()

                        }catch (e: NumberFormatException) {
                            Log.d("debug", "feil format hopper over")
                            continue
                        }

                        val distanse = lagret.distanceTo(fare) //sjekk distasen fra det punktet vi akkurat lagde til den lagrede lokasjonen vi sjekker
                        if (distanse <=10000.0){ //er den innafor radius på 10 km, legger vi faren i listen over nærme farer
                            nearFare.add(i)
                            break
                        }
                    }
                }
            }
        }
        return nearFare
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

        val koor = bundle?.getParcelable<LatLng>("markerKoor")

        bundle?.putParcelable("markerKoor", koor)

        bundle?.putParcelableArrayList("list", stedListe)
        bundle?.putParcelableArrayList("key", myList)
        myIntent.putExtra("Bundle", bundle)

        myIntent.flags = Intent.FLAG_ACTIVITY_NO_ANIMATION
        startActivity(myIntent)
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.filter_baatklasse, menu)

        val menuItem : MenuItem = menu!!.findItem(R.id.spinner_baatklasse)
        val spinnerBaatklasse = menuItem.actionView as Spinner
        val baatKlasser = ArrayList(listOf(*resources.getStringArray(R.array.baatklasser)))

        val spinnerAdapter = ArrayAdapter(this, R.layout.filter_design2, baatKlasser as List<Any?>)
        spinnerAdapter.setDropDownViewResource(R.layout.filter_design)
        spinnerBaatklasse.adapter = spinnerAdapter
        spinnerBaatklasse.onItemSelectedListener = this

        return true
    }

    override fun onPrepareOptionsMenu(menu: Menu): Boolean {
        val menuItem = menu.findItem(R.id.spinner_baatklasse)

        if(sjekkIngenLagrede(stedListe!!.size)) {
            menuItem.isEnabled = false
            menuItem.isVisible = false
        }
        return true
    }

    override fun onSupportNavigateUp(): Boolean {
        val navController = findNavController(R.id.nav_host_fragment)
        return navController.navigateUp(appBarConfiguration) || super.onSupportNavigateUp()
    }
    override fun onItemSelected(parent: AdapterView<*>, view: View?, pos: Int, id: Long) {
        when(pos){
            0 -> oppdaterCardViews(0)
            1 -> oppdaterCardViews(0) //trenger ikke denne siden denne er ikke farlig for noe
            2 -> oppdaterCardViews(2)
            3 -> oppdaterCardViews(3)
            4 -> oppdaterCardViews(4)
        }
    }
    override fun onNothingSelected(parent: AdapterView<*>) {
        return
    }

    private fun oppdaterCardViews(pos: Int){
        viewManager = LinearLayoutManager(this@LagretActivity)
        viewAdapder = LagretAdapter(infoSteder, this@LagretActivity, pos)


        recyclerview = findViewById<RecyclerView>(R.id.recyclerView).apply {
            layoutManager = viewManager
            adapter = viewAdapder
        }
    }
}