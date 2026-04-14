package com.vinni.dndcharacterlist.creation.rules

import com.vinni.dndcharacterlist.creation.model.AbilityType
import com.vinni.dndcharacterlist.creation.model.SkillId

data class ClassDefinition(
    val id: String,
    val name: String,
    val hitDie: Int,
    val primaryAbilities: Set<AbilityType>,
    val savingThrowProficiencies: Set<AbilityType>,
    val skillChoiceCount: Int,
    val skillOptions: Set<SkillId>,
    val subclassLevel: Int?,
    val subclasses: List<SubclassDefinition> = emptyList(),
    val spellcasting: SpellcastingDefinition? = null
)
