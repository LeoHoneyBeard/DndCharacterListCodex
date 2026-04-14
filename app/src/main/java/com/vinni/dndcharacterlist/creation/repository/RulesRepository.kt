package com.vinni.dndcharacterlist.creation.repository

import com.vinni.dndcharacterlist.creation.model.Ruleset
import com.vinni.dndcharacterlist.creation.rules.RulesContent

interface RulesRepository {
    fun getRuleset(ruleset: Ruleset): RulesContent
}
