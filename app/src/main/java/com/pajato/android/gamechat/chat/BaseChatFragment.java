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

package com.pajato.android.gamechat.chat;

import android.content.Context;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.support.v4.view.ViewPager;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;
import com.pajato.android.gamechat.R;
import com.pajato.android.gamechat.account.Account;
import com.pajato.android.gamechat.account.AccountManager;
import com.pajato.android.gamechat.chat.adapter.ChatListAdapter;
import com.pajato.android.gamechat.chat.adapter.ChatListItem;
import com.pajato.android.gamechat.chat.model.Group;
import com.pajato.android.gamechat.database.DatabaseManager;
import com.pajato.android.gamechat.event.AppEventManager;
import com.pajato.android.gamechat.main.PaneManager;
import com.pajato.android.gamechat.main.ProgressManager;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import static android.support.v7.widget.LinearLayoutCompat.VERTICAL;
import static com.pajato.android.gamechat.chat.ChatListManager.ChatListType.message;
import static com.pajato.android.gamechat.chat.ChatManager.ChatFragmentType.showMessages;
import static com.pajato.android.gamechat.chat.ChatManager.ChatFragmentType.showRoomList;
import static com.pajato.android.gamechat.chat.adapter.ChatListItem.GROUP_ITEM_TYPE;
import static com.pajato.android.gamechat.chat.adapter.ChatListItem.ROOM_ITEM_TYPE;

/**
 * Provide a base class to support fragment lifecycle debugging.  All lifecycle events except for
 * onViewCreate() are handled by providing logcat tracing information.  The fragment manager is
 * displayed in order to help catch an elusive connected check failure.
 *
 * @author Paul Michael Reilly
 */
public abstract class BaseChatFragment extends Fragment {

    // Private class constants.

    /** The logcat tag. */
    private static final String TAG = BaseChatFragment.class.getSimpleName();

    /** The lifecycle event format string with no bundle. */
    private static final String FORMAT_NO_BUNDLE =
        "Event: %s; Fragment: %s; Fragment Manager: %s; Fragment List Type: %s.";

    /** The lifecycle event format string with a bundle provided. */
    private static final String FORMAT_WITH_BUNDLE =
        "Event: %s; Fragment: %s; Fragment Manager: %s; Fragment List Type: %s; Bundle: %s.";

    // Protected instance variables.

    /** Show an ad at the top of the view. */
    protected AdView mAdView;

    /** The item information passed from the parent fragment. */
    protected ChatListItem mItem;

    /** The list type for this fragment. */
    protected ChatListManager.ChatListType mItemListType;

    /** The persisted layout view for this fragment. */
    protected View mLayout;

    /** A flag used to queue adapter list updates during the onResume lifecycle event. */
    protected boolean mUpdateOnResume;

    // Public constructors.

    /** Provide a default, no args constructor. */
    public BaseChatFragment() {}

    // Public instance methods.

    /** Obtain a layout file from the subclass. */
    abstract public int getLayout();

    @Override public void onActivityCreated(Bundle bundle) {
        super.onActivityCreated(bundle);
        logEvent("onActivityCreated", bundle);
    }

    @Override public void onAttach(Context context) {
        super.onAttach(context);
        logEvent("onAttach");
        AppEventManager.instance.register(this);
    }

    @Override public void onCreate(Bundle bundle) {
        super.onCreate(bundle);
        logEvent("onCreate", bundle);
    }

    /** Handle the onCreateView lifecycle event. */
    @Override public View onCreateView(final LayoutInflater inflater, final ViewGroup container,
                                       final Bundle savedInstanceState) {
        // Determine if the layout exists and reuse it if so.
        logEvent("onCreateView", savedInstanceState);
        if (mLayout != null) return mLayout;

        // The layout does not exist.  Create and persist it, and initialize the fragment layout.
        mLayout = inflater.inflate(getLayout(), container, false);
        onInitialize();
        return mLayout;
    }

    /** Log the lifecycle event and kill the ads. */
    @Override public void onDestroy() {
        logEvent("onDestroy");
        if (mAdView != null) mAdView.destroy();
        super.onDestroy();
    }

    @Override public void onDestroyView() {
        super.onDestroyView();
        logEvent("onDestroyView");
    }

    @Override public void onDetach() {
        super.onDetach();
        logEvent("onDetach");
        AppEventManager.instance.unregister(this);
    }

    /** Initialize the fragment. */
    public void onInitialize() {
        // All chat and game fragments will use the options menu.
        setHasOptionsMenu(true);
    }

