@file:OptIn(ExperimentalLayoutApi::class)

package com.vinni.dndcharacterlist.feature.character.creation.presentation

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.AssistChip
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.vinni.dndcharacterlist.core.rules.creation.model.AbilityMethod
import com.vinni.dndcharacterlist.core.rules.creation.model.AbilityScores
import com.vinni.dndcharacterlist.core.rules.creation.model.AbilityType
import com.vinni.dndcharacterlist.core.rules.creation.model.CharacterCreationStep
import com.vinni.dndcharacterlist.core.rules.creation.rules.AbilityGenerationRules
import com.vinni.dndcharacterlist.core.rules.creation.rules.ClassDefinition
import com.vinni.dndcharacterlist.core.rules.creation.rules.RaceDefinition

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CharacterCreationScreen(
    state: CharacterCreationUiState,
    onExitRequest: () -> Unit,
    onExitDismiss: () -> Unit,
    onExitConfirm: () -> Unit,
    onPrevious: () -> Unit,
    onNext: () -> Unit,
    onSubmit: () -> Unit,
    onNameChange: (String) -> Unit,
    onRaceChange: (String) -> Unit,
    onSubraceChange: (String?) -> Unit,
    onBackgroundChange: (String) -> Unit,
    onClassChange: (String) -> Unit,
    onSubclassChange: (String?) -> Unit,
    onAbilityMethodChange: (AbilityMethod) -> Unit,
    onAbilitiesChange: (AbilityScores) -> Unit,
    onPointBuyAdjust: (AbilityType, Int) -> Unit,
    onRollAbilities: () -> Unit,
    onApplyStandardArray: () -> Unit,
    onSkillToggle: (String) -> Unit,
    onReplacementSkillChange: (String, String) -> Unit
) {
    BackHandler(onBack = onExitRequest)

    if (state.isDiscardConfirmationVisible) {
        AlertDialog(
            onDismissRequest = onExitDismiss,
            title = { Text("Discard character creation?") },
            text = { Text("Your in-progress character will be lost if you leave now.") },
            confirmButton = {
                TextButton(onClick = onExitConfirm) {
                    Text("Discard")
                }
            },
            dismissButton = {
                TextButton(onClick = onExitDismiss) {
                    Text("Keep editing")
                }
            }
        )
    }

    Scaffold(
        topBar = {
            TopAppBar(
                navigationIcon = {
                    IconButton(onClick = onExitRequest) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back"
                        )
                    }
                },
                title = { Text("Character creation wizard") }
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            StepHeader(currentStep = state.currentStep)

            when (state.currentStep) {
                CharacterCreationStep.ORIGIN -> OriginStep(
                    state = state,
                    onNameChange = onNameChange,
                    onRaceChange = onRaceChange,
                    onSubraceChange = onSubraceChange,
                    onBackgroundChange = onBackgroundChange
                )

                CharacterCreationStep.CLASS -> ClassStep(
                    state = state,
                    onClassChange = onClassChange,
                    onSubclassChange = onSubclassChange
                )

                CharacterCreationStep.ABILITIES -> AbilitiesStep(
                    state = state,
                    onAbilityMethodChange = onAbilityMethodChange,
                    onAbilitiesChange = onAbilitiesChange,
                    onPointBuyAdjust = onPointBuyAdjust,
                    onRollAbilities = onRollAbilities,
                    onApplyStandardArray = onApplyStandardArray
                )

                CharacterCreationStep.SKILLS -> SkillsStep(
                    state = state,
                    onSkillToggle = onSkillToggle,
                    onReplacementSkillChange = onReplacementSkillChange
                )

                CharacterCreationStep.DERIVED -> DerivedStep(state = state)
                CharacterCreationStep.SUMMARY -> SummaryStep(state = state)
            }

            state.stepError?.let { message ->
                Text(
                    text = message,
                    color = MaterialTheme.colorScheme.error
                )
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                TextButton(
                    modifier = Modifier.weight(1f),
                    enabled = state.currentStep != CharacterCreationStep.ORIGIN && !state.isSubmitting,
                    onClick = onPrevious
                ) {
                    Text("Back")
                }
                Button(
                    modifier = Modifier.weight(1f),
                    enabled = !state.isSubmitting,
                    onClick = {
                        if (state.currentStep == CharacterCreationStep.SUMMARY) {
                            onSubmit()
                        } else {
                            onNext()
                        }
                    }
                ) {
                    Text(
                        if (state.currentStep == CharacterCreationStep.SUMMARY) {
                            if (state.isSubmitting) "Creating..." else "Create"
                        } else {
                            "Next"
                        }
                    )
                }
            }
        }
    }
}

