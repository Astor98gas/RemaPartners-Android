package com.arsansys.remapartners.ui.screen

import android.content.BroadcastReceiver
import android.content.ContentValues.TAG
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.graphics.BitmapFactory
import android.util.Log
import android.widget.Toast
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.systemBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Image
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import androidx.navigation.NavController
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.arsansys.remapartners.data.model.entities.ProductoEntity
import com.arsansys.remapartners.data.model.entities.UserEntity
import com.arsansys.remapartners.data.model.enums.EMoneda
import com.arsansys.remapartners.data.model.firebase.Note
import com.arsansys.remapartners.data.repository.ImageApiRest
import com.arsansys.remapartners.data.repository.ImageRepository
import com.arsansys.remapartners.data.repository.ProductosApiRest
import com.arsansys.remapartners.data.repository.UserApiRest
import com.arsansys.remapartners.data.repository.UserRetrofitInstance
import com.arsansys.remapartners.data.util.ImageCache
import com.google.android.gms.tasks.OnCompleteListener
import com.google.firebase.messaging.FirebaseMessaging
import kotlinx.coroutines.launch
import com.arsansys.remapartners.data.util.SessionManager
import com.arsansys.remapartners.ui.navigation.Screen
import kotlinx.coroutines.delay

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    navController: NavController
) {
    val context = LocalContext.current
    val sessionManager = remember { SessionManager(context) }

    val retrofit = UserRetrofitInstance.getRetrofitInstance(context)
    val userApi = retrofit.create(UserApiRest::class.java)
    val productosApi = retrofit.create(ProductosApiRest::class.java)

    val imageCache = ImageCache(
        ImageRepository(
            UserRetrofitInstance.getRetrofitInstance(context).create(ImageApiRest::class.java)
        )
    )

    val coroutineScope = rememberCoroutineScope()
    var firebaseToken by remember { mutableStateOf("") }

    val users = remember { mutableStateListOf<UserEntity>() }
    val productos = remember { mutableStateListOf<ProductoEntity>() }

    // Obtener el nombre de usuario desde SessionManager
    var username = remember { mutableStateOf(sessionManager.fetchUsername() ?: "") }

    // Receptor de broadcast para el token expirado
    val tokenExpiredReceiver = remember {
        object : BroadcastReceiver() {
            override fun onReceive(context: Context, intent: Intent) {
                if (intent.action == "com.arsansys.remapartners.TOKEN_EXPIRED") {
                    username.value = ""
                }
            }
        }
    }

    var isRefreshing by remember { mutableStateOf(false) }

    // Registrar el receptor cuando el composable se active
    DisposableEffect(Unit) {
        val filter = IntentFilter("com.arsansys.remapartners.TOKEN_EXPIRED")
        LocalBroadcastManager.getInstance(context).registerReceiver(tokenExpiredReceiver, filter)

        // Limpiar cuando el composable se destruya
        onDispose {
            LocalBroadcastManager.getInstance(context).unregisterReceiver(tokenExpiredReceiver)
        }
    }

    // Cargar productos al iniciar siempre, independientemente del estado de login
    LaunchedEffect(Unit) {
        // Obtener el token de Firebase
        FirebaseMessaging.getInstance().token.addOnCompleteListener(OnCompleteListener { task ->
            if (!task.isSuccessful) {
                Log.w(TAG, "Fetching FCM registration token failed", task.exception)
                return@OnCompleteListener
            }
            firebaseToken = task.result ?: ""
            Log.d(TAG, "Token: $firebaseToken")
        })

        // Cargar productos al inicio independientemente del estado de login
        cargarProductos(productosApi, productos, context)
    }

    PullToRefreshBox(
        isRefreshing = isRefreshing,
        onRefresh = {
            coroutineScope.launch {
                isRefreshing = true
                try {
                    cargarProductos(productosApi, productos, context)
                } finally {
                    delay(2000)
                    isRefreshing = false
                }
            }
        },
        modifier = Modifier
            .fillMaxSize()
            .systemBarsPadding()
            .imePadding()
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Cabecera que varía según el estado de login
            if (!sessionManager.fetchAuthToken().isNullOrEmpty()) {
                // Nuevo diseño para el encabezado cuando el usuario está conectado
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 8.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Mostrar el nombre del usuario a la izquierda
                    Text(
                        text = if (username.value.isNotEmpty()) "Hola, ${username.value}" else "Bienvenido",
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Medium,
                        modifier = Modifier.weight(1f)
                    )

                    // Botones en la derecha
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        // Botón para ir al perfil - puedes cambiar la ruta según convenga
                        Button(
                            onClick = {
                                // Navegar a la pantalla de perfil cuando exista
                                // navController.navigate(Screen.Profile.route)
                                Toast.makeText(context, "Ir al perfil", Toast.LENGTH_SHORT).show()
                            },
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Text("Perfil")
                        }

                        // Botón para cerrar sesión
                        Button(
                            onClick = {
                                sessionManager.clearData()
                                username.value = ""
                            },
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Text("Cerrar Sesión")
                        }
                    }
                }
            } else {
                // Cabecera para usuarios no logueados
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        "Productos disponibles",
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold
                    )

                    Button(
                        onClick = {
                            navController.navigate(Screen.Login.route)
                        },
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Text("Iniciar Sesión")
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Título del catálogo con estilo mejorado
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(8.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer
                )
            ) {
                Text(
                    "Catálogo de productos",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(12.dp),
                    textAlign = TextAlign.Center,
                    color = MaterialTheme.colorScheme.onPrimaryContainer
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Mostrar productos independientemente del estado de login
            LazyColumn(
                modifier = Modifier.fillMaxWidth()
            ) {
                items(productos) { producto ->
                    ProductoCard(producto = producto)
                }

                // Mostrar mensaje si no hay productos
                if (productos.isEmpty()) {
                    item {
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 32.dp),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Text(
                                text = "No hay productos disponibles",
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(24.dp),
                                textAlign = TextAlign.Center
                            )
                        }
                    }
                }
            }
        }
    }
}

