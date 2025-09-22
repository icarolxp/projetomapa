// ARQUIVO: MainActivity.kt (Versão final com todas as importações)

package com.example.projetomapa.activites.dashboard

// A LISTA COMPLETA DE IMPORTAÇÕES. O ERRO É RESOLVIDO AQUI.
import android.Manifest
import android.annotation.SuppressLint
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Money
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.example.projetomapa.R
import com.example.projetomapa.ui.theme.ProjetomapaTheme
import com.google.android.gms.location.LocationServices
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.LatLng
import com.google.maps.android.compose.*
import kotlinx.coroutines.launch
import java.util.*

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            ProjetomapaTheme {
                MainScreen()
            }
        }
    }
}

// --- DATA CLASSES ---
data class BottomNavItem(val label: String, val icon: Int, val route: String)
data class MapMarkerInfo(
    val id: String = UUID.randomUUID().toString(),
    val title: String,
    val description: String,
    val local: String,
    val horario: String,
    val valor: String,
    val position: LatLng,
    val profilePicRes: Int,
    val eventImageRes: Int
)

// --- ESTRUTURA PRINCIPAL E NAVEGAÇÃO ---
@Composable
fun MainScreen() {
    val navController = rememberNavController()
    Scaffold(bottomBar = { MyBottomBar(navController = navController) }) { innerPadding ->
        NavHost(
            navController = navController,
            startDestination = "home",
            modifier = Modifier.padding(innerPadding)
        ) {
            composable("home") { HomeScreen() }
            composable("location") { LocationScreen() }
            composable("profile") { ProfileScreen() }
        }
    }
}

// --- TELAS ---
@OptIn(ExperimentalMaterial3Api::class, ExperimentalMapsComposeApi::class)
@SuppressLint("MissingPermission")
@Composable
fun LocationScreen() {
    val context = LocalContext.current
    val fusedLocationClient = remember { LocationServices.getFusedLocationProviderClient(context) }
    var hasLocationPermission by remember { mutableStateOf(false) }
    var userLocation by remember { mutableStateOf<LatLng?>(null) }
    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission(),
        onResult = { isGranted -> if (isGranted) { hasLocationPermission = true } }
    )
    LaunchedEffect(key1 = true) { permissionLauncher.launch(Manifest.permission.ACCESS_FINE_LOCATION) }
    LaunchedEffect(hasLocationPermission) {
        if (hasLocationPermission) {
            fusedLocationClient.lastLocation.addOnSuccessListener { location ->
                location?.let { userLocation = LatLng(it.latitude, it.longitude) }
            }
        }
    }
    var selectedMarker by remember { mutableStateOf<MapMarkerInfo?>(null) }
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val scope = rememberCoroutineScope()

    Box(modifier = Modifier.fillMaxSize()) {
        if (hasLocationPermission && userLocation != null) {
            val cameraPositionState = rememberCameraPositionState { position = CameraPosition.fromLatLngZoom(userLocation!!, 14f) }
            LaunchedEffect(userLocation) { cameraPositionState.position = CameraPosition.fromLatLngZoom(userLocation!!, 14f) }
            val markers = remember(userLocation) {
                listOf(
                    MapMarkerInfo("Evento no Botequim", "Happy hour!", "Amazon Botequim", "19:00", "R$ 25,00", LatLng(userLocation!!.latitude + 0.005, userLocation!!.longitude + 0.005), R.drawable.profile, R.drawable.sample),
                    MapMarkerInfo("Futebol de Quinta", "Jogo semanal.", "Arena da Amazônia", "21:00", "R$ 15,00", LatLng(userLocation!!.latitude - 0.005, userLocation!!.longitude - 0.005), R.drawable.location, R.drawable.btn_1)
                )
            }
            GoogleMap(modifier = Modifier.fillMaxSize(), cameraPositionState = cameraPositionState, properties = MapProperties(isMyLocationEnabled = true)) {
                markers.forEach { markerInfo ->
                    AdvancedMarker(state = rememberMarkerState(position = markerInfo.position), title = markerInfo.title, onClick = { selectedMarker = markerInfo; scope.launch { sheetState.show() }; true }) {
                        Image(painter = painterResource(id = markerInfo.profilePicRes), contentDescription = markerInfo.title, modifier = Modifier.size(48.dp).clip(CircleShape).border(2.dp, Color.White, CircleShape))
                    }
                }
            }
        } else {
            Text(text = "Por favor, conceda a permissão de localização.", modifier = Modifier.align(Alignment.Center))
        }

        if (selectedMarker != null) {
            ModalBottomSheet(onDismissRequest = { scope.launch { sheetState.hide() }.invokeOnCompletion { if (!sheetState.isVisible) selectedMarker = null } }, sheetState = sheetState) {
                Column(modifier = Modifier.fillMaxWidth(), horizontalAlignment = Alignment.CenterHorizontally) {
                    Image(painter = painterResource(id = selectedMarker!!.eventImageRes), contentDescription = selectedMarker!!.title, contentScale = ContentScale.Crop, modifier = Modifier.fillMaxWidth().height(180.dp))
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(selectedMarker!!.title, style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
                        Spacer(modifier = Modifier.height(16.dp))
                        DetailRow(icon = Icons.Default.LocationOn, text = selectedMarker!!.local)
                        Spacer(modifier = Modifier.height(8.dp))
                        DetailRow(icon = Icons.Default.Schedule, text = selectedMarker!!.horario)
                        Spacer(modifier = Modifier.height(8.dp))
                        DetailRow(icon = Icons.Default.Money, text = selectedMarker!!.valor, isBold = true)
                        Spacer(modifier = Modifier.height(24.dp))
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceEvenly, verticalAlignment = Alignment.CenterVertically) {
                            ActionButton(icon = Icons.Default.Close, color = Color(0xFFE53935)) { scope.launch { sheetState.hide() }.invokeOnCompletion { selectedMarker = null } }
                            ActionButton(icon = Icons.Default.Check, color = Color(0xFF43A047)) { scope.launch { sheetState.hide() }.invokeOnCompletion { selectedMarker = null } }
                        }
                    }
                    Spacer(modifier = Modifier.height(16.dp))
                }
            }
        }
    }
}

