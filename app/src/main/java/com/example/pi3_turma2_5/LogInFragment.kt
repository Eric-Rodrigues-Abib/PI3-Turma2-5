package com.example.pi3_turma2_5

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.InputMethodManager
import androidx.navigation.fragment.findNavController
import com.example.pi3_turma2_5.databinding.FragmentLogInBinding
import com.example.pi3_turma2_5.databinding.FragmentSignInBinding
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase

class LogInFragment : Fragment() {


    private val TAG = "LoginFragment"
    private lateinit var auth: FirebaseAuth

    private var _binding: FragmentLogInBinding? = null
    private val binding get() = _binding!!
    

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentLogInBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Ao clicar no texto de cadastro, navega para o fragmento de cadastro
        binding.tvCadastrar.setOnClickListener {
            findNavController().navigate(R.id.action_logInFragment_to_signInFragment)
        }

        // Ao clicar no texto de esqueci a senha, navega para o fragmento de redefinir senha
        binding.tvEsqueciSenha.setOnClickListener {
            findNavController().navigate(R.id.action_logInFragment_to_redefinirSenhaFragment)
        }

        // Ao clicar no botão de entrar, verifica se os campos de email e senha estão preenchidos
        binding.btnEntrar.setOnClickListener {

            // Verifica se os campos de email e senha estão preenchidos
            if (!binding.etEmail.toString().isEmpty() && !binding.etSenha.toString().isEmpty()){
                // Se estiverem preenchidos, chama a função logInAccount
                logInAccount(
                    binding.etEmail.text.toString().trim(),
                    binding.etSenha.text.toString().trim()
                )
            } else {
                // Se não estiverem preenchidos, exibe uma Snackbar informando que os campos estão vazios
                binding.etEmail.text.clear()
                binding.etSenha.text.clear()
                Snackbar.make(requireView(),"Email/Senha inválido",Snackbar.LENGTH_LONG).show()
            }
        }
    }

    /**
     * Função para fazer o login do usuário com email e senha
     * @param email Email do usuário
     * @param senha Senha do usuário
     */
    private fun logInAccount(
        email: String,
        senha: String
    ) {
        hideKeyboard()

        // Initialize Firebase Auth
        auth = Firebase.auth

        // Sign in with email and password
        auth.signInWithEmailAndPassword(email, senha)
            .addOnCompleteListener {
                if (it.isSuccessful) {
                    // sucesso no login, navega para a FeatureActivity
                    startActivity(
                        Intent(
                            requireActivity(),
                            FeatureActivity::class.java)
                    )
                    requireActivity().finish()
                } else {
                    Log.e(
                        TAG,
                        "logInAccount: Failed to log in user ${it.exception?.message}"
                    )
                    Snackbar.make(requireView(),"Email/Senha incorreto(s), porfavor revise",Snackbar.LENGTH_LONG).show()
                }
            }
    }

    // Função para esconder o teclado quando o usuário clica fora do campo de texto
    private fun hideKeyboard(){
        val imm = activity?.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        imm.hideSoftInputFromWindow(requireView().windowToken, 0)
    }

    // Limpa o binding quando a view é destruída
    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

}