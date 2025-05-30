package com.example.pi3_turma2_5


import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.example.pi3_turma2_5.databinding.FragmentRedefinirSenhaBinding
import com.google.firebase.auth.FirebaseAuth

class RedefinirSenhaFragment : Fragment() {

    private lateinit var etEmail: EditText
    private lateinit var btnEnviar: Button
    private lateinit var auth: FirebaseAuth

    private var _binding: FragmentRedefinirSenhaBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentRedefinirSenhaBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Inicializa os componentes da view
        etEmail = view.findViewById(R.id.etEmailRedefinir)
        btnEnviar = view.findViewById(R.id.btnEnviarNovaSenha)
        auth = FirebaseAuth.getInstance()

        // Configura o clique do botão de enviar
        btnEnviar.setOnClickListener {
            // Recebe o email do campo de texto e verifica se está vazio
            val email = etEmail.text.toString().trim()

            // Se o email estiver vazio, exibe um Toast solicitando o preenchimento
            if (email.isEmpty()) {
                Toast.makeText(requireContext(), "Digite seu email", Toast.LENGTH_SHORT).show()
            } else {
                // Se o email estiver preenchido, tenta enviar o email de redefinição de senha
                auth.sendPasswordResetEmail(email)
                    .addOnCompleteListener { task ->
                        if (task.isSuccessful) {
                            Toast.makeText(
                                requireContext(),
                                "Email de redefinição enviado",
                                Toast.LENGTH_LONG
                            ).show()
                            etEmail.setText("")
                            findNavController().navigate(R.id.action_redefinirSenhaFragment_to_logInFragment)
                        } else {
                            Toast.makeText(
                                requireContext(),
                                "Erro: ${task.exception?.message}",
                                Toast.LENGTH_LONG
                            ).show()
                        }
                    }
            }
        }

        // Configura o clique do botão de voltar
        binding.IBback.setOnClickListener {
            findNavController().navigate(R.id.action_redefinirSenhaFragment_to_logInFragment)
        }
    }

    // Limpa o binding quando a view é destruída
    override fun onDestroyView() {
        super.onDestroyView()
    }
}