@Composable
fun ProfileScreen() {
    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Text(text = "Tela de Perfil")
    }
}

// --- COMPONENTES REUTILIZÁVEIS ---
@Composable
fun DetailRow(icon: ImageVector, text: String, isBold: Boolean = false) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Icon(imageVector = icon, contentDescription = null, tint = Color.Gray)
        Spacer(modifier = Modifier.width(8.dp))
        Text(text = text, style = MaterialTheme.typography.bodyLarge, fontWeight = if (isBold) FontWeight.Bold else FontWeight.Normal)
    }
}

@Composable
fun ActionButton(icon: ImageVector, color: Color, onClick: () -> Unit) {
    Button(onClick = onClick, shape = CircleShape, modifier = Modifier.size(64.dp), colors = ButtonDefaults.buttonColors(containerColor = color), contentPadding = PaddingValues(0.dp)) {
        Icon(imageVector = icon, contentDescription = null, tint = Color.White)
    }
}

@Composable
fun MyBottomBar(navController: NavController) {
    val navItems = listOf(
        BottomNavItem("Home", R.drawable.btn_3, "home"),
        BottomNavItem("Local", R.drawable.location, "location"),
        BottomNavItem("Perfil", R.drawable.profile, "profile")
    )
    NavigationBar(containerColor = Color.White) {
        val navBackStackEntry by navController.currentBackStackEntryAsState()
        val currentRoute = navBackStackEntry?.destination?.route
        navItems.forEach { item ->
            NavigationBarItem(
                selected = currentRoute == item.route,
                onClick = {
                    navController.navigate(item.route) {
                        popUpTo(navController.graph.startDestinationId)
                        launchSingleTop = true
                    }
                },
                icon = { Icon(painterResource(id = item.icon), item.label) },
                label = { Text(text = item.label) },
                colors = NavigationBarItemDefaults.colors(selectedIconColor = MaterialTheme.colorScheme.primary, unselectedIconColor = Color.Gray)
            )
        }
    }
}

// --- PREVIEW ---
@Preview(showBackground = true)
@Composable
fun MainScreenPreview() {
    ProjetomapaTheme {
        MainScreen()
    }
}