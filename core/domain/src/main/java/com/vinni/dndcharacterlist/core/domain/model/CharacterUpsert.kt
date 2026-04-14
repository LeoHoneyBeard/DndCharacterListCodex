package com.vinni.dndcharacterlist.core.domain.model

data class CharacterUpsert(
    val id: Long? = null,
    val name: String,
    val characterClass: String,
    val subclass: String,
    val race: String,
    val alignment: String,
    val background: String,
    val level: Int,
    val armorClass: Int,
    val hitPoints: Int,
    val strength: Int,
    val dexterity: Int,
    val constitution: Int,
    val intelligence: Int,
    val wisdom: Int,
    val charisma: Int,
    val notes: String
)