    /** Handle an options menu choice. */
    @Override public boolean onOptionsItemSelected(final MenuItem item) {
        switch (item.getItemId()) {
            case R.id.toolbar_game_icon:
                // Show the game panel.
                ViewPager viewPager = (ViewPager) getActivity().findViewById(R.id.viewpager);
                if(viewPager != null) {
                    viewPager.setCurrentItem(PaneManager.GAME_INDEX);
                }
                break;
            case R.id.search:
                // TODO: Handle a search in the groups panel by fast scrolling to chat.
                break;
            case R.id.joinDeveloperGroups:
                joinDeveloperGroups();
                break;
            default:
                return super.onOptionsItemSelected(item);
        }

        return true;
    }

    /** Log the lifecycle event, stop showing ads and turn off the app event bus. */
    @Override public void onPause() {
        super.onPause();
        logEvent("onPause");
        if (mAdView != null) mAdView.pause();
    }

    /** Log the lifecycle event and resume showing ads. */
    @Override public void onResume() {
        // Log the event, handle ads and apply any queued adapter updates.  Only one try is
        // attempted.
        super.onResume();
        logEvent("onResume");
        ProgressManager.instance.hide();
        if (mAdView != null) mAdView.resume();
        if (!mUpdateOnResume) return;
        updateAdapterList();
        mUpdateOnResume = false;
    }

    /** Log the lifecycle event. */
    @Override public void onStart() {
        super.onStart();
        logEvent("onStart");
    }

    /** Log the lifecycle event. */
    @Override public void onStop() {
        super.onStop();
        logEvent("onStop");
    }

    /** Log the lifecycle event. */
    @Override public void onViewStateRestored(Bundle bundle) {
        super.onViewStateRestored(bundle);
        logEvent("onViewStateRestored", bundle);
    }

    /** Set the item defining this fragment (passed from the parent (spawning) fragment. */
    public void setItem(final ChatListItem item) {
        mItem = item;
    }

    /** Return TRUE iff the list can be considered up to date. */
    public boolean updateAdapterList() {
        // Determine if the fragment has a view and that it has a list type.
        View layout = getView();
        if (layout == null || mItemListType == null) return false;

        // It has both.  Ensure that the list view (recycler) exists.
        RecyclerView view = (RecyclerView) layout.findViewById(R.id.chatList);
        if (view == null) return false;

        // The recycler view exists.  Show the chat list, either groups, messages or rooms.
        RecyclerView.Adapter adapter = view.getAdapter();
        if (!(adapter instanceof ChatListAdapter)) return true;

        // Inject the list items into the recycler view making sure to scroll to the end of the
        // list when showing messages.
        ChatListAdapter listAdapter = (ChatListAdapter) adapter;
        listAdapter.clearItems();
        listAdapter.addItems(ChatListManager.instance.getList(mItemListType, mItem));
        if (mItemListType == message) view.scrollToPosition(listAdapter.getItemCount() - 1);
        return true;
    }

    // Protected instance methods.

    /** Initialize the fragment's chat list. */
    protected void initList(@NonNull final View layout, final List<ChatListItem> items,
                            final boolean stackFromEnd) {
        // Initialize the recycler view.
        Context context = layout.getContext();
        LinearLayoutManager layoutManager = new LinearLayoutManager(context, VERTICAL, false);
        RecyclerView recyclerView = (RecyclerView) layout.findViewById(R.id.chatList);
        if (stackFromEnd) layoutManager.setStackFromEnd(true);
        recyclerView.setLayoutManager(layoutManager);
        recyclerView.setItemAnimator(new DefaultItemAnimator());

        // Set up the adapter on the recycler view with a set of default messages.
        ChatListAdapter adapter = new ChatListAdapter();
        adapter.addItems(items);
        recyclerView.setAdapter(adapter);
    }

    /** Initialize the ad view by building and loading an ad request. */
    protected void initAdView(@NonNull final View layout) {
        mAdView = (AdView) layout.findViewById(R.id.adView);
        if (mAdView != null) {
            AdRequest adRequest = new AdRequest.Builder().build();
            mAdView.loadAd(adRequest);
        }
    }

    /** Log a lifecycle event that has no bundle. */
    protected void logEvent(final String event) {
        String manager = getFragmentManager().toString();
        String format = FORMAT_NO_BUNDLE;
        Log.v(TAG, String.format(Locale.US, format, event, this, manager, mItemListType));
    }

