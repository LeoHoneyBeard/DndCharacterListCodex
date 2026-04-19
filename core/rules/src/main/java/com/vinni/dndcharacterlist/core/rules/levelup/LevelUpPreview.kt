package com.vinni.dndcharacterlist.core.rules.levelup

import com.vinni.dndcharacterlist.core.rules.creation.rules.SubclassDefinition

data class LevelUpPreview(
    val currentLevel: Int,
    val nextLevel: Int,
    val currentHitPoints: Int,
    val currentHitPointsMax: Int,
    val hitPointIncrease: Int,
    val nextHitPoints: Int,
    val nextHitPointsMax: Int,
    val requiresSubclassSelection: Boolean,
    val availableSubclasses: List<SubclassDefinition>,
    val blockingReason: String? = null
)
