/*
 * Copyright (C) 2016 Pajato Technologies, Inc.
 *
 * This file is part of Pajato GameChat.

 * GameChat is free software: you can redistribute it and/or modify it under the terms of the GNU
 * General Public License as published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 *
 * GameChat is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without
 * even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * General Public License for more details.

 * You should have received a copy of the GNU General Public License along with GameChat.  If not,
 * see <http://www.gnu.org/licenses/>.
 */

package com.pajato.android.gamechat.common;

import android.support.annotation.NonNull;

import com.pajato.android.gamechat.R;
import com.pajato.android.gamechat.chat.fragment.ChatEnvelopeFragment;
import com.pajato.android.gamechat.chat.fragment.ChatShowOfflineFragment;
import com.pajato.android.gamechat.chat.fragment.ChatShowRoomsFragment;
import com.pajato.android.gamechat.chat.fragment.ChatShowSignedOutFragment;
import com.pajato.android.gamechat.chat.fragment.CreateGroupFragment;
import com.pajato.android.gamechat.chat.fragment.CreateRoomFragment;
import com.pajato.android.gamechat.chat.fragment.JoinRoomsFragment;
import com.pajato.android.gamechat.chat.fragment.ChatShowGroupsFragment;
import com.pajato.android.gamechat.chat.fragment.SelectForInviteFragment;
import com.pajato.android.gamechat.chat.fragment.ShowMessagesFragment;
import com.pajato.android.gamechat.chat.fragment.ShowNoJoinedRoomsFragment;
import com.pajato.android.gamechat.chat.fragment.ShowNoMessagesFragment;
import com.pajato.android.gamechat.common.ToolbarManager.ToolbarType;
import com.pajato.android.gamechat.exp.ExpType;
import com.pajato.android.gamechat.exp.fragment.CheckersFragment;
import com.pajato.android.gamechat.exp.fragment.ChessFragment;
import com.pajato.android.gamechat.exp.fragment.ExpShowGroupsFragment;
import com.pajato.android.gamechat.exp.fragment.ExpShowOfflineFragment;
import com.pajato.android.gamechat.exp.fragment.ExpShowRoomsFragment;
import com.pajato.android.gamechat.exp.fragment.ExpShowSignedOutFragment;
import com.pajato.android.gamechat.exp.fragment.ExpShowTypeListFragment;
import com.pajato.android.gamechat.exp.fragment.ExpEnvelopeFragment;
import com.pajato.android.gamechat.exp.fragment.ShowExperiencesFragment;
import com.pajato.android.gamechat.exp.fragment.ShowNoExperiencesFragment;
import com.pajato.android.gamechat.exp.fragment.TTTFragment;
import com.pajato.android.gamechat.common.DispatchManager.DispatcherKind;

import static com.pajato.android.gamechat.common.DispatchManager.DispatcherKind.chat;
import static com.pajato.android.gamechat.common.DispatchManager.DispatcherKind.exp;
import static com.pajato.android.gamechat.common.ToolbarManager.ToolbarType.chatChain;
import static com.pajato.android.gamechat.common.ToolbarManager.ToolbarType.chatMain;
import static com.pajato.android.gamechat.common.ToolbarManager.ToolbarType.createGroupTT;
import static com.pajato.android.gamechat.common.ToolbarManager.ToolbarType.createRoomTT;
import static com.pajato.android.gamechat.common.ToolbarManager.ToolbarType.expChain;
import static com.pajato.android.gamechat.common.ToolbarManager.ToolbarType.expMain;
import static com.pajato.android.gamechat.common.ToolbarManager.ToolbarType.joinRoomTT;
import static com.pajato.android.gamechat.exp.ExpType.ttt;

/**
 * Defines the fragments that can be shown in the chat or experience panes.
 *
 * @author Paul Michael Reilly
 */
