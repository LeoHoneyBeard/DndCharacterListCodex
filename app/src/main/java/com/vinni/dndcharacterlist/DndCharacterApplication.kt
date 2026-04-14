package com.vinni.dndcharacterlist

import android.app.Application
import com.vinni.dndcharacterlist.core.data.di.coreDataModule
import com.vinni.dndcharacterlist.core.rules.di.coreRulesModule
import com.vinni.dndcharacterlist.feature.character.creation.characterCreationModule
import com.vinni.dndcharacterlist.feature.character.detail.characterDetailModule
import com.vinni.dndcharacterlist.feature.character.editor.characterEditorModule
import com.vinni.dndcharacterlist.feature.character.list.characterListModule
import org.koin.android.ext.koin.androidContext
import org.koin.core.context.startKoin

class DndCharacterApplication : Application() {
    override fun onCreate() {
        super.onCreate()

        startKoin {
            androidContext(this@DndCharacterApplication)
            modules(
                coreDataModule,
                coreRulesModule,
                characterListModule,
                characterDetailModule,
                characterEditorModule,
                characterCreationModule
            )
        }
    }
}
