package com.udacity.project4.locationreminders.reminderslist

import android.os.Bundle
import android.view.View
import android.view.View.FIND_VIEWS_WITH_TEXT
import androidx.fragment.app.testing.launchFragmentInContainer
import androidx.navigation.NavController
import androidx.navigation.Navigation
import androidx.recyclerview.widget.RecyclerView
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.ViewAssertion
import androidx.test.espresso.action.ViewActions.click
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.matcher.ViewMatchers.*
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.filters.MediumTest
import com.google.common.truth.Truth
import com.udacity.project4.R
import com.udacity.project4.ServiceLocator
import com.udacity.project4.locationreminders.data.FakeAndroidDataSource
import com.udacity.project4.locationreminders.data.ReminderDataSource
import com.udacity.project4.locationreminders.data.dto.ReminderDTO
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.test.runBlockingTest
import org.junit.After
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mockito.mock
import org.mockito.Mockito.verify


@RunWith(AndroidJUnit4::class)
@ExperimentalCoroutinesApi
//UI Testing
@MediumTest
class ReminderListFragmentTest {

//    TODO: test the navigation of the fragments.
//    TODO: test the displayed data on the UI.
//    TODO: add testing for the error messages.
    private lateinit var fakeReminderDataSource: ReminderDataSource

    @Before
    fun initRepository() = runBlocking{
        val reminder1 = ReminderDTO("Title1", "Description1", "Location1", 1.0, 1.0, "1")
        val reminder2 = ReminderDTO("Title2", "Description2", "Location2", 2.0, 2.0, "2")

        fakeReminderDataSource = FakeAndroidDataSource(mutableListOf(reminder1, reminder2))

        ServiceLocator.reminderDataSource = fakeReminderDataSource
    }

    @After
    fun cleanupDb() = runBlocking {
        ServiceLocator.resetRepository()
    }

    @Test
    fun clickAddFab_navigateToSaveReminderFragment() {
        val scenario = launchFragmentInContainer<ReminderListFragment>(Bundle(), R.style.AppTheme)
        val navController = mock(NavController::class.java)
        scenario.onFragment {
            Navigation.setViewNavController(it.view!!, navController)
        }

        onView(withId(R.id.addReminderFAB)).perform(click())

        verify(navController).navigate(
            ReminderListFragmentDirections.toSaveReminder()
        )
    }

    @Test
    fun fragmentLoaded_RecyclerViewIsVisible() = runBlockingTest {

        val scenario = launchFragmentInContainer<ReminderListFragment>(Bundle(), R.style.AppTheme)
        val navController = mock(NavController::class.java)
        scenario.onFragment {
            Navigation.setViewNavController(it.view!!, navController)
        }

        val reminder1 = ReminderDTO("Title1", "Description1", "Location1", 1.0, 1.0, "1")
        val reminder2 = ReminderDTO("Title2", "Description2", "Location2", 2.0, 2.0, "2")

        fakeReminderDataSource.saveReminder(reminder1)
        fakeReminderDataSource.saveReminder(reminder2)

        onView(withId(R.id.reminderssRecyclerView)).check(matches(isDisplayed()))
    }

    @Test
    fun saveTwoReminders_twoRemindersVisible() = runBlockingTest {

        val scenario = launchFragmentInContainer<ReminderListFragment>(Bundle(), R.style.AppTheme)
        val navController = mock(NavController::class.java)
        scenario.onFragment {
            Navigation.setViewNavController(it.view!!, navController)
        }

        Thread.sleep(3000)

        onView(withId(R.id.reminderssRecyclerView))
            .check(hasViewWithTextAtPosition(0, "Description1"))
//        onView(withId(R.id.reminderssRecyclerView))
//            .check(hasViewWithTextAtPosition(2, "Title2"))
    }

    fun hasViewWithTextAtPosition(index: Int, text: CharSequence): ViewAssertion? {
        return ViewAssertion { view, e ->
            if (view !is RecyclerView) {
                throw e
            }
            val outviews: ArrayList<View> = ArrayList()
            view.findViewHolderForAdapterPosition(index)!!.itemView.findViewsWithText(
                outviews, text,
                FIND_VIEWS_WITH_TEXT
            )
            Truth.assertThat(outviews).isNotEmpty();
        }
    }
}