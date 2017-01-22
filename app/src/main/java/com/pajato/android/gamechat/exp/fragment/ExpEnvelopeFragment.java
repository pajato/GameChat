/*
 * Copyright (C) 2016 Pajato Technologies LLC.
 *
 * This file is part of Pajato GameChat.

 * GameChat is free software: you can redistribute it and/or modify it under the terms of the GNU
 * General Public License as published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.

 * GameChat is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without
 * even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * General Public License for more details.

 * You should have received a copy of the GNU General Public License along with GameChat.  If not,
 * see http://www.gnu.org/licenses
 */

package com.pajato.android.gamechat.exp.fragment;

import android.os.Bundle;
import android.support.v4.view.ViewPager;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;

import com.pajato.android.gamechat.R;
import com.pajato.android.gamechat.common.DispatchManager;
import com.pajato.android.gamechat.common.FabManager;
import com.pajato.android.gamechat.common.FragmentType;
import com.pajato.android.gamechat.common.InvitationManager;
import com.pajato.android.gamechat.common.adapter.MenuEntry;
import com.pajato.android.gamechat.event.AuthenticationChangeHandled;
import com.pajato.android.gamechat.event.ClickEvent;
import com.pajato.android.gamechat.event.TagClickEvent;
import com.pajato.android.gamechat.exp.BaseExperienceFragment;
import com.pajato.android.gamechat.main.MainActivity;
import com.pajato.android.gamechat.main.PaneManager;

import org.greenrobot.eventbus.Subscribe;

import java.util.ArrayList;
import java.util.List;

import static com.pajato.android.gamechat.common.DispatchManager.DispatcherKind.exp;
import static com.pajato.android.gamechat.common.FragmentType.checkers;
import static com.pajato.android.gamechat.common.FragmentType.chess;
import static com.pajato.android.gamechat.common.FragmentType.tictactoe;

/**
 * A Fragment that contains and controls the current experience shown to the User.
 *
 * @author Bryan Scott
 * @author Paul Reilly
 */
public class ExpEnvelopeFragment extends BaseExperienceFragment {

    // Public constants.

    /** The lookup key for the FAB game home memu. */
    public static final String GAME_HOME_FAM_KEY = "gameHomeFamKey";

    // Public instance methods.

    /** There has been a handled authentication change event.  Deal with the fragment to display. */
    @Subscribe public void onAuthenticationChange(final AuthenticationChangeHandled event) {
        // Simply start the next logical fragment.
        DispatchManager.instance.startNextFragment(this.getActivity(), exp);
    }

    /** Process a button click event with a tag value. */
    @Subscribe public void onClick(final TagClickEvent event) {
        Object payload = event.view.getTag();
        if (payload == null || !(payload instanceof MenuEntry)) return;

        // Handle invitation - extend app invitation, dismiss menu and return (there is no
        // new experience to start).
        if (((MenuEntry) payload).titleResId == R.string.InviteFriendFromExpEnv) {
            InvitationManager.instance.extendAppInvitation(getActivity(), mExperience.getGroupKey());
            FabManager.game.dismissMenu(this);
            return;
        } else if (((MenuEntry) payload).titleResId == R.string.InviteFriendFromChat ||
                ((MenuEntry)payload).titleResId == R.string.InviteFriendFromTTT) {
            // These aren't handled here so we want to return
            return;
        }

        // Process the payload assuming it is a valid fragment type index.  Abort if wrong.
        int index = ((MenuEntry) payload).fragmentTypeIndex;
        if (index < 0 || index > FragmentType.values().length) return;

        // The index represents an experience type.  Start the appropriate fragment after
        // dismissing the FAM.
        FabManager.game.dismissMenu(this);
        DispatchManager.instance.startNextFragment(getActivity(), FragmentType.values()[index]);
    }

    /** Process a given button click event looking for one on the game fab button. */
    @Subscribe public void onClick(final ClickEvent event) {
        // Grab the View ID and the floating action button and dimmer views.
        View view = event.view;
        FragmentType type = null;
        switch (view.getId()) {
            case R.id.IconTicTacToe:
                type = tictactoe;
                break;
            case R.id.IconCheckers:
                type = checkers;
                break;
            case R.id.IconChess:
                type = chess;
                break;
            case R.drawable.ic_casino_black_24dp:
                // And do it for the rooms option buttons.
                showFutureFeatureMessage(R.string.FutureSelectRooms);
                FabManager.game.dismissMenu(this);
                break;
            case R.id.gameFab:
                // If the click is on the fab, we have to handle if it's open or closed.
                FabManager.game.toggle(this);
                break;
            default:
                break;
        }

        if (type != null) DispatchManager.instance.startNextFragment(getActivity(), type);
    }

    /** Handle the options menu by inflating it. */
    @Override public void onCreateOptionsMenu(final Menu menu, final MenuInflater menuInflater) {
        // Inflate the menu; this adds items to the action bar if it is present.
        menuInflater.inflate(R.menu.game_menu, menu);
    }

    @Override
    public void onCreate(Bundle bundle) {
        super.onCreate(bundle);
        super.setLayoutId(R.layout.fragment_exp);
    }

    /** Intialize the game fragment envelope. */
    @Override public void onStart() {
        // Inflate the layout, and initialize the various managers.
        super.onStart();
        FabManager.game.setTag(this.getTag());
        FabManager.game.setMenu(GAME_HOME_FAM_KEY, getHomeMenu());
    }

    /** Handle a menu item selection. */
    @Override public boolean onOptionsItemSelected(final MenuItem item) {
        // Case on the item.
        switch (item.getItemId()) {
            case R.id.toolbar_chat_icon:
                // If the toolbar chat icon is clicked, on smartphone devices we can change panes.
                ViewPager viewPager = (ViewPager) getActivity().findViewById(R.id.viewpager);
                if (viewPager != null) viewPager.setCurrentItem(PaneManager.CHAT_INDEX);
                break;
            default:
                return super.onOptionsItemSelected(item);
        }

        return true;
    }

    /** Dispatch to a more suitable fragment. */
    @Override public void onResume() {
        // The experience manager will load a fragment to view into this envelope fragment.
        super.onResume();
        DispatchManager.instance.startNextFragment(getActivity(), exp);
    }

    // Private instance methods.

    /** Return the home FAM used in the top level show games and show no games fragments. */
    private List<MenuEntry> getHomeMenu() {
        final List<MenuEntry> menu = new ArrayList<>();
        menu.add(getEntry(R.string.PlayTicTacToe, R.mipmap.ic_tictactoe_red, tictactoe));
        menu.add(getEntry(R.string.PlayCheckers, R.mipmap.ic_checkers, checkers));
        menu.add(getEntry(R.string.PlayChess, R.mipmap.ic_chess, chess));
        menu.add(getNoTintEntry(R.string.InviteFriendFromExpEnv, R.drawable.ic_email_black_24dp));
        return menu;
    }
}
