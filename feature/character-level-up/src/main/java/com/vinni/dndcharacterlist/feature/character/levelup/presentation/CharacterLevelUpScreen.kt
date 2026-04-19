package com.vinni.dndcharacterlist.feature.character.levelup.presentation

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.RadioButton
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
fun CharacterLevelUpScreen(
    state: CharacterLevelUpUiState,
    onBack: () -> Unit,
    onSubclassSelected: (String) -> Unit,
    onApply: () -> Unit
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
                title = { Text("Level Up") }
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

            state.characterId == null -> {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(paddingValues)
                        .padding(24.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = state.blockingMessage ?: "Character not found.",
                        textAlign = TextAlign.Center
                    )
                }
            }

            else -> {
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
                                text = state.characterName,
                                style = MaterialTheme.typography.headlineMedium,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = listOfNotNull(
                                    state.className.takeIf { it.isNotBlank() },
                                    state.currentSubclass.takeIf { it.isNotBlank() }
                                ).joinToString(" | ")
                            )
                            Text(
                                text = "Level ${state.currentLevel} -> ${state.nextLevel}",
                                style = MaterialTheme.typography.titleMedium
                            )
                        }
                    }

                    InfoSection(title = "Hit Points") {
                        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                            MetricCard(
                                modifier = Modifier.weight(1f),
                                label = "Current",
                                value = "${state.currentHitPoints}/${state.currentHitPointsMax}"
                            )
                            MetricCard(
                                modifier = Modifier.weight(1f),
                                label = "Gain",
                                value = "+${state.hitPointIncrease}"
                            )
                            MetricCard(
                                modifier = Modifier.weight(1f),
                                label = "Next",
                                value = "${state.nextHitPoints}/${state.nextHitPointsMax}"
                            )
                        }
                    }

                    state.subclassRequirement?.let { requirement ->
                        InfoSection(title = requirement.title) {
                            Text(
                                text = requirement.description,
                                style = MaterialTheme.typography.bodyMedium
                            )
                            requirement.options.forEach { option ->
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .selectable(
                                            selected = state.selectedSubclassId == option.id,
                                            onClick = { onSubclassSelected(option.id) }
                                        )
                                        .padding(vertical = 4.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                                ) {
                                    RadioButton(
                                        selected = state.selectedSubclassId == option.id,
                                        onClick = null
                                    )
                                    Text(text = option.name)
                                }
                            }
                        }
                    }

                    state.unsupportedRequirements.forEach { requirement ->
                        MessageCard(
                            title = requirement.title,
                            message = requirement.description
                        )
                    }

                    state.blockingMessage?.let { message ->
                        MessageCard(
                            title = "Blocked",
                            message = message
                        )
                    }

                    state.actionErrorMessage?.let { message ->
                        MessageCard(
                            title = "Action required",
                            message = message
                        )
                    }

                    Button(
                        modifier = Modifier.fillMaxWidth(),
                        enabled = state.canApply,
                        onClick = onApply
                    ) {
                        Text(if (state.isApplying) "Applying..." else "Apply level up")
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
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center
            )
            Text(
                text = label,
                style = MaterialTheme.typography.bodySmall,
                textAlign = TextAlign.Center
            )
        }
    }
}

@Composable
private fun MessageCard(
    title: String,
    message: String
) {
    Card(modifier = Modifier.fillMaxWidth()) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold
            )
            Text(text = message)
        }
    }
}
