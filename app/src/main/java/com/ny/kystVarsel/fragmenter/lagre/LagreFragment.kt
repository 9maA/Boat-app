package com.ny.kystVarsel.fragmenter.lagre

import android.app.Dialog
import android.content.Context
import android.os.Bundle
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.DialogFragment
import com.ny.kystVarsel.R

class LagreFragment : DialogFragment() {
    private lateinit var listener: NoticeDialogListener

    interface NoticeDialogListener {
        fun onDialogPositiveClick(dialog: DialogFragment)
        fun onDialogNegativeClick(dialog: DialogFragment)
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

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        return activity?.let {
            val builder = AlertDialog.Builder(it)

            builder.setMessage(R.string.lagre_sted)
                    .setPositiveButton(R.string.ok
                    ) { _, _ ->
                        // Send the positive button event back to the host activity
                        listener.onDialogPositiveClick(this)
                    }
                    .setNegativeButton(R.string.avbryt
                    ) { _, _ ->
                        // Send the negative button event back to the host activity
                        listener.onDialogNegativeClick(this)
                    }

            builder.create()
        } ?: throw IllegalStateException("Activity cannot be null")
    }
}

