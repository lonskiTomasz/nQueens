package com.queens.puzzle.ui.navigation

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.navigation3.rememberViewModelStoreNavEntryDecorator
import androidx.navigation3.runtime.entryProvider
import androidx.navigation3.runtime.rememberNavBackStack
import androidx.navigation3.runtime.rememberSaveableStateHolderNavEntryDecorator
import androidx.navigation3.ui.NavDisplay
import com.queens.puzzle.ui.besttimes.BestTimesScreen
import com.queens.puzzle.ui.game.GameScreen
import com.queens.puzzle.ui.home.HomeScreen
import com.queens.puzzle.ui.win.WinScreen

@Composable
fun QueensNavHost(modifier: Modifier = Modifier) {
    val backStack = rememberNavBackStack(HomeKey)

    NavDisplay(
        backStack = backStack,
        modifier = modifier,
        onBack = { backStack.removeLastOrNull() },
        entryDecorators = listOf(
            rememberSaveableStateHolderNavEntryDecorator(),
            rememberViewModelStoreNavEntryDecorator(),
        ),
        entryProvider = entryProvider {
            entry<HomeKey> {
                HomeScreen(
                    onStartGame = { boardSize, puzzleType, gameId ->
                        backStack.add(GameKey(boardSize.value, gameId, puzzleType))
                    },
                    onResumeGame = { resumable ->
                        backStack.add(
                            GameKey(resumable.boardSize.value, resumable.gameId, resumable.puzzleType),
                        )
                    },
                    onSeeAllBestTimes = { backStack.add(BestTimesKey) },
                )
            }
            entry<GameKey> { key ->
                GameScreen(
                    boardSize = key.boardSize,
                    gameId = key.gameId,
                    puzzleType = key.puzzleType,
                    onNavigateBack = { backStack.removeLastOrNull() },
                    onNavigateToWin = { solveId ->
                        backStack.removeLastOrNull()
                        backStack.add(WinKey(solveId))
                    },
                )
            }
            entry<WinKey> { key ->
                WinScreen(
                    solveId = key.solveId,
                    onPlay = { boardSize, puzzleType, gameId ->
                        backStack.removeLastOrNull()
                        backStack.add(GameKey(boardSize, gameId, puzzleType))
                    },
                    onSeeBestTimes = {
                        backStack.removeLastOrNull()
                        backStack.add(BestTimesKey)
                    },
                    onClose = {
                        // Straight home: the win screen is the end of a game, not a step in one.
                        backStack.removeAll { it != HomeKey }
                    },
                )
            }
            entry<BestTimesKey> {
                BestTimesScreen(onNavigateBack = { backStack.removeLastOrNull() })
            }
        },
    )
}
