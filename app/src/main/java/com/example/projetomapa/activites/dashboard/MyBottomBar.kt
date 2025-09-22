package com.example.projetomapa.Activites.dashboard

// Importações necessárias para o Material 3 e Compose
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.navigation.NavController
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.example.projetomapa.R // Importa a classe R do seu projeto

// Data class para representar cada item da barra de navegação
data class BottomNavItem(
    val label: String,
    val icon: Int, // Usamos Int para o ID do recurso drawable
    val route: String
)

@Composable
fun MyBottomBar(navController: NavController) {
    // Lista dos itens que aparecerão na barra
    val navItems = listOf(
        // Substitua os ícones pelos seus que estão na pasta res/drawable
        BottomNavItem("Home", R.drawable.btn_3, "home"),
        BottomNavItem("Local", R.drawable.location, "location"),
        BottomNavItem("Perfil", R.drawable.profile, "profile")
    )

    NavigationBar(
        containerColor = Color.White // Cor de fundo da barra
    ) {
        val navBackStackEntry by navController.currentBackStackEntryAsState()
        val currentRoute = navBackStackEntry?.destination?.route

        navItems.forEach { item ->
            NavigationBarItem(
                selected = currentRoute == item.route,
                onClick = {
                    navController.navigate(item.route) {
                        // Evita empilhar a mesma tela várias vezes
                        popUpTo(navController.graph.startDestinationId)
                        launchSingleTop = true
                    }
                },
                icon = {
                    Icon(
                        painter = painterResource(id = item.icon),
                        contentDescription = item.label
                    )
                },
                label = {
                    Text(text = item.label)
                },
                colors = NavigationBarItemDefaults.colors(
                    selectedIconColor = Color.Blue, // Cor do ícone selecionado
                    unselectedIconColor = Color.Gray, // Cor do ícone não selecionado
                    selectedTextColor = Color.Blue, // Cor do texto selecionado
                    unselectedTextColor = Color.Gray // Cor do texto não selecionado
                )
            )
        }
    }
}

// Preview para visualização no Android Studio
@Preview(showBackground = true)
@Composable
fun MyBottomBarPreview() {
    // O NavController do preview não é funcional, serve apenas para não dar erro
    val navController = rememberNavController()
    MyBottomBar(navController = navController)
}