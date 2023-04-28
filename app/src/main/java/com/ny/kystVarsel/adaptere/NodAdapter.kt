package com.ny.kystVarsel.adaptere

import android.text.util.Linkify
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.ny.kystVarsel.R
import com.ny.kystVarsel.dataClasses.Nod

//Adapter for å presentere Nodetater data til cardview i recyclerview
class NodAdapter(private val list: ArrayList<Nod>)
    : RecyclerView.Adapter<NodAdapter.ViewHolder>() {

    //Viewholder - dataen som skal vises for et nødnummer
    inner class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val textViewTittel: TextView = view.findViewById(R.id.tittelNod)
        val textViewNr: TextView = view.findViewById(R.id.nrNod)
        val textViewAapning: TextView = view.findViewById(R.id.aapningsstid)
    }

    //Inflater cardviewet
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val inflatedView = LayoutInflater.from(parent.context)
                .inflate(R.layout.nodetat, parent, false)
        return ViewHolder(inflatedView)
    }

    //Binder viewholderen med dataen
    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        //Henter verdiene fra Nodobjektet
        val enNod = list[position]
        val tittel = enNod.tittel
        val nr = enNod.nr
        val aapent = enNod.tid

        //Setter dataen inn i viewet
        holder.textViewTittel.text = tittel
        holder.textViewNr.text = nr
        holder.textViewAapning.text = aapent

        //Source: https://stackoverflow.com/questions/6497327/android-make-phone-numbers-clickable-autodetect
        //Gjør linker tlfnummerene til telefonappen på tlf
        val t = holder.textViewNr
        Linkify.addLinks(t, Linkify.ALL)
    }

    //Henter størrelsen på listen
    override fun getItemCount(): Int {
        return list.size
    }
}