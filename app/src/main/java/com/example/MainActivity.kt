package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CallSplit
import androidx.compose.material.icons.filled.Compress
import androidx.compose.material.icons.filled.Dashboard
import androidx.compose.material.icons.filled.Folder
import androidx.compose.material.icons.filled.MergeType
import androidx.compose.material.icons.filled.PhotoLibrary
import androidx.compose.material.icons.outlined.CallSplit
import androidx.compose.material.icons.outlined.Compress
import androidx.compose.material.icons.outlined.Dashboard
import androidx.compose.material.icons.outlined.Folder
import androidx.compose.material.icons.outlined.MergeType
import androidx.compose.material.icons.outlined.PhotoLibrary
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import com.example.ui.PdfViewModel
import com.example.ui.components.LoadingOverlay
import com.example.ui.screens.CompressScreen
import com.example.ui.screens.DashboardScreen
import com.example.ui.screens.FilesHistoryScreen
import com.example.ui.screens.ImageToPdfScreen
import com.example.ui.screens.MergeScreen
import com.example.ui.screens.PdfViewerDialog
import com.example.ui.screens.SplitScreen
import com.example.ui.screens.LoginScreen
import com.example.ui.theme.PdfToolKitTheme
import com.example.ui.theme.RedPrimary

enum class AppTab(
    val title: String,
    val selectedIcon: ImageVector,
    val unselectedIcon: ImageVector
) {
    DASHBOARD("Home", Icons.Filled.Dashboard, Icons.Outlined.Dashboard),
    COMPRESS("Compress", Icons.Filled.Compress, Icons.Outlined.Compress),
    MERGE("Merge", Icons.Filled.MergeType, Icons.Outlined.MergeType),
    SPLIT("Split", Icons.Filled.CallSplit, Icons.Outlined.CallSplit),
    IMAGE_TO_PDF("Image", Icons.Filled.PhotoLibrary, Icons.Outlined.PhotoLibrary),
    FILES("Vault", Icons.Filled.Folder, Icons.Outlined.Folder)
}

class MainActivity : ComponentActivity() {
    private val viewModel: PdfViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        setContent {
            PdfToolKitTheme {
                MainAppScreen(viewModel = viewModel)
            }
        }
    }
}

@Composable
fun MainAppScreen(viewModel: PdfViewModel) {
    var currentTab by remember { mutableStateOf(AppTab.DASHBOARD) }
    val currentUser by viewModel.currentUser.collectAsState()
    val isProcessing by viewModel.isProcessing.collectAsState()
    val statusText by viewModel.processingStatusText.collectAsState()
    val userMessage by viewModel.userMessage.collectAsState()

    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(userMessage) {
        val msg = userMessage
        if (msg != null) {
            snackbarHostState.showSnackbar(msg)
            viewModel.clearUserMessage()
        }
    }

    if (currentUser == null || !currentUser!!.isLoggedIn) {
        LoginScreen(
            viewModel = viewModel,
            onLoginSuccess = {
                currentTab = AppTab.DASHBOARD
            }
        )
    } else {
        Scaffold(
            modifier = Modifier.fillMaxSize(),
            snackbarHost = { SnackbarHost(snackbarHostState) },
            bottomBar = {
                NavigationBar(
                    containerColor = com.example.ui.theme.SleekNavBackground,
                    tonalElevation = 0.dp
                ) {
                    AppTab.values().forEach { tab ->
                        val isSelected = tab == currentTab
                        NavigationBarItem(
                            selected = isSelected,
                            onClick = { currentTab = tab },
                            icon = {
                                Icon(
                                    imageVector = if (isSelected) tab.selectedIcon else tab.unselectedIcon,
                                    contentDescription = tab.title
                                )
                            },
                            label = { Text(tab.title) },
                            colors = NavigationBarItemDefaults.colors(
                                selectedIconColor = Color(0xFF1D192B),
                                selectedTextColor = Color(0xFF1D192B),
                                indicatorColor = com.example.ui.theme.SleekNavIndicator,
                                unselectedIconColor = com.example.ui.theme.TextSecondary,
                                unselectedTextColor = com.example.ui.theme.TextSecondary
                            ),
                            modifier = Modifier.testTag("nav_tab_${tab.name.lowercase()}")
                        )
                    }
                }
            }
        ) { innerPadding ->
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
            ) {
                when (currentTab) {
                    AppTab.DASHBOARD -> DashboardScreen(
                        viewModel = viewModel,
                        onNavigateToCompress = { currentTab = AppTab.COMPRESS },
                        onNavigateToMerge = { currentTab = AppTab.MERGE },
                        onNavigateToSplit = { currentTab = AppTab.SPLIT },
                        onNavigateToImageToPdf = { currentTab = AppTab.IMAGE_TO_PDF },
                        onNavigateToFiles = { currentTab = AppTab.FILES }
                    )
                    AppTab.COMPRESS -> CompressScreen(viewModel = viewModel)
                    AppTab.MERGE -> MergeScreen(viewModel = viewModel)
                    AppTab.SPLIT -> SplitScreen(viewModel = viewModel)
                    AppTab.IMAGE_TO_PDF -> ImageToPdfScreen(viewModel = viewModel)
                    AppTab.FILES -> FilesHistoryScreen(viewModel = viewModel)
                }

                // Global PDF Viewer Dialog
                PdfViewerDialog(viewModel = viewModel)

                // Global Processing Overlay
                LoadingOverlay(isProcessing = isProcessing, statusText = statusText)
            }
        }
    }
}
