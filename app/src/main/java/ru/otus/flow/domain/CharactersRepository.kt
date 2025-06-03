package ru.otus.flow.domain

import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.asFlow
import kotlinx.coroutines.flow.buffer
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.distinctUntilChangedBy
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onStart
import ru.otus.flow.data.RAMRetrofitService
import ru.otus.flow.data.dto.CharacterDto
import kotlin.random.Random

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

    fun getCharacterById(id: Long) = flow {
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

    fun searchCharacters(query: String) = flow {
        // Имитация поиска с задержкой
        delay(500) // Искусственная задержка для демонстрации
        emit(
            api.searchCharacters(name = query).results.map { dto ->
                RaMCharacter(
                    id = dto.id,
                    name = dto.name,
                    image = dto.image,
                )
            }
        )
    }

    // Используем buffer для оптимизации производительности
    fun getCharactersWithBuffer() = flow {
        api.getCharacters().results
            .asFlow()
            .buffer(10) // Буферизуем 10 элементов
            .map { dto ->
                RaMCharacter(
                    id = dto.id,
                    name = dto.name,
                    image = dto.image,
                )
            }
            .collect { emit(it) }
    }

    fun getCharacterUpdates() = callbackFlow {
        // Используем callbackFlow для работы с callback-based API
        val callback = object : RAMRetrofitService.CharacterCallback {
            override fun onNewCharacter(character: CharacterDto) {
                trySend(character.toDomain())
            }

            override fun onError(e: Throwable) {
                close(e)
            }
        }

        api.registerForCharacterUpdates(callback)

        awaitClose { api.unregisterForCharacterUpdates(callback) }
    }

    fun getCharactersWithDistinct() = flow {
        // Используем distinctUntilChanged для исключения дубликатов
        api.getCharacters().results
            .asFlow()
            .distinctUntilChangedBy { it.id } // Исключаем дубликаты по ID
            .map { dto -> dto.toDomain() }
            .collect { emit(it) }
    }

    fun getAllCharactersWithRetry() = flow {
        // Метод для демонстрации retry
        if (Random.nextBoolean()) {
            throw java.net.UnknownHostException("Simulated network error")
        }
        emit(api.getCharacters().results.map { it.toDomain() })
    }

    // Добавляем новый метод для получения избранных персонажей
    fun getFavoriteCharactersFlow(): Flow<List<RaMCharacter>> = flow {
        // В реальном приложении здесь могла бы быть работа с локальной БД (Room) или SharedPreferences
        // Для учебных целей будем имитировать данные

        // Имитируем загрузку избранных персонажей (например, с ID 1, 2, 5)
        val favoriteIds = setOf(1L, 2L, 5L)

        emit(
            api.getCharacters().results
                .filter { dto -> favoriteIds.contains(dto.id) }
                .map { dto ->
                    RaMCharacter(
                        id = dto.id,
                        name = dto.name,
                        image = dto.image,
                        isFavorite = true
                    )
                }
        )

        // Имитируем периодическое обновление избранного
        delay(5000)
        emit(
            api.getCharacters().results
                .filter { dto -> favoriteIds.contains(dto.id) }
                .map { dto ->
                    RaMCharacter(
                        id = dto.id,
                        name = dto.name,
                        image = dto.image,
                        isFavorite = true
                    )
                }
        )
    }.onStart {
        println("Loading favorite characters...")
    }

    private fun CharacterDto.toDomain() = RaMCharacter(
        id = this.id,
        name = this.name,
        image = this.image
    )
}
