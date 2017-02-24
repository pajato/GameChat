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
        int cellSize = getCellSize(fragment);
        for (int i = 0; i < 64; i++) {
            TextView currentTile = getCellView(fragment.getContext(), i, cellSize);
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

    /** Return the computed cell size based on the device size provided by the given fragment. */
    private int getCellSize(@NonNull final BaseFragment fragment) {
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
        return Math.min(boardWidth, boardHeight) / 8;
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

    /** Return the number of physical pixels for a given number of device independent pixels. */
    private int getPixels(final DisplayMetrics metrics, final int dp) {
        return Math.round(dp * (metrics.xdpi / DisplayMetrics.DENSITY_DEFAULT));
    }

    /** Set the highlight at a given position using a given color resource id. */
    private void setHighlight(Context context, int position, final int colorResId) {
        mGrid.getChildAt(position).setBackgroundColor(ContextCompat.getColor(context, colorResId));
    }
}
