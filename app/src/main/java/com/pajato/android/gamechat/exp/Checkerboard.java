package com.pajato.android.gamechat.exp;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.v4.content.ContextCompat;
import android.util.DisplayMetrics;
import android.widget.GridLayout;
import android.widget.TextView;

import com.pajato.android.gamechat.common.BaseFragment;
import com.pajato.android.gamechat.main.PaneManager;

import static com.pajato.android.gamechat.R.id.board;

/**
 * Provide a checkerboard class to be used for chess and checkers.
 *
 * @author Paul Michael Reilly on 2/12/17.
 */

public class Checkerboard {

    // Private class constants.

    /** The amount of space taken by a smart phone toolbar in dp units. */
    private static final int SMART_PHONE_HEIGHT = 56;

    /** The amount of space taken by a tablet toolbar in dp units. */
    private static final int TABLET_HEIGHT = 64;

    /** The amount of vertical space allocated for the player controls. */
    private static final int CONTROLS_HEIGHT = 112;

    /** The amount of vertical space allocated to the FAB control. */
    private static final int FAB_HEIGHT= 88 + 16;

    // Private instance variables.

    /** The GridLayout used to build the checkerboard UI. */
    private GridLayout mGrid;

    /** The size of the board square. */
    private int mCellSize;

    // Public instance methods.

    /** Add a given cell to the board. */
    public void addCell(TextView cell) {
        mGrid.addView(cell);
    }

    /** Return the cell at a given position. */
    public TextView getCell(final int position) {
        return (TextView) mGrid.getChildAt(position);
    }

    /** Return the size of the board cell on this device. */
    public int getCellSize() {
        return mCellSize;
    }

    /** Initialize the checkerboard by finding the grid layout and computing the cell size. */
    public void init(@NonNull BaseFragment fragment) {
        mGrid = (GridLayout) fragment.getActivity().findViewById(board);

        // Establish the cell size for the checkerboard.
        DisplayMetrics metrics = fragment.getContext().getResources().getDisplayMetrics();
        final float pxHeight = metrics.heightPixels;
        final float pxWidth = metrics.widthPixels;
        final int unavailableHeight = CONTROLS_HEIGHT + FAB_HEIGHT +
                (PaneManager.instance.isTablet() ? TABLET_HEIGHT : SMART_PHONE_HEIGHT);
        final int unavailableWidth = 32;
        final int boardHeight = Math.round(pxHeight) - getPixels(metrics, unavailableHeight);
        final int width = Math.round(pxWidth);
        final int boardWidth = (PaneManager.instance.isTablet() ? width / 2 : width) -
                getPixels(metrics, unavailableWidth);
        mCellSize = Math.min(boardWidth, boardHeight) / 8;
    }

    /** Remove all cells from the board. */
    public void reset() {
        // There appears to be a bug with GridLayout in that if the row and column counts are not
        // specified, an illegal argument exception can occur, so explicitly set the row and column
        // counts.
        mGrid.removeAllViews();
        mGrid.setRowCount(8);
        mGrid.setColumnCount(8);
    }

    /** Set the highlight at a given position using a given color resource id. */
    public void setHighlight(Context context, int position, final int colorResId) {
        mGrid.getChildAt(position).setBackgroundColor(ContextCompat.getColor(context, colorResId));
    }

    // Private instance methods.

    /** Return the number of physical pixels for a given number of device independent pixels. */
    private int getPixels(final DisplayMetrics metrics, final int dp) {
        return Math.round(dp * (metrics.xdpi / DisplayMetrics.DENSITY_DEFAULT));
    }
}
