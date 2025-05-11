package com.example.pi3_turma2_5

import android.app.AlertDialog
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.navigation.fragment.findNavController
import com.example.pi3_turma2_5.databinding.FragmentPasswordDetailsBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase

class PasswordDetailsFragment : Fragment() {

    private val TAG = "PasswordDetailsFragment"
    private var _binding: FragmentPasswordDetailsBinding? = null
    private val binding get() = _binding!!

    private var documentId: String = ""

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
        documentId = arguments?.getString("documentId") ?: ""

        binding.tvNomeSite.text = nome
        binding.tvCategoria.text = categoria
        binding.tvSenha.text = senha
        binding.tvAccessToken.text = accessToken

        binding.IBback.setOnClickListener {
            findNavController().navigate(R.id.action_passwordDetailsFragment_to_passListFragment)
        }

        binding.IBedit.setOnClickListener {
            //TODO: navegar para a pagina de edição
            val bundle = Bundle().apply {
                putString("nomeSite", nome)
                putString("categoria", categoria)
                putString("senha", senha)
                putString("accessToken", accessToken)
                putString("documentId", documentId)
            }
            findNavController().navigate(R.id.action_passwordDetailsFragment_to_passwordEditFragment, bundle)
        }

        binding.btnExcluir.setOnClickListener {
            showDeleteConfirmationDialog()
        }
    }

    private fun showDeleteConfirmationDialog() {
        val builder = AlertDialog.Builder(requireContext())
        builder.setTitle("Você tem certeza?")
        builder.setMessage("Ao excluir essa senha, não será possível recuperá-la." +
                "\n\t Se deseja, clique em excluir")
        builder.setPositiveButton("Eu quero!") { _, _ ->
            deletePassword()
        }
        // cancel the action, user dont want to delete
        builder.setNegativeButton("Cancelar") { dialog, which -> }
        builder.show()
    }

    private fun deletePassword(){
        if (documentId.isEmpty()) {
            Toast.makeText(requireContext(), "ID inválido, não foi possível excluir", Toast.LENGTH_SHORT).show()
            return
        }

        val db = Firebase.firestore
        val user = FirebaseAuth.getInstance().currentUser?.uid.toString()
        db.collection("users").document(user).collection("listPassword")
            .document(documentId)
            .delete()
            .addOnSuccessListener {
                Toast.makeText(requireContext(), "Senha excluída com sucesso", Toast.LENGTH_SHORT).show()
                findNavController().navigate(R.id.action_passwordDetailsFragment_to_passListFragment)
            }
            .addOnFailureListener { e ->
                Toast.makeText(requireContext(), "Erro ao excluir a senha: ${e.message}", Toast.LENGTH_SHORT).show()
                Log.e(TAG, "Erro ao excluir a senha", e)

            }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}