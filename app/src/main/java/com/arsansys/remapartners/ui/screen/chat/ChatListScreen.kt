package com.arsansys.remapartners.ui.screen.chat

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.nestedscroll.nestedScroll
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
import com.arsansys.remapartners.data.repository.productos.ProductosApiRest
import com.arsansys.remapartners.data.repository.user.UserApiRest
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

                // Cargar chats donde el usuario es comprador
                val buyerChatsResponse = chatService.getByBuyerId(userId)

                // Cargar chats donde el usuario es vendedor
                val sellerChatsResponse = chatService.getBySellerId(userId)

                val buyerChats = if (buyerChatsResponse.isSuccessful) {
                    buyerChatsResponse.body() ?: emptyList()
                } else {
                    emptyList()
                }

                val sellerChats = if (sellerChatsResponse.isSuccessful) {
                    sellerChatsResponse.body() ?: emptyList()
                } else {
                    emptyList()
                }

                // Combinar y ordenar las listas
                chats = (buyerChats + sellerChats).sortedByDescending {
                    it.ultimaActualizacion ?: it.fechaCreacion
                }

            } catch (e: Exception) {
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
                                        Screen.ProductDetail.createRoute(chat.idProducto.toString())
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

    // Estado para almacenar los datos del usuario
    var nombreUsuario by remember { mutableStateOf("Cargando...") }
    var nombreProducto by remember { mutableStateOf("") }
    val context = LocalContext.current

    // Cargar los datos del usuario
    LaunchedEffect(otherPersonId) {
        try {
            // Acceso al servicio de usuarios
            val userApiRest =
                RetrofitInstance.getRetrofitInstance(context).create(UserApiRest::class.java)

            // Obtener información del usuario
            val response = userApiRest.getUserById(otherPersonId.toString()!!)
            if (response.isSuccessful && response.body() != null) {
                val usuario = response.body()!!
                nombreUsuario = when {
                    !usuario.username.isNullOrEmpty() -> usuario.username
                    else -> if (isCurrentUserSeller) "Comprador" else "Vendedor"
                }.toString()
            } else {
                nombreUsuario = if (isCurrentUserSeller) "Comprador" else "Vendedor"
            }
        } catch (e: Exception) {
            nombreUsuario = if (isCurrentUserSeller) "Comprador" else "Vendedor"
            android.util.Log.e("ChatItem", "Error al obtener usuario: ${e.message}")
        }
    }

    // Cargar datos del producto
    LaunchedEffect(chat.idProducto) {
        try {
            val productoService = RetrofitInstance.getRetrofitInstance(context)
                .create(ProductosApiRest::class.java)

            val response = productoService.getProductoById(chat.idProducto ?: "")
            if (response.isSuccessful && response.body() != null) {
                nombreProducto = response.body()!!.titulo ?: "Producto sin nombre"
            } else {
                nombreProducto = "Producto no disponible"
            }
        } catch (e: Exception) {
            nombreProducto = "Producto no disponible"
            android.util.Log.e("ChatItem", "Error al obtener producto: ${e.message}")
        }
    }

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

            Spacer(modifier = Modifier.width(16.dp))

            // Información del chat
            Column(
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = "Chat con $nombreUsuario",
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    text = nombreProducto,
                    fontWeight = FontWeight.Medium,
                    fontSize = 14.sp,
                    color = MaterialTheme.colorScheme.primary,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
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

    // Nuevos estados para nombres
    var nombreUsuario by remember { mutableStateOf("Usuario") }
    var nombreProducto by remember { mutableStateOf("Producto") }

    val coroutineScope = rememberCoroutineScope()
    val chatService = remember {
        ChatServiceImpl(
            RetrofitInstance.getRetrofitInstance(context).create(ChatApiRest::class.java)
        )
    }

    val scrollBehavior = TopAppBarDefaults.pinnedScrollBehavior()

    // Cargar el chat al iniciar
    LaunchedEffect(chatId, productId) {
        if (chatId.isNotEmpty()) {
            try {
                isLoading = true
                val response = chatService.getChatById(chatId)
                if (response.isSuccessful) {
                    chat = response.body()

                    // Determinar si somos vendedor o comprador
                    val isCurrentUserSeller = chat?.idVendedor == userId
                    val otherPersonId =
                        if (isCurrentUserSeller) chat?.idComprador else chat?.idVendedor

                    // Cargar datos del otro usuario
                    try {
                        val userApiRest = RetrofitInstance.getRetrofitInstance(context)
                            .create(UserApiRest::class.java)
                        val userResponse = userApiRest.getUserById(otherPersonId ?: "")
                        if (userResponse.isSuccessful && userResponse.body() != null) {
                            val usuario = userResponse.body()!!
                            nombreUsuario = when {
                                !usuario.username.isNullOrEmpty() -> usuario.username
                                else -> if (isCurrentUserSeller) "Comprador" else "Vendedor"
                            }.toString()
                        }
                    } catch (e: Exception) {
                        nombreUsuario = if (isCurrentUserSeller) "Comprador" else "Vendedor"
                    }

                    // Cargar datos del producto
                    try {
                        val productoService = RetrofitInstance.getRetrofitInstance(context)
                            .create(ProductosApiRest::class.java)
                        val productResponse =
                            productoService.getProductoById(chat?.idProducto ?: "")
                        if (productResponse.isSuccessful && productResponse.body() != null) {
                            nombreProducto =
                                productResponse.body()!!.titulo ?: "Producto sin nombre"
                        }
                    } catch (e: Exception) {
                        nombreProducto = "Producto no disponible"
                    }
                } else {
                    error = "No se pudo cargar el chat"
                }
            } catch (e: Exception) {
                error = "Error: ${e.message}"
            } finally {
                isLoading = false
            }
        } else if (productId.isNotEmpty()) {
            // Implementación para crear un nuevo chat si solo tenemos productId
        }
    }

    Scaffold(
        modifier = Modifier.nestedScroll(scrollBehavior.nestedScrollConnection),
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(
                            text = nombreUsuario,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                        Text(
                            text = nombreProducto,
                            fontSize = 12.sp,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Volver"
                        )
                    }
                },
                actions = {
                    IconButton(onClick = {
                        chat?.idProducto?.let { productId ->
                            navController.navigate(Screen.ProductDetail.createRoute(productId))
                        }
                    }) {
                        Icon(
                            imageVector = Icons.Default.ShoppingCart,
                            contentDescription = "Ver producto"
                        )
                    }
                },
                scrollBehavior = scrollBehavior,
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface
                )
            )
        },
        bottomBar = {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .windowInsetsPadding(WindowInsets.systemBars)
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