package com.dw.assisstant.util

import android.content.Context
import android.os.Build
import java.io.File
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

object CrashReporter {

    private const val FILE_NAME = "zain_crash_report.txt"

    fun install(context: Context) {

        val applicationContext =
            context.applicationContext

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
                // Never allow the crash reporter itself
                // to cause another crash.
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

        val formatter =
            SimpleDateFormat(
                "yyyy-MM-dd HH:mm:ss",
                Locale.US
            )

        val report =
            buildString {

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
                    "Thread: ${thread.name}"
                )

                appendLine()

                appendLine("EXCEPTION")
                appendLine("--------------------------------")
                appendLine(
                    throwable.toString()
                )

                appendLine()
                appendLine("STACK TRACE")
                appendLine("--------------------------------")

                throwable.printStackTrace(
                    java.io.PrintWriter(
                        java.io.StringWriter()
                    )
                )

                val writer =
                    java.io.StringWriter()

                throwable.printStackTrace(
                    java.io.PrintWriter(writer)
                )

                appendLine(
                    writer.toString()
                )

                appendLine()
                appendLine("================================")
            }

        File(
            context.filesDir,
            FILE_NAME
        ).writeText(report)
    }

    fun getLatestReport(
        context: Context
    ): String? {

        val file =
            File(
                context.filesDir,
                FILE_NAME
            )

        return if (file.exists()) {
            file.readText()
        } else {
            null
        }
    }

    fun hasReport(
        context: Context
    ): Boolean {

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
    }
}
