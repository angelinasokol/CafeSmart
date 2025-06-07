package com.bignerdranch.android.cafesmart.data

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bignerdranch.android.cafesmart.databinding.ItemDrinkBinding

class DrinkAdapter : RecyclerView.Adapter<DrinkAdapter.DrinkViewHolder>() {

    private var drinks: List<Drink> = emptyList()

    inner class DrinkViewHolder(private val binding: ItemDrinkBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(drink: Drink) {
            binding.drinkNameTextView.text = drink.name
            val tempDescription = when (drink.temperatureCategory) {
                "cold" -> "Холодный"
                "warm" -> "Тёплый"
                "hot" -> "Горячий"
                else -> "Неизвестно"
            }
            binding.drinkTempTextView.text = tempDescription
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): DrinkViewHolder {
        val binding = ItemDrinkBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return DrinkViewHolder(binding)
    }

    override fun onBindViewHolder(holder: DrinkViewHolder, position: Int) {
        holder.bind(drinks[position])
    }

    override fun getItemCount(): Int = drinks.size

    fun setDrinks(newDrinks: List<Drink>) {
        drinks = newDrinks
        notifyDataSetChanged()
    }
}
