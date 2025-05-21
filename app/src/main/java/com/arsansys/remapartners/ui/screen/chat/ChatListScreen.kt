package com.arsansys.remapartners.ui.screen.chat

import android.util.Log
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.arsansys.remapartners.data.model.entities.ChatEntity
import com.arsansys.remapartners.data.model.entities.MensajeEntity
import com.arsansys.remapartners.data.repository.RetrofitInstance
import com.arsansys.remapartners.data.repository.chat.ChatApiRest
import com.arsansys.remapartners.data.service.chat.ChatServiceImpl
import com.arsansys.remapartners.data.util.SessionManager
import com.arsansys.remapartners.ui.navigation.Screen
import kotlinx.coroutines.launch
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChatsListScreen(navController: NavController) {
    val context = LocalContext.current
    val sessionManager = remember { SessionManager(context) }
    val userId = sessionManager.fetchUserId()

    Log.d("ChatsScreen", "ID del usuario actual: $userId")

    var chats by remember { mutableStateOf<List<ChatEntity>>(emptyList()) }
    var isLoading by remember { mutableStateOf(true) }
    var error by remember { mutableStateOf<String?>(null) }

    val coroutineScope = rememberCoroutineScope()
    val chatService = remember {
        val api = RetrofitInstance.getRetrofitInstance(context).create(ChatApiRest::class.java)
        ChatServiceImpl(api)
    }

    // Cargar los chats al iniciar la pantalla
    LaunchedEffect(userId) {
        if (userId != null) {
            try {
                isLoading = true
                Log.d("ChatsScreen", "Cargando chats para usuario: $userId")

                // Cargar chats donde el usuario es comprador
                val buyerChatsResponse = chatService.getByBuyerId(userId)
                Log.d(
                    "ChatsScreen",
                    "Respuesta chats como comprador - Código: ${buyerChatsResponse.code()}"
                )

                // Cargar chats donde el usuario es vendedor
                val sellerChatsResponse = chatService.getBySellerId(userId)
                Log.d(
                    "ChatsScreen",
                    "Respuesta chats como vendedor - Código: ${sellerChatsResponse.code()}"
                )

                val buyerChats = if (buyerChatsResponse.isSuccessful) {
                    buyerChatsResponse.body() ?: emptyList()
                } else {
                    Log.e(
                        "ChatsScreen",
                        "Error al cargar chats como comprador: ${
                            buyerChatsResponse.errorBody()?.string()
                        }"
                    )
                    emptyList()
                }

                val sellerChats = if (sellerChatsResponse.isSuccessful) {
                    sellerChatsResponse.body() ?: emptyList()
                } else {
                    Log.e(
                        "ChatsScreen",
                        "Error al cargar chats como vendedor: ${
                            sellerChatsResponse.errorBody()?.string()
                        }"
                    )
                    emptyList()
                }

                // Combinar y ordenar las listas
                chats = (buyerChats + sellerChats).sortedByDescending {
                    it.ultimaActualizacion ?: it.fechaCreacion
                }

                Log.d("ChatsScreen", "Total de chats cargados: ${chats.size}")
            } catch (e: Exception) {
                Log.e("ChatsScreen", "Error al cargar los chats", e)
                error = "Error al cargar los chats: ${e.message}"
            } finally {
                isLoading = false
            }
        } else {
            error = "Debes iniciar sesión para ver tus conversaciones"
            isLoading = false
        }
    }


    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Mis Conversaciones") },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Volver")
                    }
                }
            )
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            when {
                isLoading -> {
                    CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
                }

                error != null -> {
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(16.dp),
                        verticalArrangement = Arrangement.Center,
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = error!!,
                            color = MaterialTheme.colorScheme.error,
                            style = MaterialTheme.typography.bodyLarge
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        Button(onClick = {
                            coroutineScope.launch {
                                error = null
                                isLoading = true
                                try {
                                    val buyerChatsResponse = chatService.getByBuyerId(userId!!)
                                    val sellerChatsResponse = chatService.getBySellerId(userId)

                                    val buyerChats =
                                        if (buyerChatsResponse.isSuccessful) buyerChatsResponse.body()
                                            ?: emptyList() else emptyList()
                                    val sellerChats =
                                        if (sellerChatsResponse.isSuccessful) sellerChatsResponse.body()
                                            ?: emptyList() else emptyList()

                                    chats = (buyerChats + sellerChats).sortedByDescending {
                                        it.ultimaActualizacion ?: it.fechaCreacion
                                    }
                                } catch (e: Exception) {
                                    error = "Error al cargar los chats: ${e.message}"
                                } finally {
                                    isLoading = false
                                }
                            }
                        }) {
                            Text("Intentar de nuevo")
                        }
                    }
                }

                chats.isEmpty() -> {
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(16.dp),
                        verticalArrangement = Arrangement.Center,
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = "No tienes conversaciones activas",
                            style = MaterialTheme.typography.bodyLarge,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                else -> {
                    LazyColumn(
                        modifier = Modifier.fillMaxSize()
                    ) {
                        items(chats) { chat ->
                            ChatItem(
                                chat = chat,
                                currentUserId = userId!!,
                                onChatClick = {
                                    navController.navigate(
                                        Screen.ChatDetail.route +
                                                "?chatId=${chat.id}" +
                                                "&productId=${chat.idProducto}"
                                    )
                                },
                                onProductClick = {
                                    navController.navigate(
                                        Screen.ProductDetail.route +
                                                "?id=${chat.idProducto}"
                                    )
                                }
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun ChatItem(
    chat: ChatEntity,
    currentUserId: String,
    onChatClick: () -> Unit,
    onProductClick: () -> Unit
) {
    val lastMessage = chat.mensajes?.lastOrNull()
    val isCurrentUserSeller = chat.idVendedor == currentUserId
    val otherPersonId = if (isCurrentUserSeller) chat.idComprador else chat.idVendedor


    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp)
            .clickable(onClick = onChatClick),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        shape = RoundedCornerShape(8.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Avatar o imagen del producto
            Box(
                modifier = Modifier
                    .size(50.dp)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.surfaceVariant)
            ) {
                // Aquí podría ir una imagen de perfil o del producto
                AsyncImage(
                    model = ImageRequest.Builder(LocalContext.current)
                        .data("https://via.placeholder.com/50") // Placeholder por ahora
                        .crossfade(true)
                        .build(),
                    contentDescription = "Imagen de chat",
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.fillMaxSize()
                )
            }

            Spacer(modifier = Modifier.width(16.dp))

            // Información del chat
            Column(
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = if (isCurrentUserSeller) "Chat con Comprador" else "Chat con Vendedor",
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )

                // Mostrar último mensaje o "Sin mensajes"
                lastMessage?.let { message ->
                    Text(
                        text = message.mensaje ?: "",
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        fontSize = 14.sp,
                        color = if (message.leido == false && message.idEmisor != currentUserId)
                            MaterialTheme.colorScheme.primary
                        else
                            MaterialTheme.colorScheme.onSurfaceVariant
                    )
                } ?: Text(
                    text = "Sin mensajes",
                    fontSize = 14.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                // Fecha del último mensaje
            }

            Spacer(modifier = Modifier.width(8.dp))

            // Botón para ver el producto
            IconButton(onClick = onProductClick) {
                Icon(
                    imageVector = Icons.Default.ShoppingCart,
                    contentDescription = "Ver producto",
                    tint = MaterialTheme.colorScheme.primary
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChatDetailScreen(
    navController: NavController,
    chatId: String,
    productId: String
) {
    val context = LocalContext.current
    val sessionManager = remember { SessionManager(context) }
    val userId = sessionManager.fetchUserId() ?: ""

    var chat by remember { mutableStateOf<ChatEntity?>(null) }
    var newMessage by remember { mutableStateOf("") }
    var isLoading by remember { mutableStateOf(true) }
    var error by remember { mutableStateOf<String?>(null) }

    val coroutineScope = rememberCoroutineScope()
    val chatService = remember {
        ChatServiceImpl(
            RetrofitInstance.getRetrofitInstance(context).create(ChatApiRest::class.java)
        )
    }

    // Cargar el chat al iniciar
    LaunchedEffect(chatId) {
        if (chatId.isNotEmpty()) {
            try {
                isLoading = true
                val response = chatService.getChatById(chatId)
                if (response.isSuccessful) {
                    chat = response.body()
                } else {
                    error = "Error al cargar el chat: ${response.message()}"
                }
            } catch (e: Exception) {
                error = "Error al cargar el chat: ${e.message}"
            } finally {
                isLoading = false
            }
        } else {
            error = "ID de chat inválido"
            isLoading = false
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = chat?.let {
                            if (userId == it.idVendedor) "Chat con Comprador" else "Chat con Vendedor"
                        } ?: "Conversación"
                    )
                },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Volver")
                    }
                },
                actions = {
                    // Botón para ver el producto
                    IconButton(onClick = {
                        navController.navigate(
                            Screen.ProductDetail.route +
                                    "?id=$productId"
                        )
                    }) {
                        Icon(
                            imageVector = Icons.Default.ShoppingCart,
                            contentDescription = "Ver producto"
                        )
                    }
                }
            )
        },
        bottomBar = {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                OutlinedTextField(
                    value = newMessage,
                    onValueChange = { newMessage = it },
                    placeholder = { Text("Escribe un mensaje...") },
                    modifier = Modifier.weight(1f),
                    maxLines = 5
                )

                Spacer(modifier = Modifier.width(8.dp))

                Button(
                    onClick = {
                        if (newMessage.isNotBlank() && chatId.isNotEmpty() && userId.isNotEmpty()) {
                            coroutineScope.launch {
                                try {
                                    val message = MensajeEntity(
                                        id = null,
                                        idEmisor = userId,
                                        mensaje = newMessage,
                                        leido = false
                                    )

                                    val response = chatService.addMessage(chatId, message)
                                    if (response.isSuccessful) {
                                        chat = response.body()
                                        newMessage = ""
                                    } else {
                                        error = "Error al enviar el mensaje"
                                    }
                                } catch (e: Exception) {
                                    error = "Error al enviar el mensaje: ${e.message}"
                                }
                            }
                        }
                    },
                    enabled = newMessage.isNotBlank()
                ) {
                    Text("Enviar")
                }
            }
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            when {
                isLoading -> {
                    CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
                }

                error != null -> {
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(16.dp),
                        verticalArrangement = Arrangement.Center,
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = error!!,
                            color = MaterialTheme.colorScheme.error,
                            style = MaterialTheme.typography.bodyLarge
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        Button(onClick = {
                            coroutineScope.launch {
                                error = null
                                isLoading = true
                                try {
                                    val response = chatService.getChatById(chatId)
                                    if (response.isSuccessful) {
                                        chat = response.body()
                                    } else {
                                        error = "Error al cargar el chat: ${response.message()}"
                                    }
                                } catch (e: Exception) {
                                    error = "Error al cargar el chat: ${e.message}"
                                } finally {
                                    isLoading = false
                                }
                            }
                        }) {
                            Text("Intentar de nuevo")
                        }
                    }
                }

                chat?.mensajes.isNullOrEmpty() -> {
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(16.dp),
                        verticalArrangement = Arrangement.Center,
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = "No hay mensajes en esta conversación",
                            style = MaterialTheme.typography.bodyLarge,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                else -> {
                    // Lista de mensajes
                    LazyColumn(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(horizontal = 16.dp),
                        reverseLayout = true
                    ) {
                        val messages = chat?.mensajes?.filterNotNull()?.asReversed() ?: emptyList()
                        items(messages) { message ->
                            MessageItem(
                                message = message,
                                isFromCurrentUser = message.idEmisor == userId
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun MessageItem(
    message: MensajeEntity,
    isFromCurrentUser: Boolean
) {
    val dateFormatter = DateTimeFormatter.ofPattern("dd MMMM YY, HH:mm")
    val backgroundColor = if (isFromCurrentUser)
        MaterialTheme.colorScheme.primary.copy(alpha = 0.8f)
    else
        MaterialTheme.colorScheme.surfaceVariant

    val textColor = if (isFromCurrentUser)
        Color.White
    else
        MaterialTheme.colorScheme.onSurfaceVariant

    // Formatear la fecha correctamente
    val formattedDate = try {
        // Intentar parsear la fecha que viene como String
        val fechaHora = LocalDateTime.parse(message.fecha)
        fechaHora.format(dateFormatter)
    } catch (e: Exception) {
        Log.e("ChatScreen", "Error al formatear fecha: ${e.message}")
        // Si falla, mostrar la fecha como viene o un valor por defecto
        message.fecha.split("T").getOrNull(1)?.substring(0, 5) ?: "00:00"
    }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        horizontalArrangement = if (isFromCurrentUser) Arrangement.End else Arrangement.Start
    ) {
        Box(
            modifier = Modifier
                .widthIn(min = 40.dp, max = 280.dp)
                .clip(
                    RoundedCornerShape(
                        topStart = 16.dp,
                        topEnd = 16.dp,
                        bottomStart = if (isFromCurrentUser) 16.dp else 0.dp,
                        bottomEnd = if (isFromCurrentUser) 0.dp else 16.dp
                    )
                )
                .background(backgroundColor)
                .padding(12.dp)
        ) {
            Column {
                Text(
                    text = message.mensaje ?: "",
                    color = textColor,
                    style = MaterialTheme.typography.bodyMedium
                )

                Text(
                    text = formattedDate,
                    color = if (isFromCurrentUser) Color.White.copy(alpha = 0.7f) else MaterialTheme.colorScheme.onSurfaceVariant.copy(
                        alpha = 0.7f
                    ),
                    style = MaterialTheme.typography.labelSmall,
                    modifier = Modifier.align(Alignment.End)
                )
            }
        }
    }
}