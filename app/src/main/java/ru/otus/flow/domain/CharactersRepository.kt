package ru.otus.flow.domain

import kotlinx.coroutines.flow.flow
import ru.otus.flow.data.RAMRetrofitService

class CharactersRepository(private val api: RAMRetrofitService) {

    suspend fun getAllCharacters(): Result<List<RaMCharacter>> = runCatching {
        api.getCharacters().results
            .map { dto ->
                RaMCharacter(
                    id = dto.id,
                    name = dto.name,
                    image = dto.image,
                )
            }
    }

    suspend fun getCharacterById(id: Long) = flow {
        emit(
            api.getCharacterById(id)
        )
    }

    fun getAllCharactersByFlow() = flow {
        // throw Exception("Test") // Имитация ошибки для того чтобы получить Error State
        emit(
            api.getCharacters().results
                .map { dto ->
                    RaMCharacter(
                        id = dto.id,
                        name = dto.name,
                        image = dto.image,
                    )
                }
        )
    }
}
