package com.example.pi3_turma2_5


import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.google.firebase.auth.FirebaseAuth

class RedefinirSenhaFragment : Fragment() {

    private lateinit var etEmail: EditText
    private lateinit var btnEnviar: Button
    private lateinit var auth: FirebaseAuth

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_redefinir_senha, container, false)

        etEmail = view.findViewById(R.id.etEmailRedefinir)
        btnEnviar = view.findViewById(R.id.btnEnviarNovaSenha)
        auth = FirebaseAuth.getInstance()

        btnEnviar.setOnClickListener {
            val email = etEmail.text.toString().trim()

            if (email.isEmpty()) {
                Toast.makeText(requireContext(), "Digite seu email", Toast.LENGTH_SHORT).show()
            } else {
                auth.sendPasswordResetEmail(email)
                    .addOnCompleteListener { task ->
                        if (task.isSuccessful) {
                            Toast.makeText(requireContext(), "Email de redefinição enviado", Toast.LENGTH_LONG).show()
                            etEmail.setText("")
                        } else {
                            Toast.makeText(requireContext(), "Erro: ${task.exception?.message}", Toast.LENGTH_LONG).show()
                        }
                    }
            }
        }

        return view
    }
}
