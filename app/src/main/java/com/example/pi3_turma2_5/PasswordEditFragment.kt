package com.example.pi3_turma2_5

import android.app.AlertDialog
import android.os.Bundle
import android.util.Base64
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.navigation.fragment.findNavController
import com.example.pi3_turma2_5.databinding.FragmentPasswordEditBinding
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import java.math.BigInteger
import java.security.MessageDigest


class PasswordEditFragment : Fragment() {
    private val TAG = "PasswordEditFragment"
    private var _binding: FragmentPasswordEditBinding? = null
    private val binding get() = _binding!!
    private lateinit var auth: FirebaseAuth

    private var documentId: String = ""

    private lateinit var originalNome: String
    private lateinit var originalSenha: String
    private lateinit var originalCategoria: String


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentPasswordEditBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        auth = FirebaseAuth.getInstance()

        // recebe os dados do fragmento anterior
        val nome = arguments?.getString("nomeSite") ?: "Sem nome"
        val categoria = arguments?.getString("categoria") ?: "Sem categoria"
        val senha = arguments?.getString("senha") ?: "Sem senha"
        val accessToken = arguments?.getString("accessToken") ?: "Sem access token"
        documentId = arguments?.getString("documentId") ?: ""

        // Salva os dados originais
        originalNome = nome
        originalSenha = senha
        originalCategoria = categoria

        // preenche os campos com os dados recebidos
        binding.etNomeSite.setText(nome)
        binding.etSenha.setText(senha)
        when (categoria) {
            "Site Web" -> binding.RadioGroup.check(binding.radioButton1.id)
            "Aplicativo" -> binding.RadioGroup.check(binding.radioButton2.id)
            "Teclado físico" -> binding.RadioGroup.check(binding.radioButton3.id)
        }

        binding.IBback.setOnClickListener {
            val bundle = Bundle().apply {
                putString("nomeSite", nome)
                putString("categoria", categoria)
                putString("senha", senha)
                putString("accessToken", accessToken)
                putString("documentId", documentId)
            }

            if (houveAlteracao()) {
                val alertDialog = AlertDialog.Builder(requireContext())
                alertDialog.setTitle("Atenção")
                alertDialog.setMessage("Você tem certeza que deseja voltar? As alterações não serão salvas.")
                alertDialog.setPositiveButton("Sim") { _, _ ->
                    findNavController().navigate(R.id.action_passwordEditFragment_to_passwordDetailsFragment, bundle)
                }
                alertDialog.setNegativeButton("Não", null)
                alertDialog.show()
            } else {
                findNavController().navigate(R.id.action_passwordEditFragment_to_passwordDetailsFragment, bundle)
            }
        }

        binding.btnSalvarAlteracoes.setOnClickListener {
            val novoNome = binding.etNomeSite.text.toString()
            val novaSenha = binding.etSenha.text.toString()
            val novaCategoria = getSelectedCategory()

            val user = auth.currentUser
            val db = FirebaseFirestore.getInstance()

            if (!houveAlteracao()) {
                Toast.makeText(requireContext(), "Nenhuma alteração detectada.", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            val selectedOption = getSelectedCategory()
            if (selectedOption != "Selecione uma das opções") {
                val senhaHash = createMd5Hash(novaSenha)
                val novoAccessToken = generateAccessToken()

                val novosDados = mapOf(
                    "nomeSite" to novoNome,
                    "senha" to senhaHash,
                    "categoria" to novaCategoria,
                    "accessToken" to novoAccessToken
                )

                db.collection("users")
                    .document(user!!.uid)
                    .collection("listPassword")
                    .document(documentId)
                    .update(novosDados)
                    .addOnSuccessListener {
                        Toast.makeText(requireContext(), "Senha atualizada com sucesso!", Toast.LENGTH_SHORT).show()
                        findNavController().navigate(R.id.action_passwordEditFragment_to_passListFragment)
                    }
                    .addOnFailureListener { e ->
                        Toast.makeText(requireContext(), "Erro ao atualizar: ${e.message}", Toast.LENGTH_SHORT).show()
                    }
            } else {
                Snackbar.make(
                    binding.root,
                    "Selecione uma das opções",
                    Snackbar.LENGTH_SHORT
                ).show()
                return@setOnClickListener
            }
        }

    }

    private fun houveAlteracao(): Boolean {
        val nomeAtual = binding.etNomeSite.text.toString()
        val senhaAtual = binding.etSenha.text.toString()
        val categoriaAtual = getSelectedCategory()

        return nomeAtual != originalNome || senhaAtual != originalSenha || categoriaAtual != originalCategoria
    }

    private fun getSelectedCategory(): String {
        return when (binding.RadioGroup.checkedRadioButtonId) {
            binding.radioButton1.id -> "Site Web"
            binding.radioButton2.id -> "Aplicativo"
            binding.radioButton3.id -> "Teclado físico"
            else -> "Selecione uma das opções"
        }
    }

    //depois, transformar isso em uma função em uma pasta chamada utils
    private fun generateAccessToken(): String {
        // generates the access token 256 (base64)
        val accessToken = ByteArray(32)
        for (i in accessToken.indices) {
            accessToken[i] = (0..255).random().toByte()
        }
        return Base64.encodeToString(accessToken, android.util.Base64.NO_WRAP)
    }

    //depois, transformar isso em uma função em uma pasta chamada utils
    private fun createMd5Hash(SenhaSite: String): String {
        // create the logic behind the cryptographing the password
        val md = MessageDigest.getInstance("MD5")
        return BigInteger(1, md.digest(SenhaSite.toByteArray())).toString(16).padStart(32, '0')
    }

    override fun onDestroyView() {
        super.onDestroyView()
    }
}