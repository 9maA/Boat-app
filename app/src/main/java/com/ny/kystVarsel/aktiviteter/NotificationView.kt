package com.ny.kystVarsel.aktiviteter

import android.app.Activity
import android.os.Bundle
import com.ny.kystVarsel.R

class NotificationView : Activity() {
    public override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.notification)
    }
}