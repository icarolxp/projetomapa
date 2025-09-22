package com.example.projetomapa.activites.dashboard // <-- AQUI ESTÁ A CORREÇÃO

import android.Manifest
import android.annotation.SuppressLint
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.Image
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Money
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Star
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
import androidx.compose.ui.text.style.TextAlign
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
    val profilePicRes: Int
)

data class CategoryItem(val iconRes: Int, val label: String)

data class StoreInfo(
    val imageRes: Int,
    val name: String,
    val category: String,
    val rating: Double
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
    val sheetState = rememberModalBottomSheetState()
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
                        profilePicRes = R.drawable.profile
                    ),
                    MapMarkerInfo(
                        title = "Futebol de Quinta",
                        description = "Jogo semanal no campinho.",
                        local = "Arena da Amazônia",
                        horario = "21:00",
                        valor = "R$ 15,00",
                        position = LatLng(userLocation!!.latitude - 0.005, userLocation!!.longitude - 0.005),
                        profilePicRes = R.drawable.location
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
            ModalBottomSheet(onDismissRequest = { selectedMarker = null }, sheetState = sheetState) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(selectedMarker!!.title, style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
                    Spacer(modifier = Modifier.height(16.dp))
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(imageVector = Icons.Default.LocationOn, contentDescription = "Local", tint = Color.Gray)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(selectedMarker!!.local, style = MaterialTheme.typography.bodyLarge)
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(imageVector = Icons.Default.Schedule, contentDescription = "Horário", tint = Color.Gray)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(selectedMarker!!.horario, style = MaterialTheme.typography.bodyLarge)
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(imageVector = Icons.Default.Money, contentDescription = "Valor", tint = Color.Gray)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(selectedMarker!!.valor, style = MaterialTheme.typography.bodyLarge, fontWeight = FontWeight.Bold)
                    }
                    Spacer(modifier = Modifier.height(24.dp))
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceEvenly) {
                        Button(onClick = { selectedMarker = null }, shape = CircleShape, modifier = Modifier.size(64.dp), colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFE53935))) {
                            Icon(imageVector = Icons.Default.Close, contentDescription = "Recusar", tint = Color.White)
                        }
                        Button(onClick = { selectedMarker = null }, shape = CircleShape, modifier = Modifier.size(64.dp), colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF43A047))) {
                            Icon(imageVector = Icons.Default.Check, contentDescription = "Aceitar", tint = Color.White)
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

// --- COMPONENTES ---
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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeTopAppBar() {
    TopAppBar(
        title = { Text("Encontre Lojas", fontWeight = FontWeight.Bold) },
        actions = { IconButton(onClick = { /* TODO */ }) { Icon(Icons.Default.Search, "Buscar") } },
        colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.primaryContainer, titleContentColor = MaterialTheme.colorScheme.onPrimaryContainer)
    )
}

@Composable
fun CategoryList() {
    val categories = listOf(
        CategoryItem(R.drawable.building, "Lojas"),
        CategoryItem(R.drawable.location, "Perto"),
        CategoryItem(R.drawable.sample, "Promoções"),
        CategoryItem(R.drawable.profile, "Sua Conta"),
        CategoryItem(R.drawable.wallet, "Carteira"),
        CategoryItem(R.drawable.search_icon, "Buscar")
    )
    LazyRow(contentPadding = PaddingValues(vertical = 8.dp)) {
        items(categories) { category ->
            CategoryItemView(item = category) { /* TODO */ }
        }
    }
}

@Composable
fun CategoryItemView(item: CategoryItem, onClick: () -> Unit) {
    Column(modifier = Modifier.padding(end = 16.dp).clickable(onClick = onClick), horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.Center) {
        Icon(painter = painterResource(id = item.iconRes), contentDescription = item.label, modifier = Modifier.size(40.dp))
        Spacer(modifier = Modifier.height(4.dp))
        Text(text = item.label, fontSize = 12.sp)
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun PromoBanners() {
    val bannerImages = listOf(R.drawable.sample, R.drawable.btn_1, R.drawable.btn_2)
    val pagerState = rememberPagerState(pageCount = { bannerImages.size })
    HorizontalPager(
        state = pagerState,
        contentPadding = PaddingValues(horizontal = 16.dp),
        pageSpacing = 16.dp,
        modifier = Modifier.fillMaxWidth()
    ) { page ->
        Image(painter = painterResource(id = bannerImages[page]), contentDescription = "Banner ${page + 1}", contentScale = ContentScale.Crop, modifier = Modifier.fillMaxWidth().height(180.dp).clip(RoundedCornerShape(16.dp)))
    }
}

@Composable
fun PopularStoresList() {
    val stores = listOf(
        StoreInfo(R.drawable.sample, "Café do Ponto", "Cafeteria", 4.8),
        StoreInfo(R.drawable.btn_1, "Livraria Cultura", "Livraria", 4.9),
        StoreInfo(R.drawable.btn_2, "Supermercado Dia", "Mercado", 4.5),
        StoreInfo(R.drawable.btn_4, "Posto Shell", "Conveniência", 4.6)
    )
    LazyRow(
        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp)
    ) {
        items(stores) { store ->
            StoreCard(store = store, modifier = Modifier.padding(end = 16.dp))
        }
    }
}

@Composable
fun StoreCard(store: StoreInfo, modifier: Modifier = Modifier) {
    Card(
        modifier = modifier.width(220.dp),
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column {
            Image(
                painter = painterResource(id = store.imageRes),
                contentDescription = store.name,
                contentScale = ContentScale.Crop,
                modifier = Modifier.fillMaxWidth().height(120.dp)
            )
            Column(modifier = Modifier.padding(12.dp)) {
                Text(text = store.name, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                Spacer(modifier = Modifier.height(4.dp))
                Text(text = store.category, fontSize = 12.sp, color = Color.Gray)
                Spacer(modifier = Modifier.height(8.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(imageVector = Icons.Default.Star, contentDescription = "Avaliação", tint = Color(0xFFFFC107), modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(text = store.rating.toString(), fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}

// --- PREVIEWS ---
@Preview(showBackground = true)
@Composable
fun MainScreenPreview() {
    ProjetomapaTheme {
        MainScreen()
    }
}