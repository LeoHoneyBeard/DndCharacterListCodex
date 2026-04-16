package com.vinni.dndcharacterlist.core.rules.creation.repository

import com.vinni.dndcharacterlist.core.rules.creation.model.AbilityType
import com.vinni.dndcharacterlist.core.rules.creation.model.Ruleset
import com.vinni.dndcharacterlist.core.rules.creation.model.SkillId
import com.vinni.dndcharacterlist.core.rules.creation.model.SpellSlots
import com.vinni.dndcharacterlist.core.rules.creation.rules.BackgroundDefinition
import com.vinni.dndcharacterlist.core.rules.creation.rules.ClassDefinition
import com.vinni.dndcharacterlist.core.rules.creation.rules.RaceDefinition
import com.vinni.dndcharacterlist.core.rules.creation.rules.RulesContent
import com.vinni.dndcharacterlist.core.rules.creation.rules.SkillDefinition
import com.vinni.dndcharacterlist.core.rules.creation.rules.SpellcastingDefinition
import com.vinni.dndcharacterlist.core.rules.creation.rules.SubclassDefinition
import com.vinni.dndcharacterlist.core.rules.creation.rules.SubraceDefinition

class Phb2014RulesRepository : RulesRepository {

    override fun getRuleset(ruleset: Ruleset): RulesContent {
        return when (ruleset) {
            Ruleset.PHB_2014 -> phb2014Content
        }
    }

