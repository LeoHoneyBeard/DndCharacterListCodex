package com.vinni.dndcharacterlist.feature.character.levelup

import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavType
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import com.vinni.dndcharacterlist.core.navigation.NavigationDestination
import com.vinni.dndcharacterlist.feature.character.levelup.domain.CharacterLevelUpPlanner
import com.vinni.dndcharacterlist.feature.character.levelup.domain.LevelUpCharacterUseCase
import com.vinni.dndcharacterlist.feature.character.levelup.presentation.CharacterLevelUpScreen
import com.vinni.dndcharacterlist.feature.character.levelup.presentation.CharacterLevelUpViewModel
import org.koin.androidx.compose.koinViewModel
import org.koin.androidx.viewmodel.dsl.viewModel
import org.koin.core.parameter.parametersOf
import org.koin.dsl.module

private const val CHARACTER_ID_ARG = "characterId"

object CharacterLevelUpDestination : NavigationDestination {
    override val route: String = "level-up"

    fun route(characterId: Long): String = "$route/$characterId"
}

fun NavGraphBuilder.characterLevelUpGraph(
    onBack: () -> Unit,
    onLeveledUp: () -> Unit
) {
    composable(
        route = "${CharacterLevelUpDestination.route}/{$CHARACTER_ID_ARG}",
        arguments = listOf(navArgument(CHARACTER_ID_ARG) { type = NavType.LongType })
    ) { backStackEntry ->
        val characterId = backStackEntry.arguments?.getLong(CHARACTER_ID_ARG) ?: return@composable
        val viewModel: CharacterLevelUpViewModel = koinViewModel(parameters = { parametersOf(characterId) })
        CharacterLevelUpScreen(
            state = viewModel.uiState,
            onBack = onBack,
            onHitPointGainChange = viewModel::updateHitPointGain,
            onApplyLevelUp = { viewModel.applyLevelUp(onLeveledUp) }
        )
    }
}

val characterLevelUpModule = module {
    factory { CharacterLevelUpPlanner(get()) }
    factory { LevelUpCharacterUseCase(get()) }
    viewModel { (characterId: Long) -> CharacterLevelUpViewModel(get(), get(), get(), characterId) }
}
