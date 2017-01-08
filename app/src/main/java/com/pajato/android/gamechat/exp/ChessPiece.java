package com.pajato.android.gamechat.exp;

import com.google.firebase.database.Exclude;
import com.google.firebase.database.IgnoreExtraProperties;
import com.pajato.android.gamechat.R;

import java.util.HashMap;
import java.util.Map;

import static android.R.attr.name;

/**
 * A simple P.O.J.O. class that keeps track of a chess pieces type and the team it is on.
 */
@IgnoreExtraProperties
public class ChessPiece {

    public String pieceId; // One of the piece types (KING, QUEEN, ... etc)

    public int teamId; // Either PRIMARY_TEAM or SECONDARY_TEAM

    /** Build an empty args constructor for the database. */
    @SuppressWarnings("unused") public ChessPiece() {}

    /** Constructor requires a piece and team specified, provided as constants in the class. */
    ChessPiece(final String piece, final int team) {
        this.pieceId = piece;
        this.teamId = team;
    }

    /** Provide a default map for a Firebase create/update. */
    @Exclude
    public Map<String, Object> toMap() {
        Map<String, Object> result = new HashMap<>();
        result.put("peiceId", pieceId);
        result.put("teamId", teamId);
        return result;
    }

    /** Build an instance accepting Object values for all fields. */
    public ChessPiece(final Object pieceId, final Object teamId) {
        this.pieceId = pieceId instanceof String ? (String) pieceId : "";
        this.teamId = teamId instanceof Integer ? (Integer) teamId : 0;
    }

    @Exclude
    public String getPiece() {
        return this.pieceId;
    }

    @Exclude
    public int getTeam() {
        return this.teamId;
    }

}