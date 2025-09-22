package com.example.projetomapa.activites.dashboard

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.projetomapa.R
import com.example.projetomapa.ui.theme.ProjetomapaTheme

// --- DATA CLASSES ---
data class CategoryItem(
    val iconRes: Int,
    val label: String
)

data class StoreInfo(
    val imageRes: Int,
    val name: String,
    val category: String,
    val rating: Double
)


// --- TELA PRINCIPAL (HOME) ---
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen() {
    Scaffold(
        topBar = {
            HomeTopAppBar()
        }
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier.fillMaxSize().padding(innerPadding),
            contentPadding = PaddingValues(vertical = 16.dp)
        ) {
            item {
                Column(modifier = Modifier.padding(horizontal = 16.dp)) {
                    CategoryList()
                }
            }
            item { Spacer(modifier = Modifier.height(24.dp)) }
            item { PromoBanners() }
            item {
                Spacer(modifier = Modifier.height(24.dp))
                Text(
                    text = "Lojas Populares",
                    style = MaterialTheme.typography.titleLarge,
                    modifier = Modifier.padding(horizontal = 16.dp)
                )
            }
            item {
                PopularStoresList()
            }
        }
    }
}

// --- COMPONENTES DA HOMESCREEN ---

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
    val categories = listOf(CategoryItem(R.drawable.building, "Lojas"), CategoryItem(R.drawable.location, "Perto"), CategoryItem(R.drawable.sample, "Promoções"), CategoryItem(R.drawable.profile, "Sua Conta"), CategoryItem(R.drawable.wallet, "Carteira"), CategoryItem(R.drawable.search_icon, "Buscar"))
    LazyRow(contentPadding = PaddingValues(vertical = 8.dp)) { items(categories) { category -> CategoryItemView(item = category) { /* TODO */ } } }
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
    HorizontalPager(state = pagerState, contentPadding = PaddingValues(horizontal = 16.dp), pageSpacing = 16.dp, modifier = Modifier.fillMaxWidth()) { page ->
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

// --- PREVIEW ---
@Preview(showBackground = true)
@Composable
fun HomeScreenPreview() {
    ProjetomapaTheme {
        HomeScreen()
    }
}