package com.pajato.android.gamechat.exp;

import android.content.Context;
import android.support.annotation.NonNull;

import com.pajato.android.gamechat.common.model.Account;
import com.pajato.android.gamechat.common.Dispatcher;
import com.pajato.android.gamechat.exp.model.ExpProfile;
import com.pajato.android.gamechat.exp.model.Player;

import java.util.List;

/**
 * Provides a base class for experience objects that are also games.
 */
public abstract class BaseGameExpFragment extends BaseExperienceFragment {

    abstract protected List<Account> getPlayers(final Dispatcher<ExpFragmentType, ExpProfile> dispatcher);

    abstract protected List<Player> getDefaultPlayers(final Context context, final List<Account> players);

    abstract protected void createExperience(final Context context, final List<Account> playerAccounts);

    /** Create a new experience to be displayed in this fragment. */
    protected void createExperience(@NonNull final Context context,
                                    @NonNull final Dispatcher<ExpFragmentType, ExpProfile> dispatcher) {
        // Set up the players and persist the game.
        List<Account> players = getPlayers(dispatcher);
        createExperience(context, players);
    }
}