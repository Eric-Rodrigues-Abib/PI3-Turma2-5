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

        binding.tvCadastrar.setOnClickListener {
            findNavController().navigate(R.id.action_logInFragment_to_signInFragment)
        }

        binding.btnEntrar.setOnClickListener {

            if (!binding.etEmail.toString().isEmpty() && !binding.etSenha.toString().isEmpty()){
                logInAccount(
                    binding.etEmail.text.toString().trim(),
                    binding.etSenha.text.toString().trim()
                )
            } else {
                binding.etEmail.text.clear()
                binding.etSenha.text.clear()
                Snackbar.make(requireView(),"Email/Senha inválido",Snackbar.LENGTH_LONG).show()
            }
        }
    }

    private fun logInAccount(
        email: String,
        senha: String
    ) {
        hideKeyboard()

        auth = Firebase.auth

        auth.signInWithEmailAndPassword(email, senha)
            .addOnCompleteListener {
                if (it.isSuccessful) {
                    //needs to start the new MainFeatureActivity
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

    private fun hideKeyboard(){
        val imm = activity?.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        imm.hideSoftInputFromWindow(requireView().windowToken, 0)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

}