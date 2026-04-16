package com.example.habitosapp

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.TextView
import androidx.activity.OnBackPressedCallback
import androidx.appcompat.app.ActionBarDrawerToggle
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.GravityCompat
import com.example.habitosapp.databinding.ActivityMainBinding
import com.example.habitosapp.ui.auth.LoginActivity
import com.example.habitosapp.ui.habitos.HabitosActivity

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private val session       by lazy { AppContainer.getSessionManager() }
    private val authViewModel by lazy { AppContainer.authViewModel() }
    private lateinit var drawerToggle: ActionBarDrawerToggle

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Log.d("LIFECYCLE", "MainActivity → onCreate")

        if (!session.isLoggedIn()) {
            irALogin()
            return
        }

        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        configurarToolbarYDrawer()
        configurarDashboard()

        onBackPressedDispatcher.addCallback(this, object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                if (binding.drawerLayout.isDrawerOpen(GravityCompat.START)) {
                    binding.drawerLayout.closeDrawer(GravityCompat.START)
                } else {
                    isEnabled = false
                    onBackPressedDispatcher.onBackPressed()
                }
            }
        })
    }

    override fun onStart()   { super.onStart();   Log.d("LIFECYCLE", "MainActivity → onStart")   }
    override fun onResume()  { super.onResume();  Log.d("LIFECYCLE", "MainActivity → onResume")  }
    override fun onPause()   { super.onPause();   Log.d("LIFECYCLE", "MainActivity → onPause")   }
    override fun onStop()    { super.onStop();    Log.d("LIFECYCLE", "MainActivity → onStop")    }
    override fun onRestart() { super.onRestart(); Log.d("LIFECYCLE", "MainActivity → onRestart") }
    override fun onDestroy() { super.onDestroy(); Log.d("LIFECYCLE", "MainActivity → onDestroy") }

    // ── Configuración ──────────────────────────────────────────────
    private fun configurarToolbarYDrawer() {
        setSupportActionBar(binding.toolbar)
        supportActionBar?.title = "Panel principal"

        drawerToggle = ActionBarDrawerToggle(
            this, binding.drawerLayout, binding.toolbar,
            R.string.drawer_open, R.string.drawer_close
        )
        binding.drawerLayout.addDrawerListener(drawerToggle)
        drawerToggle.syncState()

        // Mostrar nombre del usuario en el header del drawer
        binding.navigationView
            .getHeaderView(0)
            .findViewById<TextView>(R.id.tvNombreUsuario)
            ?.text = session.getNombreUsuario()

        binding.navigationView.setNavigationItemSelectedListener { item ->
            when (item.itemId) {
                R.id.nav_inicio -> true
                R.id.nav_habitos -> {
                    startActivity(Intent(this, HabitosActivity::class.java))
                    true
                }
                R.id.nav_cerrar_sesion -> {
                    confirmarCerrarSesion()
                    true
                }
                else -> false
            }.also { binding.drawerLayout.closeDrawer(GravityCompat.START) }
        }
    }

    private fun configurarDashboard() {
        binding.tvBienvenida.text = "Bienvenido, ${session.getNombreUsuario()}"
        binding.btnIrHabitos.setOnClickListener {
            startActivity(Intent(this, HabitosActivity::class.java))
        }
    }

    // ── Cerrar sesión ──────────────────────────────────────────────
    private fun confirmarCerrarSesion() {
        AlertDialog.Builder(this)
            .setTitle("Cerrar sesión")
            .setMessage("¿Deseas cerrar la sesión actual?")
            .setPositiveButton("Sí") { _, _ ->
                authViewModel.logout { irALogin() }
            }
            .setNegativeButton("No", null)
            .show()
    }

    private fun irALogin() {
        startActivity(
            Intent(this, LoginActivity::class.java).apply {
                flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            }
        )
        finish()
    }

}