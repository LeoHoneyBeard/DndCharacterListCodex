package com.vinni.dndcharacterlist.feature.character.editor.presentation

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.AssistChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.TextButton
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CharacterEditorScreen(
    state: CharacterEditorUiState,
    onBack: () -> Unit,
    onValueChange: (CharacterEditorUiState.() -> CharacterEditorUiState) -> Unit,
    onSave: () -> Unit,
    onDelete: () -> Unit
) {
    Scaffold(
        topBar = {
            TopAppBar(
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back"
                        )
                    }
                },
                title = {
                    Text(if (state.characterId == null) "New character" else "Edit character")
                }
            )
        }
    ) { paddingValues ->
        if (state.isLoading) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator()
            }
        } else {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .padding(16.dp)
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                EditorSection(
                    title = "Identity",
                    subtitle = "Start with the essentials and use presets where they help."
                ) {
                    OutlinedTextField(
                        modifier = Modifier.fillMaxWidth(),
                        value = state.name,
                        onValueChange = { value -> onValueChange { copy(name = value) } },
                        label = { Text("Name") },
                        supportingText = { state.nameError?.let { Text(it) } },
                        isError = state.nameError != null,
                        singleLine = true
                    )
                    PresetTextField(
                        value = state.characterClass,
                        label = "Class",
                        presets = DndEditorPresets.Classes,
                        onValueChange = { value -> onValueChange { copy(characterClass = value) } }
                    )
                    OutlinedTextField(
                        modifier = Modifier.fillMaxWidth(),
                        value = state.subclass,
                        onValueChange = { value -> onValueChange { copy(subclass = value) } },
                        label = { Text("Subclass") },
                        supportingText = { Text("Optional, but useful once the class is chosen.") },
                        singleLine = true
                    )
                    PresetTextField(
                        value = state.race,
                        label = "Race",
                        presets = DndEditorPresets.Races,
                        onValueChange = { value -> onValueChange { copy(race = value) } }
                    )
                    PresetTextField(
                        value = state.alignment,
                        label = "Alignment",
                        presets = DndEditorPresets.Alignments,
                        onValueChange = { value -> onValueChange { copy(alignment = value) } }
                    )
                    PresetTextField(
                        value = state.background,
                        label = "Background",
                        presets = DndEditorPresets.Backgrounds,
                        onValueChange = { value -> onValueChange { copy(background = value) } }
                    )
                }

                EditorSection(
                    title = "Progression",
                    subtitle = "Level drives proficiency bonus automatically."
                ) {
                    OutlinedTextField(
                        modifier = Modifier.fillMaxWidth(),
                        value = state.level,
                        onValueChange = { value ->
                            onValueChange { copy(level = value.filter(Char::isDigit)) }
                        },
                        label = { Text("Level") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        supportingText = {
                            Text(state.levelError ?: "Proficiency bonus: +${state.proficiencyBonus}")
                        },
                        isError = state.levelError != null,
                        singleLine = true
                    )
                }

                EditorSection(title = "Combat") {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        NumberField(
                            modifier = Modifier.weight(1f),
                            value = state.armorClass,
                            label = "Armor Class",
                            error = state.armorClassError,
                            onValueChange = { value -> onValueChange { copy(armorClass = value) } }
                        )
                        NumberField(
                            modifier = Modifier.weight(1f),
                            value = state.hitPoints,
                            label = "Hit Points",
                            error = state.hitPointsError,
                            onValueChange = { value -> onValueChange { copy(hitPoints = value) } }
                        )
                    }
                }

                EditorSection(
                    title = "Ability Scores",
                    subtitle = state.abilityScoreError ?: "Keep scores within the standard 1-30 D&D range."
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        AbilityScoreField(
                            modifier = Modifier.weight(1f),
                            state = state,
                            label = "STR",
                            value = state.strength,
                            onValueChange = { value -> onValueChange { copy(strength = value) } }
                        )
                        AbilityScoreField(
                            modifier = Modifier.weight(1f),
                            state = state,
                            label = "DEX",
                            value = state.dexterity,
                            onValueChange = { value -> onValueChange { copy(dexterity = value) } }
                        )
                        AbilityScoreField(
                            modifier = Modifier.weight(1f),
                            state = state,
                            label = "CON",
                            value = state.constitution,
                            onValueChange = { value -> onValueChange { copy(constitution = value) } }
                        )
                    }
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        AbilityScoreField(
                            modifier = Modifier.weight(1f),
                            state = state,
                            label = "INT",
                            value = state.intelligence,
                            onValueChange = { value -> onValueChange { copy(intelligence = value) } }
                        )
                        AbilityScoreField(
                            modifier = Modifier.weight(1f),
                            state = state,
                            label = "WIS",
                            value = state.wisdom,
                            onValueChange = { value -> onValueChange { copy(wisdom = value) } }
                        )
                        AbilityScoreField(
                            modifier = Modifier.weight(1f),
                            state = state,
                            label = "CHA",
                            value = state.charisma,
                            onValueChange = { value -> onValueChange { copy(charisma = value) } }
                        )
                    }
                }

                EditorSection(title = "Notes", subtitle = "Anything story, equipment, goals, or reminders.") {
                    OutlinedTextField(
                        modifier = Modifier.fillMaxWidth(),
                        value = state.notes,
                        onValueChange = { value -> onValueChange { copy(notes = value) } },
                        label = { Text("Notes") },
                        minLines = 5
                    )
                }

                state.validationMessage?.let { message ->
                    Text(
                        text = message,
                        color = MaterialTheme.colorScheme.error
                    )
                }
                Button(
                    modifier = Modifier.fillMaxWidth(),
                    enabled = state.isSaveEnabled,
                    onClick = onSave
                ) {
                    Text(if (state.isSaving) "Saving..." else "Save character")
                }
                if (state.canDelete) {
                    TextButton(
                        modifier = Modifier.fillMaxWidth(),
                        enabled = !state.isSaving,
                        onClick = onDelete
                    ) {
                        Text("Delete character")
                    }
                }
            }
        }
    }
}

