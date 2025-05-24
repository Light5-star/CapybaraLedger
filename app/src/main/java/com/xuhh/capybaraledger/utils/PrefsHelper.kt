package com.xuhh.capybaraledger.utils

import android.content.Context
import android.content.SharedPreferences
import androidx.preference.PreferenceManager

object PrefsHelper {
    private const val KEY_AGREEMENT_ACCEPTED = "agreement_accepted"

    private fun getPrefs(context: Context): SharedPreferences {
        return PreferenceManager.getDefaultSharedPreferences(context)
    }

    fun isAgreementAccepted(context: Context): Boolean {
        return getPrefs(context).getBoolean(KEY_AGREEMENT_ACCEPTED, false)
    }

    fun setAgreementAccepted(context: Context) {
        getPrefs(context).edit().putBoolean(KEY_AGREEMENT_ACCEPTED, true).apply()
    }
} 