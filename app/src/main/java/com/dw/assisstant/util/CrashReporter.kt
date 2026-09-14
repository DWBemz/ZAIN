package com.dw.assisstant.util

import android.content.Context
import android.os.Build
import java.io.File
import java.io.PrintWriter
import java.io.StringWriter
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

object CrashReporter {

    private const val FILE_NAME = "zain_crash_report.txt"

    fun install(context: Context) {

        val applicationContext = context.applicationContext

        val defaultHandler =
            Thread.getDefaultUncaughtExceptionHandler()

        Thread.setDefaultUncaughtExceptionHandler { thread, throwable ->

            try {
                saveCrash(
                    applicationContext,
                    thread,
                    throwable
                )
            } catch (_: Exception) {
                // Never let the crash reporter cause another crash.
            }

            defaultHandler?.uncaughtException(
                thread,
                throwable
            )
        }
    }

    private fun saveCrash(
        context: Context,
        thread: Thread,
        throwable: Throwable
    ) {

        val formatter = SimpleDateFormat(
            "yyyy-MM-dd HH:mm:ss",
            Locale.US
        )

        val stackWriter = StringWriter()

        throwable.printStackTrace(
            PrintWriter(stackWriter)
        )

        val report = buildString {

            appendLine("================================")
            appendLine("ZAIN CRASH REPORT")
            appendLine("================================")
            appendLine()

            appendLine(
                "Time: ${formatter.format(Date())}"
            )

            appendLine(
                "Device: ${Build.MANUFACTURER} ${Build.MODEL}"
            )

            appendLine(
                "Android: ${Build.VERSION.RELEASE}"
            )

            appendLine(
                "SDK: ${Build.VERSION.SDK_INT}"
            )

            appendLine(
                "Package: ${context.packageName}"
            )

            appendLine(
                "Thread: ${thread.name}"
            )

            appendLine()

            appendLine("EXCEPTION")
            appendLine("--------------------------------")
            appendLine(throwable.toString())

            appendLine()

            appendLine("STACK TRACE")
            appendLine("--------------------------------")
            appendLine(stackWriter.toString())

            appendLine()
            appendLine("================================")
        }

        // Private internal copy
        File(
            context.filesDir,
            FILE_NAME
        ).writeText(report)

        // Accessible external app-specific copy
        val externalDirectory =
            context.getExternalFilesDir(null)

        if (externalDirectory != null) {

            File(
                externalDirectory,
                FILE_NAME
            ).writeText(report)
        }
    }

    fun getLatestReport(
        context: Context
    ): String? {

        val externalDirectory =
            context.getExternalFilesDir(null)

        val externalFile =
            externalDirectory?.let {
                File(it, FILE_NAME)
            }

        if (externalFile?.exists() == true) {
            return externalFile.readText()
        }

        val internalFile =
            File(
                context.filesDir,
                FILE_NAME
            )

        return if (internalFile.exists()) {
            internalFile.readText()
        } else {
            null
        }
    }

    fun hasReport(
        context: Context
    ): Boolean {

        val externalDirectory =
            context.getExternalFilesDir(null)

        val externalFile =
            externalDirectory?.let {
                File(it, FILE_NAME)
            }

        if (externalFile?.exists() == true) {
            return true
        }

        return File(
            context.filesDir,
            FILE_NAME
        ).exists()
    }

    fun clearReport(
        context: Context
    ) {

        File(
            context.filesDir,
            FILE_NAME
        ).delete()

        context.getExternalFilesDir(null)?.let {
            File(
                it,
                FILE_NAME
            ).delete()
        }
    }
}