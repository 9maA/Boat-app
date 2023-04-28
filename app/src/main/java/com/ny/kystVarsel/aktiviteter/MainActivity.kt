package com.ny.kystVarsel.aktiviteter

import android.annotation.SuppressLint
import android.app.Dialog
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.location.Address
import android.location.Geocoder
import android.os.Bundle
import android.view.Menu
import android.view.View
import android.view.View.*
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.drawerlayout.widget.DrawerLayout
import androidx.fragment.app.DialogFragment
import androidx.navigation.findNavController
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.navigateUp
import androidx.navigation.ui.setupActionBarWithNavController
import androidx.navigation.ui.setupWithNavController
import androidx.room.Room
import com.github.kittinunf.fuel.Fuel
import com.github.kittinunf.fuel.coroutines.awaitString
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.*
import com.google.android.material.navigation.NavigationView
import com.ny.kystVarsel.*
import com.ny.kystVarsel.dataBase.AppDatabase
import com.ny.kystVarsel.dataBase.StedDAO
import com.ny.kystVarsel.dataClasses.Channel
import com.ny.kystVarsel.dataClasses.Info
import com.ny.kystVarsel.dataClasses.Sted
import com.ny.kystVarsel.fragmenter.lagre.LagreFragment
import com.ny.kystVarsel.fragmenter.settNavn.SettNavnFragment
import com.ny.kystVarsel.primarParser.GeneralParser
import com.ny.kystVarsel.primarParser.LinkParser
import kotlinx.android.synthetic.*
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.io.IOException
import java.io.InputStream
import java.util.*
import kotlin.collections.ArrayList
import kotlin.collections.HashMap


