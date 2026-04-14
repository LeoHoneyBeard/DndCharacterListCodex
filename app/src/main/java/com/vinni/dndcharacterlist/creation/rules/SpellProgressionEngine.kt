package com.vinni.dndcharacterlist.creation.rules

import com.vinni.dndcharacterlist.creation.model.SpellSlots

class SpellProgressionEngine {

    fun slotsAtLevel(classDefinition: ClassDefinition?, level: Int): SpellSlots? {
        return classDefinition?.spellcasting?.slotsByLevel?.get(level)
    }
}
