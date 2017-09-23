package com.pajato.android.gamechat.exp.fragment;

import android.content.Context;
import android.graphics.drawable.GradientDrawable;
import android.support.v4.content.ContextCompat;
import android.view.View;
import android.widget.ImageView;

import com.pajato.android.gamechat.R;
import com.pajato.android.gamechat.common.Dispatcher;
import com.pajato.android.gamechat.common.FabManager;
import com.pajato.android.gamechat.common.ToolbarManager;
import com.pajato.android.gamechat.common.adapter.ListItem;
import com.pajato.android.gamechat.event.ClickEvent;
import com.pajato.android.gamechat.exp.BaseExperienceFragment;

import org.greenrobot.eventbus.Subscribe;

import java.util.Arrays;
import java.util.List;

import static android.graphics.PorterDuff.Mode.SRC_ATOP;
import static com.pajato.android.gamechat.R.color.colorAccent;
import static com.pajato.android.gamechat.R.color.colorPrimary;
import static com.pajato.android.gamechat.R.color.colorVeryLightGray;
import static com.pajato.android.gamechat.common.ToolbarManager.MenuItemType.chat;
import static com.pajato.android.gamechat.common.ToolbarManager.MenuItemType.helpAndFeedback;
import static com.pajato.android.gamechat.common.ToolbarManager.MenuItemType.invite;
import static com.pajato.android.gamechat.common.ToolbarManager.MenuItemType.settings;
import static com.pajato.android.gamechat.exp.fragment.ExpEnvelopeFragment.GAME_HOME_FAM_KEY;

/**
 *
 */
public class SetupExperienceFragment extends BaseExperienceFragment {
    private int mExpId = R.id.IconTicTacToe;

    @Override public String getToolbarSubtitle() {
        return null;
    }

    @Override public String getToolbarTitle() {
        return getString(R.string.SetupNewExp);
    }

    /** Handle a button click event by delegating the event to the base class. */
    @Subscribe public void onClick(final ClickEvent event) {
        logEvent("SetupExp Click Event: " + event.toString());

        int viewId = event.view.getId();

        switch (viewId) {
            case R.id.friendLayout:
                showFutureFeatureMessage(R.string.ChooseOpponent);
                break;
            case R.id.timerZero:
            case R.id.timerFifteen:
            case R.id.timerThirty:
            case R.id.timerFortyFive:
            case R.id.timerSixty:
                List<Integer> timers = Arrays.asList(R.id.timerZero, R.id.timerFifteen,
                        R.id.timerThirty, R.id.timerFortyFive, R.id.timerSixty);
                setBorder(timers, viewId);
                showFutureFeatureMessage(R.string.TurnTimer);
                break;
            case R.id.player1Icon:
            case R.id.player2Icon:
                List<Integer> playIcons = Arrays.asList(R.id.player1Icon, R.id.player2Icon);
                setBorder(playIcons, viewId);
                showFutureFeatureMessage(R.string.ChooseColor);
                break;
            case R.id.tutorMode:
            case R.id.noTutor:
                List<Integer> tutorIcons = Arrays.asList(R.id.tutorMode, R.id.noTutor);
                setBorder(tutorIcons, viewId);
                showFutureFeatureMessage(R.string.TutorMode);
                break;
            case R.id.IconCheckers:
            case R.id.IconChess:
            case R.id.IconTicTacToe:
                List<Integer> gameIcons = Arrays.asList(R.id.IconCheckers, R.id.IconChess, R.id.IconTicTacToe);
                mExpId = viewId;
                setBorder(gameIcons, viewId);
                break;
            case R.id.playWithSetup:
                processClickEvent(mLayout.findViewById(mExpId), this.type);
                break;
            default:
                processClickEvent(event.view, this.type);
                break;
        }
    }

    @Override public void onResume() {
        super.onResume();
        FabManager.game.setImage(R.drawable.ic_add_white_24dp);
        FabManager.game.init(this, GAME_HOME_FAM_KEY);
    }

    @Override public void onSetup(Context context, Dispatcher dispatcher) {
        mDispatcher = dispatcher;
    }

    @Override public void onStart() {
        super.onStart();
        ToolbarManager.instance.init(this, helpAndFeedback, chat, invite, settings);

        ImageView imageView = mLayout.findViewById(R.id.player1Icon);
        imageView.setColorFilter(ContextCompat.getColor(getContext(), colorPrimary), SRC_ATOP);
        imageView = mLayout.findViewById(R.id.player2Icon);
        imageView.setColorFilter(ContextCompat.getColor(getContext(), colorAccent), SRC_ATOP);
        imageView = mLayout.findViewById(R.id.tutorMode);
        imageView.setColorFilter(ContextCompat.getColor(getContext(), colorPrimary), SRC_ATOP);
        imageView = mLayout.findViewById(R.id.noTutor);
        imageView.setColorFilter(ContextCompat.getColor(getContext(), colorPrimary), SRC_ATOP);

        List<Integer> choices = Arrays.asList(R.id.timerZero, R.id.timerFifteen, R.id.timerThirty,
                R.id.timerFortyFive, R.id.timerSixty, R.id.player1Icon, R.id.player2Icon,
                R.id.tutorMode, R.id.noTutor, R.id.IconCheckers, R.id.IconChess, R.id.IconTicTacToe);
        setBorder(choices, -1);
    }

    @Override public List<ListItem> getList() {
        return null;
    }

    private void setBorder(List<Integer> ids, int chosenId) {
        for (int iconId : ids) {
            View view = mLayout.findViewById(iconId);
            if (view == null)
                break;
            if (view.getId() == chosenId) {
                view.setBackgroundResource(R.drawable.button_background_selected);
            } else {
                view.setBackgroundResource(R.drawable.button_background);
            }
        }
    }
}