public enum FragmentType {
    chatEnvelope (ChatEnvelopeFragment.class, null, R.layout.fragment_chat),
    chatGroupList (ChatShowGroupsFragment.class, chatMain, R.layout.fragment_chat_list),
    chatOffline (ChatShowOfflineFragment.class, chatMain, R.layout.fragment_chat_offline),
    chatRoomList (ChatShowRoomsFragment.class, chatChain, R.layout.fragment_chat_list),
    chatSignedOut (ChatShowSignedOutFragment.class, chatMain, R.layout.fragment_chat_signed_out),
    createGroup (CreateGroupFragment.class, createGroupTT, R.layout.fragment_chat_create),
    createRoom (CreateRoomFragment.class, createRoomTT, R.layout.fragment_chat_create),
    expEnvelope (ExpEnvelopeFragment.class, null, R.layout.fragment_exp),
    expGroupList (ExpShowGroupsFragment.class, expMain, R.layout.fragment_game_no_games),
    expOffline (ExpShowOfflineFragment.class, expMain, R.layout.fragment_game_offline),
    expRoomList (ExpShowRoomsFragment.class, expChain, R.layout.fragment_game_no_games),
    expSignedOut (ExpShowSignedOutFragment.class, expMain, R.layout.fragment_exp_signed_out),
    experienceList (ShowExperiencesFragment.class, expMain, R.layout.fragment_game_no_games),
    joinRoom (JoinRoomsFragment.class, joinRoomTT, R.layout.fragment_chat_join_rooms),
    messageList (ShowMessagesFragment.class, chatChain, R.layout.fragment_chat_messages),
    noExperiences (ShowNoExperiencesFragment.class, chatMain, R.layout.fragment_game_no_games),
    noMessages (ShowNoMessagesFragment.class, chatMain, R.layout.fragment_chat_no_messages),
    selectGroupsAndRooms (SelectForInviteFragment.class, chatChain,
            R.layout.fragment_select_for_invite),
    showNoJoinedRooms (ShowNoJoinedRoomsFragment.class, chatChain,
                       R.layout.fragment_chat_no_joined_rooms),
    tictactoeList (ExpShowTypeListFragment.class, expMain, R.layout.fragment_chat_no_joined_rooms),
    tictactoe (TTTFragment.class, expChain, R.layout.fragment_game_ttt, ttt, tictactoeList),
    checkersList (ExpShowTypeListFragment.class, expMain, R.layout.fragment_game_no_games),
    checkers (CheckersFragment.class, expChain, R.layout.fragment_checkers, ExpType.checkers,
              checkersList),
    chessList (ExpShowTypeListFragment.class, expMain, R.layout.fragment_game_no_games),
    chess (ChessFragment.class, expChain, R.layout.fragment_checkers, ExpType.chess, chessList);

    // Public instance variables.

    /** The experience type for this value. */
    public ExpType expType;

    /** The fragment base class for the type. */
    public  Class<? extends BaseFragment> fragmentClass;

    /** The fragment layout resource id. */
    public int layoutResId;

    /** The fragment type that will be used to show a homogeneous experience collection. */
    public FragmentType listType;

    /** The fragment toolbar type. */
    public ToolbarType toolbarType;

    // Public constructors.

    /** Build an instance with a given fragment class, toolbar type and layout resource id. */
    FragmentType(@NonNull final Class<? extends BaseFragment> fragmentClass,
                 final ToolbarType toolbarType, final int layoutResId) {
        this.fragmentClass = fragmentClass;
        this.toolbarType = toolbarType;
        this.layoutResId = layoutResId;
    }

    /**
     * Build an instance that supports a list of experiences of a single type.
     *
     * @param fragmentClass A given fragment class.
     * @param toolbarType A toolbar type, possibly null.
     * @param layoutResId The layout resource id.
     * @param expType The Firebase model type information.
     * @param listType The fragment type that will show a list of given model type experiences.
     */
    FragmentType(@NonNull final Class<? extends BaseFragment> fragmentClass,
                 final ToolbarType toolbarType, final int layoutResId, final ExpType expType,
                 final FragmentType listType) {
        this(fragmentClass, toolbarType, layoutResId);
        this.expType = expType;
        this.listType = listType;
    }

    // Public instance methods.

    /** Return the fragment envelope resource id for given type. */
    public int getEnvelopeId(final FragmentType type) {
        if (getKind(type) == chat)
            return R.id.chatFragmentContainer;
        else
            return R.id.expFragmentContainer;
    }

    /** Return the dispatch kind for this fragment type. */
    public DispatcherKind getKind(final FragmentType type) {
        switch(type) {
            case chatEnvelope:
            case chatGroupList:
            case chatOffline:
            case chatRoomList:
            case chatSignedOut:
            case createGroup:
            case createRoom:
            case joinRoom:
            case messageList:
            case selectGroupsAndRooms:
            case showNoJoinedRooms:
                return chat;
            default:
                return exp;
        }
    }
}
