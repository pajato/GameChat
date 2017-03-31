/*
 * Copyright (C) 2016 Pajato Technologies LLC.
 *
 * This file is part of Pajato GameChat.

 * GameChat is free software: you can redistribute it and/or modify it under the terms of the GNU
 * General Public License as published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.

 * GameChat is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without
 * even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * General Public License for more details.

 * You should have received a copy of the GNU General Public License along with GameChat.  If not,
 * see http://www.gnu.org/licenses
 */

package com.pajato.android.gamechat.exp;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.v4.content.ContextCompat;
import android.util.DisplayMetrics;
import android.view.Gravity;
import android.view.View;
import android.widget.GridLayout;
import android.widget.TextView;

import com.pajato.android.gamechat.common.BaseFragment;
import com.pajato.android.gamechat.main.PaneManager;

import java.util.List;

import static android.util.TypedValue.COMPLEX_UNIT_SP;
import static com.pajato.android.gamechat.R.color.colorLightGray;
import static com.pajato.android.gamechat.R.id.board;

/**
 * Provide a checkerboard class to be used for chess and checkers.
 *
 * @author Paul Michael Reilly on 2/12/17.
 */

public class Checkerboard {

    // Private class constants.

    /**
     * The space taken by a smart phone notification bar and digital buttons in dp units. The
     * number comes from the calculation of 56dp * 1.5. 56 comes from an estimation of the DP of the
     * phone's lower digital button area, and the additional half of the 1.5 comes from the
     * notification bar at the top of the phone's screen, which was eyeballed to be roughly half the
     * size of the digital button area. This is something that could potentially be improved upon.
     */
    private static final int SMART_PHONE_HEIGHT = 84;

    /**
     * The space taken by a tablet notification bar and digital buttons in dp units. Similar to the
     * Smart Phone Height variable, it is based on a calculation of 28dp * 1.5, which was a similar
     * estimate of the tablet's digital button area, and the roughly-half-the-size notification bar.
     */
    private static final int TABLET_HEIGHT = 42;

    /**
     * The amount of vertical space allocated to the mini FAB control, which consists of three
     * parts: the (implicit) top margin for the FAB control, the FAB icon size and the bottom
     * margin.
     */
    private static final int FAB_HEIGHT = 48 + 40 + 16;

    // Private instance variables.

    /** The size, in pixels, of the checkerboard cells to use on this device. */
    private int mCellSize;

    /** The GridLayout used to build the checkerboard UI. */
    private GridLayout mGrid;

    // Public constructor.

    /** Build an instance to establish the cell size for a given context. */
    public Checkerboard(final Context context) {
        mCellSize = getCellSize(context);
    }

    // Public instance methods.

    /** Return the cell at a given position. */
    public TextView getCell(final int position) {
        return (TextView) mGrid.getChildAt(position);
    }

    /** Initialize the checkerboard by finding the grid layout and computing the cell size. */
    public void init(@NonNull BaseFragment fragment, final View.OnClickListener handler) {
        mGrid = (GridLayout) fragment.getActivity().findViewById(board);
        if (mGrid == null)
            return;

        // Reset the grid layout.
        // There appears to be a bug with GridLayout in that if the row and column counts are not
        // specified, an illegal argument exception can occur, so explicitly set the row and column
        // counts.
        mGrid.removeAllViews();
        mGrid.setRowCount(8);
        mGrid.setColumnCount(8);
        for (int i = 0; i < 64; i++) {
            TextView currentTile = getCellView(fragment.getContext(), i, mCellSize);
            currentTile.setOnClickListener(handler);
            mGrid.addView(currentTile);
        }
    }

    /** Set the background for the tile at the given position. */
    void handleTileBackground(final Context context, final int position) {
        // Handle the checkerboard positions (where 'checkerboard' means the background pattern).
        boolean isEven = (position % 2 == 0);
        boolean isOdd = (position % 2 == 1);
        boolean evenRowEvenColumn = ((position / 8) % 2 == 0) && isEven;
        boolean oddRowOddColumn = ((position / 8) % 2 == 1) && isOdd;

        // Create the checkerboard pattern on the button backgrounds.
        TextView tile = getCell(position);
        if (evenRowEvenColumn || oddRowOddColumn)
            tile.setBackgroundColor(ContextCompat.getColor(context, android.R.color.white));
        else
            tile.setBackgroundColor(ContextCompat.getColor(context, colorLightGray));
    }

