package com.arsansys.remapartners.ui.screen.productos

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.graphics.BitmapFactory
import android.util.Log
import android.widget.Toast
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Chat
import androidx.compose.material.icons.automirrored.filled.Logout
import androidx.compose.material.icons.filled.Category
import androidx.compose.material.icons.filled.Chat
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.ExpandLess
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material.icons.filled.Image
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.SearchOff
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Badge
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Divider
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
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
import com.arsansys.remapartners.data.model.entities.CategoriaEntity
import com.arsansys.remapartners.data.model.entities.ProductoEntity
import com.arsansys.remapartners.data.model.enums.EEstado
import com.arsansys.remapartners.data.model.enums.EMoneda
import com.arsansys.remapartners.data.repository.productos.ImageApiRest
import com.arsansys.remapartners.data.repository.productos.ImageRepository
import com.arsansys.remapartners.data.repository.RetrofitInstance
import com.arsansys.remapartners.data.service.ProductoServiceInstance
import com.arsansys.remapartners.data.service.categorias.CategoriaService
import com.arsansys.remapartners.data.service.categorias.CategoriaServiceImpl
import com.arsansys.remapartners.data.service.categorias.CategoriaServiceInstance
import com.arsansys.remapartners.data.service.productos.ProductoService
import com.arsansys.remapartners.data.util.ImageCache
import kotlinx.coroutines.launch
import com.arsansys.remapartners.data.util.SessionManager
import com.arsansys.remapartners.ui.navigation.Screen
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.withContext

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(navController: NavController) {
    val context = LocalContext.current
    val sessionManager = remember { SessionManager(context) }
    val coroutineScope = rememberCoroutineScope()

    val retrofit = RetrofitInstance.getRetrofitInstance(context)
    val productoService = ProductoServiceInstance.getInstance(context)
    val categoriaService = CategoriaServiceInstance.getInstance(context)

    var mostrarDialogoSeleccionCategoria by remember { mutableStateOf(false) }


    val productos = remember { mutableStateListOf<ProductoEntity>() }
    var isRefreshing by remember { mutableStateOf(false) }
    var isLoading by remember { mutableStateOf(true) }


    // Estados para filtros
    var authState by remember { mutableStateOf(sessionManager.fetchAuthToken() != null) }
    var username by remember { mutableStateOf(sessionManager.fetchUsername() ?: "") }

    // Nuevos estados para filtros
    var categoriaSeleccionada by remember { mutableStateOf<String?>(null) }
    var ubicacionSeleccionada by remember { mutableStateOf<String?>(null) }
    var precioOrden by remember { mutableStateOf<Boolean?>(null) } // null=sin orden, true=ascendente, false=descendente
    var estadoSeleccionado by remember { mutableStateOf<String?>(null) }
    var soloDestacados by remember { mutableStateOf(false) }

    var filtroExpanded by remember { mutableStateOf(false) }


    // Filtro de productos
    val productosFiltrados by remember(
        productos.size,  // Usar size para detectar cambios en la lista
        categoriaSeleccionada,
        ubicacionSeleccionada,
        precioOrden,
        estadoSeleccionado,
        soloDestacados
    ) {
        // Convertir a derivedStateOf para mejorar la reactividad
        derivedStateOf {
            val filtrados = productos.filter { producto ->
                (categoriaSeleccionada == null || producto.idCategoria == categoriaSeleccionada) &&
                        (ubicacionSeleccionada == null || producto.direccion?.contains(
                            ubicacionSeleccionada!!,
                            ignoreCase = true
                        ) == true) &&
                        (estadoSeleccionado == null || producto.estado?.name == estadoSeleccionado) &&
                        (!soloDestacados || producto.destacado == true)
            }

            // Aplicar ordenación
            when (precioOrden) {
                true -> filtrados.sortedBy { it.precioCentimos ?: Int.MAX_VALUE }
                false -> filtrados.sortedByDescending { it.precioCentimos ?: 0 }
                else -> filtrados
            }
        }
    }

    // Receptor para token expirado
    val tokenExpiredReceiver = remember {
        object : BroadcastReceiver() {
            override fun onReceive(context: Context, intent: Intent) {
                if (intent.action == "com.arsansys.remapartners.TOKEN_EXPIRED") {
                    authState = false
                    username = ""
                    navController.navigate(Screen.Login.route)
                }
            }
        }
    }

    var categorias by remember { mutableStateOf<List<CategoriaEntity>>(emptyList()) }
    var isLoadingCategorias by remember { mutableStateOf(true) }
    var nombreCategoriaSeleccionada by remember { mutableStateOf<String?>(null) }

// Cargar categorías al inicio
    LaunchedEffect(Unit) {
        isLoading = true
        try {
            withContext(Dispatchers.IO) {
                // Carga de categorías
                val categoriasResponse = categoriaService.getAllCategorias()
                withContext(Dispatchers.Main) {
                    categorias = categoriasResponse
                    Log.d("HomeScreen", "Categorías cargadas: ${categorias.size}")
                    isLoadingCategorias = false
                }
            }
            // Carga de productos
            cargarProductos(productoService, productos, context)
        } catch (e: Exception) {
            Log.e("HomeScreen", "Error cargando datos: ${e.message}", e)
        } finally {
            isLoading = false
        }
    }

    // Agregar este efecto para forzar la actualización de productosFiltrados
    LaunchedEffect(productos.size) {
        if (productos.isNotEmpty()) {
            Log.d("HomeScreen", "Forzando actualización de productos filtrados después de carga")
        }
    }

    // Registrar receptor
    DisposableEffect(Unit) {
        val filter = IntentFilter("com.arsansys.remapartners.TOKEN_EXPIRED")
        LocalBroadcastManager.getInstance(context).registerReceiver(tokenExpiredReceiver, filter)
        onDispose {
            LocalBroadcastManager.getInstance(context).unregisterReceiver(tokenExpiredReceiver)
        }
    }



    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = if (username.isNotEmpty())
                            "Hola, $username"
                        else "Productos disponibles",
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                },
                actions = {
                    if (authState) {
                        // Usuario autenticado - mostrar acciones de chats y logout
                        IconButton(onClick = {
                            Log.d("HomeScreen", "Navegando a lista de chats")
                            navController.navigate(Screen.ChatList.route)
                        }) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.Chat,
                                contentDescription = "Mis chats"
                            )
                        }
                        IconButton(onClick = {
                            // Lógica del logout modificada
                            coroutineScope.launch {
                                sessionManager.clearData()
                                authState = false  // Actualizar el estado para recomponer
                                username = ""
                            }
                        }) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.Logout,
                                contentDescription = "Cerrar sesión"
                            )
                        }
                    } else {
                        // Usuario no autenticado - mostrar botón de login
                        Button(
                            onClick = { navController.navigate(Screen.Login.route) },
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Text("Iniciar Sesión")
                        }
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer,
                    titleContentColor = MaterialTheme.colorScheme.onPrimaryContainer
                )
            )
        }
    ) { paddingValues ->
        PullToRefreshBox(
            isRefreshing = isRefreshing,
            onRefresh = {
                coroutineScope.launch {
                    isRefreshing = true
                    try {
                        cargarProductos(productoService, productos, context)
                    } finally {
                        delay(1000)
                        isRefreshing = false
                    }
                }
            },
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            Column(modifier = Modifier.fillMaxSize()) {
                // Filtros y opciones de orden (contraído por defecto)

                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 8.dp),
                    onClick = { filtroExpanded = !filtroExpanded },
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Filtros",
                            fontWeight = FontWeight.Medium
                        )
                        Icon(
                            imageVector = if (filtroExpanded) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                            contentDescription = if (filtroExpanded) "Ocultar filtros" else "Mostrar filtros"
                        )
                    }

                    AnimatedVisibility(visible = filtroExpanded) {
                        // Filtros mejorados con categoría y ubicación
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 16.dp, vertical = 8.dp),
                        ) {
                            // Fila 1: Precio, Estado, Destacados
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceEvenly
                            ) {
                                FilterChip(
                                    selected = precioOrden != null,
                                    onClick = {
                                        precioOrden = when (precioOrden) {
                                            null -> true // Ascendente
                                            true -> false // Descendente
                                            false -> null // Sin ordenar
                                        }
                                    },
                                    label = {
                                        Row(verticalAlignment = Alignment.CenterVertically) {
                                            Text(
                                                when (precioOrden) {
                                                    true -> "Precio ↑"
                                                    false -> "Precio ↓"
                                                    null -> "Precio"
                                                }
                                            )
                                        }
                                    }
                                )

                                FilterChip(
                                    selected = estadoSeleccionado != null,
                                    onClick = {
                                        // Ciclo de estados: null -> NUEVO -> COMO_NUEVO -> BUEN_ESTADO -> ACEPTABLE -> null
                                        estadoSeleccionado = when (estadoSeleccionado) {
                                            null -> "NUEVO"
                                            "NUEVO" -> "COMO_NUEVO"
                                            "COMO_NUEVO" -> "BUEN_ESTADO"
                                            "BUEN_ESTADO" -> "ACEPTABLE"
                                            else -> null
                                        }
                                    },
                                    label = {
                                        Text(
                                            when (estadoSeleccionado) {
                                                "NUEVO" -> "Nuevo"
                                                "COMO_NUEVO" -> "Como nuevo"
                                                "BUEN_ESTADO" -> "Buen estado"
                                                "ACEPTABLE" -> "Aceptable"
                                                else -> "Estado"
                                            }
                                        )
                                    }
                                )

                                FilterChip(
                                    selected = soloDestacados,
                                    onClick = { soloDestacados = !soloDestacados },
                                    label = { Text("Destacados") }
                                )
                            }

                            Spacer(modifier = Modifier.height(8.dp))

                            // Fila 2: Categoría y Ubicación
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceEvenly
                            ) {
                                // Filtro de categoría mejorado - reemplaza completamente el componente existente
                                FilterChip(
                                    selected = categoriaSeleccionada != null,
                                    onClick = {
                                        if (categoriaSeleccionada == null) {
                                            // Mostrar diálogo de selección de categoría
                                            mostrarDialogoSeleccionCategoria = true
                                        } else {
                                            // Limpiar selección actual
                                            categoriaSeleccionada = null
                                            nombreCategoriaSeleccionada = null
                                        }
                                    },
                                    label = {
                                        Row(verticalAlignment = Alignment.CenterVertically) {
                                            Icon(
                                                imageVector = Icons.Default.Category,
                                                contentDescription = null,
                                                modifier = Modifier.size(16.dp)
                                            )
                                            Spacer(modifier = Modifier.width(4.dp))
                                            Text(
                                                text = nombreCategoriaSeleccionada ?: "Categoría",
                                                maxLines = 1,
                                                overflow = TextOverflow.Ellipsis
                                            )
                                        }
                                    }
                                )

// Diálogo de selección de categoría - agregarlo al final de la Scaffold
                                if (mostrarDialogoSeleccionCategoria) {
                                    AlertDialog(
                                        onDismissRequest = {
                                            mostrarDialogoSeleccionCategoria = false
                                        },
                                        title = { Text("Seleccionar categoría") },
                                        text = {
                                            if (isLoadingCategorias) {
                                                Box(
                                                    modifier = Modifier
                                                        .fillMaxWidth()
                                                        .height(200.dp),
                                                    contentAlignment = Alignment.Center
                                                ) {
                                                    CircularProgressIndicator()
                                                }
                                            } else if (categorias.isEmpty()) {
                                                Text("No hay categorías disponibles")
                                            } else {
                                                LazyColumn(
                                                    modifier = Modifier
                                                        .fillMaxWidth()
                                                        .heightIn(max = 300.dp)
                                                ) {
                                                    items(categorias.size) { index ->
                                                        val categoria = categorias[index]
                                                        Surface(
                                                            modifier = Modifier
                                                                .fillMaxWidth()
                                                                .clickable {
                                                                    categoriaSeleccionada =
                                                                        categoria.id
                                                                    nombreCategoriaSeleccionada =
                                                                        categoria.titulo
                                                                    mostrarDialogoSeleccionCategoria =
                                                                        false
                                                                },
                                                            color = if (categoria.id == categoriaSeleccionada)
                                                                MaterialTheme.colorScheme.primaryContainer
                                                            else
                                                                MaterialTheme.colorScheme.surface
                                                        ) {
                                                            Row(
                                                                modifier = Modifier
                                                                    .fillMaxWidth()
                                                                    .padding(
                                                                        vertical = 12.dp,
                                                                        horizontal = 16.dp
                                                                    ),
                                                                verticalAlignment = Alignment.CenterVertically
                                                            ) {
                                                                Text(
                                                                    text = categoria.titulo
                                                                        ?: "Sin nombre",
                                                                    style = MaterialTheme.typography.bodyLarge,
                                                                    modifier = Modifier.weight(1f)
                                                                )

                                                                if (categoria.id == categoriaSeleccionada) {
                                                                    Icon(
                                                                        imageVector = Icons.Default.Check,
                                                                        contentDescription = "Seleccionada",
                                                                        tint = MaterialTheme.colorScheme.primary
                                                                    )
                                                                }
                                                            }
                                                        }
                                                        Divider()
                                                    }
                                                }
                                            }
                                        },
                                        confirmButton = {
                                            TextButton(onClick = {
                                                mostrarDialogoSeleccionCategoria = false
                                            }) {
                                                Text("Cerrar")
                                            }
                                        }
                                    )
                                }
                                // Filtro de ubicación
                                FilterChip(
                                    selected = ubicacionSeleccionada != null,
                                    onClick = {
                                        ubicacionSeleccionada = when (ubicacionSeleccionada) {
                                            null -> "Madrid"
                                            "Madrid" -> "Barcelona"
                                            "Barcelona" -> "Valencia"
                                            else -> null
                                        }
                                    },
                                    label = {
                                        Text(ubicacionSeleccionada ?: "Ubicación")
                                    }
                                )
                            }

                            // Botón para limpiar todos los filtros
                            if (categoriaSeleccionada != null || ubicacionSeleccionada != null ||
                                precioOrden != null || estadoSeleccionado != null || soloDestacados
                            ) {

                                Button(
                                    onClick = {
                                        categoriaSeleccionada = null
                                        ubicacionSeleccionada = null
                                        precioOrden = null
                                        estadoSeleccionado = null
                                        soloDestacados = false
                                    },
                                    modifier = Modifier
                                        .align(Alignment.End)
                                        .padding(top = 8.dp)
                                ) {
                                    Text("Limpiar filtros")
                                }
                            }
                        }
                    }
                }

                // Contenido principal
                if (isLoading) {
                    // Skeleton loader
                    GridSkeletonLoader()
                } else if (productosFiltrados.isEmpty()) {
                    // Mensaje de no hay productos
// Calculamos correctamente si hay filtros activos
                    val hayFiltroActivo = categoriaSeleccionada != null ||
                            ubicacionSeleccionada != null ||
                            precioOrden != null ||
                            estadoSeleccionado != null ||
                            soloDestacados
                    EmptyProductsMessage(filtroActivo = hayFiltroActivo)
                } else {
                    // Grid de productos
                    LazyVerticalGrid(
                        columns = GridCells.Adaptive(minSize = 160.dp),
                        contentPadding = PaddingValues(8.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        modifier = Modifier.fillMaxSize()
                    ) {
                        items(productosFiltrados.size) { index ->
                            ProductoCardCompact(
                                producto = productosFiltrados[index],
                                navController,
                                context
                            )
                        }
                    }
                }
            }
        }
    }
}


