package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Album
import androidx.compose.material.icons.filled.Folder
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.MusicNote
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.PlaylistPlay
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.role
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.NavigationTab

private data class NavItemData(
    val tab: NavigationTab,
    val label: String,
    val icon: ImageVector,
    val tag: String
)

private val NAV_ITEMS = listOf(
    NavItemData(NavigationTab.HOME, "Início", Icons.Default.Home, "tab_home"),
    NavItemData(NavigationTab.SONGS, "Músicas", Icons.Default.MusicNote, "tab_songs"),
    NavItemData(NavigationTab.FOLDERS, "Pastas", Icons.Default.Folder, "tab_folders"),
    NavItemData(NavigationTab.ALBUMS, "Álbuns", Icons.Default.Album, "tab_albums"),
    NavItemData(NavigationTab.ARTISTS, "Artistas", Icons.Default.Person, "tab_artists"),
    NavItemData(NavigationTab.PLAYLISTS, "Playlists", Icons.Default.PlaylistPlay, "tab_playlists")
)

@Composable
fun SonoraBottomNavigation(
    currentTab: NavigationTab,
    isSearchActive: Boolean,
    onTabSelected: (NavigationTab) -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        color = MaterialTheme.colorScheme.surface,
        tonalElevation = 3.dp,
        modifier = modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.fillMaxWidth()) {
            HorizontalDivider(
                thickness = 0.5.dp,
                color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.25f)
            )

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(64.dp)
                    .padding(horizontal = 2.dp),
                horizontalArrangement = Arrangement.SpaceEvenly,
                verticalAlignment = Alignment.CenterVertically
            ) {
                NAV_ITEMS.forEach { item ->
                    val isSelected = currentTab == item.tab && !isSearchActive

                    Column(
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxHeight()
                            .semantics { role = Role.Tab }
                            .clickable(onClick = { onTabSelected(item.tab) })
                            .testTag(item.tag),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        // Compact pill indicator around icon
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(14.dp))
                                .background(
                                    if (isSelected) MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.65f)
                                    else Color.Transparent
                                )
                                .padding(horizontal = 10.dp, vertical = 3.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = item.icon,
                                contentDescription = item.label,
                                tint = if (isSelected) MaterialTheme.colorScheme.primary
                                else MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.75f),
                                modifier = Modifier.size(20.dp)
                            )
                        }

                        Spacer(modifier = Modifier.height(2.dp))

                        // Label with carefully controlled typography to guarantee NO truncations or overflows
                        Text(
                            text = item.label,
                            style = MaterialTheme.typography.labelSmall.copy(
                                fontSize = 10.5.sp,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                letterSpacing = (-0.1).sp,
                                lineHeight = 12.sp
                            ),
                            maxLines = 1,
                            softWrap = false,
                            overflow = TextOverflow.Clip,
                            color = if (isSelected) MaterialTheme.colorScheme.primary
                            else MaterialTheme.colorScheme.onSurfaceVariant,
                            textAlign = TextAlign.Center
                        )
                    }
                }
            }
        }
    }
}
