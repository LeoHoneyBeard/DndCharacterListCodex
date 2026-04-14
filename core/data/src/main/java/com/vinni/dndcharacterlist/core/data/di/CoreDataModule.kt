package com.vinni.dndcharacterlist.core.data.di

import com.vinni.dndcharacterlist.core.data.local.CharacterDatabase
import com.vinni.dndcharacterlist.core.data.repository.RoomCharacterRepository
import com.vinni.dndcharacterlist.core.domain.repository.CharacterRepository
import org.koin.android.ext.koin.androidContext
import org.koin.dsl.module

val coreDataModule = module {
    single { CharacterDatabase.getInstance(androidContext()) }
    single { get<CharacterDatabase>().characterDao() }
    single<CharacterRepository> { RoomCharacterRepository(get()) }
}