@Composable
private fun StepHeader(currentStep: CharacterCreationStep) {
    Card(modifier = Modifier.fillMaxWidth()) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Text(
                text = "Step ${currentStep.ordinal + 1} of ${CharacterCreationStep.entries.size}",
                style = MaterialTheme.typography.labelLarge,
                color = MaterialTheme.colorScheme.primary
            )
            Text(
                text = currentStep.title(),
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.SemiBold
            )
        }
    }
}

@Composable
private fun OriginStep(
    state: CharacterCreationUiState,
    onNameChange: (String) -> Unit,
    onRaceChange: (String) -> Unit,
    onSubraceChange: (String?) -> Unit,
    onBackgroundChange: (String) -> Unit
) {
    val selectedRace = state.rulesContent.races.firstOrNull { it.id == state.draft.raceId }

    SectionCard(
        title = "Origin",
        subtitle = "Choose the PHB 2014 identity basics for the character."
    ) {
        OutlinedTextField(
            modifier = Modifier.fillMaxWidth(),
            value = state.draft.name,
            onValueChange = onNameChange,
            label = { Text("Name") },
            singleLine = true
        )
        SelectionChips(
            label = "Race",
            options = state.rulesContent.races,
            selectedId = state.draft.raceId,
            optionLabel = RaceDefinition::name,
            optionId = RaceDefinition::id,
            onSelected = onRaceChange
        )
        selectedRace?.subraces?.takeIf { it.isNotEmpty() }?.let { subraces ->
            SelectionChips(
                label = "Subrace",
                options = subraces,
                selectedId = state.draft.subraceId,
                optionLabel = { it.name },
                optionId = { it.id },
                onSelected = { onSubraceChange(it) }
            )
        }
        SelectionChips(
            label = "Background",
            options = state.rulesContent.backgrounds,
            selectedId = state.draft.backgroundId,
            optionLabel = { it.name },
            optionId = { it.id },
            onSelected = onBackgroundChange
        )
    }
}

@Composable
private fun ClassStep(
    state: CharacterCreationUiState,
    onClassChange: (String) -> Unit,
    onSubclassChange: (String?) -> Unit
) {
    val selectedClass = state.rulesContent.classes.firstOrNull { it.id == state.draft.classId }

    SectionCard(
        title = "Class",
        subtitle = "Pick the class chassis and any subclass available at level 1."
    ) {
        SelectionChips(
            label = "Class",
            options = state.rulesContent.classes,
            selectedId = state.draft.classId,
            optionLabel = ClassDefinition::name,
            optionId = ClassDefinition::id,
            onSelected = onClassChange
        )
        selectedClass?.let { classDefinition ->
            RuleSummaryLine("Hit Die", "d${classDefinition.hitDie}")
            RuleSummaryLine(
                "Saving Throws",
                classDefinition.savingThrowProficiencies.joinToString { it.name.take(3) }
            )
            RuleSummaryLine(
                "Primary Abilities",
                classDefinition.primaryAbilities.joinToString { it.name.take(3) }
            )
            if (classDefinition.subclassLevel == 1 && classDefinition.subclasses.isNotEmpty()) {
                SelectionChips(
                    label = "Subclass",
                    options = classDefinition.subclasses,
                    selectedId = state.draft.subclassId,
                    optionLabel = { it.name },
                    optionId = { it.id },
                    onSelected = { onSubclassChange(it) }
                )
            }
        }
    }
}

