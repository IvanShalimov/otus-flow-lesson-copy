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
}