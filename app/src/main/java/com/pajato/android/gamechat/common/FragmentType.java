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

import com.pajato.android.gamechat.R;
import com.pajato.android.gamechat.chat.fragment.ChatEnvelopeFragment;
import com.pajato.android.gamechat.chat.fragment.ChatShowOfflineFragment;
import com.pajato.android.gamechat.chat.fragment.ChatShowRoomsFragment;
import com.pajato.android.gamechat.chat.fragment.ChatShowSignedOutFragment;
import com.pajato.android.gamechat.chat.fragment.CreateGroupFragment;
import com.pajato.android.gamechat.chat.fragment.CreateRoomFragment;
import com.pajato.android.gamechat.chat.fragment.JoinRoomsFragment;
import com.pajato.android.gamechat.chat.fragment.ChatShowGroupsFragment;
import com.pajato.android.gamechat.chat.fragment.ShowMessagesFragment;
import com.pajato.android.gamechat.chat.fragment.ShowNoJoinedRoomsFragment;
import com.pajato.android.gamechat.chat.fragment.ShowNoMessagesFragment;
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

/**
 * Defines the fragments that can be shown in the chat or experience panes.
 *
 * @author Paul Michael Reilly
 */
public enum FragmentType {
    chatEnvelope (chat, ChatEnvelopeFragment.class),
    chatGroupList (chat, ChatShowGroupsFragment.class, R.id.chatFragmentContainer),
    chatOffline (chat, ChatShowOfflineFragment.class, R.id.chatFragmentContainer),
    chatRoomList (chat, ChatShowRoomsFragment.class, R.id.chatFragmentContainer),
    chatSignedOut (chat, ChatShowSignedOutFragment.class, R.id.chatFragmentContainer),
    createGroup (chat, CreateGroupFragment.class, R.id.chatFragmentContainer),
    createRoom (chat, CreateRoomFragment.class, R.id.chatFragmentContainer),
    expEnvelope (exp, ExpEnvelopeFragment.class),
    expGroupList (exp, ExpShowGroupsFragment.class, R.id.expFragmentContainer),
    expOffline (exp, ExpShowOfflineFragment.class, R.id.expFragmentContainer),
    expRoomList (exp, ExpShowRoomsFragment.class, R.id.expFragmentContainer),
    expSignedOut (exp, ExpShowSignedOutFragment.class, R.id.expFragmentContainer),
    experienceList (exp, ShowExperiencesFragment.class, R.id.expFragmentContainer),
    joinRoom (chat, JoinRoomsFragment.class, R.id.chatFragmentContainer),
    messageList (chat, ShowMessagesFragment.class, R.id.chatFragmentContainer),
    noExperiences (exp, ShowNoExperiencesFragment.class, R.id.expFragmentContainer),
    noMessages (chat, ShowNoMessagesFragment.class, R.id.chatFragmentContainer),
    showNoJoinedRooms (chat, ShowNoJoinedRoomsFragment.class, R.id.chatFragmentContainer),
    tictactoeList (exp, ExpShowTypeListFragment.class, R.id.expFragmentContainer),
    tictactoe (exp, TTTFragment.class, R.id.expFragmentContainer, ExpType.ttt, tictactoeList),
    checkersList (exp, ExpShowTypeListFragment.class, R.id.expFragmentContainer),
    checkers (exp, CheckersFragment.class, R.id.expFragmentContainer, ExpType.checkers, checkersList),
    chessList(exp, ExpShowTypeListFragment.class, R.id.expFragmentContainer),
    chess (exp, ChessFragment.class, R.id.expFragmentContainer, ExpType.chess, chessList);

    // Public instance variables.

    /** The fragment kind, one of chat or exp. */
    public DispatchManager.DispatcherKind kind;

    /** The envelope container id used to replace the current or chain to another fragment. */
    public int envelopeResId;

    /** The fragment base class for the type. */
    public  Class<? extends BaseFragment> fragmentClass;

    /** The experience type for this value. */
    public ExpType expType;

    /** The fragment type that will be used to show a homogeneous experience collection. */
    public FragmentType listType;

    // Public constructors.

    /** Build an instance with only a given fragment kind and class. */
    FragmentType(final DispatcherKind kind, final Class<? extends BaseFragment> fragmentClass) {
        this.kind = kind;
        this.fragmentClass = fragmentClass;
    }

    /** Build an instance with a given class and an envelope container id resource. */
    FragmentType(final DispatcherKind kind, final Class<? extends BaseFragment> fragmentClass,
                 final int envelopeResId) {
        this(kind, fragmentClass);
        this.envelopeResId = envelopeResId;
    }

    /**
     * Build an instance that supports a list of experiences of a single type.
     *
     * @param kind The dispatcher kind, one of chat or exp.
     * @param fragmentClass A given fragment class,
     * @param envelopeResId The resource id for the envelope fragment.
     * @param expType The Firebase model type information.
     * @param listType The fragment type that will show a list of given model type experiences.
     */
    FragmentType(final DispatcherKind kind, final Class<? extends BaseFragment> fragmentClass,
                 final int envelopeResId, final ExpType expType, final FragmentType listType) {
        this(kind, fragmentClass, envelopeResId);
        this.expType = expType;
        this.listType = listType;
    }
}
