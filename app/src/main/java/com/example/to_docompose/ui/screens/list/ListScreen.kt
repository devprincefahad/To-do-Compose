package com.example.to_docompose.ui.screens.list

import android.annotation.SuppressLint
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.SnackbarResult
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import com.example.to_docompose.R
import com.example.to_docompose.data.models.TodoTask
import com.example.to_docompose.ui.theme.fabBackgroundColor
import com.example.to_docompose.ui.viewmodels.SharedViewModel
import com.example.to_docompose.util.Action
import com.example.to_docompose.util.RequestState
import com.example.to_docompose.util.SearchAppBarState
import kotlinx.coroutines.launch

@SuppressLint("UnusedMaterial3ScaffoldPaddingParameter")
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ListScreen(
    navigateToTaskScreen: (taskId: Int) -> Unit,
    sharedViewModel: SharedViewModel
) {
    LaunchedEffect(key1 = true) {
        sharedViewModel.getAllTasks()
        sharedViewModel.readSortState()
    }

    val action by sharedViewModel.action

    val allTasks by sharedViewModel.allTasks.collectAsState()
    val searchedTasks by sharedViewModel.searchedTasks.collectAsState()

    val sortState by sharedViewModel.sortState.collectAsState()

    val lowPriorityTasks by sharedViewModel.lowPriorityTasks.collectAsState()
    val highPriorityTasks by sharedViewModel.highPriorityTasks.collectAsState()

    val searchAppBarState: SearchAppBarState
            by sharedViewModel.searchAppBarState

    val searchTextState: String by sharedViewModel.searchTextState

    val snackBarHostState = remember { SnackbarHostState() }

    DisplaySnackBar(
        handleDatabaseAction = { sharedViewModel.handleDatabaseActions(action = action) },
        taskTitle = sharedViewModel.title.value,
        action = action,
        snackBarHostState = snackBarHostState,
        onUndoClicked = {
            sharedViewModel.action.value = it
        }
    )

    Scaffold(
        snackbarHost = { SnackbarHost(snackBarHostState) },
        topBar = {
            ListAppBar(
                sharedViewModel = sharedViewModel,
                searchAppBarState = searchAppBarState,
                searchTextState = searchTextState
            )
        },
        content = { padding ->
            Column(
                modifier = Modifier.padding(padding)
            ) {
                ListContent(
                    allTasks = allTasks,
                    searchedTasks = searchedTasks,
                    searchAppBarState = searchAppBarState,
                    navigateToTaskScreen = navigateToTaskScreen,
                    lowPriorityTasks = lowPriorityTasks,
                    highPriorityTasks = highPriorityTasks,
                    sortState = sortState
                )
            }
        },
        floatingActionButton = {
            ListFab(onFabClicked = navigateToTaskScreen)
        }
    )
}

@Composable
fun ListFab(
    onFabClicked: (taskId: Int) -> Unit
) {
    FloatingActionButton(
        onClick = {
            onFabClicked(-1)
        }
    ) {
        Icon(
            imageVector = Icons.Filled.Add,
            contentDescription = stringResource(id = R.string.add_button),
            tint = MaterialTheme.fabBackgroundColor
        )
    }
}

@Composable
fun DisplaySnackBar(
    handleDatabaseAction: () -> Unit,
    onUndoClicked: (Action) -> Unit,
    taskTitle: String,
    action: Action,
    snackBarHostState: SnackbarHostState
) {
    handleDatabaseAction()
    val scope = rememberCoroutineScope()
    LaunchedEffect(key1 = action) {
        if (action != Action.NO_ACTION) {
            scope.launch {
                val snackBarResult = snackBarHostState.showSnackbar(
                    message = setMessage(action = action, taskTitle = taskTitle),
                    actionLabel = setActionLabel(action = action),
                    duration = SnackbarDuration.Short
                )
                undoDeletedTask(
                    action = action,
                    snackBarResult = snackBarResult,
                    onUndoClicked = onUndoClicked
                )
            }
        }
    }
}

private fun setMessage(action: Action, taskTitle: String): String {
    return when (action) {
        Action.DELETE_ALL -> "All Tasks Removed"
        else -> "${action.name} : $taskTitle"
    }
}

private fun setActionLabel(action: Action): String {
    return if (action.name == "DELETE") {
        "UNDO"
    } else {
        "OK"
    }
}

private fun undoDeletedTask(
    action: Action,
    snackBarResult: SnackbarResult,
    onUndoClicked: (Action) -> Unit
) {
    if (snackBarResult == SnackbarResult.ActionPerformed && action == Action.DELETE) {
        onUndoClicked(Action.UNDO)
    }
}