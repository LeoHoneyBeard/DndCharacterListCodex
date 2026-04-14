package com.vinni.dndcharacterlist.creation.rules

data class RulesContent(
    val races: List<RaceDefinition>,
    val classes: List<ClassDefinition>,
    val backgrounds: List<BackgroundDefinition>,
    val skills: List<SkillDefinition>
)
