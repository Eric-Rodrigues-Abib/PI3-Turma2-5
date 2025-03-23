package com.example.pi3_turma2_5

import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.navigation.fragment.findNavController
import com.example.pi3_turma2_5.databinding.FragmentInfoBinding
import com.example.pi3_turma2_5.userPreferences.PreferencesHelper
import kotlin.concurrent.timerTask

class InfoFragment : Fragment() {

    private val TAG = "ÏnfoFragment"

    private var _binding: FragmentInfoBinding? = null
    private val binding get() = _binding!!


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentInfoBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        try {
            val preferencesHelper = PreferencesHelper.getInstance(requireContext())

            binding.btnEntendido.setOnClickListener {
                try{
                    preferencesHelper.infoBool = true
                    findNavController().navigate(R.id.action_infoFragment_to_termsAndConditionFragment)
                } catch (e: Exception){
                    Log.e("InfoFragment", "Erro ao salvar infoSeen", e)
                }
            }
        } catch (e: Exception) {
            Log.e("InfoFragment", "Erro ao inicializar PreferencesHelper", e)
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}