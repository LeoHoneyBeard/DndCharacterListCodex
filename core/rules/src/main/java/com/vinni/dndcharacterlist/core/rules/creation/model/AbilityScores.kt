package com.vinni.dndcharacterlist.core.rules.creation.model

data class AbilityScores(
    val strength: Int,
    val dexterity: Int,
    val constitution: Int,
    val intelligence: Int,
    val wisdom: Int,
    val charisma: Int
) {
    operator fun get(abilityType: AbilityType): Int {
        return when (abilityType) {
            AbilityType.STRENGTH -> strength
            AbilityType.DEXTERITY -> dexterity
            AbilityType.CONSTITUTION -> constitution
            AbilityType.INTELLIGENCE -> intelligence
            AbilityType.WISDOM -> wisdom
            AbilityType.CHARISMA -> charisma
        }
    }

    fun withBonus(bonuses: Map<AbilityType, Int>): AbilityScores {
        return copy(
            strength = strength + (bonuses[AbilityType.STRENGTH] ?: 0),
            dexterity = dexterity + (bonuses[AbilityType.DEXTERITY] ?: 0),
            constitution = constitution + (bonuses[AbilityType.CONSTITUTION] ?: 0),
            intelligence = intelligence + (bonuses[AbilityType.INTELLIGENCE] ?: 0),
            wisdom = wisdom + (bonuses[AbilityType.WISDOM] ?: 0),
            charisma = charisma + (bonuses[AbilityType.CHARISMA] ?: 0)
        )
    }
}

