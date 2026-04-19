package com.vinni.dndcharacterlist.core.rules

import com.vinni.dndcharacterlist.core.rules.creation.repository.Phb2014RulesRepository
import com.vinni.dndcharacterlist.core.rules.editor.CharacterEditorDraft
import com.vinni.dndcharacterlist.core.rules.editor.CharacterEditorRules
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class CharacterEditorRulesTest {

    private val rules = CharacterEditorRules(Phb2014RulesRepository())

    @Test
    fun validDraftResolvesCanonicalSelections() {
        val resolved = rules.resolveSelections(validDraft())

        assertEquals("wizard", resolved?.classDefinition?.id)
        assertEquals("human", resolved?.race?.id)
        assertEquals("sage", resolved?.background?.id)
    }

    @Test
    fun invalidClassIsRejected() {
        val issues = rules.validate(validDraft().copy(characterClass = "Time Lord"))

        assertTrue(issues.any { it.key == "class_required" })
    }

    @Test
    fun raceWithRequiredSubraceIsRejectedWhenSubraceIsMissing() {
        val issues = rules.validate(validDraft().copy(race = "Elf", subraceId = ""))

        assertTrue(issues.any { it.key == "subrace_required" })
    }

    private fun validDraft(): CharacterEditorDraft {
        return CharacterEditorDraft(
            ruleset = "PHB_2014",
            name = "Aylin",
            characterClass = "Wizard",
            subclass = "",
            race = "Human",
            subraceId = "",
            alignment = "Neutral Good",
            background = "Sage",
            level = "1",
            armorClass = "12",
            hitPoints = "8",
            strength = "8",
            dexterity = "14",
            constitution = "12",
            intelligence = "15",
            wisdom = "10",
            charisma = "13",
            notes = ""
        )
    }
}
