package com.vinni.dndcharacterlist.feature.character.list.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.vinni.dndcharacterlist.core.domain.model.CharacterRecord
import com.vinni.dndcharacterlist.core.domain.repository.CharacterRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update

enum class CharacterListSortMode(val label: String) {
    UPDATED_AT("Last updated"),
    NAME("Name"),
    LEVEL("Level")
}

enum class CharacterListLevelFilter(val label: String) {
    ANY("Any level"),
    LEVELS_1_TO_4("Levels 1-4"),
    LEVELS_5_TO_10("Levels 5-10"),
    LEVELS_11_TO_16("Levels 11-16"),
    LEVELS_17_TO_20("Levels 17-20")
}

private data class CharacterListControlsState(
    val searchQuery: String = "",
    val sortMode: CharacterListSortMode = CharacterListSortMode.UPDATED_AT,
    val classFilter: String? = null,
    val raceFilter: String? = null,
    val levelFilter: CharacterListLevelFilter = CharacterListLevelFilter.ANY
)

data class CharacterListItem(
    val id: Long,
    val name: String,
    val summary: String
)

data class CharacterListUiState(
    val isLoading: Boolean = true,
    val characters: List<CharacterListItem> = emptyList(),
    val hasSavedCharacters: Boolean = false,
    val searchQuery: String = "",
    val sortMode: CharacterListSortMode = CharacterListSortMode.UPDATED_AT,
    val classFilter: String? = null,
    val raceFilter: String? = null,
    val levelFilter: CharacterListLevelFilter = CharacterListLevelFilter.ANY,
    val availableClasses: List<String> = emptyList(),
    val availableRaces: List<String> = emptyList(),
    val hasActiveFilters: Boolean = false,
    val errorMessage: String? = null
)

class CharacterListViewModel(
    repository: CharacterRepository,
    scope: CoroutineScope? = null
) : ViewModel() {
    private val stateScope = scope ?: viewModelScope
    private val controlsState = MutableStateFlow(CharacterListControlsState())

    val uiState: StateFlow<CharacterListUiState> = combine(
        repository.observeCharacters(),
        controlsState
    ) { characters, controls ->
        val visibleCharacters = characters
            .filterByQuery(controls.searchQuery)
            .filterByClass(controls.classFilter)
            .filterByRace(controls.raceFilter)
            .filterByLevel(controls.levelFilter)
            .sortedByMode(controls.sortMode)
        CharacterListUiState(
            isLoading = false,
            characters = visibleCharacters.map(CharacterRecord::toListItem),
            hasSavedCharacters = characters.isNotEmpty(),
            searchQuery = controls.searchQuery,
            sortMode = controls.sortMode,
            classFilter = controls.classFilter,
            raceFilter = controls.raceFilter,
            levelFilter = controls.levelFilter,
            availableClasses = characters.availableFilterOptions(CharacterRecord::characterClass),
            availableRaces = characters.availableFilterOptions(CharacterRecord::race),
            hasActiveFilters = controls.classFilter != null ||
                controls.raceFilter != null ||
                controls.levelFilter != CharacterListLevelFilter.ANY
        )
    }
        .catch {
            emit(
                CharacterListUiState(
                    isLoading = false,
                    searchQuery = controlsState.value.searchQuery,
                    sortMode = controlsState.value.sortMode,
                    classFilter = controlsState.value.classFilter,
                    raceFilter = controlsState.value.raceFilter,
                    levelFilter = controlsState.value.levelFilter,
                    errorMessage = "Couldn't load characters. Try again."
                )
            )
        }
        .stateIn(
            scope = stateScope,
            started = SharingStarted.Eagerly,
            initialValue = CharacterListUiState(isLoading = true)
        )

    fun updateSearchQuery(query: String) {
        controlsState.update { current -> current.copy(searchQuery = query) }
    }

    fun setSortMode(mode: CharacterListSortMode) {
        controlsState.update { current -> current.copy(sortMode = mode) }
    }

    fun setClassFilter(value: String?) {
        controlsState.update { current -> current.copy(classFilter = value) }
    }

    fun setRaceFilter(value: String?) {
        controlsState.update { current -> current.copy(raceFilter = value) }
    }

    fun setLevelFilter(value: CharacterListLevelFilter) {
        controlsState.update { current -> current.copy(levelFilter = value) }
    }
}

internal fun CharacterRecord.toListItem(): CharacterListItem {
    val parts = listOfNotNull(
        "Lvl $level",
        race.toDisplayPart(),
        characterClass.toDisplayPart(),
        subclass.toDisplayPart(),
        "AC $armorClass",
        "HP $hitPoints"
    )
    return CharacterListItem(
        id = id,
        name = name,
        summary = parts.joinToString(" | ")
    )
}

private fun List<CharacterRecord>.filterByQuery(query: String): List<CharacterRecord> {
    val normalizedQuery = query.trim()
    if (normalizedQuery.isBlank()) return this
    return filter { character ->
        character.name.contains(normalizedQuery, ignoreCase = true)
    }
}

private fun List<CharacterRecord>.filterByClass(selectedClass: String?): List<CharacterRecord> {
    if (selectedClass == null) return this
    return filter { character ->
        character.characterClass.equals(selectedClass, ignoreCase = true)
    }
}

private fun List<CharacterRecord>.filterByRace(selectedRace: String?): List<CharacterRecord> {
    if (selectedRace == null) return this
    return filter { character ->
        character.race.equals(selectedRace, ignoreCase = true)
    }
}

private fun List<CharacterRecord>.filterByLevel(levelFilter: CharacterListLevelFilter): List<CharacterRecord> {
    if (levelFilter == CharacterListLevelFilter.ANY) return this
    return filter { character -> levelFilter.matches(character.level) }
}

private fun List<CharacterRecord>.sortedByMode(mode: CharacterListSortMode): List<CharacterRecord> {
    val comparator = when (mode) {
        CharacterListSortMode.UPDATED_AT -> compareByDescending<CharacterRecord> { it.updatedAt }
            .thenBy(String.CASE_INSENSITIVE_ORDER) { it.name }
        CharacterListSortMode.NAME -> compareBy(String.CASE_INSENSITIVE_ORDER, CharacterRecord::name)
            .thenByDescending { it.updatedAt }
        CharacterListSortMode.LEVEL -> compareByDescending<CharacterRecord> { it.level }
            .thenBy(String.CASE_INSENSITIVE_ORDER) { it.name }
            .thenByDescending { it.updatedAt }
    }
    return sortedWith(comparator)
}

private fun CharacterListLevelFilter.matches(level: Int): Boolean {
    return when (this) {
        CharacterListLevelFilter.ANY -> true
        CharacterListLevelFilter.LEVELS_1_TO_4 -> level in 1..4
        CharacterListLevelFilter.LEVELS_5_TO_10 -> level in 5..10
        CharacterListLevelFilter.LEVELS_11_TO_16 -> level in 11..16
        CharacterListLevelFilter.LEVELS_17_TO_20 -> level in 17..20
    }
}

private fun List<CharacterRecord>.availableFilterOptions(
    selector: (CharacterRecord) -> String
): List<String> {
    return mapNotNull { character -> selector(character).toDisplayPart() }
        .distinctBy { value -> value.lowercase() }
        .sortedWith(String.CASE_INSENSITIVE_ORDER)
}

private fun String.toDisplayPart(): String? {
    val sanitized = trim()
    return sanitized.takeUnless {
        sanitized.isBlank() || sanitized.equals("null", ignoreCase = true)
    }
}

