package com.arsansys.remapartners.data.repository.chat

import com.arsansys.remapartners.data.model.entities.ChatEntity
import com.arsansys.remapartners.data.model.entities.MensajeEntity
import retrofit2.Response
import retrofit2.http.*

interface ChatApiRest {
    companion object {
        const val BASE_URL = "api/chat/"
    }

    @GET(BASE_URL + "getById/{id}")
    suspend fun getChatById(@Path("id") id: String): Response<ChatEntity>

    @GET(BASE_URL + "getByProductId/{id}")
    suspend fun getByProductId(@Path("id") id: String): Response<List<ChatEntity>>

    @GET(BASE_URL + "getByBuyerId/{id}")
    suspend fun getByBuyerId(@Path("id") id: String): Response<List<ChatEntity>>

    @GET(BASE_URL + "getBySellerId/{id}")
    suspend fun getBySellerId(@Path("id") id: String): Response<List<ChatEntity>>

    @GET(BASE_URL + "getByParticipants/{idProducto}/{idComprador}/{idVendedor}")
    suspend fun getByParticipants(
        @Path("idProducto") idProducto: String,
        @Path("idComprador") idComprador: String,
        @Path("idVendedor") idVendedor: String
    ): Response<ChatEntity>

    @POST(BASE_URL + "create")
    suspend fun createChat(@Body chat: ChatEntity): Response<ChatEntity>

    @POST(BASE_URL + "addMessage/{chatId}")
    suspend fun addMessage(
        @Path("chatId") chatId: String,
        @Body mensaje: MensajeEntity
    ): Response<ChatEntity>

    @DELETE(BASE_URL + "delete/{chatId}")
    suspend fun deleteChat(@Path("chatId") chatId: String): Response<ChatEntity>
}