package com.example.pi3_turma2_5

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.navigation.fragment.findNavController
import com.example.pi3_turma2_5.databinding.FragmentPasswordDetailsBinding
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase

class PasswordDetailsFragment : Fragment() {

    private val TAG = "PasswordDetailsFragment"
    private var _binding: FragmentPasswordDetailsBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentPasswordDetailsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val nome = arguments?.getString("nomeSite") ?: "Sem nome"
        val categoria = arguments?.getString("categoria") ?: "Sem categoria"
        val senha = arguments?.getString("senha") ?: "Sem senha"
        val accessToken = arguments?.getString("accessToken") ?: "Sem access token"

        binding.tvNomeSite.text = nome
        binding.tvCategoria.text = categoria
        binding.tvSenha.text = senha
        binding.tvAccessToken.text = accessToken

        binding.IBback.setOnClickListener {
            findNavController().navigate(R.id.action_passwordDetailsFragment_to_passListFragment)
        }

        binding.IBedit.setOnClickListener {
            //TODO: navegar para a pagina de edição
        }
    }

}