@Composable
private fun AbilitiesStep(
    state: CharacterCreationUiState,
    onAbilityMethodChange: (AbilityMethod) -> Unit,
    onAbilitiesChange: (AbilityScores) -> Unit,
    onPointBuyAdjust: (AbilityType, Int) -> Unit,
    onRollAbilities: () -> Unit,
    onApplyStandardArray: () -> Unit
) {
    val baseScores = state.draft.baseAbilities ?: AbilityScores(8, 8, 8, 8, 8, 8)
    val method = state.draft.abilityMethod

    SectionCard(
        title = "Abilities",
        subtitle = "Pick a generation method and assign base scores before racial bonuses."
    ) {
        SelectionChips(
            label = "Method",
            options = AbilityMethod.entries,
            selectedId = state.draft.abilityMethod?.name,
            optionLabel = { it.name.replace('_', ' ') },
            optionId = { it.name },
            onSelected = { methodName ->
                AbilityMethod.entries.firstOrNull { it.name == methodName }?.let(onAbilityMethodChange)
            }
        )
        when (method) {
            AbilityMethod.STANDARD_ARRAY -> {
                Text("Allowed values: ${AbilityGenerationRules.standardArray.joinToString()}")
                TextButton(onClick = onApplyStandardArray) {
                    Text("Apply standard array")
                }
                AbilityInputs(
                    scores = baseScores,
                    onScoresChange = onAbilitiesChange
                )
            }

            AbilityMethod.POINT_BUY -> {
                RuleSummaryLine(
                    "Points remaining",
                    AbilityGenerationRules.pointBuyRemaining(baseScores).toString()
                )
                PointBuyEditor(
                    scores = baseScores,
                    onAdjust = onPointBuyAdjust
                )
            }

            AbilityMethod.ROLL -> {
                TextButton(onClick = onRollAbilities) {
                    Text("Roll a new set")
                }
                AbilityInputs(
                    scores = baseScores,
                    onScoresChange = onAbilitiesChange
                )
            }

            AbilityMethod.MANUAL,
            null -> {
                AbilityInputs(
                    scores = baseScores,
                    onScoresChange = onAbilitiesChange
                )
            }
        }
        state.derived.finalAbilities?.let { final ->
            RuleSummaryLine(
                "Final scores after racial bonuses",
                "STR ${final.strength}, DEX ${final.dexterity}, CON ${final.constitution}, INT ${final.intelligence}, WIS ${final.wisdom}, CHA ${final.charisma}"
            )
        }
    }
}

@Composable
private fun SkillsStep(
    state: CharacterCreationUiState,
    onSkillToggle: (String) -> Unit,
    onReplacementSkillChange: (String, String) -> Unit
) {
    val classDefinition = state.rulesContent.classes.firstOrNull { it.id == state.draft.classId }
    val background = state.rulesContent.backgrounds.firstOrNull { it.id == state.draft.backgroundId }
    val skillLabels = state.rulesContent.skills.associateBy({ it.id }, { it.name })

    SectionCard(
        title = "Skills",
        subtitle = "Choose class skills and resolve any background conflicts."
    ) {
        classDefinition?.let { definition ->
            Text("Choose ${definition.skillChoiceCount} class skills")
            FlowRow(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                definition.skillOptions.forEach { skillId ->
                    AssistChip(
                        onClick = { onSkillToggle(skillId) },
                        label = { Text(skillLabels[skillId] ?: skillId) }
                    )
                }
            }
        }
        background?.let { selectedBackground ->
            selectedBackground.grantedSkills.forEach { backgroundSkill ->
                val conflict = backgroundSkill in state.draft.selectedClassSkills
                if (conflict) {
                    val replacement = state.draft.selectedReplacementSkills[backgroundSkill]
                    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                        Text("Replace background skill ${skillLabels[backgroundSkill] ?: backgroundSkill}")
                        FlowRow(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            state.rulesContent.skills.forEach { skill ->
                                AssistChip(
                                    onClick = { onReplacementSkillChange(backgroundSkill, skill.id) },
                                    label = { Text(skill.name) }
                                )
                            }
                        }
                        replacement?.let {
                            Text("Selected replacement: ${skillLabels[it] ?: it}")
                        }
                    }
                }
            }
        }
        RuleSummaryLine(
            "Current proficiencies",
            state.derived.skillProficiencies.joinToString { skillLabels[it] ?: it }
        )
    }
}

