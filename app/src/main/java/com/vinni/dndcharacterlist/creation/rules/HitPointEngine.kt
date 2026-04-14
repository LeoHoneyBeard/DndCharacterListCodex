package com.vinni.dndcharacterlist.creation.rules

class HitPointEngine {

    fun levelOneHitPoints(hitDie: Int, constitutionModifier: Int): Int {
        return hitDie + constitutionModifier
    }
}
