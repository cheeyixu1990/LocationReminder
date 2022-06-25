package com.udacity.project4.locationreminders.reminderslist

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.udacity.project4.locationreminders.data.FakeDataSource
import com.udacity.project4.locationreminders.data.dto.ReminderDTO
import com.udacity.project4.locationreminders.getOrAwaitValue
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.test.*
import org.hamcrest.CoreMatchers
import org.hamcrest.CoreMatchers.`is`
import org.hamcrest.MatcherAssert
import org.hamcrest.MatcherAssert.assertThat
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TestWatcher
import org.junit.runner.Description
import org.junit.runner.RunWith
import org.koin.test.AutoCloseKoinTest

@RunWith(AndroidJUnit4::class)
@ExperimentalCoroutinesApi
class RemindersListViewModelTest: AutoCloseKoinTest() {

    private lateinit var remindersListViewModel: RemindersListViewModel

    private lateinit var reminderDataSource: FakeDataSource

    @ExperimentalCoroutinesApi
    @get:Rule
    var mainCoroutineRule = MainCoroutineRule()

    // Executes each task synchronously using Architecture Components.
    @get:Rule
    var instantExecutorRule = InstantTaskExecutorRule()


    @Before
    fun setupViewModel() {
        val reminder1 = ReminderDTO("Title1", "Description1", "Location1", 1.0, 1.0, "1")
        val reminder2 = ReminderDTO("Title2", "Description2", "Location2", 2.0, 2.0, "2")
        val reminder3 = ReminderDTO("Title3", "Description3", "Location3", 3.0, 3.0, "3")
        val reminder4 = ReminderDTO("Title4", "Description4", "Location3", 4.0, 4.0, "4")
        val reminders = listOf(reminder1, reminder2, reminder3, reminder4)
        reminderDataSource = FakeDataSource(reminders.toMutableList())

        remindersListViewModel = RemindersListViewModel(ApplicationProvider.getApplicationContext(), reminderDataSource)
    }

    @Test
    fun invalidateShowNoData_noDataReturnTrue() {
        remindersListViewModel.invalidateShowNoData()

        assertThat(remindersListViewModel.showNoData.getOrAwaitValue(), `is`(true))
    }

    @Test
    fun loadReminders_noDataReturnInDataSource_returnError() {
        reminderDataSource.setReturnError(true)
        remindersListViewModel.loadReminders()

        assertThat(remindersListViewModel.showSnackBar.getOrAwaitValue(), `is`("Test - Tasks not found"))
    }

    @Test
    fun loadReminders_data_invalidateShowNoData_returnFalse() {
        remindersListViewModel.loadReminders()

        assertThat(remindersListViewModel.remindersList.getOrAwaitValue().count(), `is`(4))
        assertThat(remindersListViewModel.showNoData.getOrAwaitValue(), `is`(false))
    }

    @Test
    fun remindersLoading_testLoading() {
        mainCoroutineRule.pauseDispatcher()

        remindersListViewModel.loadReminders()

        assertThat(remindersListViewModel.showLoading.getOrAwaitValue(), `is`(true))

        mainCoroutineRule.resumeDispatcher()

        assertThat(remindersListViewModel.showLoading.getOrAwaitValue(), `is`(false))
    }
}

@ExperimentalCoroutinesApi
class MainCoroutineRule(val dispatcher: TestCoroutineDispatcher = TestCoroutineDispatcher()):
    TestWatcher(),
    TestCoroutineScope by TestCoroutineScope(dispatcher) {
    override fun starting(description: Description?) {
        super.starting(description)
        Dispatchers.setMain(dispatcher)
    }

    override fun finished(description: Description?) {
        super.finished(description)
        cleanupTestCoroutines()
        Dispatchers.resetMain()
    }
}