// Función para cargar productos
private suspend fun cargarProductos(
    productosApi: ProductosApiRest,
    productos: MutableList<ProductoEntity>,
    context: Context
) {
    try {
        val response = productosApi.getProductos()
        if (response.isSuccessful) {
            val productosResponse = response.body() ?: emptyList()
            productos.clear()
            productos.addAll(productosResponse)
        } else if (response.code() == 401 || response.code() == 403) {
            // El token ha expirado - El interceptor ya lo manejará
            Toast.makeText(
                context,
                "Tu sesión ha expirado. Por favor, inicia sesión nuevamente.",
                Toast.LENGTH_LONG
            ).show()
        } else {
            Toast.makeText(
                context,
                "Error al obtener productos: ${response.message()}",
                Toast.LENGTH_SHORT
            ).show()
        }
    } catch (e: Exception) {
        Toast.makeText(
            context,
            "Error: ${e.message}",
            Toast.LENGTH_SHORT
        ).show()
    }
}

@Composable
fun ProductoCard(producto: ProductoEntity) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()

    // Usar la caché compartida
    val imageCache = remember {
        ImageCache(
            ImageRepository(
                UserRetrofitInstance.getRetrofitInstance(context).create(ImageApiRest::class.java)
            )
        )
    }

    // Estado para almacenar la imagen principal cargada como ByteArray
    var mainImageBytes by remember { mutableStateOf<ByteArray?>(null) }
    var isLoading by remember { mutableStateOf(true) }

    // Procesar las URLs de las imágenes
    val imageUrls = remember {
        producto.imagenes?.filterNotNull() ?: emptyList()
    }

    // Cargar la imagen principal al montar el componente
    LaunchedEffect(imageUrls) {
        if (imageUrls.isNotEmpty()) {
            isLoading = true
            try {
                // Primero, cargar solo la imagen principal
                val mainUrl = imageUrls.first()
                mainImageBytes = imageCache.getImage(mainUrl)

                // Luego precargar el resto en segundo plano
                coroutineScope.launch {
                    if (imageUrls.size > 1) {
                        imageCache.preloadImages(imageUrls.drop(1))
                    }
                }
            } catch (e: Exception) {
                Log.e("ProductoCard", "Error al cargar imagen: ${e.message}", e)
            } finally {
                isLoading = false
            }
        } else {
            isLoading = false
        }
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
        shape = RoundedCornerShape(12.dp),
        border = BorderStroke(
            width = 1.dp,
            color = MaterialTheme.colorScheme.primary.copy(alpha = 0.5f)
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 16.dp)
        ) {
            // Imagen principal del producto
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(200.dp),
                contentAlignment = Alignment.Center
            ) {
                // Mostrar indicador de carga mientras se carga la imagen
                if (isLoading) {
                    androidx.compose.material3.CircularProgressIndicator()
                } else if (mainImageBytes != null) {
                    // Convertir ByteArray a Bitmap y mostrarlo
                    val bitmap =
                        BitmapFactory.decodeByteArray(mainImageBytes, 0, mainImageBytes!!.size)
                    Image(
                        bitmap = bitmap.asImageBitmap(),
                        contentDescription = "Imagen de ${producto.titulo}",
                        contentScale = ContentScale.Crop,
                        modifier = Modifier.fillMaxSize()
                    )
                } else {
                    // Mostrar placeholder cuando no hay imagen o hay error
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(MaterialTheme.colorScheme.surfaceVariant),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Image,
                            contentDescription = "Sin imagen",
                            modifier = Modifier.size(40.dp),
                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }

            // Contenido del producto
            Column(
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
            ) {
                // Título principal con marca y modelo
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    // Título del producto
                    Text(
                        text = producto.titulo ?: "Sin título",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.weight(1f)
                    )

                    // Precio destacado
                    Card(
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.primaryContainer
                        ),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Text(
                            text = formatearPrecio(producto.precioCentimos, producto.moneda),
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onPrimaryContainer,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                // Información de marca en tarjeta
                Card(
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surfaceVariant
                    ),
                    shape = RoundedCornerShape(6.dp),
                    modifier = Modifier.padding(vertical = 2.dp)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 8.dp, vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Marca:",
                            fontWeight = FontWeight.Medium,
                            fontSize = 14.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = producto.marca ?: "Sin marca",
                            fontSize = 14.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                // Información de modelo en tarjeta
                Card(
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surfaceVariant
                    ),
                    shape = RoundedCornerShape(6.dp),
                    modifier = Modifier.padding(vertical = 2.dp)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 8.dp, vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Modelo:",
                            fontWeight = FontWeight.Medium,
                            fontSize = 14.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = producto.modelo ?: "Sin modelo",
                            fontSize = 14.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                // Información de ubicación en tarjeta
                Card(
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surfaceVariant
                    ),
                    shape = RoundedCornerShape(6.dp),
                    modifier = Modifier.padding(vertical = 2.dp)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 8.dp, vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.LocationOn,
                            contentDescription = "Ubicación",
                            modifier = Modifier.size(16.dp),
                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = producto.direccion ?: "Ubicación no disponible",
                            fontSize = 14.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                // Descripción en tarjeta
                producto.descripcion?.let {
                    Card(
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.surface
                        ),
                        shape = RoundedCornerShape(8.dp),
                        border = BorderStroke(
                            width = 1.dp,
                            color = MaterialTheme.colorScheme.outlineVariant
                        ),
                        modifier = Modifier.padding(vertical = 4.dp)
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(8.dp)
                        ) {
                            Text(
                                text = "Descripción:",
                                fontWeight = FontWeight.Medium,
                                fontSize = 14.sp
                            )
                            Text(
                                text = it,
                                fontSize = 14.sp,
                                maxLines = 2,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                // Miniaturas de las imágenes adicionales (si hay más de una)
                if (imageUrls.size > 1) {
                    Card(
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.surface
                        ),
                        shape = RoundedCornerShape(8.dp),
                        border = BorderStroke(
                            width = 1.dp,
                            color = MaterialTheme.colorScheme.outlineVariant
                        ),
                        modifier = Modifier.padding(vertical = 4.dp)
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(8.dp)
                        ) {
                            Text(
                                text = "Imágenes adicionales:",
                                fontWeight = FontWeight.Medium,
                                fontSize = 14.sp,
                                modifier = Modifier.padding(bottom = 4.dp)
                            )

                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(50.dp),
                                horizontalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                // Mostrar solo las primeras 4 imágenes adicionales como miniaturas
                                imageUrls.drop(1).take(4).forEach { imagenUrl ->
                                    ThumbnailImage(
                                        imageUrl = imagenUrl,
                                        modifier = Modifier
                                            .size(50.dp)
                                            .clip(RoundedCornerShape(4.dp))
                                    )
                                }

                                // Indicador si hay más imágenes
                                if (imageUrls.size > 5) {
                                    Box(
                                        modifier = Modifier
                                            .size(50.dp)
                                            .clip(RoundedCornerShape(4.dp))
                                            .background(MaterialTheme.colorScheme.primary),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Text(
                                            text = "+${imageUrls.size - 5}",
                                            color = MaterialTheme.colorScheme.onPrimary,
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 14.sp
                                        )
                                    }
                                }
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                // Fila de etiquetas de estado/stock/destacado
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    // Estado del producto
                    producto.estado?.let {
                        Card(
                            colors = CardDefaults.cardColors(
                                containerColor = when (it.name) {
                                    "NUEVO" -> MaterialTheme.colorScheme.primaryContainer
                                    "COMO_NUEVO" -> MaterialTheme.colorScheme.secondaryContainer
                                    "BUEN_ESTADO" -> MaterialTheme.colorScheme.tertiaryContainer
                                    else -> MaterialTheme.colorScheme.surfaceVariant
                                }
                            ),
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Text(
                                text = when (it.name) {
                                    "NUEVO" -> "Nuevo"
                                    "COMO_NUEVO" -> "Como nuevo"
                                    "BUEN_ESTADO" -> "Buen estado"
                                    "ACEPTABLE" -> "Aceptable"
                                    else -> it.name
                                },
                                fontSize = 12.sp,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                            )
                        }
                    }

                    // Stock disponible
                    producto.stock?.let {
                        Card(
                            colors = CardDefaults.cardColors(
                                containerColor = if (it > 0) MaterialTheme.colorScheme.primaryContainer
                                else MaterialTheme.colorScheme.errorContainer
                            ),
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Text(
                                text = "Stock: $it",
                                fontSize = 12.sp,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                            )
                        }
                    }

                    // Destacado
                    if (producto.destacado == true) {
                        Card(
                            colors = CardDefaults.cardColors(
                                containerColor = MaterialTheme.colorScheme.tertiaryContainer
                            ),
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Text(
                                text = "Destacado",
                                fontSize = 12.sp,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                            )
                        }
                    }
                }
            }
        }
    }
}

// Función para formatear precio
private fun formatearPrecio(precioCentimos: Int?, moneda: EMoneda?): String {
    if (precioCentimos == null) return "Precio no disponible"

    val precio = precioCentimos / 100.0
    val simbolo = when (moneda) {
        EMoneda.EUR -> "€"
        EMoneda.USD -> "$"
        EMoneda.GBP -> "£"
        else -> "€"
    }

    return "$precio $simbolo"
}

@Composable
fun ThumbnailImage(imageUrl: String, modifier: Modifier = Modifier) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()

    // Usar la caché compartida
    val imageCache = remember {
        ImageCache(
            ImageRepository(
                UserRetrofitInstance.getRetrofitInstance(context).create(ImageApiRest::class.java)
            )
        )
    }

    var imageBytes by remember { mutableStateOf<ByteArray?>(null) }
    var isLoading by remember { mutableStateOf(true) }

    LaunchedEffect(imageUrl) {
        isLoading = true
        coroutineScope.launch {
            try {
                imageBytes = imageCache.getImage(imageUrl)
            } catch (e: Exception) {
                Log.e("ThumbnailImage", "Error: ${e.message}")
            } finally {
                isLoading = false
            }
        }
    }

    Box(modifier = modifier) {
        if (isLoading) {
            androidx.compose.material3.CircularProgressIndicator(
                modifier = Modifier
                    .size(20.dp)
                    .align(Alignment.Center)
            )
        } else if (imageBytes != null) {
            val bitmap = BitmapFactory.decodeByteArray(imageBytes, 0, imageBytes!!.size)
            Image(
                bitmap = bitmap.asImageBitmap(),
                contentDescription = null,
                contentScale = ContentScale.Crop,
                modifier = Modifier.fillMaxSize()
            )
        } else {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(MaterialTheme.colorScheme.surfaceVariant),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Image,
                    contentDescription = "Sin imagen",
                    modifier = Modifier.size(16.dp),
                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}