@Composable
fun ProductoCardCompact(producto: ProductoEntity, navController: NavController, context: Context) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .height(320.dp),
        shape = RoundedCornerShape(12.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        onClick = {
            navController.navigate(
                Screen.ProductDetail.createRoute(producto.id.toString())
            )
        }
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
            // Imagen con proporción 1:1
            ProductImageWithStatus(
                producto = producto,
                modifier = Modifier
                    .fillMaxWidth()
                    .aspectRatio(1f)
            )

            // Información resumida
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(8.dp)
                    .weight(1f)
            ) {
                // Título
                Text(
                    text = producto.titulo ?: "Sin título",
                    fontWeight = FontWeight.Medium,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )

                // Precio destacado
                Text(
                    text = formatearPrecio(producto.precioCentimos, producto.moneda),
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary,
                    fontSize = 16.sp
                )

// Categoría - mostramos siempre un espacio para ella
                var nombreCategoria by remember { mutableStateOf("Cargando...") }

// Usamos remember para mantener la referencia al categoriaId
                val categoriaIdRemembered = remember(producto.idCategoria) { producto.idCategoria }

// LaunchedEffect con clave más estable para evitar reinicios innecesarios
                LaunchedEffect(categoriaIdRemembered) {
                    categoriaIdRemembered?.let { categoriaId ->
                        try {
                            // Mostramos explícitamente estado de carga
                            nombreCategoria = "Cargando..."

                            // Uso de withContext para asegurar que la operación se complete
                            withContext(Dispatchers.IO) {
                                val nombre = obtenerNombreCategoria(
                                    categoriaId,
                                    CategoriaServiceInstance.getInstance(context),
                                    context
                                )
                                // Actualizamos el estado en el hilo principal
                                withContext(Dispatchers.Main) {
                                    nombreCategoria = nombre
                                }
                            }
                        } catch (e: Exception) {
                            Log.e("ProductoCard", "Error cargando categoría: ${e.message}", e)
                            nombreCategoria = "Sin categoría"
                        }
                    } ?: run {
                        nombreCategoria = "Sin categoría"
                    }
                }

                // Mostramos siempre la fila de categoría para mantener el layout consistente
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.padding(vertical = 4.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Category,
                        contentDescription = null,
                        modifier = Modifier.size(14.dp),
                        tint = MaterialTheme.colorScheme.secondary
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = nombreCategoria,
                        fontSize = 12.sp,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        color = MaterialTheme.colorScheme.secondary
                    )
                }

                Spacer(modifier = Modifier.weight(1f))

                // Estado y ubicación
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Estado
                    producto.estado?.let {
                        StatusChip(estado = it)
                    }

                    // Ubicación con icono
                    producto.direccion?.let {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.End,
                            modifier = Modifier.weight(1f)
                        ) {
                            Icon(
                                imageVector = Icons.Default.LocationOn,
                                contentDescription = "Ubicación",
                                modifier = Modifier.size(12.dp),
                                tint = MaterialTheme.colorScheme.outline
                            )
                            Spacer(modifier = Modifier.width(2.dp))
                            Text(
                                text = it.split(",").firstOrNull() ?: "",
                                fontSize = 11.sp,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis,
                                textAlign = TextAlign.End,
                                modifier = Modifier.weight(1f, fill = false)
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun ProductImageWithStatus(producto: ProductoEntity, modifier: Modifier = Modifier) {
    val context = LocalContext.current
    val imageCache = remember {
        ImageCache(
            ImageRepository(
                RetrofitInstance.getRetrofitInstance(context).create(ImageApiRest::class.java)
            )
        )
    }

    var mainImageBytes by remember { mutableStateOf<ByteArray?>(null) }
    var isLoading by remember { mutableStateOf(true) }

    val imageUrls = remember {
        producto.imagenes?.filterNotNull() ?: emptyList()
    }

    LaunchedEffect(imageUrls) {
        if (imageUrls.isNotEmpty()) {
            isLoading = true
            try {
                mainImageBytes = imageCache.getImage(imageUrls.first())
            } catch (e: Exception) {
                Log.e("ProductImage", "Error: ${e.message}")
            } finally {
                isLoading = false
            }
        } else {
            isLoading = false
        }
    }

    Box(modifier = modifier) {
        if (isLoading) {
            // Skeleton loader para la imagen
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(
                        shimmerBrush(
                            targetValue = 1300f,
                            showShimmer = true
                        )
                    )
            )
        } else if (mainImageBytes != null) {
            val bitmap = BitmapFactory.decodeByteArray(mainImageBytes, 0, mainImageBytes!!.size)
            Image(
                bitmap = bitmap.asImageBitmap(),
                contentDescription = "Imagen de ${producto.titulo}",
                contentScale = ContentScale.Crop,
                modifier = Modifier.fillMaxSize()
            )
        } else {
            // Placeholder
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

        // Badges para destacados
        if (producto.destacado == true) {
            Badge(
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .padding(8.dp),
                containerColor = MaterialTheme.colorScheme.tertiary
            ) {
                Icon(
                    imageVector = Icons.Default.Star,
                    contentDescription = "Destacado",
                    modifier = Modifier.size(12.dp),
                    tint = MaterialTheme.colorScheme.onTertiary
                )
            }
        }
    }
}

@Composable
fun StatusChip(estado: EEstado) {
    val (color, text) = when (estado.name) {
        "NUEVO" -> Pair(MaterialTheme.colorScheme.primary, "Nuevo")
        "COMO_NUEVO" -> Pair(MaterialTheme.colorScheme.secondary, "Como nuevo")
        "BUEN_ESTADO" -> Pair(MaterialTheme.colorScheme.tertiary, "Buen estado")
        "ACEPTABLE" -> Pair(MaterialTheme.colorScheme.error, "Aceptable")
        else -> Pair(MaterialTheme.colorScheme.outline, estado.name)
    }

    Surface(
        shape = RoundedCornerShape(4.dp),
        color = color.copy(alpha = 0.2f),
        modifier = Modifier.height(20.dp)
    ) {
        Text(
            text = text,
            fontSize = 10.sp,
            color = color,
            modifier = Modifier.padding(horizontal = 4.dp, vertical = 2.dp)
        )
    }
}

@Composable
fun GridSkeletonLoader() {
    LazyVerticalGrid(
        columns = GridCells.Adaptive(minSize = 160.dp),
        contentPadding = PaddingValues(8.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        modifier = Modifier.fillMaxSize()
    ) {
        items(10) {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(280.dp),
                shape = RoundedCornerShape(12.dp)
            ) {
                Column(modifier = Modifier.fillMaxSize()) {
                    // Imagen
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .aspectRatio(1f)
                            .background(
                                shimmerBrush(
                                    targetValue = 1300f,
                                    showShimmer = true
                                )
                            )
                    )

                    // Contenido
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(8.dp)
                    ) {
                        Spacer(
                            modifier = Modifier
                                .fillMaxWidth(0.8f)
                                .height(16.dp)
                                .background(
                                    shimmerBrush(
                                        targetValue = 1300f,
                                        showShimmer = true
                                    )
                                )
                        )

                        Spacer(modifier = Modifier.height(8.dp))

                        Spacer(
                            modifier = Modifier
                                .fillMaxWidth(0.4f)
                                .height(16.dp)
                                .background(
                                    shimmerBrush(
                                        targetValue = 1300f,
                                        showShimmer = true
                                    )
                                )
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun EmptyProductsMessage(filtroActivo: Boolean = false) {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.padding(32.dp)
        ) {
            Icon(
                imageVector = Icons.Default.SearchOff,
                contentDescription = null,
                modifier = Modifier.size(80.dp),
                tint = MaterialTheme.colorScheme.outline
            )

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = if (filtroActivo)
                    "No hay productos que coincidan con tu búsqueda"
                else "No hay productos disponibles",
                style = MaterialTheme.typography.titleLarge,
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = if (filtroActivo)
                    "Prueba a cambiar los filtros para ver más resultados"
                else "Los productos aparecerán aquí cuando estén disponibles",
                style = MaterialTheme.typography.bodyMedium,
                textAlign = TextAlign.Center,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

// Implementación del efecto shimmer para skeleton loaders
@Composable
fun shimmerBrush(showShimmer: Boolean = true, targetValue: Float = 1000f): Brush {
    return if (showShimmer) {
        val shimmerColors = listOf(
            MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f),
            MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.2f),
            MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f),
        )

        val transition = rememberInfiniteTransition(label = "shimmer")
        val translateAnimation = transition.animateFloat(
            initialValue = 0f,
            targetValue = targetValue,
            animationSpec = infiniteRepeatable(
                animation = tween(800),
                repeatMode = RepeatMode.Reverse
            ),
            label = "shimmer animation"
        )

        Brush.linearGradient(
            colors = shimmerColors,
            start = Offset.Zero,
            end = Offset(x = translateAnimation.value, y = translateAnimation.value)
        )
    } else {
        Brush.linearGradient(
            colors = listOf(Color.Transparent, Color.Transparent),
            start = Offset.Zero,
            end = Offset.Zero
        )
    }
}

// Función para cargar productos
private suspend fun cargarProductos(
    productoService: ProductoService,
    productos: MutableList<ProductoEntity>,
    context: Context
) {
    try {
        val productosResponse = productoService.getProductos()
        productos.clear()
        productos.addAll(productosResponse)
    } catch (e: Exception) {
        Toast.makeText(
            context,
            "Error: ${e.message}",
            Toast.LENGTH_SHORT
        ).show()
    }
}

// Función para obtener nombre de categoría a partir de ID
internal suspend fun obtenerNombreCategoria(
    categoriaId: String,
    categoriaService: CategoriaService,
    context: Context
): String {
    return coroutineScope {
        try {
            val categoria: CategoriaEntity = categoriaService.getCategoriaById(categoriaId)
            categoria.titulo ?: "Sin categoría"
        } catch (e: Exception) {
            Log.e("HomeScreen", "Error obteniendo categoría: ${e.message}")
            "Sin categoría"
        }
    }
}


// Función para formatear precio
internal fun formatearPrecio(precioCentimos: Int?, moneda: EMoneda?): String {
    if (precioCentimos == null) return "Precio no disponible"

    val precio = precioCentimos / 100.0
    val simbolo = when (moneda) {
        EMoneda.EUR -> "€"
        EMoneda.USD -> "$"
        EMoneda.GBP -> "£"
        EMoneda.JPY -> "¥"
        EMoneda.CNY -> "¥"
        EMoneda.INR -> "₹"
        EMoneda.RUB -> "₽"
        EMoneda.BRL -> "R$"
        EMoneda.ARS -> "$"
        EMoneda.CLP -> "$"
        else -> "€"
    }

    return "$precio $simbolo"
}
