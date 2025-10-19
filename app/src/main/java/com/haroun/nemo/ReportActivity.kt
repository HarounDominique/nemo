package com.haroun.nemo

import android.net.Uri
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.Alignment
import androidx.compose.ui.platform.LocalContext
import androidx.core.net.toUri
import coil.compose.rememberAsyncImagePainter
import coil.request.ImageRequest
import java.text.SimpleDateFormat
import java.util.*

class ReportActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {
            ReportForm()
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ReportForm() {
    var selectedAnimal by remember { mutableStateOf("") }
    var name by remember { mutableStateOf("") }
    var age by remember { mutableStateOf("") }
    var color by remember { mutableStateOf("") }
    var attitude by remember { mutableStateOf("") }
    var imageUri by remember { mutableStateOf<Uri?>(null) }
    var dateTime by remember { mutableStateOf("") }

    val context = LocalContext.current

    // Selector de imagen
    val launcher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        imageUri = uri
    }

    val animalOptions = listOf("Perro", "Gato", "Ave", "Roedor", "Reptil", "Otro")
    val attitudeOptions = listOf("Amigable", "Asustadizo", "Desconfiado", "Agresivo")

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Text("Reporte de mascota perdida", style = MaterialTheme.typography.titleLarge)

        // 1️⃣ Tipo de animal
        ExposedDropdownMenuBox(
            expanded = false,
            onExpandedChange = {}
        ) {
            TextField(
                value = selectedAnimal,
                onValueChange = { selectedAnimal = it },
                label = { Text("Tipo de animal") },
                readOnly = true,
                modifier = Modifier.fillMaxWidth()
            )
            DropdownMenu(
                expanded = false,
                onDismissRequest = {}
            ) {
                animalOptions.forEach { animal ->
                    DropdownMenuItem(
                        text = { Text(animal) },
                        onClick = { selectedAnimal = animal }
                    )
                }
            }
        }

        // 2️⃣ Nombre de la mascota
        OutlinedTextField(
            value = name,
            onValueChange = { name = it },
            label = { Text("Nombre de la mascota") },
            modifier = Modifier.fillMaxWidth()
        )

        // 3️⃣ Edad
        OutlinedTextField(
            value = age,
            onValueChange = { age = it },
            label = { Text("Edad") },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
            modifier = Modifier.fillMaxWidth()
        )

        // 4️⃣ Color predominante
        OutlinedTextField(
            value = color,
            onValueChange = { color = it },
            label = { Text("Color predominante") },
            modifier = Modifier.fillMaxWidth()
        )

        // 5️⃣ Actitud
        ExposedDropdownMenuBox(
            expanded = false,
            onExpandedChange = {}
        ) {
            TextField(
                value = attitude,
                onValueChange = { attitude = it },
                label = { Text("Actitud") },
                readOnly = true,
                modifier = Modifier.fillMaxWidth()
            )
            DropdownMenu(
                expanded = false,
                onDismissRequest = {}
            ) {
                attitudeOptions.forEach { a ->
                    DropdownMenuItem(
                        text = { Text(a) },
                        onClick = { attitude = a }
                    )
                }
            }
        }

        // 6️⃣ Imagen
        Button(onClick = { launcher.launch("image/*") }) {
            Text("Subir imagen (recomendado)")
        }
        imageUri?.let {
            Image(
                painter = rememberAsyncImagePainter(
                    ImageRequest.Builder(context).data(it).build()
                ),
                contentDescription = null,
                modifier = Modifier
                    .size(150.dp)
                    .padding(4.dp)
            )
        }

        // 7️⃣ Fecha y hora
        val calendar = Calendar.getInstance()
        val sdf = SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault())
        val dateStr = sdf.format(calendar.time)
        OutlinedTextField(
            value = if (dateTime.isEmpty()) dateStr else dateTime,
            onValueChange = { dateTime = it },
            label = { Text("Fecha y hora de extravío") },
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(16.dp))

        // 8️⃣ Botón de envío
        Button(
            onClick = {
                // Aquí enviar datos a backend o pasar a la pantalla del mapa
            },
            modifier = Modifier.align(Alignment.End)
        ) {
            Text("Siguiente")
        }
    }
}
