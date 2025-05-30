package com.example.pi3_turma2_5

import android.app.AlertDialog
import android.app.Dialog
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.DialogFragment
import androidx.navigation.fragment.findNavController
import com.example.pi3_turma2_5.databinding.FragmentTermsAndConditionBinding
import com.example.pi3_turma2_5.userPreferences.PreferencesHelper


class TermsAndConditionFragment : Fragment() {

    private val TAG = "TermsAndConditionFragment"

    private var _binding: FragmentTermsAndConditionBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentTermsAndConditionBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Ao clicar no texto dos termos e condições, abre um AlertDialog com os termos
        // e condições. Assim o usuário pode ler os termos e condições antes de aceitar.
        binding.txTermsAndCondition.setOnClickListener {
            val builder = AlertDialog.Builder(requireContext())
            builder.setTitle("Termos e Condições")
            builder.setMessage("Ao user nos app, você deve aceitar os termos para um bom uso," +
                    "\n\t 1. Você não pode compartilhar sua conta com outras pessoas" +
                    "\n\t 2. Não pode compartilhar informações pessoais com outros usuários" +
                    "\n\t 3. Qualquer tipo de informação vazada será de sua responsabilidade")
            builder.setPositiveButton("Lido") { dialog, which -> }
            builder.show()
        }

        val preferencesHelper = PreferencesHelper.getInstance(requireContext())

        // Ao clicar em continuar, verifica se o checkbox está marcado
        binding.btnContinuar.setOnClickListener {
            if (binding.CBitem1.isChecked)
            {
                // estando marcado o checkbox, salva o estado de termsAndConditionBool como true
                // e navega para o próximo fragmento
                preferencesHelper.termsAndConditionBool = true
                findNavController().navigate(R.id.action_termsAndConditionFragment_to_logInFragment)
            }
            else{
                // se não estiver marcado, exibe um AlertDialog informando que o usuário precisa aceitar os termos e condições
                val builder = AlertDialog.Builder(requireContext())
                builder.setTitle("Erro")
                builder.setMessage("Você precisa aceitar os termos e condições para continuar")
                builder.setPositiveButton("OK") { dialog, which -> }
                builder.show()
            }
        }
    }

    // Limpa o binding quando a view é destruída
    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}