package com.udacity.project4.locationreminders.data.local

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.filters.MediumTest
import com.udacity.project4.locationreminders.data.dto.ReminderDTO
import com.udacity.project4.locationreminders.data.dto.Result
import com.udacity.project4.locationreminders.data.dto.succeeded
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.test.runBlockingTest
import org.hamcrest.CoreMatchers.`is`
import org.hamcrest.CoreMatchers.instanceOf
import org.hamcrest.MatcherAssert.assertThat
import org.hamcrest.core.IsEqual
import org.junit.*
import org.junit.runner.RunWith

@ExperimentalCoroutinesApi
@RunWith(AndroidJUnit4::class)
//Medium Test to test the repository
@MediumTest
class RemindersLocalRepositoryTest {

    @get:Rule
    var instantExecutorRule = InstantTaskExecutorRule()

    private lateinit var remindersLocalRepository: RemindersLocalRepository
    private lateinit var database: RemindersDatabase

    @Before
    fun setup() {
        database = Room.inMemoryDatabaseBuilder(
            ApplicationProvider.getApplicationContext(),
            RemindersDatabase::class.java
        )
            .allowMainThreadQueries()
            .build()

        remindersLocalRepository =
            RemindersLocalRepository(
                database.reminderDao(),
                Dispatchers.Main
            )
    }

    @After
    fun cleanUp() {
        database.close()
    }

    @Test
    fun saveReminderAndGetSavedReminder() = runBlocking {
        val reminder = ReminderDTO("Title1", "Description1", "Location1", 1.0, 1.0, "1")
        remindersLocalRepository.saveReminder(reminder)

        val result = remindersLocalRepository.getReminder(reminder.id)

        assertThat(result.succeeded, `is`(true))
        result as Result.Success
        assertThat(result.data.title, `is`("Title1"))
        assertThat(result.data.description, `is`("Description1"))
        assertThat(result.data.location, `is`("Location1"))
        assertThat(result.data.id, `is`("1"))
        assertThat(result.data.latitude, `is`(1.0))
        assertThat(result.data.longitude, `is`(1.0))
    }

    @Test
    fun saveFourRemindersAndGetSavedRemindersCount() = runBlocking {
        val reminder1 = ReminderDTO("Title1", "Description1", "Location1", 1.0, 1.0, "1")
        val reminder2 = ReminderDTO("Title2", "Description2", "Location2", 2.0, 2.0, "2")
        val reminder3 = ReminderDTO("Title3", "Description3", "Location3", 3.0, 3.0, "3")
        val reminder4 = ReminderDTO("Title4", "Description4", "Location3", 4.0, 4.0, "4")

        remindersLocalRepository.saveReminder(reminder1)
        remindersLocalRepository.saveReminder(reminder2)
        remindersLocalRepository.saveReminder(reminder3)
        remindersLocalRepository.saveReminder(reminder4)

        val result = remindersLocalRepository.getReminders()

        assertThat(result.succeeded, `is`(true))
        result as Result.Success
        assertThat(result.data.count(), `is`(4))
        val sortedResult = result.data.sortedBy { it.id }
        assertThat(sortedResult[0].id, `is`("1"))
        assertThat(sortedResult[1].id, `is`("2"))
        assertThat(sortedResult[2].id, `is`("3"))
        assertThat(sortedResult[3].id, `is`("4"))
    }

    @Test
    fun saveFourRemindersAndDeleteAll() = runBlocking {
        val reminder1 = ReminderDTO("Title1", "Description1", "Location1", 1.0, 1.0, "1")
        val reminder2 = ReminderDTO("Title2", "Description2", "Location2", 2.0, 2.0, "2")
        val reminder3 = ReminderDTO("Title3", "Description3", "Location3", 3.0, 3.0, "3")
        val reminder4 = ReminderDTO("Title4", "Description4", "Location3", 4.0, 4.0, "4")

        remindersLocalRepository.saveReminder(reminder1)
        remindersLocalRepository.saveReminder(reminder2)
        remindersLocalRepository.saveReminder(reminder3)
        remindersLocalRepository.saveReminder(reminder4)

        remindersLocalRepository.deleteAllReminders()
        val result = remindersLocalRepository.getReminders()

        assertThat(result.succeeded, `is`(true))
        result as Result.Success
        assertThat(result.data.count(), `is`(0))
    }

    @Test
    fun saveOneReminderAndGetWithWrongId() = runBlocking {
        val reminder1 = ReminderDTO("Title1", "Description1", "Location1", 1.0, 1.0, "1")

        remindersLocalRepository.saveReminder(reminder1)

        val result = remindersLocalRepository.getReminder("2")

        assertThat(result.succeeded, `is`(false))
        result as Result.Error
        assertThat(result.message, `is`("Reminder not found!"))
    }
}