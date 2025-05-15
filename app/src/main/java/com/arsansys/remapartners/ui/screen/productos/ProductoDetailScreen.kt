package com.arsansys.remapartners.ui.screen.productos

import android.content.Context
import android.graphics.BitmapFactory
import android.location.Geocoder
import android.util.Log
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
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.Category
import androidx.compose.material.icons.filled.Image
import androidx.compose.material.icons.filled.Inventory
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Update
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Divider
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.navigation.NavController
import com.arsansys.remapartners.data.model.entities.ProductoEntity
import com.arsansys.remapartners.data.repository.productos.ImageApiRest
import com.arsansys.remapartners.data.repository.productos.ImageRepository
import com.arsansys.remapartners.data.repository.user.UserRetrofitInstance
import com.arsansys.remapartners.data.service.ProductoServiceInstance
import com.arsansys.remapartners.data.util.ImageCache
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.MapView
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProductoDetailScreen(navController: NavController, productoId: String) {
    val context = LocalContext.current
    val productoService = ProductoServiceInstance.getInstance(context)

    var producto by remember { mutableStateOf<ProductoEntity?>(null) }
    var isLoading by remember { mutableStateOf(true) }

    LaunchedEffect(productoId) {
        try {
            producto = productoService.getProductoById(productoId)
        } catch (e: Exception) {
            Log.e("ProductoDetailScreen", "Error cargando el producto: ${e.message}")
        } finally {
            isLoading = false
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Detalles del producto") },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Volver"
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer,
                    titleContentColor = MaterialTheme.colorScheme.onPrimaryContainer
                )
            )
        }
    ) { paddingValues ->
        if (isLoading) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator()
            }
        } else if (producto == null) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues),
                contentAlignment = Alignment.Center
            ) {
                Text("No se pudo cargar el producto")
            }
        } else {
            ProductoDetailContent(producto!!, context, paddingValues)
        }
    }
}

@Composable
fun ProductoDetailContent(
    producto: ProductoEntity,
    context: Context,
    paddingValues: androidx.compose.foundation.layout.PaddingValues
) {
    val scrollState = rememberScrollState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(paddingValues)
            .verticalScroll(scrollState)
            .padding(16.dp)
    ) {
        // Galería de imágenes
        ProductImageGallery(producto, context)

        Spacer(modifier = Modifier.height(24.dp))

        // Información principal
        Text(
            text = producto.titulo ?: "Sin título",
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold
        )

        Spacer(modifier = Modifier.height(8.dp))

        // Precio y estado
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = formatearPrecio(producto.precioCentimos, producto.moneda),
                style = MaterialTheme.typography.headlineSmall,
                color = MaterialTheme.colorScheme.primary,
                fontWeight = FontWeight.Bold
            )

            producto.estado?.let {
                StatusChip(estado = it)
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Descripción
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)
            ),
            shape = RoundedCornerShape(8.dp)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    text = "Descripción",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold
                )

                Spacer(modifier = Modifier.height(8.dp))

                Text(
                    text = producto.descripcion ?: "Sin descripción disponible",
                    style = MaterialTheme.typography.bodyLarge
                )
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        // Características del producto
        Text(
            text = "Detalles del producto",
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold
        )

        Spacer(modifier = Modifier.height(8.dp))

        // Categoría
        DetailRow(
            icon = Icons.Default.Category,
            label = "Categoría",
            value = producto.idCategoria?.let { obtenerNombreCategoria(it) } ?: "Sin categoría"
        )

        // Marca
        DetailRow(
            icon = null,
            label = "Marca",
            value = producto.marca ?: "No especificada"
        )

        // Stock
        DetailRow(
            icon = Icons.Default.Inventory,
            label = "Stock",
            value = producto.stock?.toString() ?: "No disponible"
        )

        // Fecha creación
        producto.fechaCreacion?.let {
            DetailRow(
                icon = Icons.Default.CalendarMonth,
                label = "Fecha de publicación",
                value = formatearFecha(it)
            )
        }

        // Fecha actualización
        producto.fechaActualizacion?.let {
            DetailRow(
                icon = Icons.Default.Update,
                label = "Última actualización",
                value = formatearFecha(it)
            )
        }

        Spacer(modifier = Modifier.height(24.dp))

        // Ubicación
        Text(
            text = "Ubicación",
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold
        )

        Spacer(modifier = Modifier.height(8.dp))

        // Dirección
        DetailRow(
            icon = Icons.Default.LocationOn,
            label = "Dirección",
            value = producto.direccion ?: "No especificada"
        )

        // Mapa
        if (producto.direccion != null) {
            Spacer(modifier = Modifier.height(16.dp))
            LocationMap(direccion = producto.direccion, context = context)
        }
    }
}

