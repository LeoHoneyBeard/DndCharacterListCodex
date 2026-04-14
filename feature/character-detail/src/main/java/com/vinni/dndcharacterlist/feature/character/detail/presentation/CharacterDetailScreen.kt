package com.vinni.dndcharacterlist.feature.character.detail.presentation

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.AssistChip
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CharacterDetailScreen(
    state: CharacterDetailUiState,
    onBack: () -> Unit,
    onEdit: () -> Unit
) {
    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back"
                        )
                    }
                },
                title = { Text("Character") }
            )
        }
    ) { paddingValues ->
        when {
            state.isLoading -> {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(paddingValues),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator()
                }
            }

            state.character == null -> {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(paddingValues)
                        .padding(24.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "Character not found.",
                        textAlign = TextAlign.Center
                    )
                }
            }

            else -> {
                val character = state.character
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(paddingValues)
                        .padding(horizontal = 16.dp, vertical = 12.dp)
                        .verticalScroll(rememberScrollState()),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Card(modifier = Modifier.fillMaxWidth()) {
                        Column(
                            modifier = Modifier.padding(16.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Text(
                                text = character.name,
                                style = MaterialTheme.typography.headlineMedium,
                                fontWeight = FontWeight.Bold
                            )
                            Text(text = character.subtitle)
                            if (character.alignment.isNotBlank()) {
                                AssistChip(
                                    onClick = {},
                                    label = { Text(character.alignment) }
                                )
                            }
                            Button(
                                modifier = Modifier.fillMaxWidth(),
                                onClick = onEdit
                            ) {
                                Text("Edit character")
                            }
                        }
                    }

                    InfoSection(title = "Combat") {
                        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                            MetricCard(
                                modifier = Modifier.weight(1f),
                                label = "Armor Class",
                                value = character.armorClass.toString()
                            )
                            MetricCard(
                                modifier = Modifier.weight(1f),
                                label = "Hit Points",
                                value = character.hitPoints.toString()
                            )
                        }
                    }

                    InfoSection(title = "Ability Scores") {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            character.stats.take(3).forEach { stat ->
                                MetricCard(
                                    modifier = Modifier.weight(1f),
                                    label = stat.label,
                                    value = stat.value.toString()
                                )
                            }
                        }
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            character.stats.drop(3).forEach { stat ->
                                MetricCard(
                                    modifier = Modifier.weight(1f),
                                    label = stat.label,
                                    value = stat.value.toString()
                                )
                            }
                        }
                    }

                    if (character.background.isNotBlank()) {
                        InfoSection(title = "Background") {
                            Text(text = character.background)
                        }
                    }

                    if (character.notes.isNotBlank()) {
                        InfoSection(title = "Notes") {
                            Text(text = character.notes)
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun InfoSection(
    title: String,
    content: @Composable () -> Unit
) {
    Card(modifier = Modifier.fillMaxWidth()) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold
            )
            content()
        }
    }
}

@Composable
private fun MetricCard(
    modifier: Modifier,
    label: String,
    value: String
) {
    Card(modifier = modifier.widthIn(min = 0.dp)) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 12.dp, horizontal = 8.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Text(
                text = value,
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = label,
                style = MaterialTheme.typography.bodySmall,
                textAlign = TextAlign.Center
            )
        }
    }
}

