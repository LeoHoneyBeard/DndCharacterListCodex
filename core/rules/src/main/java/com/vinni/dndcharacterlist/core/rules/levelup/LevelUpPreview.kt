package com.vinni.dndcharacterlist.core.rules.levelup

data class LevelUpPreview(
    val currentLevel: Int,
    val nextLevel: Int,
    val currentHitPoints: Int,
    val currentHitPointsMax: Int,
    val hitPointIncrease: Int,
    val nextHitPoints: Int,
    val nextHitPointsMax: Int,
    val requirements: List<LevelUpRequirement>,
    val blockingReason: String? = null
)
