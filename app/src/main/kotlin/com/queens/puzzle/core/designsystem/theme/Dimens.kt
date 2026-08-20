package com.queens.puzzle.core.designsystem.theme

import androidx.compose.ui.unit.dp

/**
 * Fixed sizes of things, as opposed to the space between them - see [Spacing].
 *
 * Nothing here is owned by a single composable. A size that describes one widget should live with
 * that widget - the way Material's own component tokens do. What lands here is what several
 * unrelated places have to agree on.
 */
object Dimens {

    /** Material's minimum touch target. Anything tappable is at least this in both axes. */
    val MinTouchTarget = 48.dp

    /** Height of a screen's top bar. */
    val TopBarHeight = 64.dp

    /** Filled button that carries a screen's primary action. */
    val PrimaryButtonHeight = 56.dp

    /** Text button under a primary action, and the space kept for one that is absent. */
    val SecondaryButtonHeight = 52.dp

    /** Hairline border, used wherever an outline separates two surfaces of similar tone. */
    val BorderWidth = 1.dp

    /**
     * Below this a window has no vertical budget left, and a screen reflows rather than shrinks —
     * a rotated phone, mostly. `GameScreen` seats the board beside its controls; `WinScreen`
     * scrolls its summary and keeps the buttons in place.
     */
    val CompactHeight = 480.dp
}