    /**
     * A utility method that facilitates keeping the board's checker pattern in place throughout the
     * highlighting and de-highlighting process. It accepts a tile and sets its background to white
     * or dark grey, depending on its location in the board.
     *
     * @param context The fragment context used to obtain resources.
     * @param index the index of the tile, used to determine the color of the background.
     * @param tile the tile whose color we are changing.
     */
    private void handleTileBackground(final Context context, final int index, final TextView tile) {
        // Handle the checkerboard positions (where 'checkerboard' means the background pattern).
        boolean isEven = (index % 2 == 0);
        boolean isOdd = (index % 2 == 1);
        boolean evenRowEvenColumn = ((index / 8) % 2 == 0) && isEven;
        boolean oddRowOddColumn = ((index / 8) % 2 == 1) && isOdd;

        // Create the checkerboard pattern on the button backgrounds.
        if (evenRowEvenColumn || oddRowOddColumn)
            tile.setBackgroundColor(ContextCompat.getColor(context, android.R.color.white));
        else
            tile.setBackgroundColor(ContextCompat.getColor(context, colorLightGray));
    }

    /** Set the highlight at a given position using a given color resource id. */
    void setHighlight(Context context, int position, List<Integer> possibleMoves) {
        if (position != -1)
            setHighlight(context, position, android.R.color.holo_red_dark);
        for (int possiblePosition : possibleMoves)
            if (possiblePosition != -1)
                setHighlight(context, possiblePosition, android.R.color.holo_red_light);
    }

    // Private instance methods.

    /** Return the computed cell size based on the device size provided by the given context. */
    private int getCellSize(@NonNull final Context context) {
        // Establish the cell size for the checkerboard.
        DisplayMetrics metrics = context.getResources().getDisplayMetrics();
        final int dipsHeight = getDips(metrics, metrics.heightPixels);
        final int dipsWidth = getDips(metrics, metrics.widthPixels);
        final int toolbarDps = PaneManager.instance.isTablet() ? TABLET_HEIGHT : SMART_PHONE_HEIGHT;

        // Establish the controls height. Remember to convert the SP of text in the layout to DP.
        // The default is 1-1, but is not necessarily, as SP can scale based on user settings.
        // This is very arbitrary based on the current physical XML layouts -- if those are changed
        // (particularly those in exp_toolbar_game_inc.xml and exp_checkers.xml) then this should be
        // changed to reflect them.
        int toolbarTextRow1 = convertSPtoDP(20, metrics);
        int toolbarTextRow2 = Math.max(toolbarTextRow1, (4 + convertSPtoDP(14, metrics)));
        int controlsRow1 = Math.max(toolbarTextRow1, (40 + convertSPtoDP(14, metrics)));
        int controlsRow2 = Math.max(convertSPtoDP(20, metrics), (36 + convertSPtoDP(14, metrics)));
        controlsRow2 = Math.max(56, controlsRow2);
        final int controlsHeight = toolbarTextRow1 + toolbarTextRow2 + controlsRow1 + controlsRow2;

        final int unavailableHeight = controlsHeight + FAB_HEIGHT + toolbarDps;
        final int boardHeight = dipsHeight - unavailableHeight;
        final int boardWidth = (PaneManager.instance.isTablet() ? dipsWidth / 2 : dipsWidth) - 32;
        boolean useWidth = boardHeight > boardWidth;
        return Math.round((useWidth ? boardWidth : boardHeight) / 8) * Math.round(metrics.density);
    }

    /** Return a text view representing a cell at a given index of a given size. */
    private TextView getCellView(final Context context, final int index, final int cellSize) {
        // Set up the gridlayout params, so that each cell is functionally identical.
        TextView cellView = new TextView(context);
        GridLayout.LayoutParams param = new GridLayout.LayoutParams();
        param.height = cellSize;
        param.width = cellSize;
        param.rightMargin = 0;
        param.topMargin = 0;
        param.setGravity(Gravity.CENTER);

        // Set up the cell specific information.
        cellView.setLayoutParams(param);
        cellView.setTag(String.valueOf(index));
        float sp = cellSize / context.getResources().getDisplayMetrics().scaledDensity;
        cellView.setTextSize(COMPLEX_UNIT_SP, (float)(sp * 0.9));
        cellView.setGravity(Gravity.CENTER);
        cellView.setText("");
        handleTileBackground(context, index, cellView);
        return cellView;
    }

    /** Set the highlight at a given position using a given color resource id. */
    private void setHighlight(Context context, int position, final int colorResId) {
        mGrid.getChildAt(position).setBackgroundColor(ContextCompat.getColor(context, colorResId));
    }

    /** Convert a given pixel number to dps. */
    private int getDips(final DisplayMetrics metrics, final int px) {
        // float dp = px / (metrics.densityDpi / DisplayMetrics.DENSITY_DEFAULT);
        return (px * DisplayMetrics.DENSITY_DEFAULT) / metrics.densityDpi;

    }

    /** Convert a given sp value into dps in order to account for variable text size. */
    private int convertSPtoDP(final int spValue, final DisplayMetrics displayMetrics) {
        int px = (int) (spValue * displayMetrics.scaledDensity);
        return getDips(displayMetrics, px);
    }
}
