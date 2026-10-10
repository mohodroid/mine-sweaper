package com.mohdroid.compose.component

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.role
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp


@Composable
fun SimpleButton(
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit,
    enable: Boolean = true,
    accessibilityLabel: String? = null,
    onClicked: () -> Unit
) {
    Box(
        modifier = modifier.then(
            Modifier
                .fillMaxWidth()
                .semantics {
                    role = Role.Button
                    accessibilityLabel?.let { contentDescription = it }
                }
                .clickable(enabled = enable, onClick = onClicked)
        ),
        contentAlignment = Alignment.Center
    )
    {
        content()
    }
}
