package com.example.pi3_turma2_5

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.camera.lifecycle.ProcessCameraProvider
import com.example.pi3_turma2_5.databinding.FragmentQrScannerBinding
import com.google.mlkit.vision.barcode.BarcodeScanning
import android.Manifest
import android.app.AlertDialog
import android.content.pm.PackageManager
import android.util.Log
import android.widget.Toast
import androidx.camera.core.CameraSelector
import androidx.camera.core.ExperimentalGetImage
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.ImageProxy
import androidx.camera.core.Preview
import androidx.core.content.ContextCompat
import androidx.navigation.fragment.findNavController
import com.google.firebase.Firebase
import com.google.firebase.auth.auth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.mlkit.vision.common.InputImage

class QrScannerFragment : Fragment() {
    private val TAG = "QrScannerFragment"
    private var _binding: FragmentQrScannerBinding? = null
    private val binding get() = _binding!!

    private val scanner = BarcodeScanning.getClient()
    private var cameraProvider: ProcessCameraProvider? = null

    private val cameraPermission = Manifest.permission.CAMERA

    @Volatile
    private var isProcessing = false

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentQrScannerBinding.inflate(inflater, container, false)
        return binding.root
    }

    // Requisita permissão da câmera quando o fragmento é criado
    private val requestPermissionLauncher =
        registerForActivityResult(androidx.activity.result.contract.ActivityResultContracts.RequestPermission()) { isGranted ->
            if (isGranted) {
                startCamera()
            } else {
                Toast.makeText(requireContext(), "Permissão da câmera negada", Toast.LENGTH_SHORT).show()
            }
        }

    // Verifica a permissão da câmera quando o fragmento é retomado
    override fun onResume() {
        super.onResume()
        // Verifica se a permissão da câmera já foi concedida
         // Se sim, inicia a câmera; caso contrário, solicita a permissão
         // Isso garante que a câmera só seja iniciada se a permissão for concedida
        if (ContextCompat.checkSelfPermission(requireContext(), cameraPermission)
            == PackageManager.PERMISSION_GRANTED) {
            startCamera()
        } else {
            requestPermissionLauncher.launch(cameraPermission)
        }
    }

    /**
     * Inicia a câmera e configura o analisador de imagem para processar os QR codes.
     * Utiliza ProcessCameraProvider para vincular o ciclo de vida da câmera ao fragmento.
     */
    private fun startCamera() {
        // Verifica se a permissão da câmera foi concedida
        val cameraProviderFuture = ProcessCameraProvider.getInstance(requireContext())
        cameraProviderFuture.addListener({
            cameraProvider = cameraProviderFuture.get()

            // Configura o Preview e o ImageAnalysis para a câmera
            val preview = Preview.Builder().build().also {
                it.setSurfaceProvider(binding.previewView.surfaceProvider)
            }

            // Configura o ImageAnalysis para processar os QR codes
            val analyzer = ImageAnalysis.Builder()
                .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
                .build()

            // Define o analisador de imagem para processar os QR codes
            analyzer.setAnalyzer(ContextCompat.getMainExecutor(requireContext())) { imageProxy ->
                // Processa o ImageProxy para ler QR codes
                processImageProxy(imageProxy)
            }

            // Seleciona a câmera traseira como padrão
            val cameraSelector = CameraSelector.DEFAULT_BACK_CAMERA

            try {
                cameraProvider?.unbindAll()
                cameraProvider?.bindToLifecycle(viewLifecycleOwner, cameraSelector, preview, analyzer)
            } catch (e: Exception) {
                Log.e("QrScanner", "Erro ao iniciar câmera: ${e.message}")
            }

        }, ContextCompat.getMainExecutor(requireContext()))
    }

    /**
     * Processa o ImageProxy recebido do analisador de imagem.
     * Extrai a imagem e utiliza o BarcodeScanner para detectar QR codes.
     * Se um QR code for detectado, processa o token de login contido nele.
     */
    @androidx.annotation.OptIn(ExperimentalGetImage::class)
    private fun processImageProxy(imageProxy: ImageProxy) {
        // Verifica se a imagem está disponível
        val mediaImage = imageProxy.image ?: run {
            imageProxy.close()
            return
        }
        // Cria um InputImage a partir do MediaImage
        val image = InputImage.fromMediaImage(mediaImage, imageProxy.imageInfo.rotationDegrees)

        // Processa a imagem para detectar QR codes
        scanner.process(image)
            .addOnSuccessListener { barcodes ->
                for (barcode in barcodes) {
                    // Pega o valor do QR code
                    val loginToken = barcode.rawValue
                    // Se o valor não for nulo, processa o token de login
                    if (loginToken != null && !isProcessing) {
                        isProcessing = true
                        Log.d(TAG, "QR lido: $loginToken")
                        processLoginToken(loginToken)
                        break
                    }
                }
            }
            .addOnFailureListener {
                Log.e("QRCode", "Erro na leitura: ${it.message}")
            }
            .addOnCompleteListener {
                imageProxy.close()
            }
    }

    /**
     * Processa o token de login lido do QR code.
     * Verifica se o token é válido e atualiza o status de login do usuário no Firestore.
     * Exibe um diálogo de confirmação ao usuário após o login ser confirmado com sucesso.
     */
    private fun processLoginToken(loginToken: String) {
        // Inicializa o Firebase Auth e Firestore
        val auth = Firebase.auth
        val db = FirebaseFirestore.getInstance()

        // Verifica se o usuário está logado
        val user = auth.currentUser
        if (user == null) {
            if (isAdded){
                Toast.makeText(requireContext(), "Usuário Não logado", Toast.LENGTH_SHORT).show()
                findNavController().navigate(R.id.action_qrScannerFragment_to_passListFragment)
                isProcessing = false
            }
            return
        }

        // Verifica se o loginToken é válido no Firestore
        db.collection("login")
            .whereEqualTo("loginToken", loginToken)
            .get()
            .addOnSuccessListener { result ->
                if (!isAdded) return@addOnSuccessListener

                // Se não houver documentos, o token é inválido ou expirado
                if (result.isEmpty) {
                    Toast.makeText(requireContext(), "Token inválido ou expirado", Toast.LENGTH_SHORT).show()
                    if (isAdded) {
                        findNavController().navigate(R.id.action_qrScannerFragment_to_passListFragment)
                        isProcessing = false
                    }
                    return@addOnSuccessListener
                } else {
                    // Se houver documentos, processa o primeiro documento encontrado
                    val doc = result.documents[0]
                    // Verifica se o QR code já foi utilizado
                    if (doc.contains("user")) {
                        Toast.makeText(requireContext(), "Este QR já foi utilizado", Toast.LENGTH_SHORT).show()
                        findNavController().navigate(R.id.action_qrScannerFragment_to_passListFragment)
                        isProcessing = false
                        return@addOnSuccessListener
                    }

                    // Atualiza o documento com o usuário e a data de confirmação do login
                    val docRef = db.collection("login").document(doc.id)
                    docRef.update(
                        mapOf(
                            "user" to user.uid,
                            "loginConfirmedAt" to com.google.firebase.Timestamp.now()
                        )
                    ).addOnSuccessListener {
                        // Login confirmado com sucesso, agora busca o parceiro associado ao token
                        val apiKey = doc.getString("apiKey")

                        // Busca o partner associado ao apiKey
                        db.collection("partners")
                            .whereEqualTo("apiKey", apiKey)
                            .get()
                            .addOnSuccessListener { partnerResult ->
                                val partnerDoc = partnerResult.documents.firstOrNull()
                                val partnerName = partnerDoc?.getString("url") ?: "o site"

                                // Exibe um diálogo de confirmação ao usuário
                                val dialog = AlertDialog.Builder(requireContext())
                                    .setTitle("SuperID")
                                    .setMessage("Login confirmado com sucesso em $partnerName")
                                    .setCancelable(false)
                                    .create()

                                dialog.show()

                                // mostra o diálogo por 5 segundos e depois navega para PassListFragment
                                binding.root.postDelayed({
                                    if (dialog.isShowing) dialog.dismiss()
                                    findNavController().navigate(R.id.action_qrScannerFragment_to_passListFragment)
                                }, 5000)

                            }.addOnFailureListener {
                                Toast.makeText(requireContext(), "Erro ao buscar parceiro: ${it.message}", Toast.LENGTH_SHORT).show()
                                isProcessing = false
                                findNavController().navigate(R.id.action_qrScannerFragment_to_passListFragment)
                            }
                    }.addOnFailureListener { e ->
                        isProcessing = false
                        Toast.makeText(requireContext(), "Erro ao confirmar login: ${e.message}", Toast.LENGTH_SHORT).show()
                    }
                }
            }
            .addOnFailureListener {
                Log.e(TAG, "Erro ao buscar loginToken: ${it.message}")
                Toast.makeText(requireContext(), "Erro ao buscar loginToken", Toast.LENGTH_SHORT).show()
                isProcessing = false
            }
    }
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Configura o botão de fechar o scanner para navegar de volta para PassListFragment
        binding.btnCloseScanner.setOnClickListener {
            findNavController().navigate(R.id.action_qrScannerFragment_to_passListFragment)
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        cameraProvider?.unbindAll()
    }
}