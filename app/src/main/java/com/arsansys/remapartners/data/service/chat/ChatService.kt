package com.arsansys.remapartners.data.service.chat

import com.arsansys.remapartners.data.model.entities.ChatEntity
import com.arsansys.remapartners.data.model.entities.MensajeEntity
import retrofit2.Response

interface ChatService {
    suspend fun getChatById(id: String): Response<ChatEntity>

    suspend fun getByProductId(id: String): Response<List<ChatEntity>>

    suspend fun getByBuyerId(id: String): Response<List<ChatEntity>>

    suspend fun getBySellerId(id: String): Response<List<ChatEntity>>

    suspend fun getByParticipants(
        idProducto: String,
        idComprador: String,
        idVendedor: String
    ): Response<ChatEntity>

    suspend fun createChat(chat: ChatEntity): Response<ChatEntity>

    suspend fun addMessage(
        chatId: String,
        mensaje: MensajeEntity
    ): Response<ChatEntity>

    suspend fun deleteChat(chatId: String): Response<ChatEntity>
}