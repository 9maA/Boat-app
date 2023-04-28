package com.ny.kystVarsel.adaptere

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.recyclerview.widget.RecyclerView
import com.ny.kystVarsel.R
import com.ny.kystVarsel.aktiviteter.LagretActivity
import com.ny.kystVarsel.dataClasses.StedInfo

//Adapter for å presentere LagretData til cardview i recyclerview
@Suppress("DEPRECATION")
class LagretAdapter(private val list: MutableList<StedInfo?>, private val listener: LagretActivity, private val posisjon: Int):
    RecyclerView.Adapter<LagretAdapter.ViewHolder>() {

    //Viewholder - dataen som skal vises for et lagret sted
    inner class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val textViewSted : TextView = view.findViewById(R.id.tittelSted)
        val textViewBolgeHoyde : TextView = view.findViewById(R.id.bolgeHoyde)
        val textViewVindRetning : TextView = view.findViewById(R.id.vindRetning)
        val textViewVindStyrke : TextView = view.findViewById(R.id.vindStyrke)
        val imageButton: ImageButton = view.findViewById(R.id.slettBilde)
        val fare: TextView = view.findViewById(R.id.fareKlasse)
        val fareFor: TextView = view.findViewById(R.id.fareHer)
        val imageView: ImageView = view.findViewById(R.id.fareSymbol)
        val ingenBolge = view.context.resources.getString(R.string.ingen_bolge_hoyde)
        val farligBolge = view.context.resources.getString(R.string.fare_bolge_hoyde)
    }
    //Inflater cardviewet
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val inflatedView = LayoutInflater.from(parent.context)
                .inflate(R.layout.sted, parent, false)
        return ViewHolder(inflatedView)
    }

    //Binder viewholderen med dataen
    override fun onBindViewHolder(holder: ViewHolder, position: Int) {


        //adapter tar inn en posisjon, pos 0 er ingen valgt, pos 1 er klasse A, pos 2 klasse B
        //klasse A tøler alle bølgehøyder, er båter som cruise skip
        var range = 0.0
        when(posisjon){ //sjekker hvilken båt klasse som kaller på, og gir en range som vi kan sammenligne mot
            2-> range = 4.0
            3-> range = 2.0
            4-> range = 0.3
        }

        val etSted = list[position] //henter sted i listen over lagrede
        val tittelSted = etSted?.tittel
        var bolgeHoyde = etSted?.bolgeHoyde
        val bolgeSjekk: String? = etSted?.bolgeHoyde
        val vindRetning = "Vindretning: ${etSted?.vindRetning}°"
        val vindStyrke = "Vindstyrke: ${etSted?.vindStyrke} mps"


        if (bolgeHoyde == null){ //har ikke info om bølgehøyde
            holder.textViewBolgeHoyde.text = holder.ingenBolge
        }else {
            bolgeHoyde = "Bølgehøyde: ${etSted?.bolgeHoyde}m"
            holder.textViewBolgeHoyde.text = bolgeHoyde
            if(range != 0.0){ //sjekk om vi har båtklasse som kan være i risiko
                val bolgetall = bolgeSjekk?.toDouble() //cast bølgehøyden fra API til flyttal
                if (bolgetall != null) {
                    if(bolgetall > range){ //er bølgehøyden på lokasjonen høyere enn det båten takler?
                        holder.fare.text = holder.farligBolge
                    }
                }
            }
        }

        if (etSted?.nearmeFarer?.size !=0){ //sjekk om lokasjonen har noen farer nærme seg i listen
            var tekst = "\nDette stedet er nærme farene: \n"

            //gå gjennom alle farene lokasjonen er nærme
            for (info in etSted?.nearmeFarer!!){ //går gjennom alle farene den krysser til
                val headline = info?.headline?.split(",")?.toTypedArray()
                val stedTittel = headline?.get(0) //hent info om faren
                val stedFare = headline?.get(headline.size-2)
                val tittel = "$stedTittel $stedFare \n" //konkatenerer
                tekst += tittel //legg til i strengen over
            }
            holder.fareFor.visibility = View.VISIBLE
            holder.fareFor.text = tekst

            holder.imageView.setImageResource(R.drawable.bilde15) //sett fare bilde
        }
        else {
            holder.imageView.setImageResource(R.drawable.ic_star) //sett defualt bilde
        }

        holder.textViewSted.text = tittelSted
        holder.textViewVindRetning.text = vindRetning
        holder.textViewVindStyrke.text = vindStyrke
        holder.imageButton.setImageResource(R.drawable.ic_trash)

//      Source: https://stackoverflow.com/questions/52404324/how-to-delete-or-remove-cardview-from-recyclerview-android-studio
        holder.imageButton.setOnClickListener { //gjør at man kan trykke på soppel kasse cardview
            try {
                val navn = list[position]?.tittel
                list.removeAt(position) //fjerner fra lista vi fikk inn
                notifyItemRemoved(holder.adapterPosition)
                notifyDataSetChanged()
                notifyItemRangeChanged(holder.adapterPosition, list.size)
                if (navn != null) {
                    listener.fjernSted(navn)
                }

            }catch(e: IndexOutOfBoundsException){//hvis bruker trykker for kjapt, så kan dette skje så, eller at du fjerner nest sist, så sist da får du ikke sletta
            }
        }
    }

    //Henter størrelsen på listen
    override fun getItemCount() : Int {
        return list.size
    }
}