@Composable
private fun NumberField(
    modifier: Modifier,
    value: String,
    label: String,
    error: String? = null,
    onValueChange: (String) -> Unit
) {
    OutlinedTextField(
        modifier = modifier,
        value = value,
        onValueChange = { onValueChange(it.filter(Char::isDigit)) },
        label = { Text(label) },
        supportingText = { error?.let { Text(it) } },
        isError = error != null,
        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
        singleLine = true
    )
}

@Composable
private fun AbilityScoreField(
    modifier: Modifier,
    state: CharacterEditorUiState,
    label: String,
    value: String,
    onValueChange: (String) -> Unit
) {
    val score = value.toIntOrNull()
    NumberField(
        modifier = modifier,
        value = value,
        label = "$label ${formatAbilityModifier(state.abilityModifier(value))}",
        error = if (score == null || score !in 1..30) "1-30" else null,
        onValueChange = onValueChange
    )
}

@Composable
private fun EditorSection(
    title: String,
    subtitle: String? = null,
    content: @Composable () -> Unit
) {
    Card(modifier = Modifier.fillMaxWidth()) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold
                )
                subtitle?.let {
                    Text(
                        text = it,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
            content()
        }
    }
}

@Composable
private fun PresetTextField(
    value: String,
    label: String,
    presets: List<String>,
    onValueChange: (String) -> Unit
) {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        OutlinedTextField(
            modifier = Modifier.fillMaxWidth(),
            value = value,
            onValueChange = onValueChange,
            label = { Text(label) },
            singleLine = true
        )
        ChipFlowRow(items = presets) { preset ->
            AssistChip(
                onClick = { onValueChange(preset) },
                label = { Text(preset) }
            )
        }
    }
}

@Composable
private fun ChipFlowRow(
    items: List<String>,
    content: @Composable (String) -> Unit
) {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        items.chunked(3).forEach { rowItems ->
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                rowItems.forEach { item ->
                    Box(modifier = Modifier.weight(1f, fill = false)) {
                        content(item)
                    }
                }
                repeat(3 - rowItems.size) {
                    SpacerCell(modifier = Modifier.weight(1f, fill = false))
                }
            }
        }
    }
}

@Composable
private fun SpacerCell(modifier: Modifier) {
    Box(modifier = modifier.widthIn(min = 0.dp))
}

private fun formatAbilityModifier(modifier: Int?): String {
    modifier ?: return "-"
    return if (modifier >= 0) "+$modifier" else modifier.toString()
}

private object DndEditorPresets {
    val Classes = listOf("Fighter", "Wizard", "Rogue", "Cleric", "Paladin", "Ranger")
    val Races = listOf("Human", "Elf", "Dwarf", "Halfling", "Tiefling", "Dragonborn")
    val Alignments = listOf(
        "Lawful Good",
        "Neutral Good",
        "Chaotic Good",
        "Lawful Neutral",
        "True Neutral",
        "Chaotic Neutral"
    )
    val Backgrounds = listOf("Soldier", "Sage", "Acolyte", "Criminal", "Noble", "Outlander")
}

