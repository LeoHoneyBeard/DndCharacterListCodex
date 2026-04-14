package com.vinni.dndcharacterlist.core.rules.creation.rules

import com.vinni.dndcharacterlist.core.rules.creation.model.CharacterCreationDraft
import com.vinni.dndcharacterlist.core.rules.creation.model.DerivedCharacterStats
import com.vinni.dndcharacterlist.core.rules.creation.model.Ruleset
import com.vinni.dndcharacterlist.core.rules.creation.model.ValidationIssue
import com.vinni.dndcharacterlist.core.rules.creation.repository.RulesRepository

class CharacterCreationRulesEngine(
    private val repository: RulesRepository,
    private val abilityScoreEngine: AbilityScoreEngine = AbilityScoreEngine(),
    private val proficiencyEngine: ProficiencyEngine = ProficiencyEngine(),
    private val hitPointEngine: HitPointEngine = HitPointEngine(),
    private val spellProgressionEngine: SpellProgressionEngine = SpellProgressionEngine(),
    private val validationEngine: CreationValidationEngine = CreationValidationEngine()
) {

    fun derive(draft: CharacterCreationDraft): DerivedCharacterStats {
        val content = repository.getRuleset(draft.ruleset)
        val race = content.races.firstOrNull { it.id == draft.raceId }
        val subrace = race?.subraces?.firstOrNull { it.id == draft.subraceId }
        val classDefinition = content.classes.firstOrNull { it.id == draft.classId }
        val background = content.backgrounds.firstOrNull { it.id == draft.backgroundId }

        val issues = mutableListOf<ValidationIssue>()
        issues += validationEngine.validateDraft(draft, race, subrace, classDefinition, background)

        val finalAbilities = draft.baseAbilities?.let {
            abilityScoreEngine.applyBonuses(
                baseScores = it,
                race = race,
                subrace = subrace
            )
        }
        val modifiers = finalAbilities?.let(abilityScoreEngine::modifiersFor)

        val proficiencyResult = if (classDefinition != null && background != null) {
            proficiencyEngine.deriveSkillProficiencies(
                draftSelectedSkills = draft.selectedClassSkills,
                replacementSkills = draft.selectedReplacementSkills,
                availableSkills = classDefinition.skillOptions,
                classChoiceCount = classDefinition.skillChoiceCount,
                backgroundSkills = background.grantedSkills
            )
        } else {
            ProficiencyResult(emptySet(), emptyList())
        }
        issues += proficiencyResult.validationIssues

        val proficiencyBonus = if (draft.level in 1..20) ((draft.level - 1) / 4) + 2 else null
        val maxHitPoints = if (classDefinition != null && modifiers != null) {
            hitPointEngine.levelOneHitPoints(classDefinition.hitDie, modifiers.constitution)
        } else {
            null
        }

        return DerivedCharacterStats(
            finalAbilities = finalAbilities,
            abilityModifiers = modifiers,
            proficiencyBonus = proficiencyBonus,
            savingThrowProficiencies = classDefinition?.savingThrowProficiencies.orEmpty(),
            skillProficiencies = proficiencyResult.skillProficiencies,
            maxHitPoints = maxHitPoints,
            currentHitPoints = maxHitPoints,
            spellSlots = spellProgressionEngine.slotsAtLevel(classDefinition, draft.level),
            availableSubclassOptions = if (classDefinition?.subclassLevel?.let { draft.level >= it } == true) {
                classDefinition.subclasses.map(SubclassDefinition::name)
            } else {
                emptyList()
            },
            validationIssues = issues
        )
    }
}


