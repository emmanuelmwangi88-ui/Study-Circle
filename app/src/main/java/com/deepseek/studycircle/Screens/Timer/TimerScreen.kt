package com.deepseek.studycircle.Screens.Timer

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.deepseek.studycircle.data.TimerState
import com.deepseek.studycircle.data.TimerViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TimerScreen(
    initialCircleId: String? = null,
    onBack: () -> Unit = {}
) {
    val viewModel: TimerViewModel = viewModel()
    val state by viewModel.timerState.collectAsState()
    val remainingSeconds by viewModel.remainingSeconds.collectAsState()
    val totalMinutes by viewModel.totalMinutes.collectAsState()
    val selectedSubject by viewModel.selectedSubject.collectAsState()
    val rating by viewModel.productivityRating.collectAsState()
    val notes by viewModel.sessionNotes.collectAsState()

    LaunchedEffect(initialCircleId) {
        if (initialCircleId != null) viewModel.setCircleId(initialCircleId)
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Focus Timer", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, "Back")
                    }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier.fillMaxSize().padding(padding).padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            when (state) {
                TimerState.IDLE -> SetupTimer(totalMinutes, selectedSubject, viewModel)
                TimerState.RUNNING, TimerState.PAUSED -> ActiveTimer(remainingSeconds, totalMinutes, state, viewModel)
                TimerState.COMPLETED -> CompletedTimer(totalMinutes, remainingSeconds, rating, notes, viewModel, onBack)
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ColumnScope.SetupTimer(totalMinutes: Int, selectedSubject: String, viewModel: TimerViewModel) {
    var expanded by remember { mutableStateOf(false) }
    val subjects = listOf("General Study", "Computer Science", "Mathematics", "Languages", "Medicine", "Physics")

    Text("Select Duration", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
    Spacer(Modifier.height(16.dp))
    
    LazyRow(
        modifier = Modifier
            .fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        items(listOf(15, 25, 45, 60, 90, 120)) { mins ->
            DurationChip(mins, totalMinutes == mins) { viewModel.setDuration(mins) }
        }
    }

    Slider(
        value = totalMinutes.toFloat(),
        onValueChange = { viewModel.setDuration(it.toInt()) },
        valueRange = 5f..120f,
        modifier = Modifier
            .padding(vertical = 24.dp)
    )

    ExposedDropdownMenuBox(
        expanded = expanded,
        onExpandedChange = { expanded = it },
        modifier = Modifier.fillMaxWidth()
    ) {
        OutlinedTextField(
            value = selectedSubject.ifEmpty { "Select subject..." },
            onValueChange = {},
            readOnly = true,
            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
            modifier = Modifier.menuAnchor().fillMaxWidth(),
            shape = RoundedCornerShape(12.dp)
        )
        ExposedDropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false }
        ) {
            subjects.forEach { subject ->
                DropdownMenuItem(
                    text = { Text(subject) },
                    onClick = { 
                        viewModel.setSubject(subject)
                        expanded = false 
                    }
                )
            }
        }
    }

    Spacer(Modifier.weight(1f))
    Button(
        onClick = { viewModel.startTimer() },
        modifier = Modifier.fillMaxWidth().height(56.dp),
        shape = RoundedCornerShape(16.dp)
    ) {
        Icon(Icons.Default.PlayArrow, null)
        Text("Start Focus Session", Modifier.padding(start = 8.dp))
    }
}

@Composable
fun DurationChip(minutes: Int, isSelected: Boolean, onClick: () -> Unit) {
    Surface(
        onClick = onClick,
        color = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceVariant,
        shape = RoundedCornerShape(12.dp),
        modifier = Modifier.size(70.dp, 60.dp)
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.Center) {
            Text("$minutes", fontWeight = FontWeight.Bold)
            Text("min", style = MaterialTheme.typography.bodySmall)
        }
    }
}

@Composable
fun ActiveTimer(remainingSeconds: Int, totalMinutes: Int, state: TimerState, viewModel: TimerViewModel) {
    val progress = (remainingSeconds.toFloat() / (totalMinutes * 60f)).coerceIn(0f, 1f)
    val animatedProgress by animateFloatAsState(targetValue = progress, label = "progress")
    
    Box(modifier = Modifier.size(280.dp), contentAlignment = Alignment.Center) {
        CircularProgressIndicator(
            progress = { animatedProgress },
            modifier = Modifier.fillMaxSize(),
            strokeWidth = 10.dp,
            trackColor = MaterialTheme.colorScheme.surfaceVariant
        )
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(String.format("%02d:%02d", remainingSeconds / 60, remainingSeconds % 60), style = MaterialTheme.typography.displayLarge, fontWeight = FontWeight.Bold)
            Text(if (state == TimerState.RUNNING) "Focusing..." else "Paused", color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }

    Spacer(Modifier.height(48.dp))
    Row(horizontalArrangement = Arrangement.spacedBy(24.dp)) {
        TimerButton(Icons.Default.Stop, MaterialTheme.colorScheme.errorContainer) { viewModel.stopTimer() }
        TimerButton(
            if (state == TimerState.RUNNING) Icons.Default.Pause else Icons.Default.PlayArrow,
            MaterialTheme.colorScheme.primary
        ) { if (state == TimerState.RUNNING) viewModel.pauseTimer() else viewModel.resumeTimer() }
    }
}

@Composable
fun TimerButton(icon: ImageVector, containerColor: Color, onClick: () -> Unit) {
    FilledIconButton(onClick = onClick, modifier = Modifier.size(64.dp), colors = IconButtonDefaults.filledIconButtonColors(containerColor)) {
        Icon(icon, null, modifier = Modifier.size(32.dp))
    }
}

@Composable
fun ColumnScope.CompletedTimer(totalMinutes: Int, remainingSeconds: Int, rating: Int, notes: String, viewModel: TimerViewModel, onBack: () -> Unit) {
    Text("Session Complete!", style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Bold)
    Text("Studied for ${totalMinutes - (remainingSeconds / 60)} minutes", Modifier.padding(vertical = 8.dp))

    Row(Modifier.padding(vertical = 24.dp)) {
        (1..5).forEach { i ->
            IconButton(onClick = { viewModel.setProductivityRating(i) }) {
                Icon(Icons.Default.Star, null, tint = if (i <= rating) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceVariant)
            }
        }
    }

    OutlinedTextField(value = notes, onValueChange = { viewModel.setSessionNotes(it) }, label = { Text("Notes") }, modifier = Modifier.fillMaxWidth(), minLines = 3)
    
    Spacer(Modifier.weight(1f))
    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
        OutlinedButton(onClick = { viewModel.resetTimer() }, modifier = Modifier.weight(1f)) { Text("Discard") }
        Button(onClick = { viewModel.saveSession(); onBack() }, modifier = Modifier.weight(2f)) { Text("Save Session") }
    }
}

@Preview(showBackground = true)
@Composable
fun TimerScreenPreview(
    viewModel: TimerViewModel = viewModel()
) {
    TimerScreen()
}
