package ru.otus.flow.data

import retrofit2.http.GET
import retrofit2.http.Path
import retrofit2.http.Query
import ru.otus.flow.data.dto.CharacterDto
import ru.otus.flow.data.dto.RAMResponseDto

interface RAMRetrofitService {

    companion object {
        const val ENDPOINT = "https://rickandmortyapi.com/api/"
    }

    @GET("character/")
    suspend fun getCharacters(@Query("page") page: Int = 0): RAMResponseDto


    @GET("character/{characterId}")
    suspend fun getCharacterById(@Path("characterId") characterId: Long): CharacterDto
    interface CharacterCallback {
        fun onNewCharacter(character: CharacterDto)
        fun onError(e: Throwable)
    }

    /**
     * Для callbackFlow примера - в реальном API этого нет,
     * но добавляем для демонстрации работы с callback-based API
     */
    fun registerForCharacterUpdates(callback: CharacterCallback) {
        // В реальной реализации здесь была бы подписка на обновления
        // Например, через WebSocket или Firebase
    }

    fun unregisterForCharacterUpdates(callback: CharacterCallback) {
        // Отписка от обновлений
    }

    /**
     * Расширенный поиск с поддержкой пагинации
     * Для демонстрации combine и других операторов
     */
    @GET("character/")
    suspend fun searchCharacters(
        @Query("name") name: String? = null,
        @Query("status") status: String? = null,
        @Query("species") species: String? = null,
        @Query("type") type: String? = null,
        @Query("gender") gender: String? = null,
        @Query("page") page: Int = 1
    ): RAMResponseDto

    /**
     * Для демонстрации работы с Flow и пагинацией
     */
    @GET("character/")
    suspend fun getCharactersPage(
        @Query("page") page: Int
    ): RAMResponseDto
}