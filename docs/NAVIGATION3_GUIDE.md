# Navigation 3 practice guide

This project is organised so each Navigation 3 idea appears in a real user flow.

## 1. Typed routes

Open `ui/navigation/Routes.kt`. Every destination implements `NavKey` and is annotated with `@Serializable`.

```kotlin
@Serializable
data class TripDetailRoute(val tripId: Long) : NavKey
```

Routes carry IDs, not complete database-style objects. This keeps restoration safe and prevents stale data from being copied into the back stack.

## 2. Multiple top-level back stacks

`rememberNavigationState()` creates one `rememberNavBackStack()` for each bottom destination. Switching tabs changes the active top-level route without destroying the other stacks.

Try this:

1. Open a trip detail screen.
2. Switch to Templates and open a template.
3. Switch back to Trips.
4. Confirm the trip detail screen is still present.

The `SaveableStateHolder` decorator also keeps remembered screen state associated with each entry.

## 3. Navigation as state

`Navigator` does not call a controller. It mutates the app-owned back-stack lists:

```kotlin
state.backStacks.getValue(state.topLevelRoute).add(route)
```

That is the key Navigation 3 mental model: the app owns the stack, and `NavDisplay` renders it.

## 4. Entry provider

`PackMateApp.kt` maps route types to UI using `entryProvider`:

```kotlin
entry<TripDetailRoute> { route ->
    val trip = data.trips.firstOrNull { it.id == route.tripId }
    TripDetailScreen(trip = trip, ...)
}
```

The entry reads live business state. Updating an item changes DataStore, the `Flow` emits, Compose recomposes, and the open destination immediately reflects the change.

## 5. Dialog destinations

Add-item and destructive confirmations are real navigation entries. Their metadata tells `DialogSceneStrategy` to render them as dialogs:

```kotlin
entry<AddItemDialogRoute>(
    metadata = DialogSceneStrategy.dialog()
) { ... }
```

Pressing Back removes the dialog key and reveals the underlying detail destination.

## 6. Back behavior

- Nested destination: remove the last key.
- Root of Templates or Settings: return to Trips.
- Root of Trips: the system can exit the activity.
- Re-select the active bottom tab: pop that tab to its root.

## Exercises

1. Add an Edit Trip route with a result returned to Trip Details.
2. Add an onboarding route that conditionally precedes Trips.
3. Add a share-preview bottom sheet using a custom scene strategy.
4. Add a deep-link parser that converts an incoming URI to `TripDetailRoute`.
5. Add adaptive list-detail scenes for tablets.
6. Split Templates and Trips into feature modules while keeping route ownership explicit.
