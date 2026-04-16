package com.example.habitosapp.ui.auth

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.habitosapp.AppContainer
import com.example.habitosapp.databinding.ActivityRegisterBinding
import com.example.habitosapp.MainActivity

class RegisterActivity : AppCompatActivity() {

    private lateinit var binding: ActivityRegisterBinding
    private val authViewModel by lazy { AppContainer.authViewModel() }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Log.d("LIFECYCLE", "RegisterActivity → onCreate")

        binding = ActivityRegisterBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.btnRegistrarse.setOnClickListener { intentarRegistro() }
        binding.btnIrLogin.setOnClickListener     { finish() }
    }

    override fun onDestroy() { super.onDestroy(); Log.d("LIFECYCLE", "RegisterActivity → onDestroy") }

    private fun intentarRegistro() {
        val usuario   = binding.etUsuario.text.toString().trim()
        val email     = binding.etEmail.text.toString().trim()
        val clave     = binding.etContrasena.text.toString().trim()
        val confirmar = binding.etConfirmarContrasena.text.toString().trim()

        if (usuario.isEmpty() || email.isEmpty() || clave.isEmpty() || confirmar.isEmpty()) {
            Toast.makeText(this, "Complete todos los campos", Toast.LENGTH_SHORT).show()
            return
        }

        setLoading(true)
        authViewModel.registro(
            nombreUsuario     = usuario,
            email             = email,
            password          = clave,
            confirmarPassword = confirmar,
            onSuccess = {
                Toast.makeText(this, "¡Cuenta creada! Bienvenido ${it.nombreUsuario}", Toast.LENGTH_SHORT).show()
                startActivity(
                    Intent(this, MainActivity::class.java).apply {
                        flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                    }
                )
                finish()
            },
            onError = { mensaje ->
                setLoading(false)
                Toast.makeText(this, mensaje, Toast.LENGTH_SHORT).show()
            }
        )
    }

    private fun setLoading(loading: Boolean) {
        binding.btnRegistrarse.isEnabled = !loading
        binding.progressBar.visibility   = if (loading) View.VISIBLE else View.GONE
    }
}