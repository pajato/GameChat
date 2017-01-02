package com.pajato.android.gamechat.exp.model;

import com.google.firebase.database.Exclude;
import com.google.firebase.database.IgnoreExtraProperties;
import com.pajato.android.gamechat.exp.ExpType;
import com.pajato.android.gamechat.exp.Experience;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.pajato.android.gamechat.exp.ExpType.checkers;

/**
 * Created by sscott on 12/30/16.
 */

/** Provide a Firebase model class for a checkers game experience. */
@IgnoreExtraProperties
public class Checkers implements Experience {

    public final static int ACTIVE = 0;
    public final static int RED_WINS = 1;
    public final static int BLACK_WINS = 2;
    public final static int TIE = 3;
    public final static int PENDING = 4;

    /** A POJO encapsulating the board moves */
    public CheckersBoard board;

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

    /** Build an empty args constructor for the database. */
    @SuppressWarnings("unused") public Checkers() {}

    /** Build a default Checkers using the given parameters and defaulting the rest. */
    public Checkers(final String key, final String id, final int level, final String name,
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
        type = checkers.ordinal();
        url = "android.resource://com.pajato.android.gamechat/drawable/ic_checkers";
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
        return result;
    }

    // TODO: what to do for Checkers??
    /** Return the value associated with the current player: 1 == X, 2 == O. */
    @Exclude public int getSymbolValue() {
        // This implies that player 1 is always X and player 2 is always O.
        return turn ? 1 : 4;
    }

    // TODO: what to do for Checkers??
    /** Return the symbol text value for the player whose turn is current. */
    @Exclude public String getSymbolText() {
        return turn ? players.get(0).symbol : players.get(1).symbol;
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

    /** Return the value associated with the current player: 1 == red, 2 == black. */
    @Exclude public int getTeamValue() {
        // This implies that player 1 is always red and player 2 is always black.
        // TODO: FIX THIS!!!
        return turn ? 1 : 4;
    }

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
        // TODO: Implement this!
    }

    /** Toggle the turn state. */
    @Exclude public boolean toggleTurn() {
        turn = !turn;
        return turn;
    }

    /** Return the winning player's name or null if the game is active or ended in a tie. */
    @Exclude public String getWinningPlayerName() {
        switch (state) {
            case RED_WINS:
                return players.get(0).name; // TODO: THIS WON'T WORK unless red is 0 index
            case BLACK_WINS:
                return players.get(1).name; // TODO: THIS WON'T WORK unless black is 1 index
            case ACTIVE:
            case TIE:
            case PENDING:
            default:
                return null;
        }
    }

}
