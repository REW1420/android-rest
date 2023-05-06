package edu.udb.retrofitappcrud.adaptadores

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import edu.udb.retrofitappcrud.R
import edu.udb.retrofitappcrud.modelos.Profesor


class ProfesorAdapter(private val profesor: List<Profesor>): RecyclerView.Adapter<ProfesorAdapter.ViewHolder>() {

    private var onItemClick: OnItemClickListener? = null

    class ViewHolder(view: View):RecyclerView.ViewHolder(view){
        val nombreTextView: TextView = view.findViewById(R.id.tvNombreP)
        val apellidoTextView : TextView = view.findViewById(R.id.tvApellidoP)
        val carnetTextView : TextView = view.findViewById(R.id.tvCarnetP)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.profesor_item,parent,false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val profesor = profesor[position]
        holder.nombreTextView.text = profesor.nombre
        holder.apellidoTextView.text = profesor.apellido
        holder.carnetTextView.text = profesor.carnet

        holder.itemView.setOnClickListener {
            onItemClick?.onItemClick(profesor)
        }
    }

    override fun getItemCount(): Int {
        return profesor.size
    }

    fun setOnItemClickListener(listener: OnItemClickListener) {
        onItemClick = listener
    }

    interface OnItemClickListener {
        fun onItemClick(profesor: Profesor)
    }
}