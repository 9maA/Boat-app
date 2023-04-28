package com.ny.kystVarsel.adaptere

import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.cardview.widget.CardView
import androidx.recyclerview.widget.RecyclerView
import com.ny.kystVarsel.R
import com.ny.kystVarsel.dataClasses.Info

//Adapter for å presentere FareData til cardview i recyclerview
@Suppress("SameParameterValue")
class FareAdapter(val context: Context, private val liste: MutableList<Info?>):
    RecyclerView.Adapter<FareAdapter.ViewHolder>() {
    //Viewholder - dataen som skal vises for en fare
    inner class ViewHolder(cardView: CardView) : RecyclerView.ViewHolder(cardView) {
        //Henter alle viewene fra cardviewet
        var status: TextView = cardView.findViewById(R.id.farestatus)
        var desc: TextView = cardView.findViewById(R.id.faredesc)
        var nivaa: TextView = cardView.findViewById(R.id.farenivaa)
        var symbol: ImageView = cardView.findViewById(R.id.fareBilde)
        var instruksjon: TextView = cardView.findViewById(R.id.instruction)
        var situasjon: TextView = cardView.findViewById(R.id.situasjon)
    }

    //Inflater cardviewet
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val cardView = LayoutInflater.from(parent.context).inflate(R.layout.cardview_farer, parent, false)
        return ViewHolder(cardView as CardView)
    }

    //Binder viewholderen med dataen
    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        //Henter nåværende fare (infoobjekt)
        val fare = liste[position]
        val statusTekst = "Status: " + fare?.certainty

        //Hashmap for å holde på alle farevarslersymboler
        val hm : HashMap<String, Int> = HashMap()

        //Symbolene legges inn i hashmapet
        hm["Kuling,Gult nivå"] = R.drawable.bilde1
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

        //Verdiene som skal vises hentes
        val headline = fare?.headline?.split(",")?.toTypedArray()
        val tittel = headline?.get(0)
        val partDesc = fare?.event //type fare
        val size = headline?.size
        val stedFare = size?.minus(2)?.let { headline[it] }
        val nivaaInfo:String? = hentverdi(fare, "awareness_level")
        var nivaa:String? = null
        val biter = nivaaInfo?.split(";")

        //Sjekker farenivå
        when(biter?.elementAt(1)?.trim()){
            "yellow" -> nivaa = "Gult nivå"
            "orange" -> nivaa = "Oransje nivå"
            "red" -> nivaa = "Rødt nivå"
        }

        //Sjekker hvilket faresymbol som skal brukes
        val utfall = partDesc.plus(",").plus(nivaa)
        val bilde = hm[utfall]

        //Setter faresymbolet
        if (bilde != null) {
            holder.symbol.setImageResource(bilde)
            //Dersom ingen av symbolene passer settes et utropstegn symbol
        }else {
            holder.symbol.setImageResource(R.drawable.bilde15)
        }

        //Setter dataen inn i viewet
        holder.instruksjon.text = hentverdi(fare, "consequences")
        holder.situasjon.text = hentverdi(fare, "awarenessSeriousness")
        holder.desc.text = tittel.plus("").plus(stedFare)
        holder.nivaa.text = nivaa
        holder.status.text = statusTekst
    }


    //Metode for å hente verdi
    private fun hentverdi(info: Info?, valueName: String): String? {
        for(p in info?.parameter!!){
            if (p.valueName == valueName){
                return p.value
            }
        }
        return null
    }

    //Henter størrelsen på listen
    override fun getItemCount(): Int {
        return liste.size
    }
}