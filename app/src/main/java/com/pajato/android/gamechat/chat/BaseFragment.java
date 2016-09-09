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
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;
import com.pajato.android.gamechat.R;
import com.pajato.android.gamechat.chat.adapter.ChatListAdapter;
import com.pajato.android.gamechat.chat.adapter.ChatListItem;
import com.pajato.android.gamechat.event.EventBusManager;
import com.pajato.android.gamechat.event.MessageListChangeEvent;

import org.greenrobot.eventbus.Subscribe;

import java.util.List;
import java.util.Locale;

import static android.support.v7.widget.LinearLayoutCompat.VERTICAL;
import static com.pajato.android.gamechat.chat.ChatListManager.ChatListType.message;

/**
 * Provide a base class to support fragment lifecycle debugging.  All lifecycle events except for
 * onViewCreate() are handled by providing logcat tracing information.  The fragment manager is
 * displayed in order to help catch an elusive connected check failure.
 *
 * @author Paul Michael Reilly
 */
public abstract class BaseFragment extends Fragment {

    // Private class constants.

    /** The logcat tag. */
    private static final String TAG = BaseFragment.class.getSimpleName();

    /** The lifecycle event format string with no bundle. */
    private static final String FORMAT_NO_BUNDLE =
        "Fragment: %s; Fragment Manager: %s; Fragment Type: %s; Lifecycle event: %s.";

    /** The lifecycle event format string with a bundle provided. */
    private static final String FORMAT_WITH_BUNDLE =
        "Fragment: %s; Fragment Manager: %s; Fragment Type: %s; Lifecycle event: %s; Bundle: %s.";

    // Protected instance variables.

    /** Show an ad at the top of the view. */
    protected AdView mAdView;

    /** The item information passed from the parent fragment. */
    protected ChatListItem mItem;

    /** The list type for this fragment. */
    protected ChatListManager.ChatListType mItemListType;

    /** The list type of the last fragment shown in the chat pane. */
    protected ChatListManager.ChatListType mLastChatListTypeShown;

    /** The persisted layout view for this fragment. */
    protected View mLayout;

    // Public constructors.

    /** Provide a default, no args constructor. */
    public BaseFragment() {
        EventBusManager.instance.register(this);
    }


    // Public instance methods.

    /** Obtain a layout file from the subclass. */
    abstract public int getLayout();

    @Override public void onActivityCreated(Bundle bundle) {
        logEvent("onActivityCreated", bundle);
        super.onActivityCreated(bundle);
    }

    @Override public void onAttach(Context context) {
        logEvent("onAttach");
        super.onAttach(context);
    }

    @Override public void onCreate(Bundle bundle) {
        logEvent("onCreate", bundle);
        super.onCreate(bundle);
    }

    /** Handle the onCreateView lifecycle event. */
    @Override public View onCreateView(final LayoutInflater inflater, final ViewGroup container,
                                       final Bundle savedInstanceState) {
        // Determine if the layout exists and reuse it if so.
        if (mLayout != null) return mLayout;

        // The layout does not exist.  Create and persist it, and initialize the fragment layout.
        mLayout = inflater.inflate(getLayout(), container, false);
        onInitialize();
        logEvent("onCreateView", savedInstanceState);

        return mLayout;
    }

    /** Log the lifecycle event and kill the ads. */
    @Override public void onDestroy() {
        logEvent("onDestroy");
        if (mAdView != null) mAdView.destroy();
        super.onDestroy();
    }

    @Override public void onDestroyView() {
        logEvent("onDestroyView");
        super.onDestroyView();
    }

    @Override public void onDetach() {
        logEvent("onDetach");
        super.onDetach();
    }

    /** Initialize the fragment. */
    abstract public void onInitialize();

    /** Manage the list UI every time a message change occurs. */
    @Subscribe public void onMessageListChange(final MessageListChangeEvent event) {
        // Determine if the fragment has a view and that it has a list type.
        View layout = getView();
        if (layout == null || mItemListType == null) return;

        // It has both.  Ensure that the list view (recycler) exists.
        RecyclerView view = (RecyclerView) layout.findViewById(R.id.chatList);
        if (view == null) return;

        // The recycler view exists.  Show the chat list, either groups, messages or rooms.
        RecyclerView.Adapter adapter = view.getAdapter();
        if (adapter instanceof ChatListAdapter) {
            // Inject the list items into the recycler view making sure to scroll to the end of the
            // list when showing messages.
            ChatListAdapter listAdapter = (ChatListAdapter) adapter;
            view.setVisibility(View.GONE);
            listAdapter.clearItems();
            listAdapter.addItems(ChatListManager.instance.getList(mItemListType, mItem));
            if (mItemListType == message) view.scrollToPosition(listAdapter.getItemCount() - 1);
            view.setVisibility(View.VISIBLE);
        }
    }

    /** Log the lifecycle event, stop showing ads and turn off the app event bus. */
    @Override public void onPause() {
        logEvent("onPause");
        if (mAdView != null) mAdView.pause();
        EventBusManager.instance.unregister(this);
        super.onPause();
    }

    /** Log the lifecycle event and resume showing ads. */
    @Override public void onResume() {
        logEvent("onResume");
        if (mAdView != null) mAdView.resume();
        EventBusManager.instance.register(this);
        super.onResume();
    }

    /** Log the lifecycle event. */
    @Override public void onStart() {
        logEvent("onStart");
        super.onStart();
    }

    /** Log the lifecycle event. */
    @Override public void onStop() {
        logEvent("onStop");
        super.onStop();
    }

    /** Log the lifecycle event. */
    @Override public void onViewStateRestored(Bundle bundle) {
        logEvent("onViewStateRestored", bundle);
        super.onViewStateRestored(bundle);
    }

    /** Set the item defining this fragment (passed from the parent (spawning) fragment. */
    public void setItem(final ChatListItem item) {
        mItem = item;
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
        Log.v(TAG, String.format(Locale.US, format, this, manager, mItemListType, event));
    }

    /** Log a lifecycle event that has a bundle. */
    protected void logEvent(final String event, final Bundle bundle) {
        String manager = getFragmentManager().toString();
        String format = FORMAT_WITH_BUNDLE;
        Log.v(TAG, String.format(Locale.US, format, this, manager, mItemListType, event, bundle));
    }

    /** Deal with the options menu by hiding the back button. */
    protected void setOptionsMenu(final Menu menu, final MenuInflater inflater, final int[] visible,
                                  final int[] gone) {
        // Ensure that the menu options has been inflated and make the specified items visible and
        // gone.
        if (!menu.hasVisibleItems()) inflater.inflate(R.menu.chat_menu_base, menu);
        if (visible != null) for (int itemId : visible) setItemState(menu, itemId, true);
        if (gone != null) for (int itemId : gone) setItemState(menu, itemId, false);
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
        int duration = Toast.LENGTH_LONG;
        Toast toast = Toast.makeText(context, text, duration);
        toast.show();
    }

    // Private instance methods.

    /** Make the given menu item either visible or invisible. */
    private void setItemState(final Menu menu, final int itemId, final boolean state) {
        MenuItem item = menu.findItem(itemId);
        if (item != null) item.setVisible(state);
    }

}
