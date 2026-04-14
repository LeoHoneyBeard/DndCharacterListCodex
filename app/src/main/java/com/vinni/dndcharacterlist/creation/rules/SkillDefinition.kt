package com.vinni.dndcharacterlist.creation.rules

import com.vinni.dndcharacterlist.creation.model.AbilityType
import com.vinni.dndcharacterlist.creation.model.SkillId

data class SkillDefinition(
    val id: SkillId,
    val name: String,
    val ability: AbilityType
)
