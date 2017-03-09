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
import com.pajato.android.gamechat.chat.fragment.ChatShowMembersFragment;
import com.pajato.android.gamechat.chat.fragment.ChatShowOfflineFragment;
import com.pajato.android.gamechat.chat.fragment.ChatShowRoomsFragment;
import com.pajato.android.gamechat.chat.fragment.ChatShowSignedOutFragment;
import com.pajato.android.gamechat.chat.fragment.CreateGroupFragment;
import com.pajato.android.gamechat.chat.fragment.CreateProtectedUsersFragment;
import com.pajato.android.gamechat.chat.fragment.CreateRoomFragment;
import com.pajato.android.gamechat.chat.fragment.JoinRoomsFragment;
import com.pajato.android.gamechat.chat.fragment.ManageProtectedUsersFragment;
import com.pajato.android.gamechat.chat.fragment.SelectChatInviteFragment;
import com.pajato.android.gamechat.chat.fragment.SelectGroupsFragment;
import com.pajato.android.gamechat.chat.fragment.ShowMessagesFragment;
import com.pajato.android.gamechat.chat.fragment.ShowNoJoinedRoomsFragment;
import com.pajato.android.gamechat.chat.fragment.ShowNoMessagesFragment;
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
import com.pajato.android.gamechat.exp.fragment.ShowExperiencesFragment;
import com.pajato.android.gamechat.exp.fragment.ShowNoExperiencesFragment;
import com.pajato.android.gamechat.exp.fragment.TTTFragment;

import static com.pajato.android.gamechat.common.FragmentKind.chat;
import static com.pajato.android.gamechat.common.FragmentKind.exp;
import static com.pajato.android.gamechat.common.ToolbarManager.ToolbarType.chatMain;
import static com.pajato.android.gamechat.common.ToolbarManager.ToolbarType.expMain;
import static com.pajato.android.gamechat.common.ToolbarManager.ToolbarType.none;
import static com.pajato.android.gamechat.common.ToolbarManager.ToolbarType.standardBlack;
import static com.pajato.android.gamechat.common.ToolbarManager.ToolbarType.standardWhite;
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
    chatGroupList (ChatShowGroupsFragment.class, chatMain, R.layout.chat_list),
    chatOffline (ChatShowOfflineFragment.class, chatMain, R.layout.chat_offline),
    chatRoomList (ChatShowRoomsFragment.class, standardWhite, R.layout.chat_list),
    chatSignedOut (ChatShowSignedOutFragment.class, chatMain, R.layout.chat_signed_out),
    checkers (CheckersFragment.class, standardWhite, R.layout.exp_checkers, checkersET),
    chess (ChessFragment.class, standardWhite, R.layout.exp_checkers, chessET),
    createChatGroup(CreateGroupFragment.class, standardBlack, R.layout.chat_create),
    createExpGroup(CreateGroupFragment.class, standardBlack, R.layout.chat_create),
    createProtectedUser(CreateProtectedUsersFragment.class, standardBlack,
            R.layout.chat_create_protected_user),
    createRoom (CreateRoomFragment.class, standardBlack, R.layout.chat_create),
    expEnvelope (ExpEnvelopeFragment.class, none, R.layout.exp_envelope),
    expGroupList (ExpShowGroupsFragment.class, expMain, R.layout.exp_list),
    expOffline (ExpShowOfflineFragment.class, expMain, R.layout.exp_offline),
    expRoomList (ExpShowRoomsFragment.class, standardWhite, R.layout.exp_list),
    expSignedOut (ExpShowSignedOutFragment.class, expMain, R.layout.exp_signed_out),
    experienceList (ShowExperiencesFragment.class, standardWhite, R.layout.exp_list),
    groupMembersList(ChatShowMembersFragment.class, standardBlack, R.layout.chat_members),
    groupsForProtectedUser(SelectGroupsFragment.class, standardBlack, R.layout.chat_select_groups),
    joinRoom (JoinRoomsFragment.class, standardBlack, R.layout.chat_join_rooms),
    protectedUsers (ManageProtectedUsersFragment.class, standardBlack,
            R.layout.chat_protected_users),
    messageList (ShowMessagesFragment.class, standardWhite, R.layout.chat_messages),
    noExperiences (ShowNoExperiencesFragment.class, expMain, R.layout.exp_none),
    noMessages (ShowNoMessagesFragment.class, chatMain, R.layout.chat_no_messages),
    roomMembersList(ChatShowMembersFragment.class, standardBlack, R.layout.chat_members),
    selectChatGroupsRooms(SelectChatInviteFragment.class, standardBlack,
            R.layout.select_for_invite),
    selectExpGroupsRooms (SelectExpInviteFragment.class, standardBlack, R.layout.select_for_invite),
    selectRoom (SelectRoomFragment.class, standardBlack, R.layout.exp_select_user),
    showNoJoinedRooms (ShowNoJoinedRoomsFragment.class, standardWhite, R.layout.chat_no_joined_rooms),
    tictactoe (TTTFragment.class, standardWhite, R.layout.exp_ttt, tttET);

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
    public FragmentKind getKind() {
        switch(this) {
            case chatEnvelope:
            case chatGroupList:
            case chatOffline:
            case chatRoomList:
            case chatSignedOut:
            case createChatGroup:
            case createProtectedUser:
            case createRoom:
            case groupMembersList:
            case groupsForProtectedUser:
            case joinRoom:
            case messageList:
            case protectedUsers:
            case roomMembersList:
            case selectChatGroupsRooms:
            case showNoJoinedRooms:
                return chat;
            case createExpGroup:
            default:
                return exp;
        }
    }
}
