package com.playme.player.feature.player.ui

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Check
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

data class ChoiceItem(
    val id: String,
    val title: String,
    val desc: String? = null
)

@Composable
fun SettingsChoiceSubView(
    title: String,
    items: List<ChoiceItem>,
    selectedId: String,
    onSelect: (String) -> Unit,
    onBack: () -> Unit,
    extraSectionTitle: String? = null,
    extraItems: List<ChoiceItem>? = null,
    extraSelectedId: String? = null,
    onExtraSelect: ((String) -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier.fillMaxWidth()) {
        SettingsSubViewHeader(title = title, onBack = onBack)

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 12.dp, vertical = 10.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            SettingsMobileCard {
                items.forEach { item ->
                    ChoiceRow(
                        item = item,
                        isSelected = item.id == selectedId,
                        onClick = { onSelect(item.id) }
                    )
                }
            }

            if (extraSectionTitle != null && extraItems != null && onExtraSelect != null) {
                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    Text(
                        text = extraSectionTitle,
                        color = Color(0xFF64748B),
                        fontSize = 9.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 0.8.sp,
                        modifier = Modifier.padding(start = 2.dp)
                    )
                    SettingsMobileCard {
                        extraItems.forEach { item ->
                            ChoiceRow(
                                item = item,
                                isSelected = item.id == extraSelectedId,
                                onClick = { onExtraSelect(item.id) }
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun ChoiceRow(
    item: ChoiceItem,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(horizontal = 12.dp, vertical = 8.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(
            modifier = Modifier.weight(1f).padding(end = 8.dp),
            verticalArrangement = Arrangement.spacedBy(2.dp)
        ) {
            Text(
                text = item.title,
                color = if (isSelected) Color(0xFF818CF8) else Color(0xFFE2E8F0),
                fontSize = 12.sp,
                fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Medium
            )
            item.desc?.let { desc ->
                Text(
                    text = desc,
                    color = Color(0xFF64748B),
                    fontSize = 9.5.sp,
                    lineHeight = 12.sp
                )
            }
        }
        if (isSelected) {
            Icon(
                imageVector = Icons.Rounded.Check,
                contentDescription = "Selected",
                tint = Color(0xFF818CF8),
                modifier = Modifier.size(16.dp)
            )
        }
    }
}
