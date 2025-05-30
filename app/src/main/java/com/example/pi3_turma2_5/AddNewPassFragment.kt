package com.example.pi3_turma2_5

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Switch
import androidx.navigation.fragment.findNavController
import com.example.pi3_turma2_5.databinding.FragmentAddNewPassBinding
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.Firebase
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.auth
import com.google.firebase.firestore.FirebaseFirestore
import java.math.BigInteger
import java.security.MessageDigest
import android.util.Base64
import android.util.Log
import com.google.firebase.firestore.FieldValue
import java.security.SecureRandom
import javax.crypto.Cipher
import javax.crypto.KeyGenerator
import javax.crypto.SecretKey
import javax.crypto.spec.IvParameterSpec

class AddNewPassFragment : Fragment() {

    private val TAG = "AddNewPassFragment"
    private lateinit var auth: FirebaseAuth

    private var _binding: FragmentAddNewPassBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentAddNewPassBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        auth = FirebaseAuth.getInstance()

        binding.IBback.setOnClickListener {
            // Navega de de volta para a lista de senhas
            findNavController().navigate(R.id.action_addNewPassFragment_to_passListFragment)
        }

        // Configura o clique do botão de confirmar
        binding.btnConfirmar.setOnClickListener {
            // Checa de os campos do radio group estão preenchidos
            val selectedOption = checkradiogroup()
            if (selectedOption != "Selecione uma das opções") {
                // Cria a nova senha com os dados preenchidos
                createNewPassword(
                    binding.etNomeSite.text.toString(),
                    binding.etSenha.text.toString(),
                    selectedOption
                )
            } else {
                Snackbar.make(
                    binding.root,
                    "Selecione uma das opções",
                    Snackbar.LENGTH_SHORT
                ).show()
            }
        }
    }

    /**
     * Cria uma nova senha no Firestore com os dados fornecidos.
     *
     * @param NomeSite O nome do site ou aplicativo para o qual a senha está sendo criada.
     * @param SenhaSite A senha a ser armazenada.
     * @param Categoria A categoria da senha (por exemplo, "Site Web", "Aplicativo", etc.).
     */
    private fun createNewPassword(
        NomeSite: String,
        SenhaSite: String,
        Categoria: String
    ) {
        // Pega a instância do Firebase Auth e Firestore
        val auth = Firebase.auth
        val db = FirebaseFirestore.getInstance()

        // Verifica se os campos NomeSite e SenhaSite não estão vazios
        if (!(NomeSite.isEmpty() || SenhaSite.isEmpty())) {
            val user = auth.currentUser

            // Gera a chave secreta e o vetor de inicialização (IV) para criptografia
            val secretKey = generateSecretKey()
            val iv = generateIV()
            // Criptografa a senha usando AES/CBC/PKCS7Padding
            val passwordHased = encryptPassword(SenhaSite, secretKey, iv)

            // create the Access token 256 (base64)
            val accessToken = generateAccessToken()

            // Cria um objeto HashMap para armazenar os dados da senha
            val passwordObject = hashMapOf(
                "nomeSite" to NomeSite,
                "senha" to passwordHased,
                "categoria" to Categoria,
                "accessToken" to accessToken,
                "secretKey" to Base64.encodeToString(secretKey.encoded, Base64.NO_WRAP),
                "iv" to Base64.encodeToString(iv.iv, Base64.NO_WRAP),
            )

            // Valida se a senha criptografada e o token de acesso estão vazios
            if (passwordHased.isEmpty() || accessToken.isEmpty()) {
                Snackbar.make(
                    binding.root,
                    "Erro ao criar senha/ problema gerando token/senha",
                    Snackbar.LENGTH_SHORT
                ).show()
                return
            }

            // Adiciona a senha ao Firestore na coleção "users" do usuário autenticado
            db.collection("users")
                .document(user!!.uid)
                .collection("listPassword")
                .add(passwordObject)
                .addOnSuccessListener {
                    Snackbar.make(requireView(), "Senha cadastrada com sucesso", Snackbar.LENGTH_LONG).show()
                    findNavController().navigate(R.id.action_addNewPassFragment_to_passListFragment)
                }
                .addOnFailureListener { e ->
                    Snackbar.make(requireView(), "Erro ao cadastrar senha", Snackbar.LENGTH_LONG).show()
                    Log.e(TAG, "Erro Firebase: ${e.message}", e)
                }
        }
        else {
            Snackbar.make(
                binding.root,
                "Preencha todos os campos",
                Snackbar.LENGTH_SHORT
            ).show()
        }
    }

    /**
     * Gera um token de acesso aleatório de 256 bits codificado em Base64.
     *
     * @return O token de acesso gerado.
     */
    private fun generateAccessToken(): String {
        // generates the access token 256 (base64)
        val accessToken = ByteArray(32)
        for (i in accessToken.indices) {
            accessToken[i] = (0..255).random().toByte()
        }
        return Base64.encodeToString(accessToken, android.util.Base64.NO_WRAP)
    }

    /**
     * Gera uma chave secreta AES de 256 bits.
     *
     * @return A chave secreta gerada.
     */
    private fun generateSecretKey(): SecretKey {
        val keyGenerator = KeyGenerator.getInstance("AES")
        keyGenerator.init(256)
        return keyGenerator.generateKey()
    }

    /**
     * Gera um vetor de inicialização (IV) aleatório de 16 bytes.
     *
     * @return O vetor de inicialização gerado.
     */
    private fun generateIV(): IvParameterSpec {
        val iv = ByteArray(16)
        val secureRandom = SecureRandom()
        secureRandom.nextBytes(iv)
        return IvParameterSpec(iv)
    }

    /**
     * Criptografa a senha usando AES/CBC/PKCS7Padding.
     *
     * @param senhaSite A senha a ser criptografada.
     * @param secretKey A chave secreta usada para criptografia.
     * @param iv O vetor de inicialização usado para criptografia.
     * @return A senha criptografada codificada em Base64.
     */
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

    /**
     * Verifica qual opção do RadioGroup foi selecionada e retorna a categoria correspondente.
     *
     * @return A categoria selecionada ou uma mensagem de erro se nenhuma opção for selecionada.
     */
    private fun checkradiogroup(): String {
        return when (binding.RadioGroup.checkedRadioButtonId) {
            binding.radioButton1.id -> "Site Web"
            binding.radioButton2.id -> "Aplicativo"
            binding.radioButton3.id -> "Teclado físico"
            else -> "Selecione uma das opções"
        }
    }


    override fun onDestroyView() {
        super.onDestroyView()
    }
}
