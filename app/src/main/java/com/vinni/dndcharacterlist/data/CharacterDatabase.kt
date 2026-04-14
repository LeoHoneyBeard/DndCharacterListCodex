package com.vinni.dndcharacterlist.data

import android.content.Context
import androidx.room.Database
import androidx.room.migration.Migration
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.sqlite.db.SupportSQLiteDatabase

@Database(
    entities = [CharacterEntity::class],
    version = 3,
    exportSchema = false
)
abstract class CharacterDatabase : RoomDatabase() {
    abstract fun characterDao(): CharacterDao

    companion object {
        @Volatile
        private var instance: CharacterDatabase? = null

        fun getInstance(context: Context): CharacterDatabase {
            return instance ?: synchronized(this) {
                instance ?: Room.databaseBuilder(
                    context.applicationContext,
                    CharacterDatabase::class.java,
                    "dnd_characters.db"
                )
                    .addMigrations(Migration2To3)
                    .build()
                    .also { instance = it }
            }
        }

        private val Migration2To3 = object : Migration(2, 3) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("ALTER TABLE characters ADD COLUMN ruleset TEXT NOT NULL DEFAULT ''")
                db.execSQL("ALTER TABLE characters ADD COLUMN classId TEXT NOT NULL DEFAULT ''")
                db.execSQL("ALTER TABLE characters ADD COLUMN subclassId TEXT NOT NULL DEFAULT ''")
                db.execSQL("ALTER TABLE characters ADD COLUMN raceId TEXT NOT NULL DEFAULT ''")
                db.execSQL("ALTER TABLE characters ADD COLUMN subraceId TEXT NOT NULL DEFAULT ''")
                db.execSQL("ALTER TABLE characters ADD COLUMN backgroundId TEXT NOT NULL DEFAULT ''")
                db.execSQL("ALTER TABLE characters ADD COLUMN abilityMethod TEXT NOT NULL DEFAULT ''")
                db.execSQL("ALTER TABLE characters ADD COLUMN hitPointsMax INTEGER NOT NULL DEFAULT 0")
                db.execSQL("ALTER TABLE characters ADD COLUMN savingThrowProficienciesSerialized TEXT NOT NULL DEFAULT ''")
                db.execSQL("ALTER TABLE characters ADD COLUMN skillProficienciesSerialized TEXT NOT NULL DEFAULT ''")
                db.execSQL("UPDATE characters SET hitPointsMax = hitPoints")
            }
        }
    }
}
