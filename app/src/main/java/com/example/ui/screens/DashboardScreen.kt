package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CallMerge
import androidx.compose.material.icons.filled.CallSplit
import androidx.compose.material.icons.filled.Compress
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.FolderZip
import androidx.compose.material.icons.filled.Image
import androidx.compose.material.icons.filled.PictureAsPdf
import androidx.compose.material.icons.filled.Savings
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.PdfDocumentEntity
import com.example.ui.PdfViewModel
import com.example.ui.components.ActionTileCard
import com.example.ui.components.PdfFileListItem
import com.example.ui.theme.SleekBlueContainer
import com.example.ui.theme.SleekBlueOnContainer
import com.example.ui.theme.SleekOrangeContainer
import com.example.ui.theme.SleekOrangeOnContainer
import com.example.ui.theme.SleekPurpleContainer
import com.example.ui.theme.SleekPurpleOnContainer
import com.example.ui.theme.SleekPurplePrimary
import com.example.ui.theme.SleekVioletContainer
import com.example.ui.theme.SleekVioletOnContainer
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary
import com.example.util.PdfUtils

import androidx.compose.foundation.clickable
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.ExitToApp
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Divider
import androidx.compose.material3.OutlinedButton
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import com.example.data.AuthProvider

import androidx.compose.material.icons.filled.Shield
import androidx.compose.material.icons.filled.VpnLock
import androidx.compose.material.icons.filled.Translate
import com.example.ui.screens.LanguageSelectionDialog
import com.example.ui.screens.SecurityAttestationDialog
import com.example.ui.theme.EmeraldSuccess
import com.example.ui.theme.SleekBorder

