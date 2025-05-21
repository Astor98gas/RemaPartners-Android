package com.arsansys.remapartners.data.service.chat

import com.arsansys.remapartners.data.model.entities.ChatEntity
import com.arsansys.remapartners.data.model.entities.MensajeEntity
import com.arsansys.remapartners.data.repository.chat.ChatApiRest
import retrofit2.Response

class ChatServiceImpl(private val chatApiRest: ChatApiRest) : ChatService {

    override suspend fun getChatById(id: String): Response<ChatEntity> {
        return chatApiRest.getChatById(id)
    }

    override suspend fun getByProductId(id: String): Response<List<ChatEntity>> {
        return chatApiRest.getByProductId(id)
    }
    
    override suspend fun getByBuyerId(id: String): Response<List<ChatEntity>> {
        return chatApiRest.getByBuyerId(id)
    }

    override suspend fun getBySellerId(id: String): Response<List<ChatEntity>> {
        return chatApiRest.getBySellerId(id)
    }

    override suspend fun getByParticipants(
        idProducto: String,
        idComprador: String,
        idVendedor: String
    ): Response<ChatEntity> {
        return chatApiRest.getByParticipants(idProducto, idComprador, idVendedor)
    }

    override suspend fun createChat(chat: ChatEntity): Response<ChatEntity> {
        return chatApiRest.createChat(chat)
    }

    override suspend fun addMessage(
        chatId: String,
        mensaje: MensajeEntity
    ): Response<ChatEntity> {
        return chatApiRest.addMessage(chatId, mensaje)
    }

    override suspend fun deleteChat(chatId: String): Response<ChatEntity> {
        return chatApiRest.deleteChat(chatId)
    }
}