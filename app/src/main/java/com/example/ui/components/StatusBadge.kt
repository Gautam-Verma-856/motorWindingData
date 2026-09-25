package com.example.ui.components

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Block
import androidx.compose.material.icons.rounded.CheckCircle
import androidx.compose.material.icons.rounded.HourglassEmpty
import androidx.compose.material.icons.rounded.HelpOutline
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp

/**
 * Standard Status Badge for Motor Winding Data.
 * Requirement A.4:
 * - pending  -> "Pending Admin Approval"
 * - approved -> "Approved"
 * - rejected -> "Rejected"
 */
@Composable
fun StatusBadge(status: String, modifier: Modifier = Modifier) {
    val clean = status.trim().lowercase()
    val (label, color, icon) = when (clean) {
        "pending" -> Triple("Pending Admin Approval", Color(0xFFF59E0B), Icons.Rounded.HourglassEmpty)
        "approved" -> Triple("Approved", Color(0xFF10B981), Icons.Rounded.CheckCircle)
        "rejected" -> Triple("Rejected", MaterialTheme.colorScheme.error, Icons.Rounded.Block)
        else -> Triple(if (status.isBlank()) "Unknown" else status, MaterialTheme.colorScheme.onSurfaceVariant, Icons.Rounded.HelpOutline)
    }

    Surface(
        shape = RoundedCornerShape(12.dp),
        color = color.copy(alpha = 0.15f),
        modifier = modifier.padding(start = 8.dp)
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(icon, contentDescription = null, tint = color, modifier = Modifier.size(12.dp))
            Spacer(Modifier.width(4.dp))
            Text(
                text = label,
                style = MaterialTheme.typography.labelSmall,
                fontWeight = FontWeight.Bold,
                color = color
            )
        }
    }
}
