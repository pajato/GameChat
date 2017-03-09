package com.pajato.android.gamechat.exp.chess;

import android.content.DialogInterface;
import android.support.v7.app.AlertDialog;

import com.google.firebase.database.Exclude;
import com.google.firebase.database.IgnoreExtraProperties;
import com.pajato.android.gamechat.R;
import com.pajato.android.gamechat.common.BaseFragment;
import com.pajato.android.gamechat.event.AppEventManager;
import com.pajato.android.gamechat.event.ExperienceResetEvent;
import com.pajato.android.gamechat.exp.Board;
import com.pajato.android.gamechat.exp.ExpType;
import com.pajato.android.gamechat.exp.Experience;
import com.pajato.android.gamechat.exp.State;
import com.pajato.android.gamechat.exp.model.Player;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.pajato.android.gamechat.exp.ExpType.chessET;
import static com.pajato.android.gamechat.exp.State.active;

/**
 *  Provide a Firebase model class for a chess game experience.
 *
 * @author Paul Michael Reilly on 10/1/16
 * @author Sandy Scott on 12/30/16.
 */
@IgnoreExtraProperties public class Chess implements Experience {

    /** A map of board position (0->63) to piece object. */
    public ChessBoard board;

    /** The creation timestamp. */
    public long createTime;

    /** The group push key. */
    public String groupKey;

    /** The experience push key. */
    public String key;

    /** The game level. */
    public int level;

    /** The last modification timestamp. */
    public long modTime;

    /** The experience display name. */
    public String name;

    /** The member account identifier who created the experience. */
    public String owner;

    /** The list of players, for chessET, two of them. */
    public List<Player> players;

    /** The room push key. */
    public String roomKey;

    /** The game state. */
    public State state;

    /** The current turn. */
    public boolean turn;

    /** The experience type ordinal value. */
    public String type;

    /** A list of users (by account identifier) in the room, that have not yet seen the message. */
    private List<String> unseenList = new ArrayList<>();

    /** The experience icon url. */
    public String url;

    // Castling Management booleans

    /** Remember that the primary team queen side rook has moved */
    public boolean primaryQueenSideRookHasMoved;

    /** Remember that the primary team king side rook has moved */
    public boolean primaryKingSideRookHasMoved;

    /** Remember that the primary team king has moved */
    public boolean primaryKingHasMoved;

    /** Remember that the secondary team queen side rook has moved */
    public boolean secondaryQueenSideRookHasMoved;

    /** Remember that the secondary team king side rook has moved */
    public boolean secondaryKingSideRookHasMoved;

    /** Remember that the secondary team king has moved */
    public boolean secondaryKingHasMoved;

    /** Build an empty args constructor for the database. */
    @SuppressWarnings("unused") public Chess() {}

    /** Build a default Checkers using the given parameters and defaulting the rest. */
    public Chess(final ChessBoard board, final String key, final String id, final int level,
                 final String name, final long createTime, final String groupKey,
                 final String roomKey, final List<Player> players) {
        this.board = board;
        this.createTime = createTime;
        this.key = key;
        this.groupKey = groupKey;
        this.modTime = createTime;
        this.level = level;
        this.name = name;
        this.owner = id;
        this.players = players;
        this.roomKey = roomKey;
        state = active;
        turn = true;
        type = chessET.name();
        url = "android.resource://com.pajato.android.gamechat/drawable/ic_chess";
        // TODO: convert the following into a map that is empty by default, something like:
        // Map<Integer (default position), Boolean> castlingMap = new HashMap<>();
        primaryQueenSideRookHasMoved = false;
        primaryKingSideRookHasMoved = false;
        primaryKingHasMoved = false;
        secondaryQueenSideRookHasMoved = false;
        secondaryKingSideRookHasMoved = false;
        secondaryKingHasMoved = false;

    }

    // Public instance methods.

    /** Implement the interface by returning the chess board. */
    @Override public Board getBoard() {
        return board;
    }

    /** Return the create time to satisfy the Experience contract. */
    @Exclude @Override public long getCreateTime() {
        return createTime;
    }

    /** Return the experience push key. */
    @Exclude @Override public String getExperienceKey() {
        return key;
    }

    /** Return the fragment type value or null if no such fragment type exists. */
    @Exclude @Override public ExpType getExperienceType() {
        return ExpType.valueOf(type);
    }

    /** Return the group push key. */
    @Exclude @Override public String getGroupKey() {
        return groupKey;
    }

    /** Return the experience name. */
    @Exclude @Override public String getName() {
        return name;
    }

