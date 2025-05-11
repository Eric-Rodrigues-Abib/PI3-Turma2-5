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
            //navigate to the passwords list fragment
            findNavController().navigate(R.id.action_addNewPassFragment_to_passListFragment)
        }

        binding.btnConfirmar.setOnClickListener {
            // Check if the user has selected a radio button
            val selectedOption = checkradiogroup()
            if (selectedOption != "Selecione uma das opções") {
                // Show a message to the user
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

    private fun createNewPassword(
        NomeSite: String,
        SenhaSite: String,
        Categoria: String
    ) {
        val auth = Firebase.auth
        val db = FirebaseFirestore.getInstance()

        if (!(NomeSite.isEmpty() || SenhaSite.isEmpty())) {
            val user = auth.currentUser

            // create the logic behind the cryptographing the password
            val passwordHased = createMd5Hash(SenhaSite)
            // create the Access token 256 (base64)
            val accessToken = generateAccessToken()

            val passwordObject = hashMapOf(
                "nomeSite" to NomeSite,
                "senha" to passwordHased,
                "categoria" to Categoria,
                "accessToken" to accessToken,
            )

            //validate if the passwordHased and val accessToken are not empty and valid
            if (passwordHased.isEmpty() || accessToken.isEmpty()) {
                Snackbar.make(
                    binding.root,
                    "Erro ao criar senha/ problema gerando token/senha",
                    Snackbar.LENGTH_SHORT
                ).show()
                return
            }

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

    private fun generateAccessToken(): String {
        // generates the access token 256 (base64)
        val accessToken = ByteArray(32)
        for (i in accessToken.indices) {
            accessToken[i] = (0..255).random().toByte()
        }
        return Base64.encodeToString(accessToken, android.util.Base64.NO_WRAP)
    }

    private fun createMd5Hash(SenhaSite: String): String {
        // create the logic behind the cryptographing the password
        val md = MessageDigest.getInstance("MD5")
        return BigInteger(1, md.digest(SenhaSite.toByteArray())).toString(16).padStart(32, '0')
    }

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
