package com.vinni.dndcharacterlist.feature.character.editor

import com.vinni.dndcharacterlist.feature.character.editor.presentation.CharacterEditorUiState
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class CharacterEditorUiStateTest {
    @Test
    fun saveEnabledRequiresName() {
        assertFalse(CharacterEditorUiState(name = "").isSaveEnabled)
        assertTrue(CharacterEditorUiState(name = "Minsc").isSaveEnabled)
    }

    @Test
    fun saveDisabledWhileSubmitting() {
        assertFalse(CharacterEditorUiState(name = "Lae'zel", isSaving = true).isSaveEnabled)
    }

    @Test
    fun invalidLevelProducesValidationMessage() {
        val state = CharacterEditorUiState(name = "Shadowheart", level = "99")

        assertEquals("Level must be between 1 and 20.", state.validate())
    }

    @Test
    fun fieldErrorsExposeInlineValidationState() {
        val state = CharacterEditorUiState(
            name = "",
            level = "",
            armorClass = "",
            hitPoints = "",
            strength = "31"
        )

        assertEquals("Name is required.", state.nameError)
        assertEquals("Level must be between 1 and 20.", state.levelError)
        assertEquals("Armor Class must be 0 or higher.", state.armorClassError)
        assertEquals("Hit Points must be 0 or higher.", state.hitPointsError)
        assertEquals("Ability scores must be between 1 and 30.", state.abilityScoreError)
    }

    @Test
    fun proficiencyBonusIsDerivedFromLevel() {
        assertEquals(2, CharacterEditorUiState(level = "1").proficiencyBonus)
        assertEquals(3, CharacterEditorUiState(level = "5").proficiencyBonus)
        assertEquals(6, CharacterEditorUiState(level = "20").proficiencyBonus)
    }

    @Test
    fun abilityModifierUsesFloorRulesForOddValuesBelowTen() {
        val state = CharacterEditorUiState()

        assertEquals(-5, state.abilityModifier("1"))
        assertEquals(-4, state.abilityModifier("3"))
        assertEquals(-3, state.abilityModifier("5"))
        assertEquals(-2, state.abilityModifier("7"))
        assertEquals(-1, state.abilityModifier("9"))
    }

    @Test
    fun validStateHasNoValidationError() {
        val state = CharacterEditorUiState(
            name = "Gale",
            level = "5",
            armorClass = "12",
            hitPoints = "30",
            strength = "8",
            dexterity = "14",
            constitution = "13",
            intelligence = "18",
            wisdom = "12",
            charisma = "10"
        )

        assertNull(state.validate())
    }
}


