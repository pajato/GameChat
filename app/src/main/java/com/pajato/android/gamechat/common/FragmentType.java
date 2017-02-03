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
import com.pajato.android.gamechat.chat.fragment.ChatShowGroupsFragment;
import com.pajato.android.gamechat.chat.fragment.ChatShowOfflineFragment;
import com.pajato.android.gamechat.chat.fragment.ChatShowRoomsFragment;
import com.pajato.android.gamechat.chat.fragment.ChatShowSignedOutFragment;
import com.pajato.android.gamechat.chat.fragment.CreateGroupFragment;
import com.pajato.android.gamechat.chat.fragment.CreateRoomFragment;
import com.pajato.android.gamechat.chat.fragment.JoinRoomsFragment;
import com.pajato.android.gamechat.chat.fragment.SelectChatInviteFragment;
import com.pajato.android.gamechat.chat.fragment.ShowMessagesFragment;
import com.pajato.android.gamechat.chat.fragment.ShowNoJoinedRoomsFragment;
import com.pajato.android.gamechat.chat.fragment.ShowNoMessagesFragment;
import com.pajato.android.gamechat.common.DispatchManager.DispatcherKind;
import com.pajato.android.gamechat.common.ToolbarManager.ToolbarType;
import com.pajato.android.gamechat.exp.ExpType;
import com.pajato.android.gamechat.exp.fragment.CheckersFragment;
import com.pajato.android.gamechat.exp.fragment.ChessFragment;
import com.pajato.android.gamechat.exp.fragment.ExpEnvelopeFragment;
import com.pajato.android.gamechat.exp.fragment.ExpShowGroupsFragment;
import com.pajato.android.gamechat.exp.fragment.ExpShowOfflineFragment;
import com.pajato.android.gamechat.exp.fragment.ExpShowRoomsFragment;
import com.pajato.android.gamechat.exp.fragment.ExpShowSignedOutFragment;
import com.pajato.android.gamechat.exp.fragment.SelectExpInviteFragment;
import com.pajato.android.gamechat.exp.fragment.SelectRoomFragment;
import com.pajato.android.gamechat.exp.fragment.SelectUserFragment;
import com.pajato.android.gamechat.exp.fragment.ShowExperiencesFragment;
import com.pajato.android.gamechat.exp.fragment.ShowNoExperiencesFragment;
import com.pajato.android.gamechat.exp.fragment.TTTFragment;

import static com.pajato.android.gamechat.common.DispatchManager.DispatcherKind.chat;
import static com.pajato.android.gamechat.common.DispatchManager.DispatcherKind.exp;
import static com.pajato.android.gamechat.common.ToolbarManager.ToolbarType.chatChain;
import static com.pajato.android.gamechat.common.ToolbarManager.ToolbarType.chatGroup;
import static com.pajato.android.gamechat.common.ToolbarManager.ToolbarType.chatMain;
import static com.pajato.android.gamechat.common.ToolbarManager.ToolbarType.createGroupTT;
import static com.pajato.android.gamechat.common.ToolbarManager.ToolbarType.createRoomTT;
import static com.pajato.android.gamechat.common.ToolbarManager.ToolbarType.expChain;
import static com.pajato.android.gamechat.common.ToolbarManager.ToolbarType.expMain;
import static com.pajato.android.gamechat.common.ToolbarManager.ToolbarType.expMoveTT;
import static com.pajato.android.gamechat.common.ToolbarManager.ToolbarType.joinRoomTT;
import static com.pajato.android.gamechat.common.ToolbarManager.ToolbarType.none;
import static com.pajato.android.gamechat.common.ToolbarManager.ToolbarType.selectInviteTT;
import static com.pajato.android.gamechat.exp.ExpType.checkersET;
import static com.pajato.android.gamechat.exp.ExpType.chessET;
import static com.pajato.android.gamechat.exp.ExpType.tttET;

