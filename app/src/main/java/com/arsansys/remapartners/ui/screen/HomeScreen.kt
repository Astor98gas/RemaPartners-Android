package com.arsansys.remapartners.ui.screen

import android.content.BroadcastReceiver
import android.content.ContentValues.TAG
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.util.Log
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.systemBarsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import androidx.navigation.NavController
import com.arsansys.remapartners.data.model.entities.UserEntity
import com.arsansys.remapartners.data.model.firebase.Note
import com.arsansys.remapartners.data.repository.UserApiRest
import com.arsansys.remapartners.data.repository.UserRetrofitInstance
import com.google.android.gms.tasks.OnCompleteListener
import com.google.firebase.messaging.FirebaseMessaging
import kotlinx.coroutines.launch
import com.arsansys.remapartners.data.util.SessionManager
import com.arsansys.remapartners.ui.navigation.Screen

@Composable
fun HomeScreen(
    navController: NavController
) {
    val context = LocalContext.current
    val sessionManager = remember { SessionManager(context) }

    val retrofit = UserRetrofitInstance.getRetrofitInstance(context)
    val userApi = retrofit.create(UserApiRest::class.java)

    val users = remember { mutableStateListOf<UserEntity>() }

    val coroutineScope = rememberCoroutineScope()
    var firebaseToken by remember { mutableStateOf("") }

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

    // Registrar el receptor cuando el composable se active
    DisposableEffect(Unit) {
        val filter = IntentFilter("com.arsansys.remapartners.TOKEN_EXPIRED")
        LocalBroadcastManager.getInstance(context).registerReceiver(tokenExpiredReceiver, filter)

        // Limpiar cuando el composable se destruya
        onDispose {
            LocalBroadcastManager.getInstance(context).unregisterReceiver(tokenExpiredReceiver)
        }
    }

    LaunchedEffect(Unit) {
        // Obtener el token de Firebase
        FirebaseMessaging.getInstance().token.addOnCompleteListener(OnCompleteListener { task ->
            if (!task.isSuccessful) {
                Log.w(TAG, "Fetching FCM registration token failed", task.exception)
                return@OnCompleteListener
            }

            // Obtener el nuevo token
            firebaseToken = task.result ?: ""
            Log.d(TAG, "Token: $firebaseToken")
        })
        coroutineScope.launch {
            try {
                val response = userApi.getUsers()
                if (response.isSuccessful) {
                    val userList = response.body() ?: emptyList()

                    users.clear()
                    users.addAll(userList)
                } else {
                    // Manejar el error
                }
            } catch (e: Exception) {
                // Manejar la excepción
            }
        }
    }

    Surface(
        modifier = Modifier
            .fillMaxSize()
            .systemBarsPadding()
            .imePadding()
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
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

                Button(
                    onClick = {
                        sessionManager.clearData()
                        navController.navigate(Screen.Login.route) {
                            popUpTo(Screen.Home.route) { inclusive = true }
                        }
                    }
                ) {
                    Text("Cerrar Sesión")
                }
            } else {
                // Si no hay token, mostrar botón para ir a login
                Text("No has iniciado sesión")

                Spacer(modifier = Modifier.height(16.dp))

                Button(
                    onClick = {
                        navController.navigate(Screen.Login.route)
                    }
                ) {
                    Text("Ir a Login")
                }
            }
            Button(
                onClick = {
                    coroutineScope.launch {
                        try {
                            val response = userApi.getUsers()
                            if (response.isSuccessful) {
                                val userList = response.body() ?: emptyList()

                                users.clear()
                                users.addAll(userList)
                            } else {
                                // Manejar el error
                            }
                        } catch (e: Exception) {
                            // Manejar la excepción
                        }
                    }
                }
            ) {
                Text(
                    text = "Listar Usuarios"
                )
            }
            LazyColumn(
                modifier = Modifier
                    .fillMaxWidth()
            ) {
                items(users) { user ->
                    UserCard(user = user)
                }
            }
            var title by remember { mutableStateOf("") }
            TextField(
                onValueChange = {
                    title = it
                },
                value = title,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(8.dp),
            )
            Button(
                onClick = {
                    var note: Note? = Note()
                    note?.subject = title
                    note?.content = "Prueba"
                    note?.imageUrl = ""
                    note?.token = firebaseToken
                    note?.data = mutableMapOf("key" to "value")

                    coroutineScope.launch {
                        try {
                            userApi.sendNotification(note)
                        } catch (e: Exception) {
                            Log.d("TAG", "Exception: ${e.message}")
                        }
                    }
                }
            ) {
                Text(
                    text = "Notificacion"
                )
            }
        }
    }
}

@Composable
fun UserCard(user: UserEntity) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(8.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(
            modifier = Modifier
                .padding(16.dp)
        ) {
            Text(text = "Username: ${user.username}")
            Text(text = "Email: ${user.email}")
        }
    }
}