@Composable
private fun DerivedStep(state: CharacterCreationUiState) {
    SectionCard(
        title = "Derived values",
        subtitle = "Preview automatic values before the final summary."
    ) {
        RuleSummaryLine("Proficiency Bonus", state.derived.proficiencyBonus?.let { "+$it" } ?: "-")
        RuleSummaryLine("Max HP", state.derived.maxHitPoints?.toString() ?: "-")
        RuleSummaryLine("Current HP", state.derived.currentHitPoints?.toString() ?: "-")
        RuleSummaryLine("Spell Slots", state.derived.spellSlots?.firstLevel?.let { "Level 1: $it" } ?: "None")
        RuleSummaryLine(
            "Available subclass options",
            state.derived.availableSubclassOptions.ifEmpty { listOf("None at this level") }.joinToString()
        )
    }
}

@Composable
private fun SummaryStep(state: CharacterCreationUiState) {
    val raceLabel = state.rulesContent.races.firstOrNull { it.id == state.draft.raceId }?.name ?: "-"
    val subraceLabel = state.rulesContent.races
        .flatMap { it.subraces }
        .firstOrNull { it.id == state.draft.subraceId }
        ?.name
    val classLabel = state.rulesContent.classes.firstOrNull { it.id == state.draft.classId }?.name ?: "-"
    val subclassLabel = state.rulesContent.classes
        .flatMap { it.subclasses }
        .firstOrNull { it.id == state.draft.subclassId }
        ?.name
    val backgroundLabel = state.rulesContent.backgrounds.firstOrNull { it.id == state.draft.backgroundId }?.name ?: "-"

    SectionCard(
        title = "Summary",
        subtitle = "This step is the only place where creation can be submitted."
    ) {
        RuleSummaryLine("Name", state.draft.name)
        RuleSummaryLine("Race", listOfNotNull(raceLabel, subraceLabel).joinToString(" / "))
        RuleSummaryLine("Class", listOfNotNull(classLabel, subclassLabel).joinToString(" / "))
        RuleSummaryLine("Background", backgroundLabel)
        RuleSummaryLine("HP", state.derived.maxHitPoints?.toString() ?: "-")
        RuleSummaryLine(
            "Skill proficiencies",
            state.derived.skillProficiencies.joinToString().ifBlank { "-" }
        )
    }
}

@Composable
private fun AbilityInputs(
    scores: AbilityScores,
    onScoresChange: (AbilityScores) -> Unit
) {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        ScoreField("STR", scores.strength) { onScoresChange(scores.copy(strength = it)) }
        ScoreField("DEX", scores.dexterity) { onScoresChange(scores.copy(dexterity = it)) }
        ScoreField("CON", scores.constitution) { onScoresChange(scores.copy(constitution = it)) }
        ScoreField("INT", scores.intelligence) { onScoresChange(scores.copy(intelligence = it)) }
        ScoreField("WIS", scores.wisdom) { onScoresChange(scores.copy(wisdom = it)) }
        ScoreField("CHA", scores.charisma) { onScoresChange(scores.copy(charisma = it)) }
    }
}

