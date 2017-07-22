package com.pajato.android.gamechat.chat.fragment;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.View;
import android.widget.TextView;

import com.pajato.android.gamechat.R;
import com.pajato.android.gamechat.chat.BaseChatFragment;
import com.pajato.android.gamechat.common.Dispatcher;
import com.pajato.android.gamechat.common.FabManager;
import com.pajato.android.gamechat.common.ToolbarManager;
import com.pajato.android.gamechat.common.adapter.ListAdapter;
import com.pajato.android.gamechat.common.adapter.ListItem;
import com.pajato.android.gamechat.credentials.Credentials;
import com.pajato.android.gamechat.database.AccountManager;
import com.pajato.android.gamechat.database.GroupManager;
import com.pajato.android.gamechat.database.ProtectedUserManager;
import com.pajato.android.gamechat.event.ClickEvent;

import org.greenrobot.eventbus.Subscribe;

import java.util.ArrayList;
import java.util.List;

import static com.pajato.android.gamechat.common.ToolbarManager.MenuItemType.helpAndFeedback;
import static com.pajato.android.gamechat.common.ToolbarManager.MenuItemType.settings;

/**
 * Provide a fragment to select groups for creation of protected users.
 */

public class SelectGroupsFragment extends BaseChatFragment {

    // Private class constants.

    /**
     * The logcat tag.
     */
    private static final String TAG = SelectGroupsFragment.class.getSimpleName();

    // Public instance methods

    /** Return null or a list to be displayed by the list adapter */
    public List<ListItem> getList() {
        return GroupManager.instance.getGroupsOnlyListItemData();
    }

    /** Get the toolbar subTitle, or null if none is used */
    public String getToolbarSubtitle() {
        return null;
    }

    /** Get the toolbar title */
    public String getToolbarTitle() {
        return getString(R.string.AccessToGroupsTitle);
    }

    /**
     * Handle click events.
     */
    @Subscribe
    public void onClick(final ClickEvent event) {
        if (event == null || event.view == null)
            return;
        switch (event.view.getId()) {
            case R.id.button_finish:
                Credentials credentials = ProtectedUserManager.instance.getEMailCredentials();
                if (credentials == null) {
                    Log.e(TAG, "Cannot create protected user: e-mail credentials are not set");
                    break;
                }
                AccountManager.instance.createProtectedAccount(credentials.email, credentials.name,
                        credentials.url, credentials.accountIsKnown, getGroupSelections());
                break;
            case R.id.selector:
                // Checkbox click, just update the adapter
                Object payload = event.view.getTag();
                if (payload == null || !(payload instanceof ListItem))
                    break;
                ListItem clickedItem = (ListItem) payload;
                clickedItem.selected = !clickedItem.selected;
                RecyclerView recyclerView = (RecyclerView) mLayout.findViewById(R.id.ItemList);
                ListAdapter adapter = (ListAdapter) recyclerView.getAdapter();
                List <ListItem> adapterList = adapter.getItems();
                boolean hasSelection = false;
                for (ListItem adapterItem : adapterList) {
                    if (adapterItem.selected)
                        hasSelection = true;
                    if (adapterItem.groupKey.equals(clickedItem.groupKey))
                        adapterItem.selected = clickedItem.selected;
                }
                adapter.notifyDataSetChanged();
                TextView finish = (TextView) getActivity().findViewById(R.id.button_finish);
                finish.setEnabled(hasSelection);
                break;
            default:
                processClickEvent(event.view, this.type);
                break;
        }
    }

    /** Ensure that the FAB is not visible. Disable the 'finish' button. */
    @Override public void onResume() {
        super.onResume();
        FabManager.chat.setVisibility(this, View.INVISIBLE);
        TextView finish = (TextView) getActivity().findViewById(R.id.button_finish);
        AccountManager.instance.protectedUserGroupKeys.clear();
        finish.setEnabled(false);
    }

    /** Setup the fragment configuration using the specified dispatcher. */
    public void onSetup(Context context, Dispatcher dispatcher) {
        mDispatcher = dispatcher;
    }

    /** Set up toolbar and FAM */
    @Override public void onStart() {
        super.onStart();
        ToolbarManager.instance.init(this, helpAndFeedback, settings);
    }

    // Private instance methods.

    /** Get a list of group push keys that have been selected */
    private List<String> getGroupSelections() {
        List<String> selections = new ArrayList<>();
        RecyclerView view = (RecyclerView) mLayout.findViewById(R.id.ItemList);
        ListAdapter adapter = (ListAdapter) view.getAdapter();
        // Loop through adapter and find selected groups
        for (ListItem item : adapter.getItems())
            if (item.selected)
                selections.add(item.groupKey);
        return selections;
    }

}