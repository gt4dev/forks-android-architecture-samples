/*
 * Copyright 2022 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.example.android.architecture.blueprints.todoapp.tasks

import androidx.activity.ComponentActivity
import androidx.annotation.StringRes
import androidx.compose.material3.Surface
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertIsNotDisplayed
import androidx.compose.ui.test.isToggleable
import androidx.compose.ui.test.junit4.AndroidComposeTestRule
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.lifecycle.SavedStateHandle
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.filters.MediumTest
import com.example.android.architecture.blueprints.todoapp.HiltTestActivity
import com.example.android.architecture.blueprints.todoapp.R
import com.example.android.architecture.blueprints.todoapp.TodoTheme
import com.example.android.architecture.blueprints.todoapp.data.TaskRepository
import com.example.android.architecture.blueprints.todoapp.tasks.GeneralScreenSteps.then_screen_shows
import com.example.android.architecture.blueprints.todoapp.tasks.TasksScreenSteps.given_is_shown_tasks_screen
import com.example.android.architecture.blueprints.todoapp.tasks.TasksScreenSteps.when_user_in_filter_selects_option
import dagger.hilt.android.testing.HiltAndroidRule
import dagger.hilt.android.testing.HiltAndroidTest
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TestRule
import org.junit.runner.RunWith
import javax.inject.Inject

/**
 * Integration test for the Task List screen.
 */
// TODO - Move to the sharedTest folder when https://issuetracker.google.com/224974381 is fixed
@RunWith(AndroidJUnit4::class)
@MediumTest
// @LooperMode(LooperMode.Mode.PAUSED)
// @TextLayoutMode(TextLayoutMode.Mode.REALISTIC)
@HiltAndroidTest
@OptIn(ExperimentalCoroutinesApi::class)
class TasksScreenTest {

    @get:Rule(order = 0)
    var hiltRule = HiltAndroidRule(this)

    @get:Rule(order = 1)
    val composeTestRule = createAndroidComposeRule<HiltTestActivity>()
    private val activity get() = composeTestRule.activity

    @Inject
    lateinit var repository: TaskRepository

    @Before
    fun init() {
        hiltRule.inject()
    }

    @Test
    fun displayTask_whenRepositoryHasData() = runTest {
        // GIVEN - One task already in the repository
        repository.createTask("TITLE1", "DESCRIPTION1")

        // WHEN - On startup
        setContent()

        // THEN - Verify task is displayed on screen
        composeTestRule.onNodeWithText("TITLE1").assertIsDisplayed()
    }

    @Test
    fun displayActiveTask() = runTest {
        repository.createTask("TITLE1", "DESCRIPTION1")

        setContent()

        composeTestRule.onNodeWithText("TITLE1").assertIsDisplayed()

        openFilterAndSelectOption(R.string.nav_active)
        composeTestRule.onNodeWithText("TITLE1").assertIsDisplayed()

        openFilterAndSelectOption(R.string.nav_completed)

        composeTestRule.onNodeWithText("TITLE1").assertDoesNotExist()
    }

    @Test
    fun displayCompletedTask() = runTest {
        repository.apply {
            createTask("TITLE1", "DESCRIPTION1").also { completeTask(it) }
        }

        setContent()

        composeTestRule.onNodeWithText("TITLE1").assertIsDisplayed()

        openFilterAndSelectOption(R.string.nav_active)
        composeTestRule.onNodeWithText("TITLE1").assertDoesNotExist()

        openFilterAndSelectOption(R.string.nav_completed)
        composeTestRule.onNodeWithText("TITLE1").assertIsDisplayed()
    }

