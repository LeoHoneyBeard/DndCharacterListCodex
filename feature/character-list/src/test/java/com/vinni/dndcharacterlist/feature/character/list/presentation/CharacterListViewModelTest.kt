package com.vinni.dndcharacterlist.feature.character.list.presentation

import com.vinni.dndcharacterlist.core.domain.model.CharacterRecord
import org.junit.Assert.assertEquals
import org.junit.Test

class CharacterListViewModelTest {

    @Test
    fun `toListItem omits blank and null-like labels from summary`() {
        val record = CharacterRecord(
            id = 42L,
            name = "Nyx",
            characterClass = "null",
            subclass = " ",
            race = " Tiefling ",
            alignment = "Chaotic Neutral",
            background = "Criminal",
            level = 3,
            armorClass = 15,
            hitPoints = 21,
            strength = 8,
            dexterity = 16,
            constitution = 14,
            intelligence = 12,
            wisdom = 10,
            charisma = 18,
            notes = "",
            updatedAt = 1L
        )

        assertEquals(
            CharacterListItem(
                id = 42L,
                name = "Nyx",
                summary = "Lvl 3 | Tiefling | AC 15 | HP 21"
            ),
            record.toListItem()
        )
    }
}
