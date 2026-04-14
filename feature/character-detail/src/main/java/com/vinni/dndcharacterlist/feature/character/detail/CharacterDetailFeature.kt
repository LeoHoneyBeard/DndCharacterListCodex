package com.vinni.dndcharacterlist.feature.character.detail

import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavType
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import com.vinni.dndcharacterlist.core.navigation.NavigationDestination
import com.vinni.dndcharacterlist.feature.character.detail.presentation.CharacterDetailScreen
import com.vinni.dndcharacterlist.feature.character.detail.presentation.CharacterDetailViewModel
import org.koin.androidx.compose.koinViewModel
import org.koin.androidx.viewmodel.dsl.viewModel
import org.koin.core.parameter.parametersOf
import org.koin.dsl.module

private const val CHARACTER_ID_ARG = "characterId"

object CharacterDetailDestination : NavigationDestination {
    override val route: String = "detail"

    fun route(characterId: Long): String = "$route/$characterId"
}

fun NavGraphBuilder.characterDetailGraph(
    onBack: () -> Unit,
    onEditCharacter: (Long) -> Unit
) {
    composable(
        route = "${CharacterDetailDestination.route}/{$CHARACTER_ID_ARG}",
        arguments = listOf(navArgument(CHARACTER_ID_ARG) { type = NavType.LongType })
    ) { backStackEntry ->
        val characterId = backStackEntry.arguments?.getLong(CHARACTER_ID_ARG) ?: return@composable
        val viewModel: CharacterDetailViewModel = koinViewModel(parameters = { parametersOf(characterId) })
        CharacterDetailScreen(
            state = viewModel.uiState,
            onBack = onBack,
            onEdit = { onEditCharacter(characterId) }
        )
    }
}

val characterDetailModule = module {
    viewModel { (characterId: Long) -> CharacterDetailViewModel(get(), characterId) }
}
