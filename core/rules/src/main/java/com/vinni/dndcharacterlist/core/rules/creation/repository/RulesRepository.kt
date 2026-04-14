package com.vinni.dndcharacterlist.core.rules.creation.repository

import com.vinni.dndcharacterlist.core.rules.creation.model.Ruleset
import com.vinni.dndcharacterlist.core.rules.creation.rules.RulesContent

interface RulesRepository {
    fun getRuleset(ruleset: Ruleset): RulesContent
}


