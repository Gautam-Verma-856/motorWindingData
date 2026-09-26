package com.example.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Block
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun RejectionReasonCard(
    rejectionReason: String,
    reviewedByName: String = "",
    reviewedAt: Long = 0L,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier
            .fillMaxWidth()
            .testTag("rejection_reason_card"),
        shape = RoundedCornerShape(14.dp),
        color = MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.25f),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.error.copy(alpha = 0.4f))
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Rounded.Block,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.error,
                    modifier = Modifier.size(16.dp)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = "Reason for rejection",
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.error,
                    modifier = Modifier.testTag("rejection_reason_heading")
                )
            }
            Spacer(modifier = Modifier.height(6.dp))
            val displayReason = if (rejectionReason.isNotBlank()) rejectionReason.trim() else "No reason was provided."
            Text(
                text = displayReason,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurface,
                modifier = Modifier.testTag("rejection_reason_text")
            )
            if (reviewedByName.isNotBlank() || reviewedAt > 0L) {
                Spacer(modifier = Modifier.height(6.dp))
                val reviewInfo = buildString {
                    if (reviewedByName.isNotBlank()) append("Reviewed by $reviewedByName")
                    if (reviewedAt > 0L) {
                        val dateStr = SimpleDateFormat("dd MMM yyyy, hh:mm a", Locale.getDefault()).format(Date(reviewedAt))
                        if (isNotEmpty()) append(" • ")
                        append(dateStr)
                    }
                }
                Text(
                    text = reviewInfo,
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.testTag("rejection_review_details")
                )
            }
        }
    }
}