    private val phb2014Content = RulesContent(
        races = listOf(
            RaceDefinition(
                id = "dwarf",
                name = "Dwarf",
                baseAbilityBonuses = mapOf(AbilityType.CONSTITUTION to 2),
                subraces = listOf(
                    SubraceDefinition("hill_dwarf", "Hill Dwarf", mapOf(AbilityType.WISDOM to 1)),
                    SubraceDefinition("mountain_dwarf", "Mountain Dwarf", mapOf(AbilityType.STRENGTH to 2))
                )
            ),
            RaceDefinition(
                id = "elf",
                name = "Elf",
                baseAbilityBonuses = mapOf(AbilityType.DEXTERITY to 2),
                subraces = listOf(
                    SubraceDefinition("high_elf", "High Elf", mapOf(AbilityType.INTELLIGENCE to 1)),
                    SubraceDefinition("wood_elf", "Wood Elf", mapOf(AbilityType.WISDOM to 1)),
                    SubraceDefinition("drow", "Drow", mapOf(AbilityType.CHARISMA to 1))
                )
            ),
            RaceDefinition(
                id = "halfling",
                name = "Halfling",
                baseAbilityBonuses = mapOf(AbilityType.DEXTERITY to 2),
                subraces = listOf(
                    SubraceDefinition("lightfoot", "Lightfoot", mapOf(AbilityType.CHARISMA to 1)),
                    SubraceDefinition("stout", "Stout", mapOf(AbilityType.CONSTITUTION to 1))
                )
            ),
            RaceDefinition(
                id = "human",
                name = "Human",
                baseAbilityBonuses = AbilityType.entries.associateWith { 1 }
            ),
            RaceDefinition(
                id = "dragonborn",
                name = "Dragonborn",
                baseAbilityBonuses = mapOf(
                    AbilityType.STRENGTH to 2,
                    AbilityType.CHARISMA to 1
                )
            ),
            RaceDefinition(
                id = "gnome",
                name = "Gnome",
                baseAbilityBonuses = mapOf(AbilityType.INTELLIGENCE to 2),
                subraces = listOf(
                    SubraceDefinition("forest_gnome", "Forest Gnome", mapOf(AbilityType.DEXTERITY to 1)),
                    SubraceDefinition("rock_gnome", "Rock Gnome", mapOf(AbilityType.CONSTITUTION to 1))
                )
            ),
            RaceDefinition(
                id = "half_elf",
                name = "Half-Elf",
                baseAbilityBonuses = mapOf(AbilityType.CHARISMA to 2)
            ),
            RaceDefinition(
                id = "half_orc",
                name = "Half-Orc",
                baseAbilityBonuses = mapOf(
                    AbilityType.STRENGTH to 2,
                    AbilityType.CONSTITUTION to 1
                )
            ),
            RaceDefinition(
                id = "tiefling",
                name = "Tiefling",
                baseAbilityBonuses = mapOf(
                    AbilityType.INTELLIGENCE to 1,
                    AbilityType.CHARISMA to 2
                )
            )
        ),
        classes = listOf(
            ClassDefinition(
                id = "barbarian",
                name = "Barbarian",
                hitDie = 12,
                primaryAbilities = setOf(AbilityType.STRENGTH, AbilityType.CONSTITUTION),
                savingThrowProficiencies = setOf(AbilityType.STRENGTH, AbilityType.CONSTITUTION),
                skillChoiceCount = 2,
                skillOptions = setOf("animal_handling", "athletics", "intimidation", "nature", "perception", "survival"),
                subclassLevel = 3
            ),
            ClassDefinition(
                id = "bard",
                name = "Bard",
                hitDie = 8,
                primaryAbilities = setOf(AbilityType.CHARISMA),
                savingThrowProficiencies = setOf(AbilityType.DEXTERITY, AbilityType.CHARISMA),
                skillChoiceCount = 3,
                skillOptions = allSkillIds,
                subclassLevel = 3,
                spellcasting = fullCaster(AbilityType.CHARISMA)
            ),
            ClassDefinition(
                id = "cleric",
                name = "Cleric",
                hitDie = 8,
                primaryAbilities = setOf(AbilityType.WISDOM),
                savingThrowProficiencies = setOf(AbilityType.WISDOM, AbilityType.CHARISMA),
                skillChoiceCount = 2,
                skillOptions = setOf("history", "insight", "medicine", "persuasion", "religion"),
                subclassLevel = 1,
                subclasses = listOf(
                    SubclassDefinition("knowledge_domain", "Knowledge Domain"),
                    SubclassDefinition("life_domain", "Life Domain"),
                    SubclassDefinition("light_domain", "Light Domain"),
                    SubclassDefinition("nature_domain", "Nature Domain"),
                    SubclassDefinition("tempest_domain", "Tempest Domain"),
                    SubclassDefinition("trickery_domain", "Trickery Domain"),
                    SubclassDefinition("war_domain", "War Domain")
                ),
                spellcasting = fullCaster(AbilityType.WISDOM)
            ),
            ClassDefinition(
                id = "druid",
                name = "Druid",
                hitDie = 8,
                primaryAbilities = setOf(AbilityType.WISDOM),
                savingThrowProficiencies = setOf(AbilityType.INTELLIGENCE, AbilityType.WISDOM),
                skillChoiceCount = 2,
                skillOptions = setOf("arcana", "animal_handling", "insight", "medicine", "nature", "perception", "religion", "survival"),
                subclassLevel = 2,
                spellcasting = fullCaster(AbilityType.WISDOM)
            ),
            ClassDefinition(
                id = "fighter",
                name = "Fighter",
                hitDie = 10,
                primaryAbilities = setOf(AbilityType.STRENGTH, AbilityType.DEXTERITY),
                savingThrowProficiencies = setOf(AbilityType.STRENGTH, AbilityType.CONSTITUTION),
                skillChoiceCount = 2,
                skillOptions = setOf("acrobatics", "animal_handling", "athletics", "history", "insight", "intimidation", "perception", "survival"),
                subclassLevel = 3
            ),
            ClassDefinition(
                id = "monk",
                name = "Monk",
                hitDie = 8,
                primaryAbilities = setOf(AbilityType.DEXTERITY, AbilityType.WISDOM),
                savingThrowProficiencies = setOf(AbilityType.STRENGTH, AbilityType.DEXTERITY),
                skillChoiceCount = 2,
                skillOptions = setOf("acrobatics", "athletics", "history", "insight", "religion", "stealth"),
                subclassLevel = 3
            ),
            ClassDefinition(
                id = "paladin",
                name = "Paladin",
                hitDie = 10,
                primaryAbilities = setOf(AbilityType.STRENGTH, AbilityType.CHARISMA),
                savingThrowProficiencies = setOf(AbilityType.WISDOM, AbilityType.CHARISMA),
                skillChoiceCount = 2,
                skillOptions = setOf("athletics", "insight", "intimidation", "medicine", "persuasion", "religion"),
                subclassLevel = 3,
                spellcasting = SpellcastingDefinition(
                    spellcastingAbility = AbilityType.CHARISMA,
                    slotsByLevel = emptyMap()
                )
            ),
            ClassDefinition(
                id = "ranger",
                name = "Ranger",
                hitDie = 10,
                primaryAbilities = setOf(AbilityType.DEXTERITY, AbilityType.WISDOM),
                savingThrowProficiencies = setOf(AbilityType.STRENGTH, AbilityType.DEXTERITY),
                skillChoiceCount = 3,
                skillOptions = setOf("animal_handling", "athletics", "insight", "investigation", "nature", "perception", "stealth", "survival"),
                subclassLevel = 3,
                spellcasting = SpellcastingDefinition(
                    spellcastingAbility = AbilityType.WISDOM,
                    slotsByLevel = emptyMap()
                )
            ),
            ClassDefinition(
                id = "rogue",
                name = "Rogue",
                hitDie = 8,
                primaryAbilities = setOf(AbilityType.DEXTERITY),
                savingThrowProficiencies = setOf(AbilityType.DEXTERITY, AbilityType.INTELLIGENCE),
                skillChoiceCount = 4,
                skillOptions = setOf("acrobatics", "athletics", "deception", "insight", "intimidation", "investigation", "perception", "performance", "persuasion", "sleight_of_hand", "stealth"),
                subclassLevel = 3
            ),
            ClassDefinition(
                id = "sorcerer",
                name = "Sorcerer",
                hitDie = 6,
                primaryAbilities = setOf(AbilityType.CHARISMA),
                savingThrowProficiencies = setOf(AbilityType.CONSTITUTION, AbilityType.CHARISMA),
                skillChoiceCount = 2,
                skillOptions = setOf("arcana", "deception", "insight", "intimidation", "persuasion", "religion"),
                subclassLevel = 1,
                subclasses = listOf(
                    SubclassDefinition("draconic_bloodline", "Draconic Bloodline"),
                    SubclassDefinition("wild_magic", "Wild Magic")
                ),
                spellcasting = fullCaster(AbilityType.CHARISMA)
            ),
            ClassDefinition(
                id = "warlock",
                name = "Warlock",
                hitDie = 8,
                primaryAbilities = setOf(AbilityType.CHARISMA),
                savingThrowProficiencies = setOf(AbilityType.WISDOM, AbilityType.CHARISMA),
                skillChoiceCount = 2,
                skillOptions = setOf("arcana", "deception", "history", "intimidation", "investigation", "nature", "religion"),
                subclassLevel = 1,
                subclasses = listOf(
                    SubclassDefinition("the_archfey", "The Archfey"),
                    SubclassDefinition("the_fiend", "The Fiend"),
                    SubclassDefinition("the_great_old_one", "The Great Old One")
                ),
                spellcasting = SpellcastingDefinition(
                    spellcastingAbility = AbilityType.CHARISMA,
                    slotsByLevel = mapOf(1 to SpellSlots(firstLevel = 1))
                )
            ),
            ClassDefinition(
                id = "wizard",
                name = "Wizard",
                hitDie = 6,
                primaryAbilities = setOf(AbilityType.INTELLIGENCE),
                savingThrowProficiencies = setOf(AbilityType.INTELLIGENCE, AbilityType.WISDOM),
                skillChoiceCount = 2,
                skillOptions = setOf("arcana", "history", "insight", "investigation", "medicine", "religion"),
                subclassLevel = 2,
                subclasses = listOf(
                    SubclassDefinition("abjuration", "School of Abjuration"),
                    SubclassDefinition("conjuration", "School of Conjuration"),
                    SubclassDefinition("divination", "School of Divination"),
                    SubclassDefinition("enchantment", "School of Enchantment"),
                    SubclassDefinition("evocation", "School of Evocation"),
                    SubclassDefinition("illusion", "School of Illusion"),
                    SubclassDefinition("necromancy", "School of Necromancy"),
                    SubclassDefinition("transmutation", "School of Transmutation")
                ),
                spellcasting = fullCaster(AbilityType.INTELLIGENCE)
            )
        ),
        backgrounds = listOf(
            BackgroundDefinition("acolyte", "Acolyte", setOf("insight", "religion")),
            BackgroundDefinition("charlatan", "Charlatan", setOf("deception", "sleight_of_hand")),
            BackgroundDefinition("criminal", "Criminal", setOf("deception", "stealth")),
            BackgroundDefinition("entertainer", "Entertainer", setOf("acrobatics", "performance")),
            BackgroundDefinition("folk_hero", "Folk Hero", setOf("animal_handling", "survival")),
            BackgroundDefinition("guild_artisan", "Guild Artisan", setOf("insight", "persuasion")),
            BackgroundDefinition("hermit", "Hermit", setOf("medicine", "religion")),
            BackgroundDefinition("noble", "Noble", setOf("history", "persuasion")),
            BackgroundDefinition("outlander", "Outlander", setOf("athletics", "survival")),
            BackgroundDefinition("sage", "Sage", setOf("arcana", "history")),
            BackgroundDefinition("sailor", "Sailor", setOf("athletics", "perception")),
            BackgroundDefinition("soldier", "Soldier", setOf("athletics", "intimidation")),
            BackgroundDefinition("urchin", "Urchin", setOf("sleight_of_hand", "stealth"))
        ),
        skills = listOf(
            skill("acrobatics", "Acrobatics", AbilityType.DEXTERITY),
            skill("animal_handling", "Animal Handling", AbilityType.WISDOM),
            skill("arcana", "Arcana", AbilityType.INTELLIGENCE),
            skill("athletics", "Athletics", AbilityType.STRENGTH),
            skill("deception", "Deception", AbilityType.CHARISMA),
            skill("history", "History", AbilityType.INTELLIGENCE),
            skill("insight", "Insight", AbilityType.WISDOM),
            skill("intimidation", "Intimidation", AbilityType.CHARISMA),
            skill("investigation", "Investigation", AbilityType.INTELLIGENCE),
            skill("medicine", "Medicine", AbilityType.WISDOM),
            skill("nature", "Nature", AbilityType.INTELLIGENCE),
            skill("perception", "Perception", AbilityType.WISDOM),
            skill("performance", "Performance", AbilityType.CHARISMA),
            skill("persuasion", "Persuasion", AbilityType.CHARISMA),
            skill("religion", "Religion", AbilityType.INTELLIGENCE),
            skill("sleight_of_hand", "Sleight of Hand", AbilityType.DEXTERITY),
            skill("stealth", "Stealth", AbilityType.DEXTERITY),
            skill("survival", "Survival", AbilityType.WISDOM)
        )
    )

    private fun skill(id: SkillId, name: String, ability: AbilityType): SkillDefinition {
        return SkillDefinition(id = id, name = name, ability = ability)
    }

    private fun fullCaster(ability: AbilityType): SpellcastingDefinition {
        return SpellcastingDefinition(
            spellcastingAbility = ability,
            slotsByLevel = mapOf(1 to SpellSlots(firstLevel = 2))
        )
    }

    private companion object {
        val allSkillIds: Set<SkillId> = setOf(
            "acrobatics",
            "animal_handling",
            "arcana",
            "athletics",
            "deception",
            "history",
            "insight",
            "intimidation",
            "investigation",
            "medicine",
            "nature",
            "perception",
            "performance",
            "persuasion",
            "religion",
            "sleight_of_hand",
            "stealth",
            "survival"
        )
    }
}


