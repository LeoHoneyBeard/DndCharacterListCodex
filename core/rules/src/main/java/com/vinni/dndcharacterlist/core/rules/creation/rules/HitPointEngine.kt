package com.vinni.dndcharacterlist.core.rules.creation.rules

class HitPointEngine {

    fun levelOneHitPoints(hitDie: Int, constitutionModifier: Int): Int {
        return hitDie + constitutionModifier
    }

    fun abilityModifier(score: Int): Int {
        return Math.floorDiv(score - 10, 2)
    }
}

