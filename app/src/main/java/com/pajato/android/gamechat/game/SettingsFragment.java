package com.pajato.android.gamechat.game;

import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Spinner;
import android.widget.TextView;

import com.pajato.android.gamechat.R;
import com.pajato.android.gamechat.chat.FabManager;
import com.pajato.android.gamechat.event.AppEventManager;
import com.pajato.android.gamechat.event.BackPressEvent;

import org.greenrobot.eventbus.Subscribe;

public class SettingsFragment extends BaseGameFragment {
    private String game;
    private boolean isValidUser = false;

    private View mComputer;
    private View mLocal;
    private View mOnline;

    public SettingsFragment() {

    }

    /** Handle a back press event by canceling out of the settings fragment. */
    @Subscribe(priority=1)
    public void onBackPressed(final BackPressEvent event) {
        // Re-enable the FAB and return to the start fragment after disabling further event
        // subscribers.
        AppEventManager.instance.cancel(event);
        FabManager.game.setState(this, View.VISIBLE);
        GameManager.instance.sendNewGame(GameManager.NO_GAMES_INDEX, getActivity());
    }

    /** Provide the construction time arguments to be delivered by the fragment manager. */
    @Override public void setArguments(final Bundle args) {
        // Grab the game argument that we are accepting and creating settings for.
        if(args != null && args.containsKey(GameManager.GAME_KEY)) {
            game = args.getString(GameManager.GAME_KEY);
        }
        super.setArguments(args);
    }

    /** Set the layout file. */
    @Override public int getLayout() {return R.layout.fragment_settings;}

    @Override public void onInitialize() {
        TextView title = (TextView) mLayout.findViewById(R.id.settings_title);

        getActivity().findViewById(R.id.games_fab).setVisibility(View.GONE);

        // Setup the references to the game option buttons.
        mLocal = mLayout.findViewById(R.id.settings_local_button);
        mOnline = mLayout.findViewById(R.id.settings_online_button);
        mComputer = mLayout.findViewById(R.id.settings_computer_button);

        // Handle the game-specific portions of the layout.
        if(game.equals(getString(R.string.new_game_ttt))) {
            title.setText(R.string.PlayTicTacToe);
            setupTTT();
        } else if(game.equals(getString(R.string.new_game_checkers))) {
            title.setText(R.string.PlayCheckers);
            setupCheckers();
        } else if(game.equals(getString(R.string.new_game_chess))) {
            title.setText(R.string.PlayChess);
            setupChess();
        }
    }

    /**
     * Setup the Tic-Tac-Toe game creation invitation onClicks for Local, Online and Computer games.
     */
    private void setupTTT() {
        mLocal.setOnClickListener(new View.OnClickListener() {
            @Override public void onClick(View v) {
                GameManager.instance.sendNewGame(GameManager.TTT_LOCAL_INDEX, getActivity());
            }
        });
        mOnline.setOnClickListener(new View.OnClickListener() {
            @Override public void onClick(View v) {
                if(isValidUser) {
                    GameManager.instance.sendNewGame(GameManager.TTT_ONLINE_INDEX, getActivity());
                }
            }
        });
        mComputer.setOnClickListener(new View.OnClickListener() {
            @Override public void onClick(View v) {
                //GameManager.instance.sendNewGame(GameManager.TTT_C_INDEX, getActivity());
            }
        });
    }

    /**
     * Setup the Checkers game creation invitation onClicks for Local, Online and Computer games.
     */
    private void setupCheckers() {
        mLocal.setOnClickListener(new View.OnClickListener() {
            @Override public void onClick(View v) {
                GameManager.instance.sendNewGame(GameManager.CHECKERS_INDEX, getActivity());
            }
        });
        mOnline.setOnClickListener(new View.OnClickListener() {
            @Override public void onClick(View v) {
                /*if(isValidUser) {
                    GameManager.instance.sendNewGame(GameManager.CHECKERS_ONLINE_INDEX, getActivity());
                }*/
            }
        });
        mComputer.setOnClickListener(new View.OnClickListener() {
            @Override public void onClick(View v) {
                //GameManager.instance.sendNewGame(GameManager.CHECKERS_COMPUTER_INDEX, getActivity());
            }
        });
    }

    /**
     * Setup the Chess game creation invitation onClicks for Local, Online, and Computer games.
     */
    private void setupChess() {
        mLocal.setOnClickListener(new View.OnClickListener(){
            @Override public void onClick(View v) {
                GameManager.instance.sendNewGame(GameManager.CHESS_INDEX, getActivity());
            }
        });
    }

}
