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
import androidx.compose.foundation.shape.RoundedCornerShape
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
    val scope = rememberCoroutineScope()

    Box(modifier = Modifier.fillMaxSize()) {
        if (hasLocationPermission && userLocation != null) {
            val cameraPositionState = rememberCameraPositionState { position = CameraPosition.fromLatLngZoom(userLocation!!, 14f) }
            LaunchedEffect(userLocation) { cameraPositionState.position = CameraPosition.fromLatLngZoom(userLocation!!, 14f) }
            val markers = remember(userLocation) {
                listOf(
                    MapMarkerInfo(
                        title = "Evento no Botequim",
                        description = "Happy hour da firma!",
                        local = "Amazon Botequim • Staff",
                        horario = "19:00",
                        valor = "R$ 25,00",
                        position = LatLng(userLocation!!.latitude + 0.005, userLocation!!.longitude + 0.005),
                    ),
                    MapMarkerInfo(
                        title = "Futebol de Quinta",
                        description = "Jogo semanal no campinho.",
                        local = "Arena da Amazônia",
                        horario = "21:00",
                        valor = "R$ 15,00",
                        position = LatLng(userLocation!!.latitude - 0.005, userLocation!!.longitude - 0.005),
                    )
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
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(selectedMarker!!.title, style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
                        Spacer(modifier = Modifier.height(16.dp))
                        Spacer(modifier = Modifier.height(8.dp))
                        Spacer(modifier = Modifier.height(8.dp))
                        Spacer(modifier = Modifier.height(24.dp))
                            }
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

@Preview(showBackground = true)
@Composable
fun MainScreenPreview() {
    ProjetomapaTheme {
        MainScreen()
    }
}