package com.pajato.android.gamechat.game;

import android.os.Bundle;
import android.view.KeyEvent;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ImageButton;
import android.widget.Spinner;
import android.widget.TextView;

import com.pajato.android.gamechat.R;
import com.pajato.android.gamechat.chat.BaseFragment;

public class SettingsFragment extends BaseFragment {
    private String game;
    private boolean isValidUser = false;

    private ImageButton mComputer;
    private ImageButton mLocal;
    private ImageButton mOnline;

    public SettingsFragment() {

    }

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

        // Setup the group choice spinner and adapter.
        Spinner groupChoices = (Spinner) mLayout.findViewById(R.id.settings_group_spinner);
        ArrayAdapter<CharSequence> groupAdapter = ArrayAdapter.createFromResource(getActivity(),
                R.array.groups, android.R.layout.simple_spinner_item);
        groupAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        groupChoices.setAdapter(groupAdapter);
        Spinner userChoices = (Spinner) mLayout.findViewById(R.id.settings_user_spinner);

        getActivity().findViewById(R.id.games_fab).setVisibility(View.GONE);

        // We want different users to appear in the user spinner when a different group is chosen.
        groupChoices.setOnItemSelectedListener(new UserSelector(userChoices));

        // Setup the references to the game option buttons.
        mLocal = (ImageButton) mLayout.findViewById(R.id.settings_local_button);
        mOnline = (ImageButton) mLayout.findViewById(R.id.settings_online_button);
        mComputer = (ImageButton) mLayout.findViewById(R.id.settings_computer_button);

        // Handle the game-specific portions of the layout.
        if(game.equals(getString(R.string.new_game_ttt))) {
            title.setText(R.string.playing_ttt);
            setupTTT();
        } else if(game.equals(getString(R.string.new_game_checkers))) {
            title.setText(R.string.playing_checkers);
            setupCheckers();
        } else if(game.equals(getString(R.string.new_game_chess))) {
            title.setText(R.string.playing_chess);
            setupChess();
        }
    }

    /** Handle the back button after the view has been created. */
    @Override public void onViewCreated(final View view, final Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        if(this.getView() != null) {
            this.getView().setFocusableInTouchMode(true);
            this.getView().requestFocus();
            this.getView().setOnKeyListener(new View.OnKeyListener() {
                @Override public boolean onKey(View v, int keyCode, KeyEvent event) {
                    if (keyCode == KeyEvent.KEYCODE_BACK) {
                        GameManager.instance.sendNewGame(GameManager.INIT_INDEX, getActivity());
                        return true;
                    }
                    return false;
                }
            });
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

    private class UserSelector implements AdapterView.OnItemSelectedListener {
        private Spinner mSpinner;

        UserSelector(final Spinner userChoices) {
            mSpinner = userChoices;
        }

        //TODO: Find a procedural way to generate these arrays once accounts are implemented.
        //TODO: Find a better way to handle isValidUser.
        @Override public void onItemSelected(final AdapterView<?> parent, final View view,
                                             final int position, final long id) {
            ArrayAdapter<CharSequence> userAdapter;
            // If the group spinner has chosen the family group, show the family members.
            if(position == 1) {
                userAdapter = ArrayAdapter.createFromResource(getActivity(),
                        R.array.family_group, android.R.layout.simple_spinner_item);
                userAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                isValidUser = true;
                // If the group spinner has chosen the work group, show the work members.
            } else if (position == 2) {
                userAdapter = ArrayAdapter.createFromResource(getActivity(),
                        R.array.work_group, android.R.layout.simple_spinner_item);
                userAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                isValidUser = true;
                // Otherwise, a group has not been chosen, so show an empty group.
            } else {
                userAdapter = ArrayAdapter.createFromResource(getActivity(),
                        R.array.empty_group, android.R.layout.simple_spinner_item);
                isValidUser = false;
            }
            mSpinner.setAdapter(userAdapter);
        }
        @Override public void onNothingSelected(AdapterView<?> parent) {

        }
    }
}
