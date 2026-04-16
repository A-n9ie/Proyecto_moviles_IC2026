package com.example.habitosapp.ui.habitos

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.habitosapp.AppContainer
import com.example.habitosapp.databinding.ActivityHabitosBinding
import com.example.habitosapp.domain.model.Habito

class HabitosActivity : AppCompatActivity() {

    private lateinit var binding: ActivityHabitosBinding
    private lateinit var adapter: HabitoAdapter
    private val viewModel by lazy { AppContainer.habitosViewModel() }

    // ── Ciclo de vida completo ──────────────────────────────────────
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Log.d("LIFECYCLE", "HabitosActivity → onCreate")

        binding = ActivityHabitosBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setSupportActionBar(binding.toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.title = "Mis Hábitos"

        configurarRecyclerView()
        binding.fabAgregarHabito.setOnClickListener { abrirFormulario(null) }
    }

    /**
     * onResume es el lugar correcto para cargar datos:
     * - Se ejecuta al abrir la Activity POR PRIMERA VEZ
     * - Se ejecuta al REGRESAR de HabitoFormActivity (crear/editar)
     * Esto garantiza que la lista siempre esté actualizada
     * sin lógica adicional de notificación entre Activities.
     */
    override fun onResume() {
        super.onResume()
        Log.d("LIFECYCLE", "HabitosActivity → onResume — recargando hábitos")
        cargarHabitos()
    }

    override fun onStart()   { super.onStart();   Log.d("LIFECYCLE", "HabitosActivity → onStart")   }
    override fun onPause()   { super.onPause();   Log.d("LIFECYCLE", "HabitosActivity → onPause")   }
    override fun onStop()    { super.onStop();    Log.d("LIFECYCLE", "HabitosActivity → onStop")    }
    override fun onRestart() { super.onRestart(); Log.d("LIFECYCLE", "HabitosActivity → onRestart") }
    override fun onDestroy() { super.onDestroy(); Log.d("LIFECYCLE", "HabitosActivity → onDestroy") }

    // ── Configuración ──────────────────────────────────────────────
    private fun configurarRecyclerView() {
        adapter = HabitoAdapter(
            onEditar   = { habito -> abrirFormulario(habito) },
            onEliminar = { habito -> confirmarEliminar(habito) }
        )
        binding.rvHabitos.layoutManager = LinearLayoutManager(this)
        binding.rvHabitos.adapter       = adapter
    }

    // ── Operaciones CRUD ───────────────────────────────────────────
    private fun cargarHabitos() {
        setLoading(true)
        viewModel.obtenerHabitos(
            onSuccess = { habitos ->
                setLoading(false)
                adapter.actualizarLista(habitos)
                binding.tvSinHabitos.visibility =
                    if (habitos.isEmpty()) View.VISIBLE else View.GONE
            },
            onError = { mensaje ->
                setLoading(false)
                Toast.makeText(this, mensaje, Toast.LENGTH_SHORT).show()
            }
        )
    }

    private fun confirmarEliminar(habito: Habito) {
        AlertDialog.Builder(this)
            .setTitle("Eliminar hábito")
            .setMessage("¿Eliminar \"${habito.nombre}\"? Esta acción no se puede deshacer.")
            .setPositiveButton("Eliminar") { _, _ ->
                viewModel.eliminarHabito(
                    id        = habito.id,
                    onSuccess = {
                        Toast.makeText(this, "Hábito eliminado", Toast.LENGTH_SHORT).show()
                        cargarHabitos()
                    },
                    onError = { msg ->
                        Toast.makeText(this, msg, Toast.LENGTH_SHORT).show()
                    }
                )
            }
            .setNegativeButton("Cancelar", null)
            .show()
    }

    private fun abrirFormulario(habito: Habito?) {
        val intent = Intent(this, HabitoFormActivity::class.java).apply {
            habito?.let { putExtra(HabitoFormActivity.EXTRA_HABITO, it) }
        }
        startActivity(intent)
        // NO se necesita startActivityForResult: onResume recarga automáticamente
    }

    private fun setLoading(loading: Boolean) {
        binding.progressBar.visibility = if (loading) View.VISIBLE else View.GONE
    }

    override fun onSupportNavigateUp(): Boolean {
        finish()
        return true
    }
}