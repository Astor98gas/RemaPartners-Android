package com.arsansys.remapartners.ui.screen

import android.content.BroadcastReceiver
import android.content.ContentValues.TAG
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.util.Log
import android.widget.Toast
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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Image
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
import com.arsansys.remapartners.data.repository.ProductosApiRest
import com.arsansys.remapartners.data.repository.UserApiRest
import com.arsansys.remapartners.data.repository.UserRetrofitInstance
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
                // Mostrar el nombre del usuario si está disponible
                if (username.value.isNotEmpty()) {
                    Text(
                        "¡Bienvenido, ${username.value}!",
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold
                    )
                } else {
                    Text(
                        "¡Bienvenido! Sesión iniciada correctamente",
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Botones para usuarios logueados
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Button(
                        onClick = {
                            sessionManager.clearData()
                            username.value = ""
                        }
                    ) {
                        Text("Cerrar Sesión")
                    }

                    Button(
                        onClick = {
                            coroutineScope.launch {
                                try {
                                    cargarProductos(productosApi, productos, context)
                                } catch (e: Exception) {
                                    Toast.makeText(
                                        context,
                                        "Error al cargar productos: ${e.message}",
                                        Toast.LENGTH_SHORT
                                    ).show()
                                }
                            }
                        }
                    ) {
                        Text("Actualizar Productos")
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
                        }
                    ) {
                        Text("Iniciar Sesión")
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                "Catálogo de productos",
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp)
            )

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
                        Text(
                            text = "No hay productos disponibles",
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 32.dp),
                            textAlign = TextAlign.Center
                        )
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
    val imagenes = remember { producto.imagenes?.filterNotNull() ?: emptyList() }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 16.dp)
        ) {
            // Imagen principal del producto
            if (imagenes.isNotEmpty()) {
                AsyncImage(
                    model = ImageRequest.Builder(LocalContext.current)
                        .data(imagenes.first())
                        .crossfade(true)
                        .build(),
                    contentDescription = "Imagen de ${producto.titulo}",
                    contentScale = ContentScale.Crop,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(200.dp)
                )
            } else {
                // Imagen placeholder cuando no hay imágenes disponibles
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(120.dp)
                        .background(MaterialTheme.colorScheme.surfaceVariant),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Image,
                        contentDescription = "Sin imagen",
                        modifier = Modifier.size(26.dp),
                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                    )
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
                    Text(
                        text = formatearPrecio(producto.precioCentimos, producto.moneda),
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                }

                Spacer(modifier = Modifier.height(4.dp))

                // Información secundaria
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.Start
                ) {
                    Text(
                        text = "${producto.marca ?: "Sin marca"} • ${producto.modelo ?: "Sin modelo"}",
                        fontSize = 14.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                Spacer(modifier = Modifier.height(8.dp))

                // Descripción corta
                producto.descripcion?.let {
                    Text(
                        text = it,
                        fontSize = 14.sp,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis
                    )

                    Spacer(modifier = Modifier.height(8.dp))
                }

                // Miniaturas de las imágenes adicionales (si hay más de una)
                if (imagenes.size > 1) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(50.dp),
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        // Mostrar solo las primeras 4 imágenes adicionales como miniaturas
                        imagenes.drop(1).take(4).forEach { imagenUrl ->
                            AsyncImage(
                                model = ImageRequest.Builder(LocalContext.current)
                                    .data(imagenUrl)
                                    .crossfade(true)
                                    .build(),
                                contentDescription = null,
                                contentScale = ContentScale.Crop,
                                modifier = Modifier
                                    .size(50.dp)
                                    .clip(RoundedCornerShape(4.dp))
                            )
                        }

                        // Indicador si hay más imágenes
                        if (imagenes.size > 5) {
                            Box(
                                modifier = Modifier
                                    .size(50.dp)
                                    .clip(RoundedCornerShape(4.dp))
                                    .background(MaterialTheme.colorScheme.primary),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = "+${imagenes.size - 5}",
                                    color = MaterialTheme.colorScheme.onPrimary,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 14.sp
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))
                }

                // Detalles adicionales en fila
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    // Estado del producto
                    producto.estado?.let {
                        Text(
                            text = "Estado: ${it.name}",
                            fontSize = 12.sp,
                            modifier = Modifier
                                .background(
                                    color = MaterialTheme.colorScheme.surfaceVariant,
                                    shape = RoundedCornerShape(4.dp)
                                )
                                .padding(horizontal = 6.dp, vertical = 2.dp)
                        )
                    }

                    // Stock disponible
                    producto.stock?.let {
                        Text(
                            text = "Stock: $it unidades",
                            fontSize = 12.sp,
                            modifier = Modifier
                                .background(
                                    color = if (it > 0) MaterialTheme.colorScheme.primaryContainer
                                    else MaterialTheme.colorScheme.errorContainer,
                                    shape = RoundedCornerShape(4.dp)
                                )
                                .padding(horizontal = 6.dp, vertical = 2.dp)
                        )
                    }

                    // Destacado
                    if (producto.destacado == true) {
                        Text(
                            text = "Destacado",
                            fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.onTertiaryContainer,
                            modifier = Modifier
                                .background(
                                    color = MaterialTheme.colorScheme.tertiaryContainer,
                                    shape = RoundedCornerShape(4.dp)
                                )
                                .padding(horizontal = 6.dp, vertical = 2.dp)
                        )
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