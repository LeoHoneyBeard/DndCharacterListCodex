@file:OptIn(ExperimentalLayoutApi::class)

package com.vinni.dndcharacterlist.feature.character.list.presentation

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.AssistChip
import androidx.compose.material3.AssistChipDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CharacterListScreen(
    state: kotlinx.coroutines.flow.StateFlow<CharacterListUiState>,
    onAddCharacter: () -> Unit,
    onCharacterClick: (Long) -> Unit,
    onSearchQueryChange: (String) -> Unit,
    onSortModeSelected: (CharacterListSortMode) -> Unit,
    onClassFilterSelected: (String?) -> Unit,
    onRaceFilterSelected: (String?) -> Unit,
    onLevelFilterSelected: (CharacterListLevelFilter) -> Unit
) {
    val uiState by state.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(title = { Text("D&D Characters") })
        },
        floatingActionButton = {
            FloatingActionButton(onClick = onAddCharacter) {
                Text("+")
            }
        }
    ) { paddingValues ->
        when {
            uiState.isLoading -> {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(paddingValues),
                    contentAlignment = Alignment.Center
                ) {
                    Text("Loading characters...")
                }
            }

            uiState.errorMessage != null -> {
                val errorMessage = uiState.errorMessage ?: ""
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(paddingValues)
                        .padding(24.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(errorMessage)
                }
            }

            else -> {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(paddingValues)
                        .padding(horizontal = 16.dp, vertical = 12.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    item {
                        CharacterListControls(
                            searchQuery = uiState.searchQuery,
                            selectedSortMode = uiState.sortMode,
                            selectedClassFilter = uiState.classFilter,
                            selectedRaceFilter = uiState.raceFilter,
                            selectedLevelFilter = uiState.levelFilter,
                            availableClasses = uiState.availableClasses,
                            availableRaces = uiState.availableRaces,
                            onSearchQueryChange = onSearchQueryChange,
                            onSortModeSelected = onSortModeSelected,
                            onClassFilterSelected = onClassFilterSelected,
                            onRaceFilterSelected = onRaceFilterSelected,
                            onLevelFilterSelected = onLevelFilterSelected
                        )
                    }

                    if (uiState.characters.isEmpty()) {
                        item {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(24.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    when {
                                        uiState.hasSavedCharacters &&
                                            (uiState.searchQuery.isNotBlank() || uiState.hasActiveFilters) ->
                                            "No characters match the current search and filters."
                                        uiState.hasSavedCharacters ->
                                            "No characters available for the selected view."
                                        else ->
                                            "No characters yet. Create your first hero."
                                    }
                                )
                            }
                        }
                    } else {
                        items(uiState.characters, key = CharacterListItem::id) { character ->
                            Card(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable { onCharacterClick(character.id) }
                            ) {
                                Column(
                                    modifier = Modifier.padding(16.dp),
                                    verticalArrangement = Arrangement.spacedBy(4.dp)
                                ) {
                                    Text(
                                        text = character.name,
                                        fontWeight = FontWeight.SemiBold
                                    )
                                    Text(text = character.summary)
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun CharacterListControls(
    searchQuery: String,
    selectedSortMode: CharacterListSortMode,
    selectedClassFilter: String?,
    selectedRaceFilter: String?,
    selectedLevelFilter: CharacterListLevelFilter,
    availableClasses: List<String>,
    availableRaces: List<String>,
    onSearchQueryChange: (String) -> Unit,
    onSortModeSelected: (CharacterListSortMode) -> Unit,
    onClassFilterSelected: (String?) -> Unit,
    onRaceFilterSelected: (String?) -> Unit,
    onLevelFilterSelected: (CharacterListLevelFilter) -> Unit
) {
    Card(modifier = Modifier.fillMaxWidth()) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            OutlinedTextField(
                modifier = Modifier.fillMaxWidth(),
                value = searchQuery,
                onValueChange = onSearchQueryChange,
                label = { Text("Search by name") },
                singleLine = true
            )
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                FilterChipGroup(
                    title = "Sort by",
                    options = CharacterListSortMode.entries.map(CharacterListSortMode::label),
                    selectedOption = selectedSortMode.label,
                    onOptionSelected = { selectedLabel ->
                        CharacterListSortMode.entries
                            .firstOrNull { mode -> mode.label == selectedLabel }
                            ?.let(onSortModeSelected)
                    }
                )
                FilterChipGroup(
                    title = "Class",
                    options = listOf("Any class") + availableClasses,
                    selectedOption = selectedClassFilter ?: "Any class",
                    onOptionSelected = { selectedOption ->
                        onClassFilterSelected(selectedOption.takeUnless { it == "Any class" })
                    }
                )
                FilterChipGroup(
                    title = "Race",
                    options = listOf("Any race") + availableRaces,
                    selectedOption = selectedRaceFilter ?: "Any race",
                    onOptionSelected = { selectedOption ->
                        onRaceFilterSelected(selectedOption.takeUnless { it == "Any race" })
                    }
                )
                FilterChipGroup(
                    title = "Level",
                    options = CharacterListLevelFilter.entries.map(CharacterListLevelFilter::label),
                    selectedOption = selectedLevelFilter.label,
                    onOptionSelected = { selectedLabel ->
                        CharacterListLevelFilter.entries
                            .firstOrNull { filter -> filter.label == selectedLabel }
                            ?.let(onLevelFilterSelected)
                    }
                )
            }
        }
    }
}

@Composable
private fun FilterChipGroup(
    title: String,
    options: List<String>,
    selectedOption: String,
    onOptionSelected: (String) -> Unit
) {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Text(
            text = title,
            style = MaterialTheme.typography.labelLarge
        )
        FlowRow(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            options.forEach { option ->
                AssistChip(
                    onClick = { onOptionSelected(option) },
                    label = { Text(option) },
                    colors = if (option == selectedOption) {
                        AssistChipDefaults.assistChipColors(
                            containerColor = MaterialTheme.colorScheme.primaryContainer,
                            labelColor = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                    } else {
                        AssistChipDefaults.assistChipColors()
                    }
                )
            }
        }
    }
}
