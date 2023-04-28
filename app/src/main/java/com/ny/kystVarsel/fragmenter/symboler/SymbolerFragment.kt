package com.ny.kystVarsel.fragmenter.symboler

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.ny.kystVarsel.*
import com.ny.kystVarsel.adaptere.SymbolAdapter
import com.ny.kystVarsel.dataClasses.Fare

class SymbolerFragment : Fragment(){

    private var recyclerView: RecyclerView? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        (activity as AppCompatActivity).supportActionBar?.setHomeAsUpIndicator(R.drawable.ic_meny)
        (activity as AppCompatActivity).supportActionBar?.setDisplayHomeAsUpEnabled(true)
    }

    override fun onCreateView(
            inflater: LayoutInflater,
            container: ViewGroup?,
            savedInstanceState: Bundle?
    ): View? {

        val root = inflater.inflate(R.layout.fragment_symboler, container, false)

        val fare1 = Fare("Kraftige vindkast / Kuling (inkludert storm)", "fare1")
        val fare2 = Fare("Svært kraftige vindkast", "fare2")
        val fare3 = Fare("Ekstremt kraftige vindkast", "fare3")
        val fare4 = Fare("Mye regn", "fare4")
        val fare5 = Fare("Svært mye regn", "fare5")
        val fare6 = Fare("Ekstremt mye regn", "fare6")
        val fare7 = Fare("Styrtregn", "fare7")
        val fare8 = Fare("Svært kraftig styrtregn", "fare8")
        val fare9 = Fare("Høy vannstand", "fare9")
        val fare10 = Fare("Svært høy vannstand", "fare10")
        val fare11 = Fare("Ekstremt høy vannstand", "fare11")
        val fare12 = Fare("Polart lavtrykk", "fare12")
        val fare13 = Fare("Svært kraftig polart lavtrykk", "fare13")
        val fare14 = Fare("Moderat ising på skip", "fare14")
        val fare15 = Fare("Sterk ising på skip", "fare15")

        val fareListe = arrayListOf(fare1, fare2, fare3, fare4, fare5, fare6, fare7, fare8, fare9, fare10, fare11, fare12, fare13, fare14, fare15)

        recyclerView = root.findViewById(R.id.recyclerView)
        val rv = recyclerView
        rv?.setHasFixedSize(true)
        rv?.layoutManager = LinearLayoutManager(root.context)
        val recyclerAdapter = SymbolAdapter(fareListe)
        rv?.adapter = recyclerAdapter

        return root
    }
}
