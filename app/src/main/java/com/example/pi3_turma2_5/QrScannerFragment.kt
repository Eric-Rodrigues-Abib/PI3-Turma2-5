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

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentQrScannerBinding.inflate(inflater, container, false)
        return binding.root
    }

    private val requestPermissionLauncher =
        registerForActivityResult(androidx.activity.result.contract.ActivityResultContracts.RequestPermission()) { isGranted ->
            if (isGranted) {
                startCamera()
            } else {
                Toast.makeText(requireContext(), "Permissão da câmera negada", Toast.LENGTH_SHORT).show()
            }
        }

    override fun onResume() {
        super.onResume()
        if (ContextCompat.checkSelfPermission(requireContext(), cameraPermission)
            == PackageManager.PERMISSION_GRANTED) {
            startCamera()
        } else {
            requestPermissionLauncher.launch(cameraPermission)
        }
    }

    private fun startCamera() {
        val cameraProviderFuture = ProcessCameraProvider.getInstance(requireContext())
        cameraProviderFuture.addListener({
            cameraProvider = cameraProviderFuture.get()

            val preview = Preview.Builder().build().also {
                it.setSurfaceProvider(binding.previewView.surfaceProvider)
            }

            val analyzer = ImageAnalysis.Builder()
                .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
                .build()

            analyzer.setAnalyzer(ContextCompat.getMainExecutor(requireContext())) { imageProxy ->
                processImageProxy(imageProxy)
            }

            val cameraSelector = CameraSelector.DEFAULT_BACK_CAMERA

            try {
                cameraProvider?.unbindAll()
                cameraProvider?.bindToLifecycle(viewLifecycleOwner, cameraSelector, preview, analyzer)
            } catch (e: Exception) {
                Log.e("QrScanner", "Erro ao iniciar câmera: ${e.message}")
            }

        }, ContextCompat.getMainExecutor(requireContext()))
    }

    @androidx.annotation.OptIn(ExperimentalGetImage::class)
    private fun processImageProxy(imageProxy: ImageProxy) {
        val mediaImage = imageProxy.image ?: run {
            imageProxy.close()
            return
        }
        val image = InputImage.fromMediaImage(mediaImage, imageProxy.imageInfo.rotationDegrees)

        scanner.process(image)
            .addOnSuccessListener { barcodes ->
                for (barcode in barcodes) {
                    val loginToken = barcode.rawValue
                    if (loginToken != null) {
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

    private fun processLoginToken(loginToken: String) {
        val auth = Firebase.auth
        val db = FirebaseFirestore.getInstance()

        val user = auth.currentUser
        if (user == null) {
            Toast.makeText(requireContext(), "Usuário Não logado", Toast.LENGTH_SHORT).show()
            findNavController().navigate(R.id.action_qrScannerFragment_to_passListFragment)
            return
        }

        db.collection("login")
            .whereEqualTo("loginToken", loginToken)
            .get()
            .addOnSuccessListener { result ->
                if (result.isEmpty) {
                    Toast.makeText(requireContext(), "Token inválido ou expirado", Toast.LENGTH_SHORT).show()
                    findNavController().navigate(R.id.action_qrScannerFragment_to_passListFragment)
                    return@addOnSuccessListener
                }

                val doc = result.documents[0]
                if (doc.contains("user")) {
                    Toast.makeText(requireContext(), "Este QR já foi utilizado", Toast.LENGTH_SHORT).show()
                    findNavController().navigate(R.id.action_qrScannerFragment_to_passListFragment)
                    return@addOnSuccessListener
                }

                val docRef = db.collection("login").document(doc.id)
                docRef.update(
                    mapOf(
                        "user" to user.uid,
                        "loginConfirmedAt" to com.google.firebase.Timestamp.now()
                    )
                ).addOnSuccessListener {
                    val apiKey = doc.getString("apiKey")

                    db.collection("partners")
                        .whereEqualTo("apiKey", apiKey)
                        .get()
                        .addOnSuccessListener { partnerResult ->
                            val partnerDoc = partnerResult.documents[0]
                            val partnerName = partnerDoc.getString("url") ?: "o site"

                            val dialog = AlertDialog.Builder(requireContext())
                                .setTitle("SuperID")
                                .setMessage("Login confirmado com sucesso em $partnerName")
                                .setCancelable(false)
                                .create()

                            dialog.show()

                            //make show during 5 seconds
                            binding.root.postDelayed({
                                if (dialog.isShowing) dialog.dismiss()
                                findNavController().navigate(R.id.action_qrScannerFragment_to_passListFragment)
                            }, 5000)

                        }.addOnFailureListener {
                            Toast.makeText(requireContext(), "Erro ao buscar parceiro: ${it.message}", Toast.LENGTH_SHORT).show()
                            findNavController().navigate(R.id.action_qrScannerFragment_to_passListFragment)
                        }
                }.addOnFailureListener { e ->
                    Toast.makeText(requireContext(), "Erro ao confirmar login: ${e.message}", Toast.LENGTH_SHORT).show()
                }
            }
            .addOnFailureListener {
                Log.e(TAG, "Erro ao buscar loginToken: ${it.message}")
                Toast.makeText(requireContext(), "Erro ao buscar loginToken", Toast.LENGTH_SHORT).show()
            }
    }
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.btnCloseScanner.setOnClickListener {
            findNavController().navigate(R.id.action_qrScannerFragment_to_passListFragment)
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        cameraProvider?.unbindAll()
    }
}