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
import java.security.SecureRandom
import javax.crypto.Cipher
import javax.crypto.KeyGenerator
import javax.crypto.SecretKey
import javax.crypto.spec.IvParameterSpec
import javax.crypto.spec.SecretKeySpec


class PasswordEditFragment : Fragment() {
    private val TAG = "PasswordEditFragment"
    private var _binding: FragmentPasswordEditBinding? = null
    private val binding get() = _binding!!
    private lateinit var auth: FirebaseAuth

    private var documentId: String = ""

    private lateinit var originalNome: String
    private lateinit var originalSenha: String
    private lateinit var originalCategoria: String
    private lateinit var originalSecretKey: String
    private lateinit var originalIV: String


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
        val secretKey = arguments?.getString("secretKey") ?: ""
        val iv = arguments?.getString("iv") ?: ""
        documentId = arguments?.getString("documentId") ?: ""

        // Salva os dados originais
        originalNome = nome
        originalSenha = senha
        originalCategoria = categoria
        originalSecretKey = secretKey
        originalIV = iv

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
                putString("secretKey", secretKey)
                putString("iv", iv)
                putString("documentId", documentId)
            }

            if (houveAlteracao()) {
                val alertDialog = AlertDialog.Builder(requireContext())
                alertDialog.setTitle("Atenção")
                alertDialog.setMessage("Você tem certeza que deseja voltar? As alterações não serão salvas.")
                alertDialog.setPositiveButton("Sim") { _, _ ->
                    if (isAdded) {
                        findNavController().navigate(R.id.action_passwordEditFragment_to_passwordDetailsFragment, bundle)
                    }
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

            if (novaCategoria != "Selecione uma das opções") {
                val novosDados = mutableMapOf<String, Any>(
                    "nomeSite" to novoNome,
                    "categoria" to novaCategoria
                )

                if (novaSenha != originalSenha) {
                    // NOVA SENHA => gera nova chave
                    val newKey = generateSecretKey()
                    val newIV = generateIV()
                    val senhaCriptografada = encryptPassword(novaSenha, newKey, newIV)
                    val novoAccessToken = generateAccessToken()

                    novosDados["senha"] = senhaCriptografada
                    novosDados["accessToken"] = novoAccessToken
                    novosDados["secretKey"] = Base64.encodeToString(newKey.encoded, Base64.NO_WRAP)
                    novosDados["iv"] = Base64.encodeToString(newIV.iv, Base64.NO_WRAP)

                } else {
                    // SENHA NÃO ALTERADA => mantém a chave antiga
                    novosDados["senha"] = novaSenha
                    novosDados["accessToken"] = accessToken
                    novosDados["secretKey"] = originalSecretKey
                    novosDados["iv"] = originalIV
                }

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

    private fun generateSecretKey(): SecretKey {
        val keyGenerator = KeyGenerator.getInstance("AES")
        keyGenerator.init(256)
        return keyGenerator.generateKey()
    }

    private fun generateIV(): IvParameterSpec {
        val iv = ByteArray(16)
        val secureRandom = SecureRandom()
        secureRandom.nextBytes(iv)
        return IvParameterSpec(iv)
    }

    private fun encryptPassword(
        senhaSite: String,
        secretKey: SecretKey,
        iv: IvParameterSpec
    ): String {
        val plainText = senhaSite.toByteArray()

        val cipher = Cipher.getInstance("AES/CBC/PKCS7Padding")
        cipher.init(Cipher.ENCRYPT_MODE, secretKey, iv)

        val encrypt = cipher.doFinal(plainText)
        return Base64.encodeToString(encrypt, Base64.DEFAULT)
    }

    override fun onDestroyView() {
        super.onDestroyView()
    }
}