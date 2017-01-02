package com.pajato.android.gamechat.exp.model;

import android.util.SparseIntArray;
import android.widget.GridLayout;
import android.widget.ImageButton;

import java.util.ArrayList;

/**
 * Provide a POJO representing a 12x12 checkers board and possible moves.
 */

public class CheckersBoard {
    public SparseIntArray boardMap;
    public ImageButton highlightedTile;
    public boolean mIsHighlighted = false;
    public ArrayList<Integer> possibleMoves;
}
