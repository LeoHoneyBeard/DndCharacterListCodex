package com.vinni.dndcharacterlist.core.rules.creation.rules

import com.vinni.dndcharacterlist.core.rules.creation.model.AbilityType
import com.vinni.dndcharacterlist.core.rules.creation.model.SkillId

data class SkillDefinition(
    val id: SkillId,
    val name: String,
    val ability: AbilityType
)


