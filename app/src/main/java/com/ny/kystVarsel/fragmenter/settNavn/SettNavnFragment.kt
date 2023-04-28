package com.ny.kystVarsel.fragmenter.settNavn

import android.annotation.SuppressLint
import android.app.Dialog
import android.content.Context
import android.os.Bundle
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.DialogFragment
import com.ny.kystVarsel.R


class SettNavnFragment : DialogFragment() {
    private lateinit var listener: NoticeDialogListener

    interface NoticeDialogListener {
        fun onLagrePositiveClick(dialog: DialogFragment)
        fun onLagreNegativeClick(dialog: DialogFragment)
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        try {
            listener = context as NoticeDialogListener
        } catch (e: ClassCastException) {
            throw ClassCastException((context.toString() +
                    " must implement NoticeDialogListener"))
        }
    }

    @SuppressLint("InflateParams")
    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        return activity?.let {
            val builder = AlertDialog.Builder(it)
            val inflater = requireActivity().layoutInflater

            builder.setView(inflater.inflate(R.layout.custom_dialog, null))
                    .setPositiveButton(R.string.bKnappTekst
                    ) { _, _ ->
                        // Send the positive button event back to the host activity
                        listener.onLagrePositiveClick(this)
                    }
                    .setNegativeButton(R.string.avbryt
                    ) { _, _ ->
                        // Send the negative button event back to the host activity
                        listener.onLagreNegativeClick(this)
                    }
            builder.create()

        } ?: throw IllegalStateException("Activity cannot be null")
    }
}