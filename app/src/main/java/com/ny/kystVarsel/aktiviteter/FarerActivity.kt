package com.ny.kystVarsel.aktiviteter

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.ImageButton
import android.widget.ImageView
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
import com.google.android.gms.maps.model.LatLng
import com.google.android.material.navigation.NavigationView
import com.ny.kystVarsel.adaptere.FareAdapter
import com.ny.kystVarsel.R
import com.ny.kystVarsel.dataClasses.Info
import com.ny.kystVarsel.dataClasses.Sted

//Aktivitet for å vise nåværende farer
class FarerActivity : AppCompatActivity() {

    //Globare variabler
    private lateinit var recyclerview: RecyclerView
    private lateinit var viewAdapder: RecyclerView.Adapter<*>
    private lateinit var viewManager: RecyclerView.LayoutManager
    private lateinit var toolbar: Toolbar
    private lateinit var imageView: ImageView

    private var myList: ArrayList<Info>? = null
    private var bundle: Bundle? = null
    private lateinit var appBarConfiguration: AppBarConfiguration

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_farer)

        //Endrer farge til knappen for å vise at aktiviteten kjører
        val farerKnapp : ImageButton = findViewById(R.id.farerKnapp)
        farerKnapp.setImageResource(R.drawable.ic_farer_knapp_clicked)

        //Setter opp hovedfunksjonene
        setView()
        //Viser farer på kartet
        displayFare()
    }

    //Setter view
    private fun setView() {
        //Setter toolbaren til custom toolbar
        toolbar = findViewById(R.id.toolbar)
        setSupportActionBar(toolbar)

        //Setter sammen drawerlayout for hamburgermeny
        val drawerLayout: DrawerLayout = findViewById(R.id.drawer_layout)
        val navView: NavigationView = findViewById(R.id.nav_view)
        val navController = findNavController(R.id.nav_host_fragment)
        appBarConfiguration = AppBarConfiguration(setOf(R.id.nav_fareSymboler, R.id.nav_nodetater, R.id.nav_lagetAv, R.id.nav_hjem), drawerLayout)
        setupActionBarWithNavController(navController, appBarConfiguration)
        navView.setupWithNavController(navController)

        //Henter textviewet for å vise om det er fare
//        textView = findViewById(R.id.ingenFare)
        imageView = findViewById(R.id.ingenFarerBilde)

        //Setter opp hamburgermenyen til toolbaren
        supportActionBar?.setHomeAsUpIndicator(R.drawable.ic_meny)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.setDisplayShowTitleEnabled(false)

    }

    //Viser farer på kart
    private fun displayFare(){
        //Sender med data via intent og bundle
        bundle = intent.getBundleExtra("Bundle")
        myList = bundle?.getParcelableArrayList("key")

        //Liste med infoobjekter
        val infoList = mutableListOf<Info?>()

        //Sjekker liste for å se om det finnes ingen farer, og dermed gi beskjed til bruker
        if(myList?.size ==0) {
//          textView.visibility = View.VISIBLE
//          textView.text = getString(R.string.ingen_farer)
            imageView.visibility = View.VISIBLE
        }

        //Legger listen til i den nye listen
        if (myList != null) {
            for (i in myList!!){
                infoList.add(i)
            }
        }

        //Fikser viewmanager og adapter for listen
        viewManager = LinearLayoutManager(this)
        viewAdapder = FareAdapter(this, infoList)

        //Setter opp recyclerviewet
        recyclerview = findViewById<RecyclerView>(R.id.recyclerView).apply {
            layoutManager = viewManager
            adapter = viewAdapder
        }
    }

    //Metode for å sende med data via intent mellom aktivitetene
    fun restartActivity(view: View) {
        //Håndtering av klikk på ImageButtons i toolbaren
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

        //Henter data
        val stedListe = bundle?.getParcelableArrayList<Sted?>("list")
        val koor = bundle?.getParcelable<LatLng>("markerKoor")

        //Sender med data
        bundle?.putParcelable("markerKoor", koor)
        bundle?.putParcelableArrayList("list", stedListe)
        bundle?.putParcelableArrayList("key", myList)
        myIntent.putExtra("Bundle", bundle)
        myIntent.flags = Intent.FLAG_ACTIVITY_NO_ANIMATION
        startActivity(myIntent)
    }

    //Metode for å håndere navigering i hamburgermenyen
    override fun onSupportNavigateUp(): Boolean {
        val navController = findNavController(R.id.nav_host_fragment)
        return navController.navigateUp(appBarConfiguration) || super.onSupportNavigateUp()
    }
}
