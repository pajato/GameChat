package com.pajato.android.gamechat.exp.chess;

import com.google.firebase.database.Exclude;
import com.google.firebase.database.IgnoreExtraProperties;
import com.pajato.android.gamechat.exp.ExpType;
import com.pajato.android.gamechat.exp.Experience;
import com.pajato.android.gamechat.exp.model.Player;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.pajato.android.gamechat.exp.ExpType.chessET;

/**
 *  Provide a Firebase model class for a chess game experience.
 *
 *  @author Sandy Scott on 12/30/16.
 */
@IgnoreExtraProperties public class Chess implements Experience {

    /** The set of state constants for a chess game. */
    public enum State {active, check, pending, primary_wins, secondary_wins, tie}

    /**
     * A map of board position (0->63) to piece object.
     */
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
    public List<String> unseenList;

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
    public Chess(final String key, final String id, final int level, final String name,
                    final long createTime, final String groupKey, final String roomKey,
                    final List<Player> players) {
        this.createTime = createTime;
        this.key = key;
        this.groupKey = groupKey;
        this.modTime = createTime;
        this.level = level;
        this.name = name;
        this.owner = id;
        this.players = players;
        this.roomKey = roomKey;
        state = State.active;
        turn = true;
        type = chessET.name();
        url = "android.resource://com.pajato.android.gamechat/drawable/ic_chess";
        primaryQueenSideRookHasMoved = false;
        primaryKingSideRookHasMoved = false;
        primaryKingHasMoved = false;
        secondaryQueenSideRookHasMoved = false;
        secondaryKingSideRookHasMoved = false;
        secondaryKingHasMoved = false;

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
        result.put("state", state.name());
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

    /** Return the create time to satisfy the Experience contract. */
    @Override public long getCreateTime() {
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

    /** Return the room push key. */
    @Exclude @Override public boolean getTurn() { return turn; }

    /** Set the state given a string value. */
    public void setState(final String value) {
        state = value != null ? State.valueOf(value) : null;
    }

    /** Return the unseen list. */
    @Exclude @Override public List<String> getUnseenList() { return unseenList; }

    /** Set the experience key to satisfy the Experience contract. */
    @Exclude @Override public void setExperienceKey(final String key) {
        this.key = key;
    }

    /** Set the group key to satisfy the Experience contract. */
    @Exclude @Override public void setGroupKey(final String key) {
        this.groupKey = key;
    }

    /** Set the room key to satisfy the Experience contract. */
    @Exclude @Override public void setRoomKey(final String key) {
        this.roomKey = key;
    }

    /** Set the modification timestamp. */
    @Exclude @Override public void setModTime(final long value) {
        modTime = value;
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

    /** Toggle the turn state. */
    @Exclude @Override public boolean toggleTurn() {
        turn = !turn;
        return turn;
    }

    /** Return the winning player's name or null if the game is active or ended in a tie. */
    @Exclude public String getWinningPlayerName() {
        switch (state) {
            case primary_wins:
                // Assumes 'primary' player is at index 0
                return players.get(0).name;
            case secondary_wins:
                // Assumes 'primary' player is at index 1
                return players.get(1).name;
            case active:
            case tie:
            case pending:
            default:
                return null;
        }
    }
}
