package com.vinni.dndcharacterlist.creation.rules

import com.vinni.dndcharacterlist.creation.model.SkillId

data class BackgroundDefinition(
    val id: String,
    val name: String,
    val grantedSkills: Set<SkillId>
)
