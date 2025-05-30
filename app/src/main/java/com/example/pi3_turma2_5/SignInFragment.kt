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
import com.google.android.material.snackbar.Snackbar
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

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentSignInBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Ao clicar no botão de cadastrar, chama a função signInNewAccount
        binding.btnCadastrar.setOnClickListener {
            signInNewAccount(
                binding.etNome.text.toString(),
                binding.etEmail.text.toString(),
                binding.etSenha.text.toString()
            )
        }

        // Ao clicar no botão de voltar para o login, navega para o fragmento de login
        binding.tvVoltarLogin.setOnClickListener {
            findNavController().navigate(R.id.action_signInFragment_to_logInFragment)
        }
    }

    /**
     * Cria uma nova conta de usuário com email e senha
     * @param nome Nome do usuário
     * @param email Email do usuário
     * @param senha Senha do usuário
     */
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
                        // inicializa o user
                        val user = auth.currentUser
                        // inicializa o PreferencesHelper
                        val preferencesHelper = PreferencesHelper.getInstance(requireContext())
                        // salva os dados do usuário no PreferencesHelper
                        preferencesHelper.uid = user!!.uid
                        // peg ao IMEI do dispositivo
                        val userIMEI = getIMEI(requireContext())
                        // salva o usuário no Firestore
                        salvarUsuarioNoFirestore(nome, email, senha, userIMEI, user.uid)
                        Snackbar.make(requireView(),"Usuário criado com sucesso",Snackbar.LENGTH_LONG).show()
                        findNavController().navigate(R.id.action_signInFragment_to_logInFragment)
                    }
                    else {
                        Log.e("Firebase auth", "Impossível criar " + task.exception)
                    }
                }
        } else {
            Snackbar.make(requireView(),"Nome, email ou senha invalido, Porfavor cheque novamente",Snackbar.LENGTH_LONG).show()
        }

    }

    /**
     * Salva o usuário no Firestore
     * @param nome Nome do usuário
     * @param email Email do usuário
     * @param senha Senha do usuário
     * @param imei IMEI do dispositivo
     * @param uid UID do usuário
     */
    private fun salvarUsuarioNoFirestore(
        nome: String,
        email: String,
        senha: String,
        imei: String,
        uid: String
    ) {
        // inicializa o Firestore
        val db = FirebaseFirestore.getInstance()

        // Cria um HashMap com os dados do usuário
        val user = hashMapOf(
            "nome" to nome,
            "email" to email,
            "senha" to senha,
            "imei" to imei,
            "uid" to uid
        )

        // Salva o usuário no Firestore na coleção "users" com o uid como documento
        db.collection("users")
            .document(uid)
            .set(user)
            .addOnSuccessListener {
                Log.d("Firestore","usuário salvo com sucesso")
            }
            .addOnFailureListener { e ->
                Log.e("Firestore", "Não foi possível salvar o usuário", e)
            }
    }

    /**
     * Valida os dados do usuário
     * @param nome Nome do usuário
     * @param email Email do usuário
     * @param pass Senha do usuário
     * @return Retorna true se os dados forem válidos, caso contrário retorna false
     */
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

    /**
     * Valida o nome do usuário
     * O nome deve ter entre 3 e 15 caracteres
     */
    private fun userNameValidation(nome: String): Boolean {
        if (nome.length < 3 || nome.length > 15) return false
        return true
    }

    /**
     * Valida o email do usuário
     * O email deve conter um "@" e seguir o padrão de email
     */
    private fun userEmailValidation(email: String): Boolean {
        if (!email.contains("@")) return false

        return true
    }

    /**
     * Valida a senha do usuário
     * A senha deve ter pelo menos 6 caracteres
     */
    private fun userPasswordValidation(pass: String): Boolean {
        //pelo menos 6 caracteres
        if (pass.length < 6) return false

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