@Composable
fun ProductImageGallery(producto: ProductoEntity, context: Context) {
    val imageCache = remember {
        ImageCache(
            ImageRepository(
                UserRetrofitInstance.getRetrofitInstance(context).create(ImageApiRest::class.java)
            )
        )
    }

    val imageUrls = remember {
        producto.imagenes?.filterNotNull() ?: emptyList()
    }

    var mainImageBytes by remember { mutableStateOf<ByteArray?>(null) }
    var isLoading by remember { mutableStateOf(true) }

    LaunchedEffect(imageUrls) {
        if (imageUrls.isNotEmpty()) {
            isLoading = true
            try {
                mainImageBytes = imageCache.getImage(imageUrls.first())
            } catch (e: Exception) {
                Log.e("ProductImageGallery", "Error: ${e.message}")
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
            .height(300.dp),
        shape = RoundedCornerShape(12.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        if (isLoading) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator()
            }
        } else if (mainImageBytes != null) {
            val bitmap = BitmapFactory.decodeByteArray(mainImageBytes, 0, mainImageBytes!!.size)
            Image(
                bitmap = bitmap.asImageBitmap(),
                contentDescription = "Imagen de ${producto.titulo}",
                contentScale = ContentScale.FillBounds,
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
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(
                        imageVector = Icons.Default.Image,
                        contentDescription = "Sin imagen",
                        modifier = Modifier.size(80.dp),
                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    Text(
                        text = "No hay imágenes disponibles",
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    }
}

@Composable
fun DetailRow(
    icon: androidx.compose.ui.graphics.vector.ImageVector?,
    label: String,
    value: String
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically
        ) {
            if (icon != null) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(24.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
            }

            Column {
                Text(
                    text = label,
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                Text(
                    text = value,
                    style = MaterialTheme.typography.bodyLarge
                )
            }
        }

        Divider(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 8.dp),
            color = MaterialTheme.colorScheme.outlineVariant
        )
    }
}

@Composable
fun LocationMap(direccion: String, context: Context) {
    var mapViewInitialized by remember { mutableStateOf(false) }

    // Para simplificar, generamos unas coordenadas aproximadas a partir del nombre de la ciudad
    val location = convertirDireccionALatLng(direccion, context)

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(200.dp)
            .background(Color.Gray.copy(alpha = 0.2f))
    ) {
        AndroidView(
            factory = { context ->
                MapView(context).apply {
                    onCreate(null)
                    onResume()
                    getMapAsync { googleMap ->
                        val position = LatLng(location.first, location.second)
                        googleMap.addMarker(
                            MarkerOptions()
                                .position(position)
                                .title(direccion)
                        )
                        googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(position, 12f))
                        mapViewInitialized = true
                    }
                }
            },
            modifier = Modifier.fillMaxSize()
        )

        if (!mapViewInitialized) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator()
            }
        }

        // Marca de ubicación
        Text(
            text = direccion,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurface,
            textAlign = TextAlign.Center,
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .fillMaxWidth()
                .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.7f))
                .padding(8.dp)
        )
    }
}

// Función para convertir una dirección (ciudad) a coordenadas
private fun convertirDireccionALatLng(direccion: String, context: Context): Pair<Double, Double> {
    // Coordenadas por defecto (Madrid) en caso de error
    var latitud = 40.416775
    var longitud = -3.703790

    try {
        val geocoder = android.location.Geocoder(context, Locale.getDefault())

        // Obtenemos resultados de geocodificación (limitados a 1)
        val resultados = geocoder.getFromLocationName(direccion, 1)

        if (!resultados.isNullOrEmpty()) {
            val primerResultado = resultados[0]
            latitud = primerResultado.latitude
            longitud = primerResultado.longitude
        } else {
            Log.d("Geocoder", "No se encontraron coordenadas para: $direccion")
        }
    } catch (e: Exception) {
        Log.e("Geocoder", "Error obteniendo coordenadas: ${e.message}")
    }

    return Pair(latitud, longitud)
}

// Función para formatear fecha
private fun formatearFecha(timestamp: String): String {
    try {
        val timestampLong = timestamp.toLong()
        val fecha = Date(timestampLong)
        val formato = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
        return formato.format(fecha)
    } catch (e: Exception) {
        return "Fecha desconocida"
    }
}