    @Test
    fun markTaskAsComplete() = runTest {
        repository.createTask("TITLE1", "DESCRIPTION1")

        setContent()

        // Mark the task as complete
        composeTestRule.onNode(isToggleable()).performClick()

        // Verify task is shown as complete
        openFilterAndSelectOption(R.string.nav_all)
        composeTestRule.onNodeWithText("TITLE1").assertIsDisplayed()
        openFilterAndSelectOption(R.string.nav_active)
        composeTestRule.onNodeWithText("TITLE1").assertDoesNotExist()
        openFilterAndSelectOption(R.string.nav_completed)
        composeTestRule.onNodeWithText("TITLE1").assertIsDisplayed()
    }

    @Test
    fun markTaskAsActive() = runTest {
        repository.apply {
            createTask("TITLE1", "DESCRIPTION1").also { completeTask(it) }
        }

        setContent()

        // Mark the task as active
        composeTestRule.onNode(isToggleable()).performClick()

        // Verify task is shown as active
        openFilterAndSelectOption(R.string.nav_all)
        composeTestRule.onNodeWithText("TITLE1").assertIsDisplayed()
        openFilterAndSelectOption(R.string.nav_active)
        composeTestRule.onNodeWithText("TITLE1").assertIsDisplayed()
        openFilterAndSelectOption(R.string.nav_completed)
        composeTestRule.onNodeWithText("TITLE1").assertDoesNotExist()
    }

    @Test
    fun showAllTasks() = runTest {
        // Add one active task and one completed task
        repository.apply {
            createTask("TITLE1", "DESCRIPTION1")
            createTask("TITLE2", "DESCRIPTION2").also { completeTask(it) }
        }

        setContent()

        // Verify that both of our tasks are shown
        openFilterAndSelectOption(R.string.nav_all)
        composeTestRule.onNodeWithText("TITLE1").assertIsDisplayed()
        composeTestRule.onNodeWithText("TITLE2").assertIsDisplayed()
    }

    @Test
    fun showActiveTasks() = runTest {
        // Add 2 active tasks and one completed task
        repository.apply {
            createTask("TITLE1", "DESCRIPTION1")
            createTask("TITLE2", "DESCRIPTION2")
            createTask("TITLE3", "DESCRIPTION3").also { completeTask(it) }
        }

        setContent()

        // Verify that the active tasks (but not the completed task) are shown
        openFilterAndSelectOption(R.string.nav_active)
        composeTestRule.onNodeWithText("TITLE1").assertIsDisplayed()
        composeTestRule.onNodeWithText("TITLE2").assertIsDisplayed()
        composeTestRule.onNodeWithText("TITLE3").assertDoesNotExist()
    }

    @Test
    fun showCompletedTasks() = runTest {
        // Add one active task and 2 completed tasks
        repository.apply {
            createTask("TITLE1", "DESCRIPTION1")
            createTask("TITLE2", "DESCRIPTION2").also { completeTask(it) }
            createTask("TITLE3", "DESCRIPTION3").also { completeTask(it) }
        }

        setContent()

        // Verify that the completed tasks (but not the active task) are shown
        openFilterAndSelectOption(R.string.nav_completed)
        composeTestRule.onNodeWithText("TITLE1").assertDoesNotExist()
        composeTestRule.onNodeWithText("TITLE2").assertIsDisplayed()
        composeTestRule.onNodeWithText("TITLE3").assertIsDisplayed()
    }

    @Test
    fun clearCompletedTasks() = runTest {
        // Add one active task and one completed task
        repository.apply {
            createTask("TITLE1", "DESCRIPTION1")
            createTask("TITLE2", "DESCRIPTION2").also { completeTask(it) }
        }

        setContent()

        // Click clear completed in menu
        composeTestRule.onNodeWithContentDescription(activity.getString(R.string.menu_more))
            .performClick()
        composeTestRule.onNodeWithText(activity.getString(R.string.menu_clear)).assertIsDisplayed()
        composeTestRule.onNodeWithText(activity.getString(R.string.menu_clear)).performClick()

        openFilterAndSelectOption(R.string.nav_all)
        // Verify that only the active task is shown
        composeTestRule.onNodeWithText("TITLE1").assertIsDisplayed()
        composeTestRule.onNodeWithText("TITLE2").assertDoesNotExist()
    }

