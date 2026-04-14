package com.vinni.dndcharacterlist.core.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "characters")
data class CharacterEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val ruleset: String = "",
    val name: String,
    val classId: String = "",
    val characterClass: String,
    val subclassId: String = "",
    val subclass: String,
    val raceId: String = "",
    val race: String,
    val subraceId: String = "",
    val alignment: String,
    val backgroundId: String = "",
    val background: String,
    val level: Int,
    val abilityMethod: String = "",
    val armorClass: Int,
    val hitPoints: Int,
    val hitPointsMax: Int = hitPoints,
    val strength: Int,
    val dexterity: Int,
    val constitution: Int,
    val intelligence: Int,
    val wisdom: Int,
    val charisma: Int,
    val savingThrowProficienciesSerialized: String = "",
    val skillProficienciesSerialized: String = "",
    val notes: String,
    val updatedAt: Long
)
