package com.udacity.project4.locationreminders.reminderslist

import android.app.Application
import android.os.Bundle
import android.view.View
import android.view.View.FIND_VIEWS_WITH_TEXT
import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.fragment.app.testing.launchFragmentInContainer
import androidx.navigation.NavController
import androidx.navigation.Navigation
import androidx.recyclerview.widget.RecyclerView
import androidx.test.core.app.ApplicationProvider
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.IdlingRegistry
import androidx.test.espresso.ViewAssertion
import androidx.test.espresso.action.ViewActions.click
import androidx.test.espresso.action.ViewActions.pressBack
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.matcher.ViewMatchers.*
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.filters.MediumTest
import com.google.common.truth.Truth
import com.udacity.project4.R
import com.udacity.project4.ServiceLocator
import com.udacity.project4.locationreminders.data.ReminderDataSource
import com.udacity.project4.locationreminders.data.dto.ReminderDTO
import com.udacity.project4.locationreminders.data.local.LocalDB
import com.udacity.project4.locationreminders.data.local.RemindersLocalRepository
import com.udacity.project4.locationreminders.savereminder.SaveReminderViewModel
import com.udacity.project4.util.DataBindingIdlingResource
import com.udacity.project4.utils.EspressoIdlingResource
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.test.runBlockingTest
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Assert.*
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.koin.androidx.viewmodel.dsl.viewModel
import org.koin.core.context.GlobalContext
import org.koin.core.context.startKoin
import org.koin.core.context.stopKoin
import org.koin.dsl.module
import org.koin.test.AutoCloseKoinTest
import org.koin.test.get
import org.mockito.Mockito.mock
import org.mockito.Mockito.verify


@RunWith(AndroidJUnit4::class)
@ExperimentalCoroutinesApi
//UI Testing
@MediumTest
class ReminderListFragmentTest: AutoCloseKoinTest() {

    private lateinit var fakeReminderDataSource: ReminderDataSource
    private lateinit var appContext: Application

    @get:Rule
    val instantTaskExecutorRule = InstantTaskExecutorRule()

    @Before
    fun init() {
        stopKoin()
        appContext = ApplicationProvider.getApplicationContext()
        val myModule = module {
            viewModel {
                RemindersListViewModel(
                    appContext,
                    get() as ReminderDataSource
                )
            }
            single {
                SaveReminderViewModel(
                    appContext,
                    get() as ReminderDataSource
                )
            }
            single { RemindersLocalRepository(get()) as ReminderDataSource }
            single { LocalDB.createRemindersDao(appContext) }
        }

        startKoin {
            modules(listOf(myModule))
        }
        fakeReminderDataSource = get()

        runBlocking {
            fakeReminderDataSource.deleteAllReminders()
        }
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

        scenario.close()
    }

    @Test
    fun backPressed_backToLoginPage() {
        val scenario = launchFragmentInContainer<ReminderListFragment>(Bundle(), R.style.AppTheme)
        val navController = mock(NavController::class.java)
        scenario.onFragment {
            Navigation.setViewNavController(it.view!!, navController)
        }

        onView(withId(R.id.refreshLayout)).perform(pressBack())

        verify(navController).navigate(
            ReminderListFragmentDirections.actionReminderListFragmentToAuthenticationActivity2()
        )
        scenario.close()
    }

    @Test
    fun fragmentLoaded_RecyclerViewIsVisible() = runTest {
        val reminder1 = ReminderDTO("Title1", "Description1", "Location1", 1.0, 1.0, "1")
        val reminder2 = ReminderDTO("Title2", "Description2", "Location2", 2.0, 2.0, "2")

        fakeReminderDataSource.saveReminder(reminder1)
        fakeReminderDataSource.saveReminder(reminder2)

        val scenario = launchFragmentInContainer<ReminderListFragment>(Bundle(), R.style.AppTheme)
        val navController = mock(NavController::class.java)
        scenario.onFragment {
            Navigation.setViewNavController(it.view!!, navController)
        }

        onView(withId(R.id.reminderssRecyclerView)).check(matches(isDisplayed()))
        scenario.close()
    }

    @Test
    fun saveTwoReminders_twoRemindersVisible() = runTest {

        val reminder1 = ReminderDTO("Title1", "Description1", "Location1", 1.0, 1.0, "1")
        val reminder2 = ReminderDTO("Title2", "Description2", "Location2", 2.0, 2.0, "2")

        fakeReminderDataSource.saveReminder(reminder1)
        fakeReminderDataSource.saveReminder(reminder2)

        val scenario = launchFragmentInContainer<ReminderListFragment>(Bundle(), R.style.AppTheme)
        val navController = mock(NavController::class.java)
        scenario.onFragment {
            Navigation.setViewNavController(it.view!!, navController)
        }

        onView(withId(R.id.reminderssRecyclerView))
            .check(hasViewWithTextAtPosition(0, "Description1"))
        onView(withId(R.id.reminderssRecyclerView))
            .check(hasViewWithTextAtPosition(1, "Title2"))
    }

    @Test
    fun noReminders_showNoDataMessage() {

        val scenario = launchFragmentInContainer<ReminderListFragment>(Bundle(), R.style.AppTheme)
        val navController = mock(NavController::class.java)
        scenario.onFragment {
            Navigation.setViewNavController(it.view!!, navController)
        }

        onView(withText(R.string.no_data)).check(matches(isDisplayed()))
        scenario.close()
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