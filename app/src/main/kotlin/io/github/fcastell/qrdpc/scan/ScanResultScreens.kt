package io.github.fcastell.qrdpc.scan

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import io.github.fcastell.qrdpc.payload.QrPayload
import io.github.fcastell.qrdpc.payload.displayString

@Composable
fun ConfirmationScreen(
    payload: QrPayload,
    onRescan: () -> Unit,
    onApply: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(modifier = modifier.fillMaxSize().padding(16.dp)) {
        Text("Target app", style = MaterialTheme.typography.labelMedium)
        Text(payload.packageName, style = MaterialTheme.typography.titleMedium)
        Text(
            "Restrictions (${payload.restrictions.size})",
            style = MaterialTheme.typography.labelMedium,
            modifier = Modifier.padding(top = 16.dp),
        )
        LazyColumn(modifier = Modifier.weight(1f).padding(top = 8.dp)) {
            items(payload.restrictions) { restriction ->
                Text("${restriction.key}: ${restriction.type.jsonName} = ${restriction.value.displayString()}")
            }
        }
        Row(
            modifier = Modifier.fillMaxWidth().padding(top = 16.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            OutlinedButton(onClick = onRescan) { Text("Rescan") }
            Button(onClick = onApply) { Text("Apply") }
        }
    }
}

@Composable
fun ErrorScreen(
    message: String,
    onRescan: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier.fillMaxSize().padding(16.dp),
        verticalArrangement = Arrangement.Center,
    ) {
        Text(message, style = MaterialTheme.typography.bodyLarge)
        Button(onClick = onRescan, modifier = Modifier.padding(top = 16.dp)) { Text("Rescan") }
    }
}

@Composable
fun LoadingScreen(modifier: Modifier = Modifier) {
    Box(modifier = modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        CircularProgressIndicator()
    }
}

@Composable
fun SuccessScreen(
    onScanAnother: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier.fillMaxSize().padding(16.dp),
        verticalArrangement = Arrangement.Center,
    ) {
        Text("Restrictions applied.", style = MaterialTheme.typography.bodyLarge)
        Button(onClick = onScanAnother, modifier = Modifier.padding(top = 16.dp)) { Text("Scan another") }
    }
}
