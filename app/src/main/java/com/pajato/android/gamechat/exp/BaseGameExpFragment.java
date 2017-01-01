package com.pajato.android.gamechat.exp;

import android.content.Context;
import android.support.annotation.NonNull;

import com.pajato.android.gamechat.chat.model.Account;
import com.pajato.android.gamechat.common.Dispatcher;
import com.pajato.android.gamechat.database.ExperienceManager;
import com.pajato.android.gamechat.exp.model.ExpProfile;
import com.pajato.android.gamechat.exp.model.Player;

import java.util.List;

/**
 * Created by sscott on 12/31/16.
 */

public abstract class BaseGameExpFragment extends BaseExperienceFragment {

    abstract protected List<Account> getPlayers(final Dispatcher<ExpFragmentType, ExpProfile> dispatcher);

    abstract protected List<Player> getDefaultPlayers(final Context context, final List<Account> players);

//    abstract protected void createExperience(final Context context, final List<Account> playerAccounts);

    abstract protected void createExperience(final Context context, final List<Account> playerAccounts);

    /** Create a new experience to be displayed in this fragment. */
    protected void createExperience(@NonNull final Context context,
                                    @NonNull final Dispatcher<ExpFragmentType, ExpProfile> dispatcher) {
        // Set up the players and persist the game.
        List<Account> players = getPlayers(dispatcher);
        createExperience(context, players);
    }

//    /** Provide a default implementation for setting up an experience. */
//    protected void setupExperience(final Context context, final Dispatcher<ExpFragmentType, ExpProfile> dispatcher) {
//        // Ensure that the dispatcher is valid.  Abort if not.
//        // TODO: might be better to show a toast or snackbar on error.
//        if (dispatcher == null || dispatcher.type == null) return;
//
//        // Determine if the fragment type does not require an experience. Abort if not.
//        ExpType expType = dispatcher.type.expType;
//        if (expType == null) return;
//
//        // Determine if the dispatcher has a single experience profile.
//        if (dispatcher.payload != null) {
//            // It does.  Either get the cached experience or fetch it from the database.
//            Experience exp = ExperienceManager.instance.experienceMap.get(dispatcher.key);
//            if (exp == null) {
//                // Fetch the experience from the database.
//                ExperienceManager.instance.setExperienceWatcher(dispatcher.payload);
//            }
//        } else
//            // Create a new experience.
//            createExperience(context, dispatcher);
//    }
}
