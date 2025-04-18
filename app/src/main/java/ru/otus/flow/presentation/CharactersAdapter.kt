package ru.otus.flow.presentation

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import ru.otus.flow.databinding.ItemGalleryBinding
import ru.otus.flow.domain.RaMCharacter

class CharactersAdapter(
    private val onItemClick: (Long) -> Unit
) : ListAdapter<RaMCharacter, CharactersViewHolder>(DiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CharactersViewHolder {
        return CharactersViewHolder(
            ItemGalleryBinding.inflate(
                LayoutInflater.from(parent.context), parent, false
            )
        )
    }

    override fun onBindViewHolder(holder: CharactersViewHolder, position: Int) {
        val entity = getItem(position)
        entity?.let {
            holder.bind(entity, onItemClick)
        }
    }
}

private class DiffCallback : DiffUtil.ItemCallback<RaMCharacter>() {

    override fun areItemsTheSame(oldItem: RaMCharacter, newItem: RaMCharacter): Boolean {
        return oldItem.id == newItem.id
    }

    override fun areContentsTheSame(oldItem: RaMCharacter, newItem: RaMCharacter): Boolean {
        return oldItem == newItem
    }
}
