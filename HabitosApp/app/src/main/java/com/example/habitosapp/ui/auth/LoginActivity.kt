package com.example.habitosapp.ui.auth


import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.habitosapp.AppContainer
import com.example.habitosapp.databinding.ActivityLoginBinding
import com.example.habitosapp.MainActivity

class LoginActivity : AppCompatActivity() {

    private lateinit var binding: ActivityLoginBinding
    private val authViewModel by lazy { AppContainer.authViewModel() }
    private val session       by lazy { AppContainer.getSessionManager() }

    // ── Lifecycle ──────────────────────────────────────────────────
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Log.d("LIFECYCLE", "LoginActivity → onCreate")

        // Si ya hay sesión activa, ir directo al dashboard
        if (session.isLoggedIn()) {
            abrirMain()
            return
        }

        binding = ActivityLoginBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.btnIngresar.setOnClickListener  { intentarLogin() }
        binding.btnIrRegistro.setOnClickListener {
            startActivity(Intent(this, RegisterActivity::class.java))
        }
    }

    override fun onResume()  { super.onResume();  Log.d("LIFECYCLE", "LoginActivity → onResume")  }
    override fun onPause()   { super.onPause();   Log.d("LIFECYCLE", "LoginActivity → onPause")   }
    override fun onDestroy() { super.onDestroy(); Log.d("LIFECYCLE", "LoginActivity → onDestroy") }

    // ── Lógica ─────────────────────────────────────────────────────
    private fun intentarLogin() {
        val usuario = binding.etUsuario.text.toString().trim()
        val clave   = binding.etContrasena.text.toString().trim()

        if (usuario.isEmpty() || clave.isEmpty()) {
            Toast.makeText(this, "Ingrese usuario y contraseña", Toast.LENGTH_SHORT).show()
            return
        }

        setLoading(true)
        authViewModel.login(
            nombreUsuario = usuario,
            password      = clave,
            onSuccess = {
                Toast.makeText(this, "Bienvenido ${it.nombreUsuario}", Toast.LENGTH_SHORT).show()
                abrirMain()
            },
            onError = { mensaje ->
                setLoading(false)
                Toast.makeText(this, mensaje, Toast.LENGTH_SHORT).show()
            }
        )
    }

    private fun setLoading(loading: Boolean) {
        binding.btnIngresar.isEnabled  = !loading
        binding.progressBar.visibility = if (loading) View.VISIBLE else View.GONE
    }

    private fun abrirMain() {
        startActivity(
            Intent(this, MainActivity::class.java).apply {
                flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            }
        )
        finish()
    }
}