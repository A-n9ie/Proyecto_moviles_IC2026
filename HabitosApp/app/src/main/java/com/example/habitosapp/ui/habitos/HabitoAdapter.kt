package com.example.habitosapp.ui.habitos

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.habitosapp.databinding.ItemHabitoBinding
import com.example.habitosapp.domain.model.Habito

class HabitoAdapter(
    private val onEditar: (Habito) -> Unit,
    private val onEliminar: (Habito) -> Unit
) : RecyclerView.Adapter<HabitoAdapter.ViewHolder>() {

    private val lista = mutableListOf<Habito>()

    fun actualizarLista(nuevaLista: List<Habito>) {
        lista.clear()
        lista.addAll(nuevaLista)
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ItemHabitoBinding.inflate(
            LayoutInflater.from(parent.context), parent, false
        )
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(lista[position])
    }

    override fun getItemCount() = lista.size

    inner class ViewHolder(private val b: ItemHabitoBinding)
        : RecyclerView.ViewHolder(b.root) {

        fun bind(habito: Habito) {
            b.tvNombreHabito.text      = habito.nombre
            b.tvDescripcionHabito.text = habito.descripcion.ifEmpty { "Sin descripción" }
            b.btnEditar.setOnClickListener   { onEditar(habito) }
            b.btnEliminar.setOnClickListener { onEliminar(habito) }
        }
    }
}