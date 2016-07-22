package com.pajato.android.gamechat.account;

import lombok.Data;
import lombok.AllArgsConstructor;

/**
 * Provides an account state change data model class.
 *
 * @author Paul Michael Reilly
 */
@AllArgsConstructor(suppressConstructorProperties = true)
@Data
public class AccountStateChangeEvent {

    // Private instance variables

    /** The changed account: if null the User signed out, if non-null the User signed in. */
    private Account account;

}
