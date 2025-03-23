package com.example.pi3_turma2_5

import android.annotation.SuppressLint
import android.content.Context
import android.nfc.Tag
import android.nfc.tech.TagTechnology
import android.os.Bundle
import android.util.Log
import android.util.Patterns
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.InputMethodManager
import androidx.navigation.fragment.findNavController
import com.example.pi3_turma2_5.databinding.FragmentSignInBinding
import com.example.pi3_turma2_5.userPreferences.PreferencesHelper
import com.google.android.gms.common.api.GoogleApi.Settings
import com.google.firebase.Firebase
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.auth
import com.google.firebase.firestore.FirebaseFirestore
import java.util.regex.Pattern


class SignInFragment : Fragment() {

    private val TAG = "SignInFragment"
    private lateinit var auth: FirebaseAuth

    private var _binding: FragmentSignInBinding? = null
    private val binding get() = _binding!!

    private lateinit var preferencesHelper: PreferencesHelper

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentSignInBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.btnCadastrar.setOnClickListener {
            signInNewAccount(
                binding.etNome.text.toString(),
                binding.etEmail.text.toString(),
                binding.etSenha.text.toString()
            )
        }
    }

    //falta ainda confirmar os datos e também inserir no firestore
    private fun signInNewAccount(
        nome: String,
        email: String,
        senha: String
    ) {
        auth = Firebase.auth

        // Primeiro valida os dados
        if(isUserDatavalid(nome, email, senha)) {
            //cria o usuário no authenticator
            auth.createUserWithEmailAndPassword(email, senha)
                .addOnCompleteListener(requireActivity()) { task ->
                    if (task.isSuccessful) {
                        Log.d(TAG,  "createUserWithEmail:success")
                        val user = auth.currentUser
                        preferencesHelper.uid = user!!.uid
                        val userIMEI = getIMEI(requireContext())
                        salvarUsuarioNoFirestore(nome, email, senha, userIMEI, user.uid)
                        findNavController().navigate(R.id.action_signInFragment_to_logInFragment)
                    }
                    else {
                        Log.e("Firebase auth", "Impossível criar")
                    }
                }
        }

    }

    private fun salvarUsuarioNoFirestore(
        nome: String,
        email: String,
        senha: String,
        IMEI: String,
        UID: String
    ) {
       val db = FirebaseFirestore.getInstance()

        val user = hashMapOf(
            "nome" to nome,
            "email" to email,
            "senha" to senha,
            "IMEI" to IMEI,
            "UID" to UID
        )

        db.collection("users").document(UID)
            .set(user)
            .addOnSuccessListener {
                Log.d("Firestore","usuário salvo com sucesso")
            }
            .addOnFailureListener { e ->
                Log.e("Firestore", "Não foi possível salvar o usuário", e)
            }
    }

    private fun isUserDatavalid(
        nome: String,
        email: String,
        pass: String
    ): Boolean {
        return userNameValidation(nome) &&
                userEmailValidation(email) &&
                userPasswordValidation(pass)
    }

    @SuppressLint("HardwareIds")
    private fun getIMEI(context: Context): String {
        return android.provider.Settings.Secure.getString(context.contentResolver, android.provider.Settings.Secure.ANDROID_ID)
    }

    //necessário validar ainda
    private fun userNameValidation(nome: String): Boolean {
        if (nome.length < 3 || nome.length > 15) return false
        return true
    }

    //necessário validar
    private fun userEmailValidation(email: String): Boolean {
        if (!email.contains("@")) return false
        val emailRegex = "^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\$"
        if (!email.matches(emailRegex.toRegex())) return false
        return true
    }

    //necessario validar
    private fun userPasswordValidation(pass: String): Boolean {
        //pelo menos 6 caracteres
        if (pass.length < 6) return false

        //pelo menos um numero
        var exp = ".*[0-9].*"
        var pattern = Pattern.compile(exp, Pattern.CASE_INSENSITIVE)
        var matcher = pattern.matcher(pass)
        if (!matcher.matches()) return false

        //pelo menos uma letra maiuscula
        exp = ".*[A-Z].*"
        pattern = Pattern.compile(exp)
        matcher = pattern.matcher(pass)
        if (!matcher.matches()) return false

        //pelo menos um letra minuscula
        exp = ".*[a-z].*"
        pattern = Pattern.compile(exp)
        matcher = pattern.matcher(pass)
        if (!matcher.matches()) return false

        //pelo menos um caracter especial
        exp = ".*[@#$%^&+=].*"
        pattern = Pattern.compile(exp)
        matcher = pattern.matcher(pass)
        if (!matcher.matches()) return false

        return true
    }

    private fun hideKeyboard(){
        val imm = activity?.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        imm.hideSoftInputFromWindow(requireView().windowToken, 0)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

}