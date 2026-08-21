package com.queens.puzzle.domain.rules

import com.queens.puzzle.model.BoardSize
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test
import kotlin.random.Random

/**
 * Cross-checks [BoardEvaluator] against [NQueensSolver].
 *
 * The solver is pinned to the known solution counts first, so a broken oracle cannot bless a
 * broken evaluator; the evaluator is then held to the solver's output.
 */
class NQueensSolverOracleTest {

    @Test
    fun `solution counts match the known sequence`() {
        // OEIS A000170: the number of distinct N-Queens solutions for n = 1..10.
        val expected = listOf(1, 0, 0, 2, 10, 4, 40, 92, 352, 724)

        expected.forEachIndexed { index, count ->
            val n = index + 1
            assertEquals("n=$n", count, NQueensSolver.countSolutions(n))
        }
    }

    @Test
    fun `no solution exists below the minimum playable size`() {
        assertNull(NQueensSolver.firstSolution(2))
        assertNull(NQueensSolver.firstSolution(3))
        assertNotNull(NQueensSolver.firstSolution(BoardSize.MIN))
    }

    @Test
    fun `every generated solution is confirmed solved by the evaluator`() {
        forEachPlayableSize { boardSize ->
            NQueensSolver.solutions(boardSize.value).forEach { solution ->
                val evaluation = BoardEvaluator.evaluate(boardSize, solution)

                assertTrue("$boardSize $solution", evaluation.isSolved)
                assertTrue("$boardSize $solution", evaluation.conflicts.isEmpty())
            }
        }
    }

    @Test
    fun `moving any single queen breaks the solution`() {
        forEachPlayableSize { boardSize ->
            val solution = NQueensSolver.firstSolution(boardSize.value)!!

            for (queen in solution) {
                for (target in boardSize.positions()) {
                    if (target in solution) continue

                    val perturbed = solution - queen + target
                    val evaluation = BoardEvaluator.evaluate(boardSize, perturbed)

                    assertFalse("$boardSize moved $queen to $target", evaluation.isSolved)
                    assertTrue("$boardSize moved $queen to $target", evaluation.hasConflicts)
                }
            }
        }
    }

    @Test
    fun `removing a queen leaves the board clean but unsolved`() {
        forEachPlayableSize { boardSize ->
            val solution = NQueensSolver.firstSolution(boardSize.value)!!

            for (queen in solution) {
                val evaluation = BoardEvaluator.evaluate(boardSize, solution - queen)

                assertFalse(evaluation.isSolved)
                assertFalse(evaluation.hasConflicts)
            }
        }
    }

    @Test
    fun `the evaluator agrees with a pairwise scan on random boards`() {
        val random = Random(seed = 20260818)
        val boardSize = BoardSize(8)

        repeat(500) {
            val queens = boardSize.positions()
                .shuffled(random)
                .take(random.nextInt(0, boardSize.value + 1))
                .toSet()

            val expected = queens.filter { queen -> queens.any { queen.attacks(it) } }.toSet()

            assertEquals(queens.toString(), expected, BoardEvaluator.evaluate(boardSize, queens).conflicts)
        }
    }

    private fun forEachPlayableSize(block: (BoardSize) -> Unit) {
        for (n in BoardSize.MIN..8) block(BoardSize(n))
    }
}
