package com.queens.puzzle.core.designsystem.theme

import androidx.compose.ui.unit.dp

/**
 * The gaps and insets the layouts are built from.
 *
 * Material 3 ships no spacing scale — `MaterialTheme` exposes `colorScheme`, `typography`,
 * `shapes` and `motionScheme`, and nothing else — so the app defines its own. These follow M3's
 * own model for dimensions: every token is named for the relationship it expresses, not for its
 * size. Two roles landing on the same number is expected and is not duplication; what matters is
 * that changing "the gap between sibling chips" moves every sibling-chip gap and nothing else.
 *
 * Deliberately not a t-shirt ramp. The design source runs on a 2dp grid (3, 6, 10, 14, 18 all
 * appear), so a `Small`/`Medium`/`Large` scale dense enough to hold it would admit nearly any
 * value and enforce nothing, while telling a reader less than the raw number does.
 *
 * Nothing here is owned by one composable. A value that describes a single widget or a single
 * screen stays private to it.
 */
object Spacing {

    // ——— Screen frame ———

    /** Inset from the window's side edges to content. */
    val ScreenPaddingHorizontal = 20.dp

    /** Inset from the window's top and bottom edges to content. */
    val ScreenPaddingVertical = 24.dp

    /**
     * Leading inset for a top bar whose first child is an `IconButton`. The button is a 48dp
     * touch target around a 24dp icon, so 12dp of it is already padding — this inset lands the
     * icon's optical edge on [ScreenPaddingHorizontal].
     */
    val TopBarIconInset = 8.dp

    // ——— Vertical rhythm ———

    /** Between a section heading and the content it introduces. */
    val SectionGap = 32.dp

    /** Between a block of content and the primary action that closes it. */
    val ActionGap = 28.dp

    /** Between two separate blocks of content. */
    val BlockGap = 16.dp

    /** Between adjacent elements that read as one group. Applies on either axis. */
    val ContentGap = 12.dp

    /** Between a field label and the control sitting under it. */
    val LabelGap = 14.dp

    /** Between a headline and the supporting line under it. */
    val TextGap = 10.dp

    /** Between a headline and a supporting line that reads as part of the same phrase. */
    val TightTextGap = 6.dp

    /** Between a value and its caption. */
    val CaptionGap = 4.dp

    // ——— Grouping within a row ———

    /** Between sibling chips, in a row or a flow. */
    val ChipGap = 10.dp

    /** Between sibling buttons. */
    val ButtonGap = 12.dp

    /** Between an icon and the text it labels, inside one control. */
    val IconTextGap = 8.dp

    /** Between a list row's leading art and its text. */
    val ListItemGap = 16.dp

    /** Between a row's text and the control at its trailing edge. */
    val RowControlGap = 14.dp

    // ——— Padding inside containers ———

    /** Inside a card or a list row. */
    val CardPadding = 12.dp

    /** Horizontal padding inside a banner or a pill that carries text. */
    val ContainerPaddingHorizontal = 16.dp

    /** Vertical padding inside a banner. */
    val ContainerPaddingVertical = 12.dp

    /** Horizontal padding inside a row that spans a card. */
    val RowPaddingHorizontal = 18.dp

    /** Vertical padding inside a row that spans a card. */
    val RowPaddingVertical = 14.dp

    /**
     * Padding around a control that already carries its own touch-target padding — a text button
     * or an icon button, which would otherwise sit twice as far in as it looks.
     */
    val TightPadding = 8.dp

    /** Inset between an icon button and the edge it sits against. */
    val IconButtonInset = 4.dp
}
