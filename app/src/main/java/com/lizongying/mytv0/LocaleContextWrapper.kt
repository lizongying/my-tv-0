package com.lizongying.mytv0

import android.content.Context
import android.content.ContextWrapper
import android.os.Build
import android.os.LocaleList
import java.util.Locale

class LocaleContextWrapper private constructor(base: Context) : ContextWrapper(base) {
    companion object {
        fun wrap(context: Context, newLocale: Locale): Context {
            val resources = context.resources
            val configuration = resources.configuration

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                configuration.setLocale(newLocale)
                val localeList = LocaleList(newLocale)
                LocaleList.setDefault(localeList)
                configuration.setLocales(localeList)
            } else {
                configuration.locale = newLocale
            }

            val updatedContext = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N_MR1) {
                context.createConfigurationContext(configuration)
            } else {
                resources.updateConfiguration(configuration, resources.displayMetrics)
                context
            }

            //api17+ For KitKat and below, return the original context
            return if (Build.VERSION.SDK_INT <= Build.VERSION_CODES.KITKAT) {
                updatedContext
            } else {
                LocaleContextWrapper(updatedContext)
            }
        }
    }
}