package com.vinni.dndcharacterlist.feature.character.editor

import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavType
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import com.vinni.dndcharacterlist.core.navigation.NavigationDestination
import com.vinni.dndcharacterlist.feature.character.editor.presentation.CharacterEditorScreen
import com.vinni.dndcharacterlist.feature.character.editor.presentation.CharacterEditorViewModel
import org.koin.androidx.compose.koinViewModel
import org.koin.androidx.viewmodel.dsl.viewModel
import org.koin.core.parameter.parametersOf
import org.koin.dsl.module

private const val CHARACTER_ID_ARG = "characterId"

object CharacterEditorDestination : NavigationDestination {
    override val route: String = "editor"

    fun route(characterId: Long? = null): String {
        return if (characterId == null) route else "$route/$characterId"
    }
}

fun NavGraphBuilder.characterEditorGraph(
    onBack: () -> Unit,
    onSaved: () -> Unit,
    onDeleted: () -> Unit
) {
    composable(CharacterEditorDestination.route) {
        val viewModel: CharacterEditorViewModel = koinViewModel(parameters = { parametersOf(null as Long?) })
        CharacterEditorScreen(
            state = viewModel.uiState,
            onBack = onBack,
            onValueChange = viewModel::update,
            onSave = { viewModel.save(onSaved) },
            onDeleteRequest = viewModel::requestDeleteConfirmation,
            onDeleteDismiss = viewModel::dismissDeleteConfirmation,
            onDeleteConfirm = { viewModel.confirmDelete(onDeleted) }
        )
    }

    composable(
        route = "${CharacterEditorDestination.route}/{$CHARACTER_ID_ARG}",
        arguments = listOf(navArgument(CHARACTER_ID_ARG) { type = NavType.LongType })
    ) { backStackEntry ->
        val characterId = backStackEntry.arguments?.getLong(CHARACTER_ID_ARG)
        val viewModel: CharacterEditorViewModel = koinViewModel(parameters = { parametersOf(characterId) })
        CharacterEditorScreen(
            state = viewModel.uiState,
            onBack = onBack,
            onValueChange = viewModel::update,
            onSave = { viewModel.save(onSaved) },
            onDeleteRequest = viewModel::requestDeleteConfirmation,
            onDeleteDismiss = viewModel::dismissDeleteConfirmation,
            onDeleteConfirm = { viewModel.confirmDelete(onDeleted) }
        )
    }
}

val characterEditorModule = module {
    viewModel { (characterId: Long?) -> CharacterEditorViewModel(get(), get(), characterId) }
}
