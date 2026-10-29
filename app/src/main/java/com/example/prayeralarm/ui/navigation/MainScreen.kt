package com.example.prayeralarm.ui.navigation

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.example.prayeralarm.PrayerAlarmApplication
import com.example.prayeralarm.ui.home.HomeScreen
import com.example.prayeralarm.ui.home.HomeViewModel
import com.example.prayeralarm.ui.alarms.AlarmPlanScreen
import com.example.prayeralarm.ui.alarms.AlarmPlanViewModel
import com.example.prayeralarm.ui.locations.LocationsScreen
import com.example.prayeralarm.ui.locations.LocationsViewModel
import com.example.prayeralarm.ui.theme.GoldPrimary
import com.example.prayeralarm.ui.theme.NavyCard
import com.example.prayeralarm.ui.theme.NavyDark
import com.example.prayeralarm.ui.theme.NavySurface
import com.example.prayeralarm.ui.theme.TextPrimary
import com.example.prayeralarm.ui.theme.TextSecondary

@Composable
fun MainScreen() {
    val context = LocalContext.current.applicationContext as PrayerAlarmApplication
    val locationRepository = context.locationRepository

    val homeViewModel: HomeViewModel = viewModel(
        factory = HomeViewModel.Factory(locationRepository)
    )
    val locationsViewModel: LocationsViewModel = viewModel(
        factory = LocationsViewModel.Factory(locationRepository)
    )
    val alarmPlanViewModel: AlarmPlanViewModel = viewModel(
        factory = AlarmPlanViewModel.Factory(context.alarmPlanPreferences)
    )

    val navController = rememberNavController()
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route

    fun navigateToTopLevel(route: String) {
        if (currentRoute != route) {
            navController.navigate(route) {
                popUpTo(navController.graph.findStartDestination().id) {
                    saveState = true
                }
                launchSingleTop = true
                restoreState = true
            }
        }
    }

    val screens = listOf(
        Screen.Home,
        Screen.Locations,
        Screen.Alarms,
        Screen.Settings
    )

    Scaffold(
        bottomBar = {
            NavigationBar(
                containerColor = NavySurface,
                contentColor = TextPrimary,
                tonalElevation = 8.dp
            ) {
                screens.forEach { screen ->
                    val isSelected = currentRoute == screen.route
                    NavigationBarItem(
                        selected = isSelected,
                        onClick = {
                            navigateToTopLevel(screen.route)
                        },
                        icon = {
                            Icon(
                                imageVector = screen.icon,
                                contentDescription = screen.title
                            )
                        },
                        label = {
                            Text(text = screen.title)
                        },
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = NavyDark,
                            selectedTextColor = GoldPrimary,
                            indicatorColor = GoldPrimary,
                            unselectedIconColor = TextSecondary,
                            unselectedTextColor = TextSecondary
                        )
                    )
                }
            }
        }
    ) { innerPadding ->
        NavHost(
            navController = navController,
            startDestination = Screen.Home.route,
            modifier = Modifier.padding(innerPadding)
        ) {
            composable(Screen.Home.route) {
                val uiState by homeViewModel.uiState.collectAsState()
                HomeScreen(
                    uiState = uiState,
                    onLocationClick = { navigateToTopLevel(Screen.Locations.route) },
                    onSettingsClick = { navigateToTopLevel(Screen.Settings.route) },
                    onToggleNotification = { prayerType ->
                        homeViewModel.togglePrayerNotification(prayerType)
                    }
                )
            }

            composable(Screen.Locations.route) {
                val locationsUiState by locationsViewModel.uiState.collectAsState()
                LocationsScreen(
                    uiState = locationsUiState,
                    onSelectLocation = { location ->
                        locationsViewModel.selectLocation(location)
                    },
                    onOpenAddDialog = {
                        locationsViewModel.openAddDialog()
                    },
                    onOpenEditDialog = { location ->
                        locationsViewModel.openEditDialog(location)
                    },
                    onDismissDialog = {
                        locationsViewModel.dismissAddEditDialog()
                    },
                    onSaveLocation = { name, lat, lon, tz ->
                        locationsViewModel.saveLocation(name, lat, lon, tz)
                    },
                    onRequestDelete = { location ->
                        locationsViewModel.requestDeleteLocation(location)
                    },
                    onCancelDelete = {
                        locationsViewModel.cancelDeleteLocation()
                    },
                    onConfirmDelete = {
                        locationsViewModel.confirmDeleteLocation()
                    }
                )
            }

            composable(Screen.Alarms.route) {
                val uiState by homeViewModel.uiState.collectAsState()
                AlarmPlanScreen(homeUiState = uiState, viewModel = alarmPlanViewModel)
            }

            composable(Screen.Settings.route) {
                PlaceholderScreen(
                    title = "Application Settings",
                    subtitle = "Calculation preferences & alert configurations scheduled for Stage 10."
                )
            }
        }
    }
}

@Composable
fun PlaceholderScreen(
    title: String,
    subtitle: String,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .padding(24.dp),
        contentAlignment = Alignment.Center
    ) {
        Card(
            colors = CardDefaults.cardColors(containerColor = NavyCard),
            shape = RoundedCornerShape(16.dp),
            modifier = Modifier.padding(16.dp)
        ) {
            Column(
                modifier = Modifier.padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleLarge,
                    color = GoldPrimary,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = subtitle,
                    style = MaterialTheme.typography.bodyMedium,
                    color = TextSecondary,
                    textAlign = TextAlign.Center
                )
            }
        }
    }
}
