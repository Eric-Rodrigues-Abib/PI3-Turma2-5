package com.example.pi3_turma2_5

import android.os.Bundle
import android.util.Log
import android.view.*
import android.widget.Toast
import androidx.core.view.MenuProvider
import androidx.fragment.app.Fragment
import androidx.lifecycle.Lifecycle
import androidx.navigation.fragment.findNavController
import com.example.pi3_turma2_5.adapter.PasswordAdapter
import com.example.pi3_turma2_5.databinding.FragmentPassListBinding
import com.example.pi3_turma2_5.model.Password
import com.google.firebase.Firebase
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.firestore

class PassListFragment : Fragment() {

    private var _binding: FragmentPassListBinding? = null
    private val binding get() = _binding!!

    private lateinit var adapter: PasswordAdapter
    private val db = Firebase.firestore
    private val passwordList = mutableListOf<Password>()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentPassListBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Recebe o resultado do QR Code, se houver
        val qrResult = arguments?.getString("qr_value")
        qrResult?.let {
            Toast.makeText(requireContext(), "QrCode recebido: $it", Toast.LENGTH_SHORT).show()
        }

        // Passa os dados pelo adapter das senhas cadastradas
        adapter = PasswordAdapter(passwordList) { password ->
            val bundle = Bundle().apply {
                putString("nomeSite", password.nomeSite)
                putString("categoria", password.categoria)
                putString("senha", password.senha)
                putString("accessToken", password.accessToken)
                putString("secretKey", password.secretKey)
                putString("iv", password.iv)
                putString("documentId", password.documentId)
            }
            findNavController().navigate(R.id.action_passListFragment_to_passwordDetailsFragment, bundle)
        }

        // Configura o RecyclerView
        binding.passwordRecyclerView.adapter = adapter

        // Configura o botão de adicionar para navegar para a tela de adicionar nova senha
        binding.btnAdd.setOnClickListener {
            findNavController().navigate(R.id.action_passListFragment_to_addNewPassFragment)
        }
        val user = FirebaseAuth.getInstance().currentUser
        // Configura o botão de QR Code para navegar para a tela de scanner
        binding.btnQRCode.setOnClickListener {
            // Verifica se o usuário está autenticado antes de navegar
            if (user == null || user.isEmailVerified == false) {
                Log.d("PassListFragment", "Usuário não autenticado ou email não verificado: ${user?.uid} e ${user?.isEmailVerified}")
                Toast.makeText(requireContext(), "Por favor, verifique seu email antes de usar o QR Code", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            findNavController().navigate(R.id.action_passListFragment_to_qrScannerFragment)
        }

        // Puxa os dados do Firebase Firestore
        fetchPasswords()

        user?.reload()?.addOnSuccessListener {
            if (!user.isEmailVerified) {
                binding.tvEmailVerification.visibility = View.VISIBLE
                binding.tvEmailVerification.setOnClickListener {
                    user.sendEmailVerification()
                        .addOnSuccessListener {
                            Toast.makeText(requireContext(), "Email de verificação enviado", Toast.LENGTH_SHORT).show()
                        }
                        .addOnFailureListener {
                            Toast.makeText(requireContext(), "Erro ao enviar email de verificação", Toast.LENGTH_SHORT).show()
                        }
                }
            } else {
                binding.tvEmailVerification.visibility = View.GONE
            }
        }
    }

    /**
     * Função para buscar as senhas do usuário autenticado no Firebase Firestore
     * e atualizar a lista de senhas exibida no RecyclerView.
     */
    private fun fetchPasswords() {
        // Verifica se o usuário está autenticado
        val userId = FirebaseAuth.getInstance().currentUser?.uid ?: return
        // Busca as senhas do usuário autenticado
        db.collection("users").document(userId).collection("listPassword")
            .get()
            .addOnSuccessListener { result ->
                passwordList.clear()
                Log.d("FIREBASE_LIST", "Documentos retornados: ${result.size()}")
                // Itera sobre os documentos retornados e adiciona à lista
                for (doc in result) {
                    val password = doc.toObject(Password::class.java).copy(documentId = doc.id)
                    Log.d("FIREBASE_LIST", "Senha carregada: $password")
                    passwordList.add(password)
                }

                if (passwordList.isEmpty()) {
                    binding.tvNoData.visibility = View.VISIBLE
                    binding.passwordRecyclerView.visibility = View.GONE
                } else {
                    binding.tvNoData.visibility = View.GONE
                    binding.passwordRecyclerView.visibility = View.VISIBLE
                }

                // Notifica o adapter que os dados foram atualizados
                adapter.notifyDataSetChanged()
            }
            .addOnFailureListener {
                Toast.makeText(requireContext(), "Erro ao buscar senhas", Toast.LENGTH_SHORT).show()
            }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    // Atualiza o fragment para pegar os dados novos ao voltar das outras telas
    // sempre mantendo atualizado
    override fun onResume() {
        super.onResume()
        fetchPasswords()
    }

}
