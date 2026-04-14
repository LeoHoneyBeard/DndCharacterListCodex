package com.vinni.dndcharacterlist.core.rules.creation.rules

import com.vinni.dndcharacterlist.core.rules.creation.model.SkillId

data class BackgroundDefinition(
    val id: String,
    val name: String,
    val grantedSkills: Set<SkillId>
)