    @Test
    fun OLD_noTasks_AllTasksFilter_AddTaskViewVisible() {
        setContent()

        openFilterAndSelectOption(R.string.nav_all)

        // Verify the "You have no tasks!" text is shown
        composeTestRule.onNodeWithText("You have no tasks!").assertIsDisplayed()
    }

    @Test
    fun NEW_noTasks_AllTasksFilter_AddTaskViewVisible() {
        val tasks_screen = given_is_shown_tasks_screen(composeTestRule, repository)
        tasks_screen.when_user_in_filter_selects_option("all")
        tasks_screen.then_screen_shows("You have no tasks!")
    }

    @Test
    fun noTasks_CompletedTasksFilter_AddTaskViewNotVisible() {
        setContent()

        openFilterAndSelectOption(R.string.nav_completed)
        // Verify the "You have no completed tasks!" text is shown
        composeTestRule.onNodeWithText("You have no completed tasks!").assertIsDisplayed()
    }

    @Test
    fun noTasks_ActiveTasksFilter_AddTaskViewNotVisible() {
        setContent()

        openFilterAndSelectOption(R.string.nav_active)
        // Verify the "You have no active tasks!" text is shown
        composeTestRule.onNodeWithText("You have no active tasks!").assertIsDisplayed()
    }

    private fun setContent() {
        composeTestRule.setContent {
            TodoTheme {
                Surface {
                    TasksScreen(
                        viewModel = TasksViewModel(repository, SavedStateHandle()),
                        userMessage = R.string.successfully_added_task_message,
                        onUserMessageDisplayed = { },
                        onAddTask = { },
                        onTaskClick = { },
                        openDrawer = { }
                    )
                }
            }
        }
    }

    private fun openFilterAndSelectOption(@StringRes option: Int) {
        composeTestRule.onNodeWithContentDescription(activity.getString(R.string.menu_filter))
            .performClick()
        composeTestRule.onNodeWithText(activity.getString(option)).assertIsDisplayed()
        composeTestRule.onNodeWithText(activity.getString(option)).performClick()
    }
}


object TasksScreenSteps {

    fun <R : TestRule, A : ComponentActivity> given_is_shown_tasks_screen(
        androTestRule: AndroidComposeTestRule<R, A>,
        repository: TaskRepository
    ): AndroidComposeTestRule<R, A> {
        androTestRule.setContent {
            TodoTheme {
                Surface {
                    TasksScreen(
                        viewModel = TasksViewModel(repository, SavedStateHandle()),
                        userMessage = R.string.successfully_added_task_message,
                        onUserMessageDisplayed = { },
                        onAddTask = { },
                        onTaskClick = { },
                        openDrawer = { }
                    )
                }
            }
        }

        return androTestRule
    }

    fun <R : TestRule, A : ComponentActivity> AndroidComposeTestRule<R, A>.when_user_in_filter_selects_option(option: String) {
        val optionId = when (option) {
            "all" -> R.string.nav_all
            else -> throw RuntimeException("unknown option: $option")
        }
        this.onNodeWithContentDescription(activity.getString(R.string.menu_filter)).performClick()
        this.onNodeWithText(activity.getString(optionId)).assertIsDisplayed()
        this.onNodeWithText(activity.getString(optionId)).performClick()

    }
}


object GeneralScreenSteps {
    fun <R : TestRule, A : ComponentActivity> AndroidComposeTestRule<R, A>.then_screen_shows(text: String) {
        this.onNodeWithText(text).assertIsDisplayed()
    }

    fun <R : TestRule, A : ComponentActivity> AndroidComposeTestRule<R, A>.then_screen_doesnt_show(text: String) {
        this.onNodeWithText(text).assertIsNotDisplayed()
    }
}