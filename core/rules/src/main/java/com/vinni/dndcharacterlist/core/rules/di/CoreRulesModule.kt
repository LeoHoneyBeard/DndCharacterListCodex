package com.vinni.dndcharacterlist.core.rules.di

import com.vinni.dndcharacterlist.core.rules.creation.mapper.CharacterCreationMapper
import com.vinni.dndcharacterlist.core.rules.creation.repository.Phb2014RulesRepository
import com.vinni.dndcharacterlist.core.rules.creation.repository.RulesRepository
import com.vinni.dndcharacterlist.core.rules.creation.rules.CharacterCreationRulesEngine
import com.vinni.dndcharacterlist.core.rules.creation.rules.HitPointEngine
import com.vinni.dndcharacterlist.core.rules.levelup.CharacterLevelUpRules
import org.koin.dsl.module

val coreRulesModule = module {
    single<RulesRepository> { Phb2014RulesRepository() }
    factory { CharacterCreationMapper() }
    factory { CharacterCreationRulesEngine(get()) }
    factory { HitPointEngine() }
    factory { CharacterLevelUpRules(get(), get()) }
}
