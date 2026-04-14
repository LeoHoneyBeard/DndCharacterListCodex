package com.vinni.dndcharacterlist.core.rules.creation.rules

import com.vinni.dndcharacterlist.core.rules.creation.model.SpellSlots

class SpellProgressionEngine {

    fun slotsAtLevel(classDefinition: ClassDefinition?, level: Int): SpellSlots? {
        return classDefinition?.spellcasting?.slotsByLevel?.get(level)
    }
}


