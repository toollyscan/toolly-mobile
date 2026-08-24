/*
 * Based on the Android Navigation 3 multiple-back-stacks recipe.
 * Copyright 2025 The Android Open Source Project. Licensed under Apache 2.0.
 */
package com.shivayogih.packmate.ui.navigation

import androidx.navigation3.runtime.NavKey

class Navigator(
    private val state: NavigationState,
) {
    fun navigate(route: NavKey) {
        if (route in state.backStacks) {
            state.topLevelRoute = route
        } else {
            state.backStacks.getValue(state.topLevelRoute).add(route)
        }
    }

    fun selectTopLevel(route: NavKey) {
        if (state.topLevelRoute == route) {
            popToRoot(route)
        } else {
            state.topLevelRoute = route
        }
    }

    fun openInTopLevel(topLevelRoute: NavKey, destination: NavKey) {
        val stack = state.backStacks.getValue(topLevelRoute)
        while (stack.size > 1) stack.removeLastOrNull()
        stack.add(destination)
        state.topLevelRoute = topLevelRoute
    }

    fun goBack() {
        val currentStack = state.backStacks.getValue(state.topLevelRoute)
        if (currentStack.size > 1) {
            currentStack.removeLastOrNull()
        } else if (state.topLevelRoute != state.startRoute) {
            state.topLevelRoute = state.startRoute
        }
    }

    private fun popToRoot(route: NavKey) {
        val stack = state.backStacks.getValue(route)
        while (stack.size > 1) stack.removeLastOrNull()
    }
}
