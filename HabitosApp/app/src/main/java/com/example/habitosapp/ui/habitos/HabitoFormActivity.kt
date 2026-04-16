package com.example.habitosapp.ui.habitos

import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.habitosapp.AppContainer
import com.example.habitosapp.databinding.ActivityHabitoFormBinding
import com.example.habitosapp.domain.model.Habito

class HabitoFormActivity : AppCompatActivity() {

    private lateinit var binding: ActivityHabitoFormBinding
    private val viewModel by lazy { AppContainer.habitosViewModel() }
    private var habitoEditando: Habito? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Log.d("LIFECYCLE", "HabitoFormActivity → onCreate")

        binding = ActivityHabitoFormBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Recuperar el Habito pasado por Intent (modo edición)
        habitoEditando = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            intent.getParcelableExtra(EXTRA_HABITO, Habito::class.java)
        } else {
            @Suppress("DEPRECATION")
            intent.getParcelableExtra(EXTRA_HABITO)
        }

        configurarUI()
        binding.btnGuardar.setOnClickListener { guardar() }
    }

    override fun onDestroy() { super.onDestroy(); Log.d("LIFECYCLE", "HabitoFormActivity → onDestroy") }

    private fun configurarUI() {
        setSupportActionBar(binding.toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        if (habitoEditando != null) {
            supportActionBar?.title = "Editar hábito"
            binding.etNombre.setText(habitoEditando!!.nombre)
            binding.etDescripcion.setText(habitoEditando!!.descripcion)
            binding.btnGuardar.text = "Actualizar"
        } else {
            supportActionBar?.title = "Nuevo hábito"
            binding.btnGuardar.text = "Crear hábito"
        }
    }

    private fun guardar() {
        val nombre      = binding.etNombre.text.toString().trim()
        val descripcion = binding.etDescripcion.text.toString().trim()

        if (nombre.isEmpty()) {
            binding.tilNombre.error = "El nombre es requerido"
            return
        }
        binding.tilNombre.error = null

        setLoading(true)

        if (habitoEditando == null) {
            // ── CREAR ────────────────────────────────────────────────
            viewModel.crearHabito(
                nombre      = nombre,
                descripcion = descripcion,
                onSuccess   = {
                    Toast.makeText(this, "Hábito \"${it.nombre}\" creado", Toast.LENGTH_SHORT).show()
                    finish()   // Regresa a HabitosActivity → onResume recarga la lista
                },
                onError = { msg ->
                    setLoading(false)
                    Toast.makeText(this, msg, Toast.LENGTH_SHORT).show()
                }
            )
        } else {
            // ── ACTUALIZAR ───────────────────────────────────────────
            val actualizado = habitoEditando!!.copy(
                nombre      = nombre,
                descripcion = descripcion
            )
            viewModel.actualizarHabito(
                habito    = actualizado,
                onSuccess = {
                    Toast.makeText(this, "Hábito actualizado", Toast.LENGTH_SHORT).show()
                    finish()
                },
                onError = { msg ->
                    setLoading(false)
                    Toast.makeText(this, msg, Toast.LENGTH_SHORT).show()
                }
            )
        }
    }

    private fun setLoading(loading: Boolean) {
        binding.btnGuardar.isEnabled   = !loading
        binding.progressBar.visibility = if (loading) View.VISIBLE else View.GONE
    }

    override fun onSupportNavigateUp(): Boolean {
        finish()
        return true
    }

    companion object {
        const val EXTRA_HABITO = "extra_habito"
    }
}