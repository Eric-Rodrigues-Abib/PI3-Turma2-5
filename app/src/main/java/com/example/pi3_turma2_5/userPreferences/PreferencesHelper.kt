package com.example.pi3_turma2_5.userPreferences

import android.content.Context
import androidx.core.content.edit

class PreferencesHelper private constructor(context: Context){
    private val sharedPreferences =
        context.applicationContext.getSharedPreferences(USER_PREFERENCES_NAME, Context.MODE_PRIVATE)

    var uid: String
        get() = sharedPreferences.getString(UID_KEY, "") ?: ""
        set(value) {
            sharedPreferences.edit() { putString(UID_KEY, value) }
        }

    var infoBool: Boolean
        get() = sharedPreferences.getBoolean(INFO_BOOL, false)
        set(value) {
            sharedPreferences.edit() { putBoolean(INFO_BOOL, value) }
        }

    var termsAndConditionBool: Boolean
        get() = sharedPreferences.getBoolean(TERMSANDCONDITION_BOOL, false)
        set(value) {
            sharedPreferences.edit() { putBoolean(TERMSANDCONDITION_BOOL, value) }
        }

    companion object {
        private const val USER_PREFERENCES_NAME = "prefs_tokens"
        private const val UID_KEY = "uid"
        private const val INFO_BOOL = "info_bool"
        private const val TERMSANDCONDITION_BOOL = "termsandcondition_bool"

        @Volatile
        private var instance: PreferencesHelper? = null

        fun getInstance(context: Context): PreferencesHelper {
            return instance ?: synchronized(this) {
                instance ?: PreferencesHelper(context).also { instance = it }
            }
        }
    }
}