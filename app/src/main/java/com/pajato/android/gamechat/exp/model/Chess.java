package com.pajato.android.gamechat.exp.model;

import com.google.firebase.database.Exclude;
import com.google.firebase.database.IgnoreExtraProperties;
import com.pajato.android.gamechat.exp.ChessPiece;
import com.pajato.android.gamechat.exp.ExpType;
import com.pajato.android.gamechat.exp.Experience;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.pajato.android.gamechat.exp.ExpType.chess;

/** Provide a Firebase model class for a chess game experience. */
@IgnoreExtraProperties
public class Chess implements Experience {

    public final static int ACTIVE = 0;
    public final static int PRIMARY_WINS = 1;
    public final static int SECONDARY_WINS = 2;
    public final static int TIE = 3;
    public final static int PENDING = 4;

    /**
     * A map of board position (0->63) to piece object.
     */
    public ChessBoard board;

    /** The creation timestamp. */
    private long createTime;

    /** The group push key. */
    public String groupKey;

    /** The experience push key. */
    public String key;

    /** The game level. */
    public int level;

    /** The last modification timestamp. */
    private long modTime;

    /** The experience display name. */
    public String name;

    /** The member account identifier who created the experience. */
    public String owner;

    /** The list of players, for chess, two of them. */
    public List<Player> players;

    /** The room push key. */
    public String roomKey;

    /** The game state. */
    public int state;

    /** The current turn. */
    public boolean turn;

    /** The experience type ordinal value. */
    public int type = -1;

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
        this.modTime = 0;
        this.level = level;
        this.name = name;
        this.owner = id;
        this.players = players;
        this.roomKey = roomKey;
        state = ACTIVE;
        turn = true;
        type = chess.ordinal();
        url = "android.resource://com.pajato.android.gamechat/drawable/ic_chess";
        primaryQueenSideRookHasMoved = false;
        primaryKingSideRookHasMoved = false;
        primaryKingHasMoved = false;
        secondaryQueenSideRookHasMoved = false;
        secondaryKingSideRookHasMoved = false;
        secondaryKingHasMoved = false;

    }

    /** Provide a default map for a Firebase create/update. */
    @Exclude
    @Override public Map<String, Object> toMap() {
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
        result.put("state", state);
        result.put("turn", turn);
        result.put("type", type);
        result.put("url", url);
        result.put("primaryQueenSideRookHasMoved", primaryQueenSideRookHasMoved);
        result.put("primaryKingSideRookHasMoved", primaryKingSideRookHasMoved);
        result.put("primaryKingHasMoved", primaryKingHasMoved);
        result.put("secondaryQueenSideRookHasMoved", secondaryQueenSideRookHasMoved);
        result.put("secondaryKingSideRookHasMoved", secondaryKingSideRookHasMoved);
        result.put("secondaryKingHasMoved", secondaryKingHasMoved);
        return result;
    }

    /** Return the experience push key. */
    @Exclude @Override public String getExperienceKey() {
        return key;
    }

    /** Return the fragment type value or null if no such fragment type exists. */
    @Exclude
    @Override public ExpType getExperienceType() {
        if (type < 0 || type >= ExpType.values().length) return null;

        return ExpType.values()[type];
    }

    /** Return the group push key. */
    @Exclude @Override public String getGroupKey() {
        return groupKey;
    }

    /** Return the experience name. */
    @Exclude @Override public String getName() {
        return name;
    }

    /** Return the room push key. */
    @Exclude @Override public String getRoomKey() { return roomKey; }

    /** Set the experience key to satisfy the Experience contract. */
    @Exclude @Override public void setExperienceKey(final String key) {
        this.key = key;
    }

    /** Set the modification timestamp. */
    @Exclude @Override public void setModTime(final long value) {
        modTime = value;
    }

    /** Update the win count based on the current state. */
    @Exclude @Override public void setWinCount() {
        switch (state) {
            case PRIMARY_WINS:
                players.get(0).winCount++;
                break;
            case SECONDARY_WINS:
                players.get(1).winCount++;
                break;
            default:
                break;
        }
    }

    /** Toggle the turn state. */
    @Exclude public boolean toggleTurn() {
        turn = !turn;
        return turn;
    }

    /** Return the winning player's name or null if the game is active or ended in a tie. */
    @Exclude public String getWinningPlayerName() {
        switch (state) {
            case PRIMARY_WINS:
                // Assumes 'primary' player is at index 0
                return players.get(0).name;
            case SECONDARY_WINS:
                // Assumes 'primary' player is at index 1
                return players.get(1).name;
            case ACTIVE:
            case TIE:
            case PENDING:
            default:
                return null;
        }
    }

}
