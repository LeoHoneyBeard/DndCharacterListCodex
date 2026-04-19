package com.vinni.dndcharacterlist.core.rules.levelup

import com.vinni.dndcharacterlist.core.rules.creation.rules.SubclassDefinition

sealed interface LevelUpRequirement {
    val title: String
    val description: String

    data class SubclassSelection(
        val options: List<SubclassDefinition>,
        override val title: String = "Subclass Choice",
        override val description: String = "Choose a subclass before applying the level up."
    ) : LevelUpRequirement

    data class UnsupportedChoice(
        override val title: String,
        override val description: String
    ) : LevelUpRequirement
}
