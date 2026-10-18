package com.playme.player.feature.library

import android.Manifest
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.widget.Toast
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.Sort
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.Folder
import androidx.compose.material.icons.filled.GridView
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material.icons.filled.ViewAgenda
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import com.playme.player.core.designsystem.PlayMeDesign
import com.playme.player.core.designsystem.darkGlass
import com.playme.player.core.designsystem.darkGlassCard
import com.playme.player.core.model.VideoFolder
import com.playme.player.core.model.VideoItem
import com.playme.player.core.repository.VideoSortOrder
import com.playme.player.feature.settings.SettingsScreen
import com.playme.player.feature.vault.PrivateVaultScreen
import org.koin.compose.koinInject

@Composable
fun LibraryScreen(
    onVideoClick: (VideoItem, List<VideoItem>) -> Unit,
    viewModel: LibraryViewModel = koinInject()
) {
    val context = LocalContext.current
    val uiState by viewModel.uiState.collectAsState()

    var isSearchExpanded by remember { mutableStateOf(false) }
    var showSortMenu by remember { mutableStateOf(false) }
    var showFolderSpeedMenu by remember { mutableStateOf(false) }
    var isGridMode by remember { mutableStateOf(false) }

    // Handle back button when viewing a folder or searching or in settings
    BackHandler(enabled = uiState.selectedTab != LibraryTab.VIDEOS || uiState.selectedFolder != null || isSearchExpanded || uiState.mediaViewMode == LibraryMediaViewMode.FOLDERS) {
        if (uiState.selectedTab != LibraryTab.VIDEOS) {
            viewModel.setTab(LibraryTab.VIDEOS)
        } else if (uiState.selectedFolder != null) {
            viewModel.selectFolder(null)
        } else if (isSearchExpanded) {
            isSearchExpanded = false
        } else if (uiState.mediaViewMode == LibraryMediaViewMode.FOLDERS) {
            viewModel.setMediaViewMode(LibraryMediaViewMode.VIDEOS)
        }
    }

    val permissionToRequest = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
        Manifest.permission.READ_MEDIA_VIDEO
    } else {
        Manifest.permission.READ_EXTERNAL_STORAGE
    }

    var hasPermission by remember {
        mutableStateOf(
            ContextCompat.checkSelfPermission(context, permissionToRequest) == PackageManager.PERMISSION_GRANTED
        )
    }

    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        hasPermission = isGranted
        if (isGranted) {
            viewModel.loadMedia()
        }
    }

    // Quick import file picker
    val filePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri ->
        if (uri != null) {
            viewModel.loadMedia()
            Toast.makeText(context, "Scanning local media…", Toast.LENGTH_SHORT).show()
        }
    }

    LaunchedEffect(Unit) {
        if (!hasPermission) {
            permissionLauncher.launch(permissionToRequest)
        }
    }

    Scaffold(
        containerColor = PlayMeDesign.ObsidianDark,
        bottomBar = {
            // Native Flush Bottom Navigation Bar (Obsidian Glass)
            NavigationBar(
                containerColor = PlayMeDesign.BottomNavBackground,
                tonalElevation = 0.dp,
                modifier = Modifier.border(0.8.dp, PlayMeDesign.DarkGlassBorder)
            ) {
                val navItemColors = NavigationBarItemDefaults.colors(
                    selectedIconColor = PlayMeDesign.ElectricViolet,
                    selectedTextColor = Color.White,
                    indicatorColor = PlayMeDesign.BadgeIndigoBg,
                    unselectedIconColor = Color(0xFF71717A),
                    unselectedTextColor = Color(0xFF71717A)
                )

                // 1. Home Tab
                NavigationBarItem(
                    selected = uiState.selectedTab == LibraryTab.VIDEOS && uiState.mediaViewMode == LibraryMediaViewMode.VIDEOS && uiState.selectedFolder == null,
                    onClick = {
                        viewModel.setTab(LibraryTab.VIDEOS)
                        viewModel.setMediaViewMode(LibraryMediaViewMode.VIDEOS)
                    },
                    icon = {
                        Icon(
                            imageVector = Icons.Default.Home,
                            contentDescription = "Home",
                            modifier = Modifier.size(22.dp)
                        )
                    },
                    label = {
                        Text(
                            text = "Home",
                            fontSize = 11.sp,
                            fontWeight = if (uiState.selectedTab == LibraryTab.VIDEOS && uiState.mediaViewMode == LibraryMediaViewMode.VIDEOS && uiState.selectedFolder == null) FontWeight.Bold else FontWeight.Normal
                        )
                    },
                    colors = navItemColors
                )

                // 2. Search Tab
                NavigationBarItem(
                    selected = isSearchExpanded,
                    onClick = {
                        isSearchExpanded = !isSearchExpanded
                    },
                    icon = {
                        Icon(
                            imageVector = Icons.Default.Search,
                            contentDescription = "Search",
                            modifier = Modifier.size(22.dp)
                        )
                    },
                    label = {
                        Text(
                            text = "Search",
                            fontSize = 11.sp,
                            fontWeight = if (isSearchExpanded) FontWeight.Bold else FontWeight.Normal
                        )
                    },
                    colors = navItemColors
                )

                // 3. Folders Tab
                NavigationBarItem(
                    selected = uiState.selectedTab != LibraryTab.VAULT && (uiState.mediaViewMode == LibraryMediaViewMode.FOLDERS || uiState.selectedFolder != null),
                    onClick = {
                        viewModel.setTab(LibraryTab.VIDEOS)
                        viewModel.setMediaViewMode(LibraryMediaViewMode.FOLDERS)
                    },
                    icon = {
                        Icon(
                            imageVector = Icons.Default.Folder,
                            contentDescription = "Folders",
                            modifier = Modifier.size(22.dp)
                        )
                    },
                    label = {
                        Text(
                            text = "Folders",
                            fontSize = 11.sp,
                            fontWeight = if (uiState.mediaViewMode == LibraryMediaViewMode.FOLDERS || uiState.selectedFolder != null) FontWeight.Bold else FontWeight.Normal
                        )
                    },
                    colors = navItemColors
                )

                // 4. Private Vault Tab
                NavigationBarItem(
                    selected = uiState.selectedTab == LibraryTab.VAULT,
                    onClick = {
                        viewModel.setTab(LibraryTab.VAULT)
                    },
                    icon = {
                        Icon(
                            imageVector = Icons.Default.Lock,
                            contentDescription = "Vault",
                            modifier = Modifier.size(22.dp)
                        )
                    },
                    label = {
                        Text(
                            text = "Vault",
                            fontSize = 11.sp,
                            fontWeight = if (uiState.selectedTab == LibraryTab.VAULT) FontWeight.Bold else FontWeight.Normal
                        )
                    },
                    colors = navItemColors
                )

                // 5. Settings Tab
                NavigationBarItem(
                    selected = uiState.selectedTab == LibraryTab.SETTINGS,
                    onClick = {
                        viewModel.setTab(LibraryTab.SETTINGS)
                    },
                    icon = {
                        Icon(
                            imageVector = Icons.Default.Settings,
                            contentDescription = "Settings",
                            modifier = Modifier.size(22.dp)
                        )
                    },
                    label = {
                        Text(
                            text = "Settings",
                            fontSize = 11.sp,
                            fontWeight = if (uiState.selectedTab == LibraryTab.SETTINGS) FontWeight.Bold else FontWeight.Normal
                        )
                    },
                    colors = navItemColors
                )
            }
        }
    ) { paddingValues ->
        if (!hasPermission) {
            // Permission Screen in Obsidian Glass
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .padding(24.dp),
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Box(
                    modifier = Modifier
                        .size(72.dp)
                        .darkGlass(shape = RoundedCornerShape(20.dp)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Home,
                        contentDescription = null,
                        tint = PlayMeDesign.ElectricViolet,
                        modifier = Modifier.size(36.dp)
                    )
                }

                Spacer(modifier = Modifier.height(20.dp))

                Text(
                    text = "Permission Required",
                    color = Color.White,
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold
                )

                Spacer(modifier = Modifier.height(8.dp))

                Text(
                    text = "PlayMe requires access to your device media to scan and display your offline video collection.",
                    color = Color(0xFFA1A1AA),
                    fontSize = 13.sp,
                    lineHeight = 18.sp
                )

                Spacer(modifier = Modifier.height(24.dp))

                Button(
                    onClick = { permissionLauncher.launch(permissionToRequest) },
                    colors = ButtonDefaults.buttonColors(containerColor = PlayMeDesign.ElectricViolet),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text("Grant Permission", color = Color.White, fontWeight = FontWeight.SemiBold)
                }
            }
            return@Scaffold
        }

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            // --- TOP HEADER ---
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 12.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    if (uiState.selectedFolder != null) {
                        // Folder Header with Back Button
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .size(36.dp)
                                    .darkGlass(shape = CircleShape)
                                    .clickable { viewModel.selectFolder(null) },
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                                    contentDescription = "Back",
                                    tint = Color.White,
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                            Spacer(modifier = Modifier.width(12.dp))
                            Text(
                                text = uiState.selectedFolder?.name ?: "Folder",
                                color = Color.White,
                                fontSize = 18.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    } else {
                        // Brand Logo: "Play" + "Me" with Electric Gradient
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = "Play",
                                color = Color.White,
                                fontSize = 22.sp,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = "Me",
                                style = TextStyle(
                                    brush = PlayMeDesign.LogoGradient,
                                    fontSize = 22.sp,
                                    fontWeight = FontWeight.ExtraBold
                                )
                            )
                        }
                    }

                    // Action buttons: Search toggle & Quick Import (matching dark glass buttons)
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        // Search Toggle Button
                        Box(
                            modifier = Modifier
                                .size(36.dp)
                                .darkGlass(shape = CircleShape)
                                .clickable { isSearchExpanded = !isSearchExpanded },
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.Search,
                                contentDescription = "Search",
                                tint = if (isSearchExpanded) PlayMeDesign.ElectricViolet else Color(0xFFD4D4D8),
                                modifier = Modifier.size(18.dp)
                            )
                        }

                        // Quick Import Button
                        Box(
                            modifier = Modifier
                                .size(36.dp)
                                .darkGlass(shape = CircleShape)
                                .clickable { filePickerLauncher.launch("video/*") },
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.Download,
                                contentDescription = "Import",
                                tint = Color(0xFFD4D4D8),
                                modifier = Modifier.size(18.dp)
                            )
                        }
                    }
                }

                // Expandable Search Capsule
                AnimatedVisibility(
                    visible = isSearchExpanded,
                    enter = fadeIn() + expandVertically(),
                    exit = fadeOut() + shrinkVertically()
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 10.dp)
                            .darkGlassCard(shape = RoundedCornerShape(12.dp))
                            .padding(horizontal = 12.dp, vertical = 8.dp)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Icon(
                                imageVector = Icons.Default.Search,
                                contentDescription = null,
                                tint = Color(0xFFA1A1AA),
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            BasicTextField(
                                value = uiState.searchQuery,
                                onValueChange = { viewModel.onSearchQueryChanged(it) },
                                textStyle = TextStyle(color = Color.White, fontSize = 13.sp),
                                cursorBrush = SolidColor(PlayMeDesign.ElectricViolet),
                                modifier = Modifier.weight(1f),
                                decorationBox = { innerTextField ->
                                    if (uiState.searchQuery.isEmpty()) {
                                        Text(
                                            text = "Search videos or history…",
                                            color = Color(0xFF71717A),
                                            fontSize = 13.sp
                                        )
                                    }
                                    innerTextField()
                                }
                            )
                            if (uiState.searchQuery.isNotEmpty()) {
                                Icon(
                                    imageVector = Icons.Default.Close,
                                    contentDescription = "Clear",
                                    tint = Color(0xFFA1A1AA),
                                    modifier = Modifier
                                        .size(16.dp)
                                        .clickable { viewModel.onSearchQueryChanged("") }
                                )
                            }
                        }
                    }
                }
            }

            // --- MAIN BODY CONTENT ---
            if (uiState.isLoading) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator(color = PlayMeDesign.ElectricViolet)
                }
            } else {
                AnimatedContent(
                    targetState = uiState.selectedTab,
                    transitionSpec = {
                        (fadeIn(animationSpec = tween(220)) + slideInHorizontally(animationSpec = tween(220)) { it / 6 })
                            .togetherWith(fadeOut(animationSpec = tween(180)) + slideOutHorizontally(animationSpec = tween(180)) { -it / 6 })
                    },
                    label = "LibraryTabTransition"
                ) { targetTab ->
                    when (targetTab) {
                    LibraryTab.VIDEOS, LibraryTab.FOLDERS -> {
                        Box(modifier = Modifier.fillMaxSize()) {
                            LazyColumn(
                                contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
                                verticalArrangement = Arrangement.spacedBy(14.dp)
                            ) {
                                // 1. Continue Watching Section (Always fixed at top)
                                if (uiState.recentHistory.isNotEmpty() && uiState.searchQuery.isBlank()) {
                                    item {
                                        Column(modifier = Modifier.fillMaxWidth()) {
                                            Row(
                                                modifier = Modifier.fillMaxWidth(),
                                                verticalAlignment = Alignment.CenterVertically,
                                                horizontalArrangement = Arrangement.SpaceBetween
                                            ) {
                                                Row(
                                                    verticalAlignment = Alignment.CenterVertically,
                                                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                                                ) {
                                                    Text(
                                                        text = "Continue Watching",
                                                        color = Color.White,
                                                        fontSize = 13.sp,
                                                        fontWeight = FontWeight.Bold
                                                    )
                                                    Box(
                                                        modifier = Modifier
                                                            .background(PlayMeDesign.BadgeIndigoBg, RoundedCornerShape(10.dp))
                                                            .border(0.8.dp, PlayMeDesign.BadgeIndigoBorder, RoundedCornerShape(10.dp))
                                                            .padding(horizontal = 6.dp, vertical = 1.dp)
                                                    ) {
                                                        Text(
                                                            text = "${uiState.recentHistory.size}",
                                                            color = PlayMeDesign.BadgeIndigoText,
                                                            fontSize = 10.sp,
                                                            fontWeight = FontWeight.Bold
                                                        )
                                                    }
                                                }
                                            }

                                            Spacer(modifier = Modifier.height(8.dp))

                                            // Horizontal Carousel
                                            LazyRow(
                                                horizontalArrangement = Arrangement.spacedBy(10.dp),
                                                modifier = Modifier.fillMaxWidth()
                                            ) {
                                                items(uiState.recentHistory) { historyItem ->
                                                    val video = uiState.videos.firstOrNull { it.contentUri == historyItem.mediaUri }
                                                        ?: VideoItem(
                                                            id = historyItem.mediaUri.hashCode().toLong(),
                                                            title = historyItem.title,
                                                            displayName = historyItem.title,
                                                            path = "",
                                                            contentUri = historyItem.mediaUri,
                                                            durationMs = historyItem.durationMs,
                                                            sizeBytes = 0L,
                                                            dateAdded = historyItem.lastWatchedTimestamp,
                                                            width = 1920,
                                                            height = 1080,
                                                            folderName = "Recent",
                                                            folderPath = "",
                                                            lastPlayedPositionMs = historyItem.lastPositionMs,
                                                            lastWatchedTimestamp = historyItem.lastWatchedTimestamp
                                                        )

                                                    HistoryCard(
                                                        video = video,
                                                        onClick = { onVideoClick(video, listOf(video)) }
                                                    )
                                                }
                                            }
                                        }
                                    }
                                }

                                // 2. Sub-Navigation Row: Video / Folder Tabs + Sort & Grid Toggle
                                item {
                                    val isVideoActive = uiState.mediaViewMode == LibraryMediaViewMode.VIDEOS && uiState.selectedFolder == null
                                    val isFolderActive = uiState.mediaViewMode == LibraryMediaViewMode.FOLDERS || uiState.selectedFolder != null

                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.SpaceBetween
                                    ) {
                                        // Tabs: Video / Folder (Only switches the below content)
                                        Row(
                                            verticalAlignment = Alignment.CenterVertically,
                                            horizontalArrangement = Arrangement.spacedBy(16.dp)
                                        ) {
                                            // Video Tab Button
                                            Column(
                                                horizontalAlignment = Alignment.CenterHorizontally,
                                                modifier = Modifier.clickable { viewModel.setMediaViewMode(LibraryMediaViewMode.VIDEOS) }
                                            ) {
                                                Text(
                                                    text = "Videos",
                                                    color = if (isVideoActive) Color.White else Color(0xFFA1A1AA),
                                                    fontSize = 13.5.sp,
                                                    fontWeight = if (isVideoActive) FontWeight.Bold else FontWeight.Medium
                                                )
                                                Spacer(modifier = Modifier.height(3.dp))
                                                Box(
                                                    modifier = Modifier
                                                        .width(28.dp)
                                                        .height(2.5.dp)
                                                        .then(
                                                            if (isVideoActive) Modifier.background(PlayMeDesign.ElectricGradient, CircleShape)
                                                            else Modifier
                                                        )
                                                )
                                            }

                                            // Folder Tab Button
                                            Column(
                                                horizontalAlignment = Alignment.CenterHorizontally,
                                                modifier = Modifier.clickable { viewModel.setMediaViewMode(LibraryMediaViewMode.FOLDERS) }
                                            ) {
                                                Text(
                                                    text = "Folders",
                                                    color = if (isFolderActive) Color.White else Color(0xFFA1A1AA),
                                                    fontSize = 13.5.sp,
                                                    fontWeight = if (isFolderActive) FontWeight.Bold else FontWeight.Medium
                                                )
                                                Spacer(modifier = Modifier.height(3.dp))
                                                Box(
                                                    modifier = Modifier
                                                        .width(28.dp)
                                                        .height(2.5.dp)
                                                        .then(
                                                            if (isFolderActive) Modifier.background(PlayMeDesign.ElectricGradient, CircleShape)
                                                            else Modifier
                                                        )
                                                )
                                            }
                                        }

                                        // Right controls: Sort & Grid Layout Toggle
                                        Row(
                                            verticalAlignment = Alignment.CenterVertically,
                                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                                        ) {
                                            // Sort Dropdown Button
                                            Box {
                                                Box(
                                                    modifier = Modifier
                                                        .size(30.dp)
                                                        .darkGlass(shape = CircleShape)
                                                        .clickable { showSortMenu = true },
                                                    contentAlignment = Alignment.Center
                                                ) {
                                                    Icon(
                                                        imageVector = Icons.AutoMirrored.Filled.Sort,
                                                        contentDescription = "Sort",
                                                        tint = Color(0xFFD4D4D8),
                                                        modifier = Modifier.size(15.dp)
                                                    )
                                                }

                                                DropdownMenu(
                                                    expanded = showSortMenu,
                                                    onDismissRequest = { showSortMenu = false },
                                                    modifier = Modifier
                                                        .background(PlayMeDesign.DarkGlassElevated)
                                                        .border(1.dp, PlayMeDesign.DarkGlassBorder, RoundedCornerShape(12.dp))
                                                ) {
                                                    DropdownMenuItem(
                                                        text = { Text("Date (Newest First)", color = Color.White, fontSize = 12.sp) },
                                                        onClick = {
                                                            viewModel.setSortOrder(VideoSortOrder.DATE_DESC)
                                                            showSortMenu = false
                                                        }
                                                    )
                                                    DropdownMenuItem(
                                                        text = { Text("Date (Oldest First)", color = Color.White, fontSize = 12.sp) },
                                                        onClick = {
                                                            viewModel.setSortOrder(VideoSortOrder.DATE_ASC)
                                                            showSortMenu = false
                                                        }
                                                    )
                                                    DropdownMenuItem(
                                                        text = { Text("Name (A to Z)", color = Color.White, fontSize = 12.sp) },
                                                        onClick = {
                                                            viewModel.setSortOrder(VideoSortOrder.NAME_ASC)
                                                            showSortMenu = false
                                                        }
                                                    )
                                                    DropdownMenuItem(
                                                        text = { Text("Size (Largest First)", color = Color.White, fontSize = 12.sp) },
                                                        onClick = {
                                                            viewModel.setSortOrder(VideoSortOrder.SIZE_DESC)
                                                            showSortMenu = false
                                                        }
                                                    )
                                                    DropdownMenuItem(
                                                        text = { Text("Duration (Longest First)", color = Color.White, fontSize = 12.sp) },
                                                        onClick = {
                                                            viewModel.setSortOrder(VideoSortOrder.DURATION_DESC)
                                                            showSortMenu = false
                                                        }
                                                    )
                                                }
                                            }

                                            // Grid/List Layout Toggle
                                            Box(
                                                modifier = Modifier
                                                    .size(30.dp)
                                                    .darkGlass(shape = CircleShape)
                                                    .clickable { isGridMode = !isGridMode },
                                                contentAlignment = Alignment.Center
                                            ) {
                                                Icon(
                                                    imageVector = if (isGridMode) Icons.Default.ViewAgenda else Icons.Default.GridView,
                                                    contentDescription = "Toggle View",
                                                    tint = Color(0xFFD4D4D8),
                                                    modifier = Modifier.size(15.dp)
                                                )
                                            }
                                        }
                                    }
                                }

                                // 3. Lower Content: Videos Feed OR Folders Feed
                                if (uiState.mediaViewMode == LibraryMediaViewMode.VIDEOS && uiState.selectedFolder == null) {
                                    // 3A. Videos Feed (List Mode or 2-Column Grid Mode with optional Name Grouping)
                                    if (uiState.videos.isEmpty()) {
                                        item {
                                            Box(
                                                modifier = Modifier
                                                    .fillMaxWidth()
                                                    .padding(top = 40.dp),
                                                contentAlignment = Alignment.Center
                                            ) {
                                                Text("No videos found", color = Color(0xFF71717A), fontSize = 14.sp)
                                            }
                                        }
                                    } else if (uiState.autoGroupSimilarVideos && uiState.searchQuery.isBlank()) {
                                        val groupedMap = viewModel.getGroupedVideos()
                                        groupedMap.forEach { (groupTitle, groupVideos) ->
                                            item(key = "group_header_$groupTitle") {
                                                Row(
                                                    modifier = Modifier
                                                        .fillMaxWidth()
                                                        .padding(top = 10.dp, bottom = 4.dp),
                                                    verticalAlignment = Alignment.CenterVertically,
                                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                                ) {
                                                    Text(
                                                        text = groupTitle,
                                                        color = Color.White,
                                                        fontSize = 13.5.sp,
                                                        fontWeight = FontWeight.Bold
                                                    )
                                                    Box(
                                                        modifier = Modifier
                                                            .background(PlayMeDesign.BadgeIndigoBg, RoundedCornerShape(10.dp))
                                                            .border(0.8.dp, PlayMeDesign.BadgeIndigoBorder, RoundedCornerShape(10.dp))
                                                            .padding(horizontal = 6.dp, vertical = 1.dp)
                                                    ) {
                                                        Text(
                                                            text = "${groupVideos.size}",
                                                            color = PlayMeDesign.BadgeIndigoText,
                                                            fontSize = 10.sp,
                                                            fontWeight = FontWeight.Bold
                                                        )
                                                    }
                                                }
                                            }

                                            if (isGridMode) {
                                                val videoChunks = groupVideos.chunked(2)
                                                items(videoChunks) { pair ->
                                                    Row(
                                                        modifier = Modifier.fillMaxWidth(),
                                                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                                                    ) {
                                                        Box(modifier = Modifier.weight(1f)) {
                                                            val isLast1 = (pair[0].id == uiState.lastPlayedOverall?.id)
                                                            VideoCard(
                                                                video = pair[0],
                                                                onClick = { onVideoClick(pair[0], uiState.videos) },
                                                                onMoveToVault = { viewModel.moveToVault(pair[0]) },
                                                                isGridMode = true,
                                                                isLastPlayed = isLast1
                                                            )
                                                        }
                                                        if (pair.size > 1) {
                                                            Box(modifier = Modifier.weight(1f)) {
                                                                val isLast2 = (pair[1].id == uiState.lastPlayedOverall?.id)
                                                                VideoCard(
                                                                    video = pair[1],
                                                                    onClick = { onVideoClick(pair[1], uiState.videos) },
                                                                    onMoveToVault = { viewModel.moveToVault(pair[1]) },
                                                                    isGridMode = true,
                                                                    isLastPlayed = isLast2
                                                                )
                                                            }
                                                        } else {
                                                            Spacer(modifier = Modifier.weight(1f))
                                                        }
                                                    }
                                                }
                                            } else {
                                                items(groupVideos) { video ->
                                                    val isLast = (video.id == uiState.lastPlayedOverall?.id)
                                                    VideoCard(
                                                        video = video,
                                                        onClick = { onVideoClick(video, uiState.videos) },
                                                        onMoveToVault = { viewModel.moveToVault(video) },
                                                        isGridMode = false,
                                                        isLastPlayed = isLast
                                                    )
                                                }
                                            }
                                        }
                                    } else if (isGridMode) {
                                        val videoChunks = uiState.videos.chunked(2)
                                        items(videoChunks) { pair ->
                                            Row(
                                                modifier = Modifier.fillMaxWidth(),
                                                horizontalArrangement = Arrangement.spacedBy(10.dp)
                                            ) {
                                                Box(modifier = Modifier.weight(1f)) {
                                                    val isLast1 = (pair[0].id == uiState.lastPlayedOverall?.id)
                                                    VideoCard(
                                                        video = pair[0],
                                                        onClick = { onVideoClick(pair[0], uiState.videos) },
                                                        onMoveToVault = { viewModel.moveToVault(pair[0]) },
                                                        isGridMode = true,
                                                        isLastPlayed = isLast1
                                                    )
                                                }
                                                if (pair.size > 1) {
                                                    Box(modifier = Modifier.weight(1f)) {
                                                        val isLast2 = (pair[1].id == uiState.lastPlayedOverall?.id)
                                                        VideoCard(
                                                            video = pair[1],
                                                            onClick = { onVideoClick(pair[1], uiState.videos) },
                                                            onMoveToVault = { viewModel.moveToVault(pair[1]) },
                                                            isGridMode = true,
                                                            isLastPlayed = isLast2
                                                        )
                                                    }
                                                } else {
                                                    Spacer(modifier = Modifier.weight(1f))
                                                }
                                            }
                                        }
                                    } else {
                                        items(uiState.videos) { video ->
                                            val isLast = (video.id == uiState.lastPlayedOverall?.id)
                                            VideoCard(
                                                video = video,
                                                onClick = { onVideoClick(video, uiState.videos) },
                                                onMoveToVault = { viewModel.moveToVault(video) },
                                                isGridMode = false,
                                                isLastPlayed = isLast
                                            )
                                        }
                                    }
                                } else {
                                    // 3B. FOLDERS VIEW (Folder Directory or Drilldown)
                                    val currentFolder = uiState.selectedFolder
                                    if (currentFolder != null) {
                                        // Folder Drilldown Header
                                        item {
                                            Row(
                                                modifier = Modifier
                                                    .fillMaxWidth()
                                                    .clip(RoundedCornerShape(12.dp))
                                                    .background(Color(0xFF141624).copy(alpha = 0.7f))
                                                    .border(0.8.dp, PlayMeDesign.DarkGlassBorder, RoundedCornerShape(12.dp))
                                                    .padding(horizontal = 10.dp, vertical = 8.dp),
                                                verticalAlignment = Alignment.CenterVertically,
                                                horizontalArrangement = Arrangement.SpaceBetween
                                            ) {
                                                Row(
                                                    verticalAlignment = Alignment.CenterVertically,
                                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                                ) {
                                                    Box(
                                                        modifier = Modifier
                                                            .size(32.dp)
                                                            .darkGlass(CircleShape)
                                                            .clickable { viewModel.selectFolder(null) },
                                                        contentAlignment = Alignment.Center
                                                    ) {
                                                        Icon(
                                                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                                                            contentDescription = "Back",
                                                            tint = Color.White,
                                                            modifier = Modifier.size(16.dp)
                                                        )
                                                    }
                                                    Column {
                                                        Text(
                                                            text = currentFolder.name,
                                                            color = Color.White,
                                                            fontSize = 14.sp,
                                                            fontWeight = FontWeight.Bold
                                                        )
                                                        Text(
                                                            text = "${uiState.videos.size} videos",
                                                            color = Color(0xFFA1A1AA),
                                                            fontSize = 11.sp
                                                        )
                                                    }
                                                }

                                                Row(
                                                    verticalAlignment = Alignment.CenterVertically,
                                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                                ) {
                                                    // Custom Folder Playback Speed Quick Selector
                                                    val currentFolderSpeed = uiState.folderSpeeds[currentFolder.path]
                                                    val effectiveSpeed = currentFolderSpeed ?: uiState.globalSpeed
                                                    Box {
                                                        Row(
                                                            modifier = Modifier
                                                                .clip(RoundedCornerShape(8.dp))
                                                                .background(
                                                                    if (currentFolderSpeed != null) PlayMeDesign.ElectricViolet.copy(alpha = 0.25f)
                                                                    else Color(0xFF27272A)
                                                                )
                                                                .border(
                                                                    0.8.dp,
                                                                    if (currentFolderSpeed != null) PlayMeDesign.ElectricViolet
                                                                    else Color(0xFF3F3F46),
                                                                    RoundedCornerShape(8.dp)
                                                                )
                                                                .clickable { showFolderSpeedMenu = true }
                                                                .padding(horizontal = 8.dp, vertical = 5.dp),
                                                            verticalAlignment = Alignment.CenterVertically,
                                                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                                                        ) {
                                                            Icon(
                                                                imageVector = Icons.Default.Speed,
                                                                contentDescription = "Folder Speed",
                                                                tint = if (currentFolderSpeed != null) PlayMeDesign.ElectricViolet else Color(0xFFA1A1AA),
                                                                modifier = Modifier.size(13.dp)
                                                            )
                                                            Text(
                                                                text = "${effectiveSpeed}x",
                                                                color = Color.White,
                                                                fontSize = 11.sp,
                                                                fontWeight = FontWeight.Bold
                                                            )
                                                        }

                                                        DropdownMenu(
                                                            expanded = showFolderSpeedMenu,
                                                            onDismissRequest = { showFolderSpeedMenu = false },
                                                            modifier = Modifier
                                                                .background(PlayMeDesign.DarkGlassElevated)
                                                                .border(1.dp, PlayMeDesign.DarkGlassBorder, RoundedCornerShape(12.dp))
                                                        ) {
                                                            DropdownMenuItem(
                                                                text = { Text("Use Global Speed (${uiState.globalSpeed}x)", color = Color.White, fontSize = 12.sp) },
                                                                onClick = {
                                                                    viewModel.setFolderPlaybackSpeed(currentFolder.path, null)
                                                                    showFolderSpeedMenu = false
                                                                }
                                                            )
                                                            listOf(0.5f, 0.75f, 1.0f, 1.25f, 1.5f, 1.75f, 2.0f, 2.5f, 3.0f).forEach { speed ->
                                                                DropdownMenuItem(
                                                                    text = {
                                                                        Text(
                                                                            text = "${speed}x" + if (currentFolderSpeed == speed) " ✓" else "",
                                                                            color = if (currentFolderSpeed == speed) PlayMeDesign.ElectricViolet else Color.White,
                                                                            fontSize = 12.sp,
                                                                            fontWeight = if (currentFolderSpeed == speed) FontWeight.Bold else FontWeight.Normal
                                                                        )
                                                                    },
                                                                    onClick = {
                                                                        viewModel.setFolderPlaybackSpeed(currentFolder.path, speed)
                                                                        showFolderSpeedMenu = false
                                                                    }
                                                                )
                                                            }
                                                        }
                                                    }

                                                    // Folder Resume / Play All Action Button
                                                    val lastInFolder = uiState.lastPlayedInFolder
                                                    if (lastInFolder != null) {
                                                        Box(
                                                            modifier = Modifier
                                                                .clip(RoundedCornerShape(50))
                                                                .background(PlayMeDesign.ElectricViolet)
                                                                .clickable { onVideoClick(lastInFolder, uiState.videos) }
                                                                .padding(horizontal = 12.dp, vertical = 6.dp)
                                                        ) {
                                                            Row(
                                                                verticalAlignment = Alignment.CenterVertically,
                                                                horizontalArrangement = Arrangement.spacedBy(4.dp)
                                                            ) {
                                                                Icon(
                                                                    imageVector = Icons.Default.PlayArrow,
                                                                    contentDescription = "Resume",
                                                                    tint = Color.White,
                                                                    modifier = Modifier.size(15.dp)
                                                                )
                                                                Text(
                                                                    text = "Resume",
                                                                    color = Color.White,
                                                                    fontSize = 11.5.sp,
                                                                    fontWeight = FontWeight.Bold
                                                                )
                                                            }
                                                        }
                                                    } else {
                                                        Box(
                                                            modifier = Modifier
                                                                .clip(RoundedCornerShape(50))
                                                                .background(Color(0xFF27272A))
                                                                .clickable { uiState.videos.firstOrNull()?.let { onVideoClick(it, uiState.videos) } }
                                                                .padding(horizontal = 12.dp, vertical = 6.dp)
                                                        ) {
                                                            Row(
                                                                verticalAlignment = Alignment.CenterVertically,
                                                                horizontalArrangement = Arrangement.spacedBy(4.dp)
                                                            ) {
                                                                Icon(
                                                                    imageVector = Icons.Default.PlayArrow,
                                                                    contentDescription = "Play All",
                                                                    tint = Color.White,
                                                                    modifier = Modifier.size(15.dp)
                                                                )
                                                                Text(
                                                                    text = "Play All",
                                                                    color = Color.White,
                                                                    fontSize = 11.5.sp,
                                                                    fontWeight = FontWeight.SemiBold
                                                                )
                                                            }
                                                        }
                                                    }
                                                }
                                            }
                                        }

                                        // Videos in Folder (Highlighted last played video)
                                        if (uiState.videos.isEmpty()) {
                                            item {
                                                Box(
                                                    modifier = Modifier
                                                        .fillMaxWidth()
                                                        .padding(top = 40.dp),
                                                    contentAlignment = Alignment.Center
                                                ) {
                                                    Text("No videos in this folder", color = Color(0xFF71717A), fontSize = 14.sp)
                                                }
                                            }
                                        } else if (isGridMode) {
                                            val videoChunks = uiState.videos.chunked(2)
                                            items(videoChunks) { pair ->
                                                Row(
                                                    modifier = Modifier.fillMaxWidth(),
                                                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                                                ) {
                                                    Box(modifier = Modifier.weight(1f)) {
                                                        val isLast1 = (pair[0].id == uiState.lastPlayedInFolder?.id)
                                                        VideoCard(
                                                            video = pair[0],
                                                            onClick = { onVideoClick(pair[0], uiState.videos) },
                                                            onMoveToVault = { viewModel.moveToVault(pair[0]) },
                                                            isGridMode = true,
                                                            isLastPlayed = isLast1
                                                        )
                                                    }
                                                    if (pair.size > 1) {
                                                        Box(modifier = Modifier.weight(1f)) {
                                                            val isLast2 = (pair[1].id == uiState.lastPlayedInFolder?.id)
                                                            VideoCard(
                                                                video = pair[1],
                                                                onClick = { onVideoClick(pair[1], uiState.videos) },
                                                                onMoveToVault = { viewModel.moveToVault(pair[1]) },
                                                                isGridMode = true,
                                                                isLastPlayed = isLast2
                                                            )
                                                        }
                                                    } else {
                                                        Spacer(modifier = Modifier.weight(1f))
                                                    }
                                                }
                                            }
                                        } else {
                                            items(uiState.videos) { video ->
                                                val isLast = (video.id == uiState.lastPlayedInFolder?.id)
                                                VideoCard(
                                                    video = video,
                                                    onClick = { onVideoClick(video, uiState.videos) },
                                                    onMoveToVault = { viewModel.moveToVault(video) },
                                                    isGridMode = false,
                                                    isLastPlayed = isLast
                                                )
                                            }
                                        }
                                    } else {
                                        // Folder Directory Root
                                        if (uiState.folders.isEmpty()) {
                                            item {
                                                Box(
                                                    modifier = Modifier
                                                        .fillMaxWidth()
                                                        .padding(top = 40.dp),
                                                    contentAlignment = Alignment.Center
                                                ) {
                                                    Text("No video folders found", color = Color(0xFF71717A), fontSize = 14.sp)
                                                }
                                            }
                                        } else if (isGridMode) {
                                            val folderChunks = uiState.folders.chunked(2)
                                            items(folderChunks) { pair ->
                                                Row(
                                                    modifier = Modifier.fillMaxWidth(),
                                                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                                                ) {
                                                    Box(modifier = Modifier.weight(1f)) {
                                                        FolderCard(
                                                            folder = pair[0],
                                                            onClick = { viewModel.selectFolder(pair[0]) },
                                                            isGridMode = true
                                                        )
                                                    }
                                                    if (pair.size > 1) {
                                                        Box(modifier = Modifier.weight(1f)) {
                                                            FolderCard(
                                                                folder = pair[1],
                                                                onClick = { viewModel.selectFolder(pair[1]) },
                                                                isGridMode = true
                                                            )
                                                        }
                                                    } else {
                                                        Spacer(modifier = Modifier.weight(1f))
                                                    }
                                                }
                                            }
                                        } else {
                                            items(uiState.folders) { folder ->
                                                FolderCard(
                                                    folder = folder,
                                                    onClick = { viewModel.selectFolder(folder) },
                                                    isGridMode = false
                                                )
                                            }
                                        }
                                    }
                                }
                            }

                            // 4. Circular Floating Action Button: Play Last Video Played (inside Folder or Globally)
                            val resumeTargetVideo = if (uiState.selectedFolder != null) {
                                uiState.lastPlayedInFolder ?: uiState.videos.firstOrNull()
                            } else {
                                uiState.lastPlayedOverall ?: uiState.videos.firstOrNull()
                            }

                            if (resumeTargetVideo != null && uiState.searchQuery.isBlank()) {
                                Box(
                                    modifier = Modifier
                                        .fillMaxSize()
                                        .padding(bottom = 20.dp, end = 20.dp),
                                    contentAlignment = Alignment.BottomEnd
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .size(56.dp)
                                            .clip(CircleShape)
                                            .background(Color(0xFF38BDF8))
                                            .clickable {
                                                val playlist = if (uiState.selectedFolder != null) uiState.videos else listOf(resumeTargetVideo)
                                                onVideoClick(resumeTargetVideo, playlist)
                                            },
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.PlayArrow,
                                            contentDescription = "Play Last Video",
                                            tint = Color.White,
                                            modifier = Modifier.size(32.dp)
                                        )
                                    }
                                }
                            }
                        }
                    }

                    LibraryTab.RECENT -> {
                        LazyColumn(
                            contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            items(uiState.recentHistory) { historyItem ->
                                val matchedVideo = uiState.videos.firstOrNull { it.contentUri == historyItem.mediaUri }
                                    ?: VideoItem(
                                        id = historyItem.mediaUri.hashCode().toLong(),
                                        title = historyItem.title,
                                        displayName = historyItem.title,
                                        path = "",
                                        contentUri = historyItem.mediaUri,
                                        durationMs = historyItem.durationMs,
                                        sizeBytes = 0L,
                                        dateAdded = historyItem.lastWatchedTimestamp,
                                        width = 1920,
                                        height = 1080,
                                        folderName = "Recent",
                                        folderPath = "",
                                        lastPlayedPositionMs = historyItem.lastPositionMs
                                    )

                                VideoCard(
                                    video = matchedVideo,
                                    onClick = { onVideoClick(matchedVideo, listOf(matchedVideo)) },
                                    onMoveToVault = { viewModel.moveToVault(matchedVideo) },
                                    isGridMode = false
                                )
                            }
                        }
                    }

                    LibraryTab.VAULT -> {
                        PrivateVaultScreen(
                            vaultItems = uiState.vaultItems,
                            onPlayVaultItem = { item ->
                                val vaultVideo = VideoItem(
                                    id = item.id.toLong(),
                                    title = item.title,
                                    displayName = item.title,
                                    path = item.vaultPath,
                                    contentUri = Uri.fromFile(java.io.File(item.vaultPath)).toString(),
                                    durationMs = item.durationMs,
                                    sizeBytes = item.sizeBytes,
                                    dateAdded = item.addedTimestamp,
                                    width = 1920,
                                    height = 1080,
                                    folderName = "Private Vault",
                                    folderPath = ""
                                )
                                onVideoClick(vaultVideo, listOf(vaultVideo))
                            }
                        )
                    }

                    LibraryTab.SETTINGS -> {
                        SettingsScreen(
                            viewModel = viewModel,
                            onBackClick = { viewModel.setTab(LibraryTab.VIDEOS) }
                        )
                    }
                }
            }
        }
    }
}
}
