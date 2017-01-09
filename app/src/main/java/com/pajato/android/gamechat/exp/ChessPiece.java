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

    public enum ChessTeam {
        NONE, PRIMARY, SECONDARY
    }

    public enum PieceType {
        NONE,
        KING,
        QUEEN,
        BISHOP,
        KNIGHT,
        ROOK,
        PAWN
    }

    // Access must be public for Firebase use
    public PieceType pieceType;

    // Access must be public for Firebase use
    public ChessTeam teamId;

    /** Build an empty args constructor for the database. */
    @SuppressWarnings("unused") public ChessPiece() {}

    /** Constructor requires a piece and team specified, provided as constants in the class. */
    public ChessPiece(final PieceType piece, final ChessTeam team) {
        this.pieceType = piece;
        this.teamId = team;
    }

    boolean isTeamPiece(final PieceType p, final ChessTeam t) {
        return (this.getPiece().equals(p) && this.getTeam().equals(t));
    }

    /** Provide a default map for a Firebase create/update. */
    public Map<String, Object> toMap() {
        Map<String, Object> result = new HashMap<>();
        result.put("peiceId", pieceType);
        result.put("teamId", teamId);
        return result;
    }

    @Exclude
    public PieceType getPiece() {
        return this.pieceType;
    }

    @Exclude
    public ChessTeam getTeam() {
        return this.teamId;
    }

    /**
     * Returns a specific drawable for the piece type specified in the parameter.
     *
     * @param pieceType the piece type, all of which are available as public constants in this class
     * @return a drawable ID that corresponds to the parameter.
     */
    @Exclude
    static int getDrawableFor(final PieceType pieceType) {
        int drawable;
        switch(pieceType) {
            case KING: drawable = R.drawable.ic_stars_black_36dp;
                break;
            case QUEEN: drawable = R.drawable.ic_games_white;
                break;
            case BISHOP: drawable = R.drawable.ic_info_black;
                break;
            case KNIGHT: drawable = R.drawable.vd_help_black_24px;
                break;
            case ROOK: drawable = R.drawable.vd_settings_black_24px;
                break;
            default:
            case PAWN: drawable = R.drawable.ic_account_circle_black_36dp;
                break;
        }
        return drawable;
    }


}