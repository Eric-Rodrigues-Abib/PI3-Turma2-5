package com.example.pi3_turma2_5.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.pi3_turma2_5.databinding.ItemPasswordBinding
import com.example.pi3_turma2_5.model.Password

class PasswordAdapter(
    private val passwordList: List<Password>,
    private val onItemClick: (Password) -> Unit
) : RecyclerView.Adapter<PasswordAdapter.PasswordViewHolder>() {

    inner class PasswordViewHolder(val binding: ItemPasswordBinding) :
        RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PasswordViewHolder {
        val binding = ItemPasswordBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return PasswordViewHolder(binding)
    }

    override fun onBindViewHolder(holder: PasswordViewHolder, position: Int) {
        val item = passwordList[position]
        holder.binding.siteNameText.text = item.nomeSite
        holder.binding.categoryText.text= item.categoria
        holder.binding.root.setOnClickListener {
            onItemClick(item)
        }
    }

    override fun getItemCount(): Int =
        passwordList.size
}