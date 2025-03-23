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

        //check if the checkbox is marked to click in continue
        binding.btnContinuar.setOnClickListener {
            if (binding.CBitem1.isChecked)
            {
                preferencesHelper.termsAndConditionBool = true
                findNavController().navigate(R.id.action_termsAndConditionFragment_to_logInFragment)
            }
            else{
                val builder = AlertDialog.Builder(requireContext())
                builder.setTitle("Erro")
                builder.setMessage("Você precisa aceitar os termos e condições para continuar")
                builder.setPositiveButton("OK") { dialog, which -> }
                builder.show()
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}