@Composable
fun DashboardScreen(
    viewModel: PdfViewModel,
    onNavigateToCompress: () -> Unit,
    onNavigateToMerge: () -> Unit,
    onNavigateToSplit: () -> Unit,
    onNavigateToImageToPdf: () -> Unit,
    onNavigateToFiles: () -> Unit
) {
    val allPdfs by viewModel.allPdfs.collectAsState()
    val recentPdfs by viewModel.recentPdfs.collectAsState()
    val totalSaved by viewModel.totalBytesSaved.collectAsState()
    val currentUser by viewModel.currentUser.collectAsState()
    val securityAttestation by viewModel.securityAttestation.collectAsState()

    val currentLanguage by viewModel.currentLanguage.collectAsState()

    var showProfileDialog by remember { mutableStateOf(false) }
    var showSecurityDialog by remember { mutableStateOf(false) }
    var showLanguageDialog by remember { mutableStateOf(false) }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            Spacer(modifier = Modifier.height(12.dp))
            // Welcome Header matching HTML
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column {
                    Text(
                        text = viewModel.getString("app_title"),
                        style = MaterialTheme.typography.titleLarge.copy(
                            fontWeight = FontWeight.SemiBold,
                            fontSize = 26.sp,
                            letterSpacing = (-0.5).sp
                        ),
                        color = MaterialTheme.colorScheme.onBackground
                    )
                    Text(
                        text = currentUser?.let { "${viewModel.getString("welcome")}, ${it.name}" } ?: viewModel.getString("app_subtitle"),
                        style = MaterialTheme.typography.bodySmall,
                        color = TextSecondary
                    )
                }

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    // Security Defense Quick Badge
                    Box(
                        modifier = Modifier
                            .size(44.dp)
                            .clip(CircleShape)
                            .background(
                                if (securityAttestation.overallSecure) EmeraldSuccess.copy(alpha = 0.15f)
                                else MaterialTheme.colorScheme.error.copy(alpha = 0.15f)
                            )
                            .clickable { showSecurityDialog = true }
                            .testTag("security_status_badge"),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Shield,
                            contentDescription = "4-Layer Defense",
                            tint = if (securityAttestation.overallSecure) EmeraldSuccess else MaterialTheme.colorScheme.error,
                            modifier = Modifier.size(24.dp)
                        )
                    }

                    // Interactive User profile avatar badge
                    Box(
                        modifier = Modifier
                            .size(44.dp)
                            .clip(CircleShape)
                            .background(
                                when (currentUser?.provider) {
                                    AuthProvider.FACEBOOK -> Color(0xFF1877F2)
                                    AuthProvider.GOOGLE -> SleekPurpleContainer
                                    else -> SleekPurpleContainer
                                }
                            )
                            .clickable { showProfileDialog = true }
                            .testTag("user_profile_avatar"),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = currentUser?.avatarInitials ?: "JS",
                            style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                            color = if (currentUser?.provider == AuthProvider.FACEBOOK) Color.White else SleekPurpleOnContainer
                        )
                    }
                }
            }
        }

        // Security & Hardware Binding Defense Status Card
        item {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { showSecurityDialog = true }
                    .testTag("security_defense_status_card"),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(
                    containerColor = if (securityAttestation.overallSecure) SleekPurpleContainer.copy(alpha = 0.5f)
                    else MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.5f)
                ),
                border = androidx.compose.foundation.BorderStroke(
                    1.dp,
                    if (securityAttestation.overallSecure) SleekPurplePrimary.copy(alpha = 0.3f)
                    else MaterialTheme.colorScheme.error.copy(alpha = 0.3f)
                )
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.weight(1f)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(40.dp)
                                .clip(RoundedCornerShape(12.dp))
                                .background(
                                    if (securityAttestation.overallSecure) SleekPurplePrimary
                                    else MaterialTheme.colorScheme.error
                                ),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.Shield,
                                contentDescription = "Defense Status",
                                tint = Color.White,
                                modifier = Modifier.size(22.dp)
                            )
                        }

                        Spacer(modifier = Modifier.width(12.dp))

                        Column {
                            Text(
                                text = "4-Layer Defense Protocol",
                                style = MaterialTheme.typography.titleMedium.copy(
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 15.sp
                                ),
                                color = TextPrimary
                            )
                            Text(
                                text = if (securityAttestation.overallSecure)
                                    "TEE Hardware Key Bound • No VPN Tunnels"
                                else
                                    "Attestation Warning: ${securityAttestation.layer3VpnMessage}",
                                style = MaterialTheme.typography.bodySmall.copy(fontSize = 11.sp),
                                color = TextSecondary,
                                maxLines = 1
                            )
                        }
                    }

                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(8.dp))
                            .background(
                                if (securityAttestation.overallSecure) EmeraldSuccess.copy(alpha = 0.2f)
                                else MaterialTheme.colorScheme.error.copy(alpha = 0.2f)
                            )
                            .padding(horizontal = 10.dp, vertical = 6.dp)
                    ) {
                        Text(
                            text = if (securityAttestation.overallSecure) "ACTIVE" else "ALERT",
                            style = MaterialTheme.typography.labelSmall.copy(
                                fontWeight = FontWeight.Bold,
                                fontSize = 11.sp
                            ),
                            color = if (securityAttestation.overallSecure) EmeraldSuccess else MaterialTheme.colorScheme.error
                        )
                    }
                }
            }
        }

        // Hero Stats Card
        item {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("hero_savings_card"),
                shape = RoundedCornerShape(28.dp),
                colors = CardDefaults.cardColors(containerColor = Color.Transparent)
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(
                            brush = Brush.horizontalGradient(
                                colors = listOf(
                                    Color(0xFF21005D),
                                    Color(0xFF4F378B),
                                    Color(0xFF6750A4)
                                )
                            )
                        )
                        .padding(20.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = Icons.Default.Savings,
                                    contentDescription = "Saved Storage",
                                    tint = Color(0xFFFFD54F),
                                    modifier = Modifier.size(20.dp)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = "TOTAL STORAGE SAVED",
                                    style = MaterialTheme.typography.labelSmall.copy(
                                        letterSpacing = 1.sp,
                                        fontWeight = FontWeight.Bold
                                    ),
                                    color = Color.White.copy(alpha = 0.85f)
                                )
                            }
                            Spacer(modifier = Modifier.height(6.dp))
                            Text(
                                text = PdfUtils.formatFileSize(totalSaved),
                                style = MaterialTheme.typography.headlineLarge.copy(
                                    fontWeight = FontWeight.Black,
                                    fontSize = 32.sp
                                ),
                                color = Color.White
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "${allPdfs.size} PDF document(s) in vault",
                                style = MaterialTheme.typography.bodySmall,
                                color = Color.White.copy(alpha = 0.8f)
                            )
                        }

                        Box(
                            modifier = Modifier
                                .size(60.dp)
                                .clip(CircleShape)
                                .background(Color.White.copy(alpha = 0.15f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.FolderZip,
                                contentDescription = "Vault",
                                tint = Color.White,
                                modifier = Modifier.size(32.dp)
                            )
                        }
                    }
                }
            }
        }

        // Action Grid Section
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                ActionTileCard(
                    title = "Compress",
                    subtitle = "Reduce file size",
                    icon = Icons.Default.Compress,
                    containerColor = SleekPurpleContainer,
                    iconContainerColor = SleekPurpleOnContainer,
                    titleColor = SleekPurpleOnContainer,
                    subtitleColor = SleekPurpleOnContainer.copy(alpha = 0.7f),
                    modifier = Modifier.weight(1f),
                    onClick = onNavigateToCompress
                )

                ActionTileCard(
                    title = "Merge",
                    subtitle = "Combine multiple",
                    icon = Icons.Default.CallMerge,
                    containerColor = SleekBlueContainer,
                    iconContainerColor = SleekBlueOnContainer,
                    titleColor = SleekBlueOnContainer,
                    subtitleColor = SleekBlueOnContainer.copy(alpha = 0.7f),
                    modifier = Modifier.weight(1f),
                    onClick = onNavigateToMerge
                )
            }
        }

        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                ActionTileCard(
                    title = "Split",
                    subtitle = "Extract page range",
                    icon = Icons.Default.CallSplit,
                    containerColor = SleekOrangeContainer,
                    iconContainerColor = SleekOrangeOnContainer,
                    titleColor = SleekOrangeOnContainer,
                    subtitleColor = SleekOrangeOnContainer.copy(alpha = 0.7f),
                    modifier = Modifier.weight(1f),
                    onClick = onNavigateToSplit
                )

                ActionTileCard(
                    title = "Image to PDF",
                    subtitle = "Convert photos to doc",
                    icon = Icons.Default.Image,
                    containerColor = SleekVioletContainer,
                    iconContainerColor = SleekVioletOnContainer,
                    titleColor = SleekVioletOnContainer,
                    subtitleColor = SleekVioletOnContainer.copy(alpha = 0.7f),
                    modifier = Modifier.weight(1f),
                    onClick = onNavigateToImageToPdf
                )
            }
        }

        // Recent Activity Section
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "RECENT ACTIVITY",
                    style = MaterialTheme.typography.labelSmall.copy(
                        fontWeight = FontWeight.SemiBold,
                        letterSpacing = 1.sp
                    ),
                    color = TextSecondary
                )

                TextButton(onClick = onNavigateToFiles) {
                    Text(
                        text = "View all",
                        style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Medium),
                        color = SleekPurplePrimary
                    )
                }
            }
        }

        if (recentPdfs.isEmpty()) {
            item {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 8.dp),
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    border = androidx.compose.foundation.BorderStroke(
                        width = 1.dp,
                        color = MaterialTheme.colorScheme.outline.copy(alpha = 0.3f)
                    )
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Icon(
                            imageVector = Icons.Default.Description,
                            contentDescription = "Empty",
                            tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f),
                            modifier = Modifier.size(40.dp)
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "No PDF files yet",
                            style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Medium),
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = "Try compressing or merging a sample document above!",
                            style = MaterialTheme.typography.bodySmall,
                            color = TextSecondary
                        )
                    }
                }
            }
        } else {
            items(recentPdfs, key = { it.id }) { pdf ->
                PdfFileListItem(
                    pdf = pdf,
                    onView = { viewModel.openPdfViewer(pdf) },
                    onFavoriteToggle = { viewModel.toggleFavorite(pdf) },
                    onDelete = { viewModel.deletePdf(pdf) },
                    onShare = {}
                )
            }
        }

        item {
            Spacer(modifier = Modifier.height(24.dp))
        }
    }

    if (showProfileDialog) {
        AlertDialog(
            onDismissRequest = { showProfileDialog = false },
            title = {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Box(
                        modifier = Modifier
                            .size(64.dp)
                            .clip(CircleShape)
                            .background(
                                when (currentUser?.provider) {
                                    AuthProvider.FACEBOOK -> Color(0xFF1877F2)
                                    else -> SleekPurpleContainer
                                }
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = currentUser?.avatarInitials ?: "JS",
                            style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                            color = if (currentUser?.provider == AuthProvider.FACEBOOK) Color.White else SleekPurpleOnContainer
                        )
                    }
                    Spacer(modifier = Modifier.height(12.dp))
                    Text(
                        text = currentUser?.name ?: "Guest User",
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                    )
                    Text(
                        text = currentUser?.email ?: "guest@pdftoolkit.local",
                        style = MaterialTheme.typography.bodySmall,
                        color = TextSecondary
                    )
                }
            },
            text = {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(8.dp))
                            .background(SleekPurpleContainer.copy(alpha = 0.5f))
                            .padding(horizontal = 12.dp, vertical = 6.dp)
                    ) {
                        Text(
                            text = "Signed in with ${currentUser?.provider?.displayName ?: "Guest"}",
                            style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.SemiBold),
                            color = SleekPurpleOnContainer
                        )
                    }

                    // Profile Language Preference Option
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable {
                                showLanguageDialog = true
                            }
                            .testTag("profile_language_setting_card"),
                        shape = RoundedCornerShape(14.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                        border = androidx.compose.foundation.BorderStroke(1.dp, SleekBorder)
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Box(
                                    modifier = Modifier
                                        .size(36.dp)
                                        .clip(CircleShape)
                                        .background(SleekPurpleContainer),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = currentLanguage.flag,
                                        fontSize = 18.sp
                                    )
                                }
                                Spacer(modifier = Modifier.width(10.dp))
                                Column {
                                    Text(
                                        text = viewModel.getString("language_selection"),
                                        style = MaterialTheme.typography.titleSmall.copy(
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 13.sp
                                        ),
                                        color = TextPrimary
                                    )
                                    Text(
                                        text = "${currentLanguage.nativeName} (${currentLanguage.displayName})",
                                        style = MaterialTheme.typography.bodySmall.copy(fontSize = 11.sp),
                                        color = TextSecondary
                                    )
                                }
                            }

                            Icon(
                                imageVector = Icons.Default.Translate,
                                contentDescription = "Change Language",
                                tint = SleekPurplePrimary,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        showProfileDialog = false
                        viewModel.logout()
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.ExitToApp,
                            contentDescription = "Logout",
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(viewModel.getString("sign_out"))
                    }
                }
            },
            dismissButton = {
                TextButton(onClick = { showProfileDialog = false }) {
                    Text(viewModel.getString("close"))
                }
            }
        )
    }

    if (showSecurityDialog) {
        SecurityAttestationDialog(
            viewModel = viewModel,
            onDismiss = { showSecurityDialog = false }
        )
    }

    if (showLanguageDialog) {
        LanguageSelectionDialog(
            viewModel = viewModel,
            onDismiss = { showLanguageDialog = false }
        )
    }
}
