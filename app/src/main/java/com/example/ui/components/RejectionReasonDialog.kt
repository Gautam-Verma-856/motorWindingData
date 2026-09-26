package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Block
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties

@Composable
fun RejectionReasonDialog(
    motorTitle: String = "Submission",
    onConfirm: (reason: String) -> Unit,
    onDismiss: () -> Unit
) {
    var reason by remember { mutableStateOf("") }
    var hasAttemptedSubmit by remember { mutableStateOf(false) }
    val isBlank = reason.trim().isEmpty()

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(dismissOnBackPress = true, dismissOnClickOutside = false)
    ) {
        Surface(
            shape = RoundedCornerShape(24.dp),
            color = MaterialTheme.colorScheme.surface,
            tonalElevation = 6.dp,
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Column(
                modifier = Modifier.padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Box(
                    modifier = Modifier
                        .size(52.dp)
                        .background(MaterialTheme.colorScheme.errorContainer, CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Rounded.Block,
                        contentDescription = "Reject",
                        tint = MaterialTheme.colorScheme.error,
                        modifier = Modifier.size(28.dp)
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                Text(
                    text = "Reject Submission",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface,
                    textAlign = TextAlign.Center
                )

                Spacer(modifier = Modifier.height(6.dp))

                Text(
                    text = "Please enter the reason for rejecting this motor submission. This reason will be displayed to the user.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = TextAlign.Center
                )

                Spacer(modifier = Modifier.height(16.dp))

                OutlinedTextField(
                    value = reason,
                    onValueChange = { reason = it },
                    label = { Text("Rejection Reason") },
                    placeholder = { Text("e.g. Invalid wire gauge or incomplete turn data...") },
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("rejection_reason_input"),
                    shape = RoundedCornerShape(12.dp),
                    minLines = 3,
                    maxLines = 5,
                    isError = hasAttemptedSubmit && isBlank,
                    supportingText = {
                        if (hasAttemptedSubmit && isBlank) {
                            Text(
                                text = "Rejection reason is required",
                                color = MaterialTheme.colorScheme.error,
                                modifier = Modifier.testTag("rejection_reason_error")
                            )
                        } else {
                            Text(
                                text = "${reason.trim().length} characters (required)",
                                style = MaterialTheme.typography.labelSmall
                            )
                        }
                    }
                )

                Spacer(modifier = Modifier.height(20.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    OutlinedButton(
                        onClick = onDismiss,
                        modifier = Modifier
                            .weight(1f)
                            .testTag("cancel_rejection_button"),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text("Cancel")
                    }

                    Button(
                        onClick = {
                            hasAttemptedSubmit = true
                            if (!isBlank) {
                                onConfirm(reason.trim())
                            }
                        },
                        enabled = !isBlank,
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.error,
                            contentColor = MaterialTheme.colorScheme.onError,
                            disabledContainerColor = MaterialTheme.colorScheme.error.copy(alpha = 0.38f),
                            disabledContentColor = MaterialTheme.colorScheme.onError.copy(alpha = 0.38f)
                        ),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier
                            .weight(1.3f)
                            .testTag("confirm_rejection_button")
                    ) {
                        Text("Confirm Rejection", fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}
