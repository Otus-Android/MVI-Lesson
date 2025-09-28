package ru.otus.mvi.presentation

import androidx.recyclerview.widget.RecyclerView
import coil.load
import ru.otus.mvi.databinding.ItemGalleryBinding
import ru.otus.mvi.domain.RaMCharacter

class CharactersViewHolder(private val binding: ItemGalleryBinding) :
    RecyclerView.ViewHolder(binding.root) {

    fun bind(character: RaMCharacter) {
        binding.uiImage.load(character.image)
        binding.uiName.text = character.name
    }
}
