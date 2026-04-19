package com.vinni.dndcharacterlist.core.rules.editor

data class CharacterEditorDraft(
    val ruleset: String,
    val name: String,
    val characterClass: String,
    val subclass: String,
    val race: String,
    val subraceId: String,
    val alignment: String,
    val background: String,
    val level: String,
    val armorClass: String,
    val hitPoints: String,
    val strength: String,
    val dexterity: String,
    val constitution: String,
    val intelligence: String,
    val wisdom: String,
    val charisma: String,
    val notes: String
)
