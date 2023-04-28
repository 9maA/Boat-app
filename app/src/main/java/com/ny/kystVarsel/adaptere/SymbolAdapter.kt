package com.ny.kystVarsel.adaptere

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.ny.kystVarsel.R
import com.ny.kystVarsel.dataClasses.Fare

//Adapter for å presentere Symboler data til cardview i recyclerview
class SymbolAdapter(private val list: ArrayList<Fare>)
    : RecyclerView.Adapter<SymbolAdapter.ViewHolder>() {

    //Viewholder - dataen som skal vises for et symbol
    inner class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val textViewTekst: TextView = view.findViewById(R.id.tekst)
        val imageViewSymbol: ImageView = view.findViewById(R.id.fareSymbol)
    }

    //Inflater cardviewet
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val inflatedView = LayoutInflater.from(parent.context)
            .inflate(R.layout.fare, parent, false)
        return ViewHolder(inflatedView)
    }

    //Binder viewholderen med dataen
    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        //Henter verdier fra fareobjektet
        val enFare = list[position]
        val tekst = enFare.tekst
        val fareSymbol = enFare.type

        //Setter dataen inn i viewet
        holder.textViewTekst.text = tekst

        //Få dette globalt på et vis?
        val bilder = arrayOf(
                R.drawable.bilde1,
                R.drawable.bilde2,
                R.drawable.bilde3,
                R.drawable.bilde4,
                R.drawable.bilde5,
                R.drawable.bilde6,
                R.drawable.bilde7,
                R.drawable.bilde8,
                R.drawable.bilde9,
                R.drawable.bilde10,
                R.drawable.bilde11,
                R.drawable.bilde12,
                R.drawable.bilde13,
                R.drawable.bilde14,
                R.drawable.bilde15
        )

        //Går igjennom listen med bilder og setter riktig symbol til riktig element
        for (i in 0..14) {
            if (fareSymbol.equals(list[i].type)) {
                holder.imageViewSymbol.setImageResource(bilder[i])
            }
        }
    }

    //Henter størrelsen på listen
    override fun getItemCount(): Int {
        return list.size
    }
}