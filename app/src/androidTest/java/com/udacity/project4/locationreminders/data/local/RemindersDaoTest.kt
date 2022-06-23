package com.udacity.project4.locationreminders.data.local

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.core.app.ApplicationProvider.getApplicationContext
import androidx.test.ext.junit.runners.AndroidJUnit4;
import androidx.test.filters.SmallTest;
import com.udacity.project4.locationreminders.data.dto.ReminderDTO

import org.junit.Before;
import org.junit.Rule;
import org.junit.runner.RunWith;

import kotlinx.coroutines.ExperimentalCoroutinesApi;
import kotlinx.coroutines.test.runBlockingTest
import org.hamcrest.CoreMatchers.`is`
import org.hamcrest.CoreMatchers.notNullValue
import org.hamcrest.MatcherAssert.assertThat
import org.junit.After
import org.junit.Test

@ExperimentalCoroutinesApi
@RunWith(AndroidJUnit4::class)
//Unit test the DAO
@SmallTest
class RemindersDaoTest {

    private lateinit var database: RemindersDatabase
    // Executes each task synchronously using Architecture Components.
    @get:Rule
    var instantExecutorRule = InstantTaskExecutorRule()

    @Before
    fun initDb() {
        // Using an in-memory database so that the information stored here disappears when the
        // process is killed.
        database = Room.inMemoryDatabaseBuilder(
            getApplicationContext(),
            RemindersDatabase::class.java
        ).build()
    }

    @After
    fun closeDb() = database.close()

    @Test
    fun insertReminderAndGetById() = runBlockingTest {
        val reminder = ReminderDTO("Title1", "Description1", "Location1", 1.0, 1.0, "1")
        database.reminderDao().saveReminder(reminder)

        val loaded = database.reminderDao().getReminderById(reminder.id)

        // THEN - The loaded data contains the expected values.
        assertThat<ReminderDTO>(loaded as ReminderDTO, notNullValue())
        assertThat(loaded.id, `is`(reminder.id))
        assertThat(loaded.title, `is`(reminder.title))
        assertThat(loaded.description, `is`(reminder.description))
        assertThat(loaded.location, `is`(reminder.location))
        assertThat(loaded.latitude, `is`(reminder.latitude))
        assertThat(loaded.longitude, `is`(reminder.longitude))
    }

    @Test
    fun insertRemindersAndDeleteAll() = runBlockingTest {
        val reminder1 = ReminderDTO("Title1", "Description1", "Location1", 1.0, 1.0, "1")
        val reminder2 = ReminderDTO("Title1", "Description1", "Location1", 1.0, 1.0, "2")
        val reminder3 = ReminderDTO("Title1", "Description1", "Location1", 1.0, 1.0, "3")
        val reminder4 = ReminderDTO("Title1", "Description1", "Location1", 1.0, 1.0, "4")

        database.reminderDao().saveReminder(reminder1)
        database.reminderDao().saveReminder(reminder2)
        database.reminderDao().saveReminder(reminder3)
        database.reminderDao().saveReminder(reminder4)

        database.reminderDao().deleteAllReminders()

        val loaded = database.reminderDao().getReminders()

        // THEN - The loaded data contains the expected values.
        assertThat(loaded.size, `is`(0))
    }

    @Test
    fun insertFourRemindersAndGetAllFourReminders() = runBlockingTest {
        val reminder1 = ReminderDTO("Title1", "Description1", "Location1", 1.0, 1.0, "1")
        val reminder2 = ReminderDTO("Title1", "Description1", "Location1", 1.0, 1.0, "2")
        val reminder3 = ReminderDTO("Title1", "Description1", "Location1", 1.0, 1.0, "3")
        val reminder4 = ReminderDTO("Title1", "Description1", "Location1", 1.0, 1.0, "4")

        database.reminderDao().saveReminder(reminder1)
        database.reminderDao().saveReminder(reminder2)
        database.reminderDao().saveReminder(reminder3)
        database.reminderDao().saveReminder(reminder4)

        val loaded = database.reminderDao().getReminders()

        // THEN - The loaded data contains the expected values.
        assertThat(loaded.size, `is`(4))
    }
}