@Suppress("BlockingMethodInNonBlockingContext", "NULLABILITY_MISMATCH_BASED_ON_JAVA_ANNOTATIONS")
class MainActivity : AppCompatActivity(), OnMapReadyCallback, LagreFragment.NoticeDialogListener, SettNavnFragment.NoticeDialogListener,
        GoogleMap.OnMarkerClickListener{

    private lateinit var mMap: GoogleMap
    private lateinit var infoList: MutableList<Info?>
    private lateinit var searchView: SearchView
    private lateinit var appBarConfiguration: AppBarConfiguration
    private lateinit var stedDAO: StedDAO
    private lateinit var steder: List<Sted>
    private lateinit var cartBadge: TextView
    private var marker: Marker? = null
    private lateinit var stedListe: ArrayList<Sted>
    //Koordinater som hentes ved klikk på kartet
    private var koor: LatLng? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        initDatabase()

        //Henter toolbar og setter den til main
        val toolbar: Toolbar = findViewById(R.id.toolbar)
        setSupportActionBar(toolbar)

        //Source: https://developers.google.com/maps/documentation/android-sdk/start
        //Initialiserer
        val mapFragment = supportFragmentManager
                .findFragmentById(R.id.map_fragment) as SupportMapFragment

        mapFragment.getMapAsync(this)


     //Lager drawerlayout, navigationview og navcontroller slik at fragments kan vises i hamburgermeny
        val drawerLayout: DrawerLayout = findViewById(R.id.drawer_layout)
        val navView: NavigationView = findViewById(R.id.nav_view)
        val navController = findNavController(R.id.nav_host_fragment)
        cartBadge = findViewById(R.id.cart_badge)

        //Legger til hver av fragmentsene
        appBarConfiguration = AppBarConfiguration(
                setOf(
                        R.id.nav_fareSymboler,
                        R.id.nav_nodetater,
                        R.id.nav_lagetAv,
                        R.id.nav_hjem
                ), drawerLayout
        )
        //Actionbaren kobles med navkontrolleren
        //Navigationview kobles med navkontrolleren
        setupActionBarWithNavController(navController, appBarConfiguration)
        navView.setupWithNavController(navController)

        //Endrer utseende til aktivitetens "knapp" til cickable
        val kartKnapp: ImageButton = findViewById(R.id.kartKnapp)
        kartKnapp.setImageResource(R.drawable.ic_kart_knapp_clicked)


        //Setter "logo" på hamburgermeny knappen i toolbar
        supportActionBar?.setHomeAsUpIndicator(R.drawable.ic_meny)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
    }


    //Søkemotoren
    private fun lagSokemotor(searchView: SearchView) {
        //Source : https://www.geeksforgeeks.org/how-to-add-searchview-in-google-maps-in-android/
        //Lager en lytter for søkemotoren
        searchView.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String): Boolean {
                val location = searchView.query.toString()

                val addressList: List<Address>?
                //Bruker Geocoder for å hente informasjon om stedet bruker søker på
                val geocoder = Geocoder(this@MainActivity)
                try {
                    addressList = geocoder.getFromLocationName(location, 1)
                } catch (e: IOException) {
                    e.printStackTrace()
                    return false
                }

                if (addressList.isEmpty()) { //fant ikke sted
                    Toast.makeText(applicationContext, "Finner ikke sted!", Toast.LENGTH_SHORT).show()
                    return false
                }
                val address = addressList[0]
                val latLng = LatLng(address.latitude, address.longitude)
                //Gjør at "kamera" zoomer inn på kartet til stedet
                mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(latLng, 14F))

                return false
            }

            override fun onQueryTextChange(newText: String?): Boolean {
                return false
            }
        })
    }

    private fun initDatabase() {
        //Source: https://developer.android.com/training/data-storage/room
        //Lager database for appen
        val db = Room.databaseBuilder(applicationContext, AppDatabase::class.java, "lagrede-steder")
                .allowMainThreadQueries()
                .build()

        stedDAO = db.stedDao()
        steder = stedDAO.getAll()
    }

    //Restarter en aktivitet
    //Sender ved liste mellom alle aktivitetene
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

        val myList: ArrayList<Info?> = ArrayList()
        for (i in infoList) {
            myList.add(i)
        }

        stedListe = ArrayList()
        for(s in steder) {//bytter over
            stedListe.add(s)
        }
        val bundle = Bundle()

        //hvis bruker har lagt en markør, så sender vi den med videre
        if (koor != null){
            bundle.putParcelable("markerKoor", koor)
        }

        bundle.putParcelableArrayList("list", stedListe)
        bundle.putParcelableArrayList("key", myList)
        myIntent?.putExtra("Bundle", bundle)

        myIntent.flags = Intent.FLAG_ACTIVITY_NO_ANIMATION
        startActivity(myIntent)
    }

    //Håndterer navigering i hamburgermenyen
    override fun onSupportNavigateUp(): Boolean {
        val navController = findNavController(R.id.nav_host_fragment)
        return navController.navigateUp(appBarConfiguration) || super.onSupportNavigateUp()
    }

    //Gjennomfører alle api kall
    private fun obligatoriskApiKall() {
        val rainDanger = "https://in2000-apiproxy.ifi.uio.no/weatherapi/metalerts/1.1/?event=rain"
        val windDanger = "https://in2000-apiproxy.ifi.uio.no/weatherapi/metalerts/1.1/?event=wind"
        val stormDanger = "https://in2000-apiproxy.ifi.uio.no/weatherapi/metalerts/1.1/?event=stormSurge"
        val polarLowDanger = "https://in2000-apiproxy.ifi.uio.no/weatherapi/metalerts/1.1/?event=polarLow"
        val galeDanger = "https://in2000-apiproxy.ifi.uio.no/weatherapi/metalerts/1.1/?event=gale&lang=no"

        val channelList = mutableListOf<Channel?>()
        infoList = mutableListOf()
        val linkList = mutableListOf<String>()

        //Kjører parsing i coroutines
        CoroutineScope(Dispatchers.IO).launch {
            channelList.add(forsteKall(rainDanger))
            channelList.add(forsteKall(windDanger))
            channelList.add(forsteKall(stormDanger))
            channelList.add(forsteKall(polarLowDanger))
            channelList.add(forsteKall(galeDanger))

            //Legger hva hver api kall fikk, mange av dem er null fordi er ikke ofte man har farer
            CoroutineScope(Dispatchers.IO).launch {
                for (chan in channelList) {
                    if (chan != null) { //sjekkker om det ikke er null
                        if (chan.item != null) { //sjekker om det ikke er null
                            for (l in chan.item) {
                                linkList.add(l.link!!) //vet her at den inneholder nå så bruker !!, selv om ikke lurt noen ganger
                            }
                        }
                    }
                }//End for loop
                for (link in linkList) {
                    val linkResponse = Fuel.get(link).awaitString()
                    val inputStream: InputStream = linkResponse.byteInputStream()

                    val infoObject = LinkParser().parse(inputStream)
                    infoList.add(infoObject)
                }
                runOnUiThread {
                    //Plotter farer i kartet

                    if (infoList.isNotEmpty()){
                        cartBadge.visibility = VISIBLE
                        cartBadge.text = infoList.size.toString()

                        plottFarer()
                    }
                    else {
                        val toast = Toast.makeText(
                                applicationContext,
                                "Det er ingen farer å vise for øyeblikket!",
                                Toast.LENGTH_LONG
                        )
                        toast.show()
                    }
                }
            }
        }
    }

    private suspend fun forsteKall(link: String): Channel? {
        val response = Fuel.get(link).awaitString()
        val inputStream: InputStream = response.byteInputStream()
        return GeneralParser().parse(inputStream)
    }

    private fun plottFarer() {

        for (info in infoList) {
            //Henter info
            var poly = info?.area?.polygon
            poly = poly?.trim()
            val splittet = poly?.split(" ")
            val midtKord = splittet?.elementAt(splittet.size / 2)?.split(",") //henter midterste koordinat
            var latLngFare: LatLng?

            try { //prøver å lage LatLng objekt basert på midtkoordinat
                latLngFare = midtKord?.elementAt(0)?.let {
                    LatLng(
                            midtKord.elementAt(0).toDouble(), midtKord.elementAt(
                            1
                    ).toDouble()
                    )
                }
            } catch (e: NumberFormatException) {
                continue
            }

            //Lager et hasmap med hvilke titler som skal kobles med hvilket bilde
            val hm: HashMap<String, Int> = HashMap()
            hm["Kuling,Gult nivå"] = R.drawable.bilde1
            hm["Storm,Gult nivå"] = R.drawable.bilde1
            hm["Kuling,Oransje nivå"] = R.drawable.bilde2
            hm["Kuling,Rødt nivå"] = R.drawable.bilde3
            hm["Mye regn,Gult nivå"] = R.drawable.bilde4
            hm["Svært mye regn,Oransje nivå"] = R.drawable.bilde5
            hm["Ekstremt mye regn,Rødt nivå"] = R.drawable.bilde6
            hm["Styrtregn,Gult nivå"] = R.drawable.bilde7
            hm["Svært kraftig styrtregn,Gult nivå"] = R.drawable.bilde8
            hm["Polart lavtrykk,Gult nivå"] = R.drawable.bilde12
            hm["Svært kraftig polart lavtrykk,Oransje nivå"] = R.drawable.bilde13
            hm["Moderat ising på skip,Gult nivå"] = R.drawable.bilde14
            hm["Sterk ising på skip,Oransje nivå"] = R.drawable.bilde15

            val headline = info?.headline?.split(",")?.toTypedArray()
            val stedTittel = headline?.get(0) //type fare, i tilfelle noe annet kommer, f.eks. kansellert kulling
            val desc1 = info?.event //type fare
            var nivaaInfo: String? = null
            var nivaa: String? = null
            val stedFare = headline?.get(headline.size - 2) //sted ligger alltid nest sist

            val tittel = "$stedTittel $stedFare" //konkatenerer type fare og nivå for å sjekke i hashmap

            for (p in info?.parameter!!) { //paramater er en liste med mange forskjellige verdier, paramater sin valuename spessifiser hva slags info
                if (p.valueName == "awareness_level") {
                    nivaaInfo = p.value
                    break
                }
            }

            val biter = nivaaInfo?.split(";") //format: tall; nivå; urgency
            when (biter?.elementAt(1)?.trim()) {
                "yellow" -> nivaa = "Gult nivå"
                "orange" -> nivaa = "Oransje nivå"
                "red" -> nivaa = "Rødt nivå"
            }

            val utfall = desc1.plus(",").plus(nivaa) //henter fra hashmap
            val bilde = hm[utfall]
            val height = 100 //dimensjoner på markør
            val width = 100


            val b: Bitmap = if (bilde != null) { //fikk vi noe ut fra hasmap?
                BitmapFactory.decodeResource(resources, bilde)
            } else { //hvis ikke bruk bare varsel trekant
                BitmapFactory.decodeResource(resources, R.drawable.bilde15)
            }
            val smallMarker = b.let { Bitmap.createScaledBitmap(it, width, height, false) }
            val smallMarkerIcon = BitmapDescriptorFactory.fromBitmap(smallMarker)
            //gjør om til lite icon
            //Source: https://stackoverflow.com/questions/35718103/how-to-specify-the-size-of-the-icon-on-the-marker-in-google-maps-v2-android
            val kystMarker = mMap.addMarker( //plotter markør
                    latLngFare?.let {
                        val title = MarkerOptions()
                                .position(it)
                                .icon(smallMarkerIcon)
                                .title(tittel)
                        title
                    }
            )
            kystMarker?.tag = "fare"
        }
    }
    //Kjører kartet
    override fun onMapReady(googleMap: GoogleMap) {
        mMap = googleMap
        mMap.setOnMarkerClickListener(this@MainActivity)

        try { //prøv å hent ut det som har blitt sendt rundt, interessert i om noen markør har blitt sendt rundt, prøver
            val bundle2 = intent.getBundleExtra("Bundle")
            koor = bundle2?.getParcelable("markerKoor")!!
        }catch (e: Exception){

        }
        lagreSted(mMap)
        val norge = LatLngBounds(//norges dimensjoner
                LatLng(55.875311, 3.972742),
                LatLng(70.902268, 32.116522)
        )
        try { //prøver at kartet starter på norge
            mMap.moveCamera(CameraUpdateFactory.newLatLngBounds(norge, 0))
        }catch (e: Exception){

        }

        obligatoriskApiKall() //når kartet er klart kan vi kalle på den slik at den er klar når appen startes
        plottLagrede()
    }

    override fun onSaveInstanceState(savedInstanceState: Bundle){
        //hvis bruker roterer skjermen eller noe skjer,
        if (koor != null){ //sjekk om markør har blitt plassert
            savedInstanceState.putParcelable("kord", koor) //lagre hvis tilfelle
        }
        super.onSaveInstanceState(savedInstanceState)
    }

    override fun onRestoreInstanceState(savedInstanceState: Bundle){
        try { //prøv å hent ut markør som har blitt plassert før hendelse
            koor = savedInstanceState.getParcelable("kord")!!
        }catch (e: NullPointerException){
        }
        super.onRestoreInstanceState(savedInstanceState)
    }

    private fun plottLagrede(){
        for (s in steder){
            plottLagret(s)
        }
    }

    private fun plottLagret(sted: Sted){//plott alle markørene i databasen som en båt med lite bilde
        val height = 100
        val width = 100
        val b = BitmapFactory.decodeResource(resources, R.drawable.baatmap)

        val smallMarker = Bitmap.createScaledBitmap(b, width, height, false)
        val smallMarkerIcon = BitmapDescriptorFactory.fromBitmap(smallMarker)
        val kord = LatLng(sted.lat.toDouble(), sted.lng.toDouble())
        val lagretMarker = mMap.addMarker(
                kord.let {
                    val title = MarkerOptions()
                            .position(it)
                            .icon(smallMarkerIcon)
                            .title(sted.tittel)
                    title
                }
        )
        lagretMarker?.tag = "lagret"
    }

  //Lagrer et sted
    private fun lagreSted(googleMap: GoogleMap) { //gjør at kartet kan trykkes på
        val height = 120
        val width = 120
        val b = BitmapFactory.decodeResource(resources, R.mipmap.ic_marker)
        val smallMarker = Bitmap.createScaledBitmap(b, width, height, false)
        val smallMarkerIcon = BitmapDescriptorFactory.fromBitmap(smallMarker)

        if(koor != null){ //har en markør blitt plassert fra før av, før en hendelse skjedde?
            marker = mMap.addMarker( //hvis ja, plasser den tilbake hvor den var
                    koor.let {
                        MarkerOptions()
                                .icon(smallMarkerIcon)
                                .position(koor!!)
                    }
            )
        }
        googleMap.setOnMapClickListener { //kartet kan trykkes på
            marker?.remove() //fjern tidligere plassert markør, max en om gangen
            koor = it //lagre koordintater til der bruker har trykket i global varaibel
            marker = mMap.addMarker( //plasser markør
                    koor.let {
                        MarkerOptions()
                                .icon(smallMarkerIcon)
                                .position(koor!!)
                    }
            )
        }
    }

    override fun onMarkerClick(marker: Marker): Boolean {
        if (marker.tag == null) { //siden fare markørene har tag "fare", og lagret markør har tag "lagret"
            showNoticeDialog() //hvis bruker trykker på markør som ikke har noe tagg
            //trykker man på pluss markør vis dialogboks
        }
        return false
    }

    //Viser dialogboksen
    private fun showNoticeDialog() { //første dialogboks
        val dialog = LagreFragment()
        dialog.show(supportFragmentManager, "dialog")
    }

    //Håndterer ok klikk
    override fun onDialogPositiveClick(dialog: DialogFragment) {
        showLagreDialog() //->kall på neste dialogboks
    }

    //Håndterer avbryt klikk
    override fun onDialogNegativeClick(dialog: DialogFragment) {
        dialog.dismiss() //lukk dialog
    }

    private fun showLagreDialog() { //andre dialogboks
        val lagreDialog = SettNavnFragment()
        lagreDialog.show(supportFragmentManager, "lagreDialog")
    }

    override fun onLagreNegativeClick(dialog: DialogFragment) {
        dialog.dismiss() //lukk dialog
    }

    override fun onLagrePositiveClick(dialog: DialogFragment) {
        val dialogView: Dialog? = dialog.dialog

        val skrivTittel = dialogView?.findViewById<EditText>(R.id.skrivTittel) //hent textview inni dialogboks


         if (skrivTittel?.text.toString().trim().isEmpty()) { //det bruker skriver inn er tomt
             val toast = Toast.makeText(
                     applicationContext,
                     "Skriv inn tittel",
                     Toast.LENGTH_LONG
             )
             toast.show()
             showLagreDialog()
         }else {
             val text = skrivTittel?.text.toString() //ellers hent teksten og sjekk om det er unikt
             val fikkLagtTil = leggInnSted(text, koor?.latitude.toString(), koor?.longitude.toString())


             if (fikkLagtTil) {
                 marker?.remove()
                 skrivTittel?.text?.clear()
             }
             else {
                 showLagreDialog() //prøv på nytt
             }
         }

    }


    fun leggInnSted(tittel: String, lat: String, long: String): Boolean {
        val sted = Sted(tittel, lat, long) //lag sted objekt
        val likt = stedDAO.finnLiktSted(sted.tittel) //kall på spørring som henter list med steder med samme navn

        if(likt.isNotEmpty()) { //er den ikke tom er det sted som har samme navn
            val text = "Et sted er allerede lagret som $tittel. lagre som et annet navn"

            val duration = Toast.LENGTH_SHORT
            val toast = Toast.makeText(applicationContext, text, duration)
            toast.show()
            return false
        }
        else {
            val toast = Toast.makeText(
                    applicationContext,
                    "Du lagret lokasjon som $tittel",
                    Toast.LENGTH_LONG
            )
            toast.show()
            stedDAO.insert(sted)
            steder = stedDAO.getAll() //for å oppdatere listen med steder med nytt sted
            plottLagret(sted)
            koor = null
            return true
        }
    }

    @SuppressLint("ServiceCast")
    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.searchview_toolbar, menu)

        searchView = menu?.findItem(R.id.searchview_kart)?.actionView as SearchView

        lagSokemotor(searchView)
        return true
    }

    override fun onPrepareOptionsMenu(menu: Menu): Boolean {
        val menuItem = menu.findItem(R.id.searchview_kart)
        menuItem.isEnabled = false

        return true
    }
}




