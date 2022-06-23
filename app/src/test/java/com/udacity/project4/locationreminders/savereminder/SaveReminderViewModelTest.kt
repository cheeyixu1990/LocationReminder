package com.udacity.project4.locationreminders.savereminder

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.udacity.project4.R
import com.udacity.project4.base.NavigationCommand
import com.udacity.project4.locationreminders.data.FakeDataSource
import com.udacity.project4.locationreminders.data.dto.ReminderDTO
import com.udacity.project4.locationreminders.getOrAwaitValue
import com.udacity.project4.locationreminders.reminderslist.MainCoroutineRule
import com.udacity.project4.locationreminders.reminderslist.ReminderDataItem
import kotlinx.coroutines.ExperimentalCoroutinesApi
import org.hamcrest.CoreMatchers.`is`
import org.hamcrest.CoreMatchers.nullValue
import org.hamcrest.MatcherAssert.assertThat
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.koin.test.AutoCloseKoinTest

@ExperimentalCoroutinesApi
@RunWith(AndroidJUnit4::class)
class SaveReminderViewModelTest: AutoCloseKoinTest() {

    private lateinit var saveReminderViewModel: SaveReminderViewModel

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

        saveReminderViewModel = SaveReminderViewModel(ApplicationProvider.getApplicationContext(), reminderDataSource)
    }

    @Test
    fun onClear_allNull() {
        saveReminderViewModel.onClear()

        assertThat(saveReminderViewModel.reminderTitle.getOrAwaitValue(), `is`(nullValue()))
        assertThat(saveReminderViewModel.reminderDescription.getOrAwaitValue(), `is`(nullValue()))
        assertThat(saveReminderViewModel.reminderSelectedLocationStr.getOrAwaitValue(), `is`(nullValue()))
        assertThat(saveReminderViewModel.selectedPOI.getOrAwaitValue(), `is`(nullValue()))
        assertThat(saveReminderViewModel.latitude.getOrAwaitValue(), `is`(nullValue()))
        assertThat(saveReminderViewModel.longitude.getOrAwaitValue(), `is`(nullValue()))
        assertThat(saveReminderViewModel.marker.getOrAwaitValue(), `is`(nullValue()))
    }

    @Test
    fun validateEnteredData_noTitle_snackBarUpdated() {
        saveReminderViewModel.validateEnteredData(ReminderDataItem(
            title = null,
            description = "Description",
            location = "Location",
            latitude = 0.0,
            longitude =  0.0
        ))

        assertThat(saveReminderViewModel.showSnackBarInt.getOrAwaitValue(), `is`(R.string.err_enter_title))
    }

    @Test
    fun validateEnteredData_noLocation_snackBarUpdated() {
        saveReminderViewModel.validateEnteredData(ReminderDataItem(
            title = "Title",
            description = "Description",
            location = null,
            latitude = 0.0,
            longitude =  0.0
        ))

        assertThat(saveReminderViewModel.showSnackBarInt.getOrAwaitValue(), `is`(R.string.err_select_location))
    }

    @Test
    fun saveReminder_showToastStr_navigationCommandBack() {
        saveReminderViewModel.saveReminder(ReminderDataItem(
            title = "Title",
            description = "Description",
            location = "Location",
            latitude = 0.0,
            longitude =  0.0
        ))

        assertThat(saveReminderViewModel.showToast.getOrAwaitValue(), `is`("Reminder Saved !"))
        assertThat(saveReminderViewModel.navigationCommand.getOrAwaitValue(), `is`(NavigationCommand.Back))
    }

}