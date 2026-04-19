package com.vinni.dndcharacterlist.feature.character.list

import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import com.vinni.dndcharacterlist.core.navigation.NavigationDestination
import com.vinni.dndcharacterlist.feature.character.list.presentation.CharacterListScreen
import com.vinni.dndcharacterlist.feature.character.list.presentation.CharacterListViewModel
import org.koin.androidx.compose.koinViewModel
import org.koin.androidx.viewmodel.dsl.viewModel
import org.koin.dsl.module

object CharacterListDestination : NavigationDestination {
    override val route: String = "characters"
}

fun NavGraphBuilder.characterListGraph(
    onCreateCharacter: () -> Unit,
    onOpenCharacter: (Long) -> Unit
) {
    composable(CharacterListDestination.route) {
        val viewModel: CharacterListViewModel = koinViewModel()
        CharacterListScreen(
            state = viewModel.uiState,
            onAddCharacter = onCreateCharacter,
            onCharacterClick = onOpenCharacter,
            onSearchQueryChange = viewModel::updateSearchQuery,
            onSortModeSelected = viewModel::setSortMode,
            onClassFilterSelected = viewModel::setClassFilter,
            onRaceFilterSelected = viewModel::setRaceFilter,
            onLevelFilterSelected = viewModel::setLevelFilter
        )
    }
}

val characterListModule = module {
    viewModel { CharacterListViewModel(get()) }
}
