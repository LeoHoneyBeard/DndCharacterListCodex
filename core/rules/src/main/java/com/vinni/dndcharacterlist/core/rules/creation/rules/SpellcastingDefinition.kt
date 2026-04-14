package com.vinni.dndcharacterlist.core.rules.creation.rules

import com.vinni.dndcharacterlist.core.rules.creation.model.AbilityType
import com.vinni.dndcharacterlist.core.rules.creation.model.SpellSlots

data class SpellcastingDefinition(
    val spellcastingAbility: AbilityType,
    val slotsByLevel: Map<Int, SpellSlots>
)


