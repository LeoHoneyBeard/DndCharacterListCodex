package com.vinni.dndcharacterlist.feature.character.creation

import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import com.vinni.dndcharacterlist.core.navigation.NavigationDestination
import com.vinni.dndcharacterlist.feature.character.creation.presentation.CharacterCreationScreen
import com.vinni.dndcharacterlist.feature.character.creation.presentation.CharacterCreationViewModel
import org.koin.androidx.compose.koinViewModel
import org.koin.androidx.viewmodel.dsl.viewModel
import org.koin.dsl.module

object CharacterCreationDestination : NavigationDestination {
    override val route: String = "creation"
}

fun NavGraphBuilder.characterCreationGraph(
    onBack: () -> Unit,
    onCharacterCreated: (Long) -> Unit
) {
    composable(CharacterCreationDestination.route) {
        val viewModel: CharacterCreationViewModel = koinViewModel()
        CharacterCreationScreen(
            state = viewModel.uiState,
            onExitRequest = { viewModel.requestExit(onBack) },
            onExitDismiss = viewModel::dismissExitConfirmation,
            onExitConfirm = { viewModel.confirmExit(onBack) },
            onPrevious = viewModel::previousStep,
            onNext = viewModel::nextStep,
            onSubmit = { viewModel.createCharacter(onCharacterCreated) },
            onRulesetChange = viewModel::updateRuleset,
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
}

val characterCreationModule = module {
    viewModel { CharacterCreationViewModel(get(), get(), get(), get()) }
}
