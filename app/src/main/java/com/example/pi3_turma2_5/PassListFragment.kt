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

        adapter = PasswordAdapter(passwordList) { password ->
            val bundle = Bundle().apply {
                putString("nomeSite", password.nomeSite)
                putString("categoria", password.categoria)
                putString("senha", password.senha)
                putString("accessToken", password.accessToken)
                putString("documentId", password.documentId)
            }
            findNavController().navigate(R.id.action_passListFragment_to_passwordDetailsFragment, bundle)
        }

        binding.passwordRecyclerView.adapter = adapter

        binding.btnAdd.setOnClickListener {
            findNavController().navigate(R.id.action_passListFragment_to_addNewPassFragment)
        }
        binding.btnQRCode.setOnClickListener {
            // TODO: ação ao clicar no botão QR Code
        }

        fetchPasswords()
    }

    private fun fetchPasswords() {
        val userId = FirebaseAuth.getInstance().currentUser?.uid ?: return
        db.collection("users").document(userId).collection("listPassword")
            .get()
            .addOnSuccessListener { result ->
                passwordList.clear()
                Log.d("FIREBASE_LIST", "Documentos retornados: ${result.size()}")
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

    override fun onResume() {
        super.onResume()
        fetchPasswords()
    }

}
