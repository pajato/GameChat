package com.pajato.android.gamechat.common.adapter;

import java.util.Locale;

/**
 * Provide a POJO to encapsulate a recycler view list item for use in the player mode menu.
 */

public class PlayModeMenuEntry {

    // Type constants
    final static int MENU_TEXT_TYPE = 100;

    /** The associated account push key, or null if not a user entry */
    public String accountKey;

    /** The group key associated with the target member, if any */
    public String groupKey;

    /** A description of the item. */
    private String mDescription;

    /** The entry type, provided by the item. */
    public int type;

    /** The text string used for menu display */
    public String title;

    /** Build an instance for a given menu item. */
    public PlayModeMenuEntry(String text, String accountKey, String groupKey) {
        this.type = MENU_TEXT_TYPE;
        this.accountKey = accountKey;
        this.groupKey = groupKey;
        this.title = text;
        String format = "Play Mode Menu item with title {%s}, account id {%s} and group key {%s}.";
        mDescription = String.format(Locale.US, format, title, accountKey, groupKey);
    }

    // Public instance methods.

    /** Return a description of the object. */
    @Override public String toString() {
        return mDescription;
    }
}
