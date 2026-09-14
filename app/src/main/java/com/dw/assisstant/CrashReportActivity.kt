package com.dw.assisstant

import android.os.Bundle
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.dw.assisstant.util.CrashReporter

class CrashReportActivity : AppCompatActivity() {

    override fun onCreate(
        savedInstanceState: Bundle?
    ) {

        super.onCreate(savedInstanceState)

        val report =
            CrashReporter.getLatestReport(this)

        val textView =
            TextView(this)

        textView.setPadding(
            24,
            24,
            24,
            24
        )

        textView.text =
            report
                ?: "No ZAIN crash report found."

        textView.textSize =
            13f

        setContentView(textView)
    }
}
