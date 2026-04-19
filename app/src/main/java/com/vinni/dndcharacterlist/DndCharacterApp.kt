package com.vinni.dndcharacterlist

import androidx.compose.runtime.Composable
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.rememberNavController
import com.vinni.dndcharacterlist.core.ui.theme.DndCharacterListTheme
import com.vinni.dndcharacterlist.feature.character.creation.CharacterCreationDestination
import com.vinni.dndcharacterlist.feature.character.creation.characterCreationGraph
import com.vinni.dndcharacterlist.feature.character.detail.CharacterDetailDestination
import com.vinni.dndcharacterlist.feature.character.detail.characterDetailGraph
import com.vinni.dndcharacterlist.feature.character.editor.CharacterEditorDestination
import com.vinni.dndcharacterlist.feature.character.editor.characterEditorGraph
import com.vinni.dndcharacterlist.feature.character.list.CharacterListDestination
import com.vinni.dndcharacterlist.feature.character.list.characterListGraph
import com.vinni.dndcharacterlist.feature.character.levelup.CharacterLevelUpDestination
import com.vinni.dndcharacterlist.feature.character.levelup.characterLevelUpGraph

@Composable
fun DndCharacterApp() {
    val navController = rememberNavController()

    DndCharacterListTheme {
        NavHost(
            navController = navController,
            startDestination = CharacterListDestination.route
        ) {
            characterListGraph(
                onCreateCharacter = { navController.navigate(CharacterCreationDestination.route) },
                onOpenCharacter = { id -> navController.navigate(CharacterDetailDestination.route(id)) }
            )
            characterCreationGraph(
                onBack = { navController.popBackStack() },
                onCharacterCreated = { createdId ->
                    navController.navigate(CharacterDetailDestination.route(createdId)) {
                        popUpTo(CharacterListDestination.route)
                    }
                }
            )
            characterDetailGraph(
                onBack = { navController.popBackStack() },
                onEditCharacter = { characterId ->
                    navController.navigate(CharacterEditorDestination.route(characterId))
                },
                onLevelUpCharacter = { characterId ->
                    navController.navigate(CharacterLevelUpDestination.route(characterId))
                },
                onDuplicateCharacter = { duplicatedId ->
                    navController.navigate(CharacterEditorDestination.route(duplicatedId))
                }
            )
            characterEditorGraph(
                onBack = { navController.popBackStack() },
                onSaved = { navController.popBackStack() },
                onDeleted = {
                    navController.popBackStack(CharacterListDestination.route, inclusive = false)
                }
            )
            characterLevelUpGraph(
                onBack = { navController.popBackStack() },
                onApplied = { navController.popBackStack() }
            )
        }
    }
}
