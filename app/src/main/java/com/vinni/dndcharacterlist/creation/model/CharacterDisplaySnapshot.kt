package com.vinni.dndcharacterlist.creation.model

data class CharacterDisplaySnapshot(
    val raceLabel: String = "",
    val classLabel: String = "",
    val subclassLabel: String? = null,
    val backgroundLabel: String = ""
)
