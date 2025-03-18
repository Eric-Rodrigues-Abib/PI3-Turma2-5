package com.example.pi3_turma2_5

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.example.pi3_turma2_5.userPreferences.PreferencesHelper
import com.google.firebase.FirebaseApp
import com.google.firebase.appcheck.FirebaseAppCheck
import com.google.firebase.appcheck.debug.DebugAppCheckProviderFactory

class MainActivity : AppCompatActivity() {


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val preferencesHelper = PreferencesHelper.getInstance(this)
        //navController = findNavController(R.id.nav_host_fragment)

        //if(preferencesHelper.infoBool && preferencesHelper.termsAndConditionBool){
            //navController.navigate(R.id.action_global_loginfragment)
        //}

    }

    private fun prepareFirebaseAppCheckDebug(){
        FirebaseApp.initializeApp(this)
        val firebaseAppCheck = FirebaseAppCheck.getInstance()
        firebaseAppCheck.installAppCheckProviderFactory(
            DebugAppCheckProviderFactory.getInstance()
        )
    }

}