    /** Log a lifecycle event that has a bundle. */
    protected void logEvent(final String event, final Bundle bundle) {
        String manager = getFragmentManager().toString();
        String format = FORMAT_WITH_BUNDLE;
        Log.v(TAG, String.format(Locale.US, format, event, this, manager, mItemListType, bundle));
    }

    /** Proces a button click that may be a chat list item click. */
    protected void processPayload(final View view) {
        // Determine if some action needs to be taken, i.e if the button click is coming
        // from a group or room item view.
        Object payload = view.getTag();
        if (!(payload instanceof ChatListItem)) return;

        // Action needs be taken.  Case on the item to determine what action.
        ChatListItem item = (ChatListItem) payload;
        switch (item.type) {
            case GROUP_ITEM_TYPE:
                // Drill into the rooms in group.
                ChatManager.instance.chainFragment(showRoomList, getActivity(), item);
                break;
            case ROOM_ITEM_TYPE:
                // Show the messages in a room.
                ChatManager.instance.chainFragment(showMessages, getActivity(), item);
                break;
            default:
                break;
        }
    }

    /** Make the given menu item either visible or invisible. */
    protected void setItemState(final Menu menu, final int itemId, final boolean state) {
        MenuItem item = menu.findItem(itemId);
        if (item != null) item.setVisible(state);
    }

    /** Set the title in the toolbar using the group name. */
    protected void setTitles(final String groupKey, final String roomKey) {
        // Ensure that the action bar exists.
        String title;
        String subtitle = null;
        ActionBar bar = ((AppCompatActivity) getActivity()).getSupportActionBar();
        if (bar != null) {
            // The action bar does exist, as expected.  Set the title and subtitle accordingly.
            if (groupKey == null && roomKey == null) {
                title = getResources().getString(R.string.app_name);
            } else if (groupKey != null && roomKey == null) {
                title = ChatListManager.instance.getGroupName(groupKey);
            } else if (groupKey == null) {
                title = ChatListManager.instance.getRoomName(roomKey);
            } else {
                title = ChatListManager.instance.getRoomName(roomKey);
                subtitle = ChatListManager.instance.getGroupName(groupKey);
            }

            // Apply the title and subtitle to the action bar.
            bar.setTitle(title);
            bar.setSubtitle(subtitle);
            return;
        }

        // The action bar does not exist!  Log the error.
        Log.e(TAG, "The action bar is not accessible in order to set the titles!");
    }

    /** Provide a way to handle volunteer solicitations for unimplemented functions. */
    protected void showFutureFeatureMessage(final int resourceId) {
        // Post a toast message.
        Context context = getContext();
        String prefix = context.getString(resourceId);
        String suffix = context.getString(R.string.FutureFeature);
        CharSequence text = String.format(Locale.getDefault(), "%s %s", prefix, suffix);
        Toast.makeText(context, text, Toast.LENGTH_LONG).show();
    }

    // Private instance methods.

    /** Development hack: poor man's invite handler to join one or more developer groups. */
    private void joinDeveloperGroups() {
        // Ensure that the supported developer groups have been joined.  This a short term hack in
        // lieu of dynamic linking support.  First ensure that the User is signed in.  Note they
        // will be prompted to sign in if they have not already done so.
        Account account = AccountManager.instance.getCurrentAccount(getContext());
        if (account == null) return;

        // Walk the list of developer groups to ensure that all are joined.
        List<String> groupList = new ArrayList<>();
        groupList.add("-KRqycW-uywIkt4Ro0iu");      // Paul Reilly Group
        groupList.add("-KS3dt8unvPfxIKnOJ49");      // GameChat Group
        groupList.add("-KS3dgEy77X0tjE6f5pY");      // Pajato Technologies LLC
        groupList.add("-KS3fObxrr04gpLoCIno");      // Pajato Support Group
        for (String groupKey : groupList) {
            // Extend an invitation to the group and est that this group has been joined.
            DatabaseManager.instance.extendGroupInvite(account, groupKey);
            if (!account.groupIdList.contains(groupKey)) {
                // Join the group now if it has been loaded.  It will be queued for joining later if
                // necessary.
                Group group = ChatListManager.instance.getGroupProfile(groupKey);
                if (group != null)
                    // The group is available.  Accept any open invitations ... and there should be
                    // at least one!
                    DatabaseManager.instance.acceptGroupInvite(account, group, groupKey);
            }
        }
    }

}
