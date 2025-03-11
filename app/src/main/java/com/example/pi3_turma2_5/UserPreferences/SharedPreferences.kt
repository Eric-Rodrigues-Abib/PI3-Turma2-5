package com.example.pi3_turma2_5.UserPreferences

import android.content.Context
import kotlinx.coroutines.InternalCoroutinesApi
import kotlinx.coroutines.internal.synchronized

private const val USER_PREFERENCES_NAME = "prefs_tokens"

private const val UID_KEY = "uid"
private const val INFO_BOOL = "info_bool"
private const val TERMSANDCONDITION_BOOL = "termsandcondition_bool"

class SharedPreferences private constructor(context: Context){

    private val sharedPreferences =
        context.applicationContext.getSharedPreferences(USER_PREFERENCES_NAME, Context.MODE_PRIVATE)


    var uid: String = ""
        // usando o get() para retornar o valor do uid
        get(){
            return sharedPreferences.getString(UID_KEY, "")!!
        }

    var infoBool: Boolean = false
        // usando o get() para retornar o valor do infoBool
        get(){
            return sharedPreferences.getBoolean(INFO_BOOL, false)
        }

    var termsAndConditionBool: Boolean = false
        //usando o get() para retornar o valor do termsAndConditionBool
        get(){
            return sharedPreferences.getBoolean(TERMSANDCONDITION_BOOL, false)
        }

    companion object {
        @Volatile
        private var INSTANCE: SharedPreferences? = null
        @OptIn(InternalCoroutinesApi::class)
        fun getInstance(context: Context): SharedPreferences? {
            return INSTANCE ?: synchronized(this){
                INSTANCE?.let {
                    return it
                }

                val instance = SharedPreferences(context)
                INSTANCE = instance
                instance
            }
        }
    }
}