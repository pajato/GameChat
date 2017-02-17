package com.pajato.android.gamechat.exp.model;

import com.google.firebase.database.Exclude;
import com.google.firebase.database.IgnoreExtraProperties;
import com.pajato.android.gamechat.exp.ExpType;
import com.pajato.android.gamechat.exp.Experience;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.pajato.android.gamechat.exp.ExpType.checkersET;

/** Provide a Firebase model class for a checkers game experience. */
@IgnoreExtraProperties
public class Checkers implements Experience {

    // State definitions.
    public final static int ACTIVE = 0;
    public final static int PRIMARY_WINS = 1;
    public final static int SECONDARY_WINS = 2;
    public final static int TIE = 3;
    public final static int PENDING = 4;

    /**
     * A map of board position (0->63) to piece type (where piece type can be PRIMARY_PIECE,
     * PRIMARY_KING, SECONDARY_PIECE or SECONDARY_KING).
     */
    public Map<String, String> board;

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

    /** The list of players, for checkers, two of them. */
    public List<Player> players;

    /** The room push key. */
    public String roomKey;

    /** The game state. */
    public int state;

    /** The current turn indicator: True = Player 1, False = Player 2. */
    public boolean turn;

    /** The experience type name. */
    public String type;
    // TODO:
    //
    // figure this one out, but use a placeholder hack for now.

    /** A list of users (by account identifier) in the room, that have not yet seen the message. */
    public List<String> unseenList;

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
        this.modTime = createTime;
        this.level = level;
        this.name = name;
        this.owner = id;
        this.players = players;
        this.roomKey = roomKey;
        state = ACTIVE;
        turn = true;
        type = checkersET.name();
        url = "android.resource://com.pajato.android.gamechat/drawable/ic_checkers";
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
        result.put("state", state);
        result.put("turn", turn);
        result.put("type", type);
        result.put("url", url);
        return result;
    }

    /** Return the experience push key. */
    @Exclude @Override public String getExperienceKey() {
        return key;
    }

    /** Return the fragment type value or null if no such fragment type exists. */
    @Exclude @Override public ExpType getExperienceType() {
        return ExpType.valueOf(type);
    }

    /** Return the create time to satisfy the Experience contract. */
    @Override public long getCreateTime() {
        return createTime;
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

    /** Remember the turn state: true means primary's turn; false means secondary's turn */
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
