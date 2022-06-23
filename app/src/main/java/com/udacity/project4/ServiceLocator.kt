package com.udacity.project4

import android.content.Context
import androidx.annotation.VisibleForTesting
import androidx.room.Room
import com.udacity.project4.locationreminders.data.ReminderDataSource
import com.udacity.project4.locationreminders.data.local.RemindersDao
import com.udacity.project4.locationreminders.data.local.RemindersDatabase
import com.udacity.project4.locationreminders.data.local.RemindersLocalRepository
import kotlinx.coroutines.runBlocking

object ServiceLocator {

    private val lock = Any()
    private var database: RemindersDatabase? = null
    @Volatile
    var reminderDataSource: ReminderDataSource? = null
        @VisibleForTesting set

    fun provideReminderDataSource(context: Context): ReminderDataSource {
        synchronized(this) {
            return reminderDataSource ?: createReminderDataSource(context)
        }
    }

    private fun createReminderDataSource(context: Context): ReminderDataSource {
        val newDataSource = RemindersLocalRepository(getDao(context))
        reminderDataSource = newDataSource
        return newDataSource
    }

    private fun getDao(context: Context): RemindersDao {
        val database = database ?: createDatabase(context)
        return database.reminderDao()
    }

    private fun createDatabase(context: Context): RemindersDatabase {
        val result = Room.databaseBuilder(
            context.applicationContext,
            RemindersDatabase::class.java, "locationReminders.db"
        ).build()
        database = result
        return result
    }

    @VisibleForTesting
    fun resetRepository() {
        synchronized(lock) {
            runBlocking {
                reminderDataSource?.deleteAllReminders()
            }
            // Clear all data to avoid test pollution.
            database?.apply {
                clearAllTables()
                close()
            }
            database = null
            reminderDataSource = null
        }
    }
}