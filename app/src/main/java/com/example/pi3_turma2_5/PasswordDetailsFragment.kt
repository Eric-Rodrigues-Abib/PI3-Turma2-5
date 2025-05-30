package com.example.pi3_turma2_5

import android.app.AlertDialog
import android.os.Bundle
import android.util.Base64
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
import java.security.SecureRandom
import javax.crypto.Cipher
import javax.crypto.KeyGenerator
import javax.crypto.SecretKey
import javax.crypto.spec.IvParameterSpec
import javax.crypto.spec.SecretKeySpec

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

        // Inicializa os campos com os dados passados pelo Bundle
        // dados recebidos do fragment anterior
        val nome = arguments?.getString("nomeSite") ?: "Sem nome"
        val categoria = arguments?.getString("categoria") ?: "Sem categoria"
        val senha = arguments?.getString("senha") ?: "Sem senha"
        val accessToken = arguments?.getString("accessToken") ?: "Sem access token"
        val secretKey = arguments?.getString("secretKey") ?: ""
        val iv = arguments?.getString("iv") ?: ""
        documentId = arguments?.getString("documentId") ?: ""

        // Decrypt a senha encryptada. A lógica aqui é
        // como a senha volta também da EditFragment
        // e se a senha já estiver decriptada, não faz nada
        val senhaFinal = try {
            decrypt(senha, secretKey, iv)
        } catch (e: Exception) {
            Log.e(TAG, "Erro ao descriptografar a senha: ${e.message}")
            senha

        }

        // Configura os TextViews com os dados recebidos
        binding.tvNomeSite.text = nome
        binding.tvCategoria.text = categoria
        binding.tvSenha.text = senhaFinal
        binding.tvAccessToken.text = accessToken

        // Configura os botões de navegação para voltar a lista de senhas
        binding.IBback.setOnClickListener {
            findNavController().navigate(R.id.action_passwordDetailsFragment_to_passListFragment)
        }

        // Configura o botão de editar senha passando o bundle com os dados
        binding.IBedit.setOnClickListener {
            //TODO: navegar para a pagina de edição
            val bundle = Bundle().apply {
                putString("nomeSite", nome)
                putString("categoria", categoria)
                putString("senha", senhaFinal)
                putString("accessToken", accessToken)
                putString("secretKey", secretKey)
                putString("iv", iv)
                putString("documentId", documentId)
            }
            findNavController().navigate(R.id.action_passwordDetailsFragment_to_passwordEditFragment, bundle)
        }

        // Configura o botão de excluir senha
        binding.btnExcluir.setOnClickListener {
            showDeleteConfirmationDialog()
        }
    }

    /**
     * Função para mostrar um diálogo de confirmação antes de excluir a senha
     * Exibe um AlertDialog com opções de confirmação e cancelamento
     */
    private fun showDeleteConfirmationDialog() {
        val builder = AlertDialog.Builder(requireContext())
        builder.setTitle("Você tem certeza?")
        builder.setMessage("Ao excluir essa senha, não será possível recuperá-la." +
                "\n\t Se deseja, clique em excluir")
        builder.setPositiveButton("Eu quero!") { _, _ ->
            deletePassword()
        }
        // Cancela a exclusão caso o usuário clique em cancelar
        builder.setNegativeButton("Cancelar") { dialog, which -> }
        builder.show()
    }

    /**
     * Função para excluir a senha do Firestore
     * Verifica se o documentId está vazio, caso esteja, não faz nada
     * Se não estiver vazio, exclui o documento correspondente
     */
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

    /**
     * Função para descriptografar a senha usando AES/CBC/PKCS7Padding
     * @param senhaSite A senha criptografada em Base64
     * @param secretKey A chave secreta em Base64
     * @param iv O vetor de inicialização (IV) em Base64
     * @return A senha decriptada como String
     */
    private fun decrypt(
        senhaSite: String,
        secretKey: String,
        iv: String
    ): String {
        return try {
            val cipher = Cipher.getInstance("AES/CBC/PKCS7Padding")

            val secretKeyBytes = Base64.decode(secretKey, Base64.NO_WRAP)
            val ivBytes = Base64.decode(iv, Base64.NO_WRAP)

            val secretKeySpec = SecretKeySpec(secretKeyBytes, "AES")
            val ivSpec = IvParameterSpec(ivBytes)

            cipher.init(Cipher.DECRYPT_MODE, secretKeySpec, ivSpec)

            val encryptedBytes = Base64.decode(senhaSite, Base64.DEFAULT)
            val decryptedBytes = cipher.doFinal(encryptedBytes)

            String(decryptedBytes)
        } catch (e: Exception) {
            Log.e(TAG, "Erro na descriptografia: ${e.message}")
            senhaSite // Retorna como está (provavelmente já decriptado)
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}