package com.vinni.dndcharacterlist

import android.app.Application
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.vinni.dndcharacterlist.creation.repository.Phb2014RulesRepository
import com.vinni.dndcharacterlist.data.CharacterDatabase
import com.vinni.dndcharacterlist.data.CharacterRepository
import com.vinni.dndcharacterlist.ui.creation.CharacterCreationScreen
import com.vinni.dndcharacterlist.ui.creation.CharacterCreationViewModel
import com.vinni.dndcharacterlist.ui.detail.CharacterDetailScreen
import com.vinni.dndcharacterlist.ui.detail.CharacterDetailViewModel
import com.vinni.dndcharacterlist.ui.editor.CharacterEditorScreen
import com.vinni.dndcharacterlist.ui.editor.CharacterEditorViewModel
import com.vinni.dndcharacterlist.ui.list.CharacterListScreen
import com.vinni.dndcharacterlist.ui.list.CharacterListViewModel
import com.vinni.dndcharacterlist.ui.theme.DndCharacterListTheme

private const val LIST_ROUTE = "characters"
private const val DETAIL_ROUTE = "detail"
private const val CREATION_ROUTE = "creation"
private const val EDITOR_ROUTE = "editor"
private const val CHARACTER_ID_ARG = "characterId"

@Composable
fun DndCharacterApp() {
    val navController = rememberNavController()
    val application = LocalContext.current.applicationContext as Application
    val repository = remember(application) {
        CharacterRepository(
            CharacterDatabase.getInstance(application).characterDao()
        )
    }
    val rulesRepository = remember { Phb2014RulesRepository() }

    DndCharacterListTheme {
        NavHost(
            navController = navController,
            startDestination = LIST_ROUTE
        ) {
            composable(LIST_ROUTE) {
                val viewModel: CharacterListViewModel = viewModel(
                    factory = viewModelFactory {
                        CharacterListViewModel(repository)
                    }
                )
                CharacterListScreen(
                    state = viewModel.uiState,
                    onAddCharacter = { navController.navigate(CREATION_ROUTE) },
                    onCharacterClick = { id ->
                        navController.navigate("$DETAIL_ROUTE/$id")
                    }
                )
            }

            composable(CREATION_ROUTE) {
                val viewModel: CharacterCreationViewModel = viewModel(
                    factory = viewModelFactory {
                        CharacterCreationViewModel(
                            repository = rulesRepository,
                            characterRepository = repository
                        )
                    }
                )
                CharacterCreationScreen(
                    state = viewModel.uiState,
                    onBack = { navController.popBackStack() },
                    onPrevious = viewModel::previousStep,
                    onNext = viewModel::nextStep,
                    onSubmit = {
                        viewModel.createCharacter { createdId ->
                            navController.navigate("$DETAIL_ROUTE/$createdId") {
                                popUpTo(LIST_ROUTE)
                            }
                        }
                    },
                    onNameChange = viewModel::updateName,
                    onRaceChange = viewModel::updateRace,
                    onSubraceChange = viewModel::updateSubrace,
                    onBackgroundChange = viewModel::updateBackground,
                    onClassChange = viewModel::updateClass,
                    onSubclassChange = viewModel::updateSubclass,
                    onAbilityMethodChange = viewModel::updateAbilityMethod,
                    onAbilitiesChange = viewModel::updateBaseAbilities,
                    onPointBuyAdjust = viewModel::adjustPointBuyAbility,
                    onRollAbilities = viewModel::rollAbilities,
                    onApplyStandardArray = viewModel::applyStandardArray,
                    onSkillToggle = viewModel::toggleClassSkill,
                    onReplacementSkillChange = viewModel::updateReplacementSkill
                )
            }

            composable(
                route = "$DETAIL_ROUTE/{$CHARACTER_ID_ARG}",
                arguments = listOf(navArgument(CHARACTER_ID_ARG) { type = NavType.LongType })
            ) { backStackEntry ->
                val characterId = backStackEntry.arguments?.getLong(CHARACTER_ID_ARG) ?: return@composable
                val viewModel: CharacterDetailViewModel = viewModel(
                    factory = viewModelFactory {
                        CharacterDetailViewModel(repository, characterId)
                    }
                )
                CharacterDetailScreen(
                    state = viewModel.uiState,
                    onBack = { navController.popBackStack() },
                    onEdit = { navController.navigate("$EDITOR_ROUTE/$characterId") }
                )
            }

            composable(EDITOR_ROUTE) {
                val viewModel: CharacterEditorViewModel = viewModel(
                    factory = viewModelFactory {
                        CharacterEditorViewModel(repository, characterId = null)
                    }
                )
                CharacterEditorScreen(
                    state = viewModel.uiState,
                    onBack = { navController.popBackStack() },
                    onValueChange = viewModel::update,
                    onSave = {
                        viewModel.save {
                            navController.popBackStack()
                        }
                    },
                    onDelete = {
                        viewModel.delete {
                            navController.popBackStack(LIST_ROUTE, inclusive = false)
                        }
                    }
                )
            }

            composable(
                route = "$EDITOR_ROUTE/{$CHARACTER_ID_ARG}",
                arguments = listOf(navArgument(CHARACTER_ID_ARG) { type = NavType.LongType })
            ) { backStackEntry ->
                val characterId = backStackEntry.arguments?.getLong(CHARACTER_ID_ARG)
                val viewModel: CharacterEditorViewModel = viewModel(
                    factory = viewModelFactory {
                        CharacterEditorViewModel(repository, characterId = characterId)
                    }
                )
                CharacterEditorScreen(
                    state = viewModel.uiState,
                    onBack = { navController.popBackStack() },
                    onValueChange = viewModel::update,
                    onSave = {
                        viewModel.save {
                            navController.popBackStack()
                        }
                    },
                    onDelete = {
                        viewModel.delete {
                            navController.popBackStack(LIST_ROUTE, inclusive = false)
                        }
                    }
                )
            }
        }
    }
}

private inline fun <reified T : ViewModel> viewModelFactory(
    crossinline create: () -> T
): ViewModelProvider.Factory {
    return object : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <VM : ViewModel> create(modelClass: Class<VM>): VM = create() as VM
    }
}