/**
 * Defines the fragments that can be shown in the chat or experience panes.
 *
 * @author Paul Michael Reilly
 */
public enum FragmentType {
    chatEnvelope (ChatEnvelopeFragment.class, none, R.layout.chat_envelope),
    chatGroupList (ChatShowGroupsFragment.class, chatGroup, R.layout.chat_list),
    chatOffline (ChatShowOfflineFragment.class, chatMain, R.layout.chat_offline),
    chatRoomList (ChatShowRoomsFragment.class, chatChain, R.layout.chat_list),
    chatSignedOut (ChatShowSignedOutFragment.class, chatMain, R.layout.chat_signed_out),
    checkers (CheckersFragment.class, expChain, R.layout.exp_checkers, checkersET),
    chess (ChessFragment.class, expChain, R.layout.exp_checkers, chessET),
    createGroup (CreateGroupFragment.class, createGroupTT, R.layout.chat_create),
    createRoom (CreateRoomFragment.class, createRoomTT, R.layout.chat_create),
    expEnvelope (ExpEnvelopeFragment.class, none, R.layout.exp_envelope),
    expGroupList (ExpShowGroupsFragment.class, expMain, R.layout.exp_none),
    expOffline (ExpShowOfflineFragment.class, expMain, R.layout.exp_offline),
    expRoomList (ExpShowRoomsFragment.class, expChain, R.layout.exp_none),
    expSignedOut (ExpShowSignedOutFragment.class, expMain, R.layout.exp_signed_out),
    experienceList (ShowExperiencesFragment.class, expMain, R.layout.exp_none),
    joinRoom (JoinRoomsFragment.class, joinRoomTT, R.layout.chat_join_rooms),
    messageList (ShowMessagesFragment.class, chatChain, R.layout.chat_messages),
    noExperiences (ShowNoExperiencesFragment.class, chatMain, R.layout.exp_none),
    noMessages (ShowNoMessagesFragment.class, chatMain, R.layout.chat_no_messages),
    selectChatGroupsRooms(SelectChatInviteFragment.class, selectInviteTT, R.layout.select_for_invite),
    selectExpGroupsRooms (SelectExpInviteFragment.class, selectInviteTT, R.layout.select_for_invite),
    selectRoom (SelectRoomFragment.class, expMoveTT, R.layout.exp_select_user),
    selectUser (SelectUserFragment.class, expMoveTT, R.layout.exp_select_user),
    showNoJoinedRooms (ShowNoJoinedRoomsFragment.class, chatChain, R.layout.chat_no_joined_rooms),
    tictactoe (TTTFragment.class, expChain, R.layout.exp_ttt, tttET);

    // Public instance variables.

    /** The experience type for this value. */
    public ExpType expType;

    /** The fragment base class for the type. */
    public  Class<? extends BaseFragment> fragmentClass;

    /** The fragment layout resource id. */
    public int layoutResId;

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
     */
    FragmentType(@NonNull final Class<? extends BaseFragment> fragmentClass,
                 final ToolbarType toolbarType, final int layoutResId, final ExpType expType) {
        this(fragmentClass, toolbarType, layoutResId);
        this.expType = expType;
    }

    // Public instance methods.

    /** Return the fragment envelope resource id for given type. */
    public int getEnvelopeId() {
        if (getKind() == chat)
            return R.id.chatFragmentContainer;
        else
            return R.id.expFragmentContainer;
    }

    /** Return the dispatch kind for this fragment type. */
    public DispatcherKind getKind() {
        switch(this) {
            case chatEnvelope:
            case chatGroupList:
            case chatOffline:
            case chatRoomList:
            case chatSignedOut:
            case createGroup:
            case createRoom:
            case joinRoom:
            case messageList:
            case selectChatGroupsRooms:
            case showNoJoinedRooms:
                return chat;
            case selectExpGroupsRooms:
                return exp;
            default:
                return exp;
        }
    }
}
