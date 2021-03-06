package com.abduqodirov.invitex.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.abduqodirov.invitex.models.Mehmon

@Database(entities = [Mehmon::class], version = 1, exportSchema = false)
abstract class MehmonDatabase : RoomDatabase() {

    abstract val mehmonDatabaseDao: MehmonDatabaseDao

    companion object {

        @Volatile
        private var INSTANCE: MehmonDatabase? = null

        fun getInstance(context: Context): MehmonDatabase {
            synchronized(this) {
                var instance = INSTANCE

                if (instance == null) {
                    instance = Room.databaseBuilder(
                        context.applicationContext,
                        MehmonDatabase::class.java,
                        "invite_database"
                    )
                        .fallbackToDestructiveMigration()
                        .build()

                    INSTANCE = instance
                }

                return instance
            }
        }

    }

}