    /** Return the experience modification time. */
    @Exclude @Override public long getModTime() {
        return modTime;
    }

    /** Return the room push key. */
    @Exclude @Override public String getRoomKey() { return roomKey; }

    /** Implement the interface by returning the list of players. */
    @Exclude @Override public List<Player> getPlayers() {
        return players;
    }

    /** Return the state value for Firebase. */
    public String getState() {
        return state.name();
    }

    /** Implement the interface by returning the experience state. */
    @Exclude @Override public State getStateType() {
        return state;
    }

    /** Return the room push key. */
    @Exclude @Override public boolean getTurn() { return turn; }

    /** Return the unseen list. */
    @Exclude @Override public List<String> getUnseenList() { return unseenList; }

    /** Implement the interface by returning null or the winning player. */
    @Exclude @Override public Player getWinningPlayer() {
        switch (state) {
            case primary_wins:
                // Assumes 'primary' player is at index 0
                return players.get(0);
            case secondary_wins:
                // Assumes 'primary' player is at index 1
                return players.get(1);
            default:
                return null;
        }
    }

    /** Reset the game after confirming with the user. */
    @Exclude @Override public boolean reset(BaseFragment fragment) {
        if (state.isDone()) {
            resetModel();
            return true;
        }
        final String key = this.key;
        new AlertDialog.Builder(fragment.getActivity())
                .setTitle(fragment.getString(R.string.ResetGameTitle))
                .setMessage(fragment.getString(R.string.ResetGameMessage))
                .setNegativeButton(android.R.string.cancel, null)
                .setPositiveButton(android.R.string.ok,
                        new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface d, int id) {
                                resetModel();
                                AppEventManager.instance.post(new ExperienceResetEvent(key));
                            }
                        })
                .create()
                .show();
        return false;
    }

    /** Set the experience key to satisfy the Experience contract. */
    @Exclude @Override public void setExperienceKey(final String key) {
        this.key = key;
    }

    /** Set the group key to satisfy the Experience contract. */
    @Exclude @Override public void setGroupKey(final String key) {
        this.groupKey = key;
    }

    /** Set the experience name */
    @Exclude @Override public void setName(final String name) {
        this.name = name;
    }

    /** Set the room key to satisfy the Experience contract. */
    @Exclude @Override public void setRoomKey(final String key) {
        this.roomKey = key;
    }

    /** Set the modification timestamp. */
    @Exclude @Override public void setModTime(final long value) {
        modTime = value;
    }

    /** Set the state given a string value. */
    public void setState(final String value) {
        state = value != null ? State.valueOf(value) : null;
    }

    /** Implement the interface by setting the experience state. */
    @Exclude @Override public void setStateType(final State value) {
        state = value;
    }

    /** Update the win count based on the current state. */
    @Exclude @Override public void setWinCount() {
        switch (state) {
            case primary_wins:
                players.get(0).winCount++;
                break;
            case secondary_wins:
                players.get(1).winCount++;
                break;
            default:
                break;
        }
    }

    /** Provide a default map for a Firebase create/update. */
    @Exclude @Override public Map<String, Object> toMap() {
        Map<String, Object> result = new HashMap<>();
        result.put("board", board);
        result.put("createTime", createTime);
        result.put("key", key);
        result.put("level", level);
        result.put("groupKey", groupKey);
        result.put("modTime", modTime);
        result.put("name", name);
        result.put("owner", owner);
        result.put("players", players);
        result.put("roomKey", roomKey);
        result.put("state", getState());
        result.put("turn", turn);
        result.put("type", type);
        result.put("unseenList", unseenList);
        result.put("url", url);
        result.put("primaryQueenSideRookHasMoved", primaryQueenSideRookHasMoved);
        result.put("primaryKingSideRookHasMoved", primaryKingSideRookHasMoved);
        result.put("primaryKingHasMoved", primaryKingHasMoved);
        result.put("secondaryQueenSideRookHasMoved", secondaryQueenSideRookHasMoved);
        result.put("secondaryKingSideRookHasMoved", secondaryKingSideRookHasMoved);
        result.put("secondaryKingHasMoved", secondaryKingHasMoved);
        return result;
    }

    /** Toggle the turn state. */
    @Exclude @Override public boolean toggleTurn() {
        turn = !turn;
        return turn;
    }

    // Private instance methods

    /** Clear the board and reset the state and turn values */
    private void resetModel() {
        board = new ChessBoard();
        board.init();
        state = active;
        turn = true;
    }
}
