package com.ny.kystVarsel.fragmenter.nodetater

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.ny.kystVarsel.R
import com.ny.kystVarsel.adaptere.NodAdapter
import com.ny.kystVarsel.dataClasses.Nod


class NodetaterFragment : Fragment() {

    private var recyclerView: RecyclerView? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        (activity as AppCompatActivity).supportActionBar?.setHomeAsUpIndicator(R.drawable.ic_meny)
    }

    //Source: https://developer.android.com/reference/android/text/util/Linkify
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val root = inflater.inflate(R.layout.fragment_nodetater, container, false)

        val nod1 = Nod("Nødnummer:", "110, 112, 113", "Åpent hele døgnet")
        val nod2 = Nod("Redningsselskapet kundesenter:", "+47 987 06 757", "Åpningstid kl 8-16")
        val nod3 = Nod("Assistanse på sjøen:", "+47 915 02 016", "Åpent hele døgnet")

        val nodListe = arrayListOf(nod1, nod2, nod3)

        recyclerView = root.findViewById(R.id.recyclerView)
        val rv = recyclerView
        rv?.setHasFixedSize(true)
        rv?.layoutManager = LinearLayoutManager(root.context)
        val recyclerAdapter = NodAdapter(nodListe)
        rv?.adapter = recyclerAdapter

        return root
    }
}