@Composable
private fun PointBuyEditor(
    scores: AbilityScores,
    onAdjust: (AbilityType, Int) -> Unit
) {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        AbilityType.entries.forEach { abilityType ->
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = abilityType.shortLabel(),
                    modifier = Modifier.weight(1f)
                )
                TextButton(onClick = { onAdjust(abilityType, -1) }) {
                    Text("-")
                }
                Surface(
                    shape = MaterialTheme.shapes.small,
                    tonalElevation = 2.dp
                ) {
                    Text(
                        text = scores[abilityType].toString(),
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp)
                    )
                }
                TextButton(onClick = { onAdjust(abilityType, 1) }) {
                    Text("+")
                }
            }
        }
    }
}

@Composable
private fun ScoreField(
    label: String,
    value: Int,
    onValueChange: (Int) -> Unit
) {
    OutlinedTextField(
        modifier = Modifier.fillMaxWidth(),
        value = value.toString(),
        onValueChange = { input -> onValueChange(input.filter(Char::isDigit).toIntOrNull() ?: 0) },
        label = { Text(label) },
        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
        singleLine = true
    )
}

@Composable
private fun RuleSummaryLine(label: String, value: String) {
    Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Text(text = value)
    }
}

@Composable
private fun SectionCard(
    title: String,
    subtitle: String,
    content: @Composable () -> Unit
) {
    Card(modifier = Modifier.fillMaxWidth()) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.SemiBold
            )
            Text(
                text = subtitle,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            content()
        }
    }
}

@Composable
private fun <T> SelectionChips(
    label: String,
    options: List<T>,
    selectedId: String?,
    optionLabel: (T) -> String,
    optionId: (T) -> String,
    onSelected: (String) -> Unit
) {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        FlowRow(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            options.forEach { option ->
                val id = optionId(option)
                val name = optionLabel(option)
                OptionCell(
                    text = name,
                    selected = id == selectedId,
                    onClick = { onSelected(id) }
                )
            }
        }
    }
}

@Composable
private fun OptionCell(
    text: String,
    selected: Boolean,
    onClick: () -> Unit
) {
    Surface(
        shape = MaterialTheme.shapes.medium,
        color = if (selected) {
            MaterialTheme.colorScheme.primaryContainer
        } else {
            MaterialTheme.colorScheme.surfaceVariant
        },
        contentColor = if (selected) {
            MaterialTheme.colorScheme.onPrimaryContainer
        } else {
            MaterialTheme.colorScheme.onSurfaceVariant
        },
        border = BorderStroke(
            width = if (selected) 2.dp else 1.dp,
            color = if (selected) {
                MaterialTheme.colorScheme.primary
            } else {
                MaterialTheme.colorScheme.outlineVariant
            }
        ),
        modifier = Modifier.clickable(onClick = onClick)
    ) {
        Text(
            text = text,
            style = MaterialTheme.typography.bodyMedium,
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 10.dp)
        )
    }
}

private fun CharacterCreationStep.title(): String {
    return when (this) {
        CharacterCreationStep.ORIGIN -> "Origin"
        CharacterCreationStep.CLASS -> "Class"
        CharacterCreationStep.ABILITIES -> "Abilities"
        CharacterCreationStep.SKILLS -> "Skills"
        CharacterCreationStep.DERIVED -> "Derived"
        CharacterCreationStep.SUMMARY -> "Summary"
    }
}

private fun AbilityType.shortLabel(): String {
    return when (this) {
        AbilityType.STRENGTH -> "STR"
        AbilityType.DEXTERITY -> "DEX"
        AbilityType.CONSTITUTION -> "CON"
        AbilityType.INTELLIGENCE -> "INT"
        AbilityType.WISDOM -> "WIS"
        AbilityType.CHARISMA -> "CHA"
    }
}

