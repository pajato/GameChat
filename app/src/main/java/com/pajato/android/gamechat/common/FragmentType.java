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
import com.pajato.android.gamechat.chat.fragment.SelectInviteFragment;
import com.pajato.android.gamechat.chat.fragment.SelectGroupsFragment;
import com.pajato.android.gamechat.chat.fragment.ShowMessagesFragment;
import com.pajato.android.gamechat.common.ToolbarManager.ToolbarType;
import com.pajato.android.gamechat.exp.ExpType;
import com.pajato.android.gamechat.exp.fragment.CheckersFragment;
import com.pajato.android.gamechat.exp.fragment.ChessFragment;
import com.pajato.android.gamechat.exp.fragment.ExpEnvelopeFragment;
import com.pajato.android.gamechat.exp.fragment.ExpShowGroupsFragment;
import com.pajato.android.gamechat.exp.fragment.ExpShowOfflineFragment;
import com.pajato.android.gamechat.exp.fragment.ExpShowRoomsFragment;
import com.pajato.android.gamechat.exp.fragment.ExpShowSignedOutFragment;
import com.pajato.android.gamechat.exp.fragment.SetupExperienceFragment;
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
    chatEnvelope (ChatEnvelopeFragment.class, none, R.layout.chat_envelope, chat),
    chatGroupList (ChatShowGroupsFragment.class, chatMain, R.layout.chat_list, chat),
    chatOffline (ChatShowOfflineFragment.class, chatMain, R.layout.chat_offline, chat),
    chatRoomList (ChatShowRoomsFragment.class, standardWhite, R.layout.chat_list, chat),
    chatSignedOut (ChatShowSignedOutFragment.class, chatMain, R.layout.chat_signed_out, chat),
    checkers (CheckersFragment.class, standardWhite, R.layout.exp_checkers, checkersET, exp),
    chess (ChessFragment.class, standardWhite, R.layout.exp_checkers, chessET, exp),
    createChatGroup(CreateGroupFragment.class, standardBlack, R.layout.chat_create, chat),
    createProtectedUser(CreateProtectedUsersFragment.class, standardBlack,
            R.layout.chat_create_protected_user, chat),
    createRoom (CreateRoomFragment.class, standardBlack, R.layout.chat_create, chat),
    expEnvelope (ExpEnvelopeFragment.class, none, R.layout.exp_envelope, exp),
    expGroupList (ExpShowGroupsFragment.class, expMain, R.layout.exp_list, exp),
    expOffline (ExpShowOfflineFragment.class, expMain, R.layout.exp_offline, exp),
    expRoomList (ExpShowRoomsFragment.class, standardWhite, R.layout.exp_list, exp),
    expSignedOut (ExpShowSignedOutFragment.class, expMain, R.layout.exp_signed_out, exp),
    experienceList (ShowExperiencesFragment.class, standardWhite, R.layout.exp_list, exp),
    groupMembersList(ChatShowMembersFragment.class, standardBlack, R.layout.chat_members, chat),
    groupsForProtectedUser(SelectGroupsFragment.class, standardBlack, R.layout.chat_select_groups,
            chat),
    joinRoom (JoinRoomsFragment.class, standardBlack, R.layout.chat_join_rooms, chat),
    protectedUsers (ManageProtectedUsersFragment.class, standardBlack,
            R.layout.chat_protected_users, chat),
    messageList (ShowMessagesFragment.class, standardWhite, R.layout.chat_messages, chat),
    noExperiences (ShowNoExperiencesFragment.class, expMain, R.layout.exp_none, exp),
    roomMembersList(ChatShowMembersFragment.class, standardBlack, R.layout.chat_members, chat),
    selectGroupsRooms(SelectInviteFragment.class, standardBlack,
            R.layout.select_for_invite, chat),
    setupExp(SetupExperienceFragment.class, standardWhite, R.layout.exp_setup, exp),
    tictactoe (TTTFragment.class, standardWhite, R.layout.exp_ttt, tttET, exp);

    // Public instance variables.

    /** The experience type for this value. */
    public ExpType expType;

    /** The fragment base class for the type. */
    public  Class<? extends BaseFragment> fragmentClass;

    /** The fragment kind (exp or chat) for this fragment type */
    public FragmentKind kind;

    /** The fragment layout resource id. */
    public int layoutResId;

    /** The fragment toolbar type. */
    public ToolbarType toolbarType;

    // Public constructors.

    /** Build an instance with a given fragment class, toolbar type and layout resource id. */
    FragmentType(@NonNull final Class<? extends BaseFragment> fragmentClass,
                 final ToolbarType toolbarType, final int layoutResId, final FragmentKind kind) {
        this.fragmentClass = fragmentClass;
        this.toolbarType = toolbarType;
        this.layoutResId = layoutResId;
        this.kind = kind;
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
                 final ToolbarType toolbarType, final int layoutResId, final ExpType expType,
                 final FragmentKind kind) {
        this(fragmentClass, toolbarType, layoutResId, kind);
        this.expType = expType;
    }

    // Public instance methods.

    /** Return the fragment envelope resource id for given type. */
    public int getEnvelopeId() {
        if (this.kind == chat)
            return R.id.chatFragmentContainer;
        else
            return R.id.expFragmentContainer;
    }

}
