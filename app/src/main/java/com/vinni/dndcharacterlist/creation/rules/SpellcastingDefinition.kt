package com.vinni.dndcharacterlist.creation.rules

import com.vinni.dndcharacterlist.creation.model.AbilityType
import com.vinni.dndcharacterlist.creation.model.SpellSlots

data class SpellcastingDefinition(
    val spellcastingAbility: AbilityType,
    val slotsByLevel: Map<Int, SpellSlots>
)
