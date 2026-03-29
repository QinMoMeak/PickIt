package com.pickit.app.presentation.component

import androidx.compose.material3.AssistChip
import androidx.compose.material3.AssistChipDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import com.pickit.app.domain.model.ProductStatus
import com.pickit.app.presentation.theme.ActionAccent
import com.pickit.app.presentation.theme.BrandPrimary
import com.pickit.app.presentation.theme.ErrorStrong

@Composable
fun StatusChip(
    status: ProductStatus,
    onClick: (() -> Unit)? = null,
) {
    val container = when (status) {
        ProductStatus.NOT_PURCHASED -> BrandPrimary.copy(alpha = 0.12f)
        ProductStatus.IN_USE -> MaterialTheme.colorScheme.secondary.copy(alpha = 0.12f)
        ProductStatus.USED_UP -> MaterialTheme.colorScheme.surfaceVariant
        ProductStatus.DAMAGED -> ErrorStrong.copy(alpha = 0.12f)
        ProductStatus.ABANDONED -> ActionAccent.copy(alpha = 0.12f)
    }

    AssistChip(
        onClick = { onClick?.invoke() },
        label = { Text(status.label) },
        colors = AssistChipDefaults.assistChipColors(containerColor = container),
    )
}
