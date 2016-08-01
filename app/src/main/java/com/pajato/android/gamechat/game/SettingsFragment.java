package com.pajato.android.gamechat.game;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ImageButton;
import android.widget.Spinner;
import android.widget.TextView;

import com.pajato.android.gamechat.R;
import com.pajato.android.gamechat.fragment.BaseFragment;

public class SettingsFragment extends BaseFragment {

    private String game;
    private View mMain;
    private boolean isValidUser = false;

    public SettingsFragment() {

    }

    @Override public void setArguments(Bundle args) {
        if(args != null && args.containsKey(GameManager.GAME_KEY)) {
            game = args.getString(GameManager.GAME_KEY);
        }
        super.setArguments(args);
    }

    @Override public View onCreateView(LayoutInflater layoutInflater, ViewGroup container,
                                       Bundle savedInstanceState) {
        mMain = layoutInflater.inflate(R.layout.fragment_settings, container, false);
        TextView title = (TextView) mMain.findViewById(R.id.settings_title);

        // Setup the group choice spinner and adapter.
        Spinner groupChoices = (Spinner) mMain.findViewById(R.id.settings_group_spinner);
        ArrayAdapter<CharSequence> groupAdapter = ArrayAdapter.createFromResource(getActivity(),
                R.array.groups, android.R.layout.simple_spinner_item);
        groupAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        groupChoices.setAdapter(groupAdapter);

        // Setup the user choices spinner
        final Spinner userChoices = (Spinner) mMain.findViewById(R.id.settings_user_spinner);

        // We want different users to appear in the user spinner when a different group is chosen.
        //TODO: Find a procedural way to generate these arrays once accounts are implemented.
        //TODO: Find a better way to handle isValidUser.
        groupChoices.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
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
                userChoices.setAdapter(userAdapter);
            }
            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });

        // Handle the game-specific portions of the layout.
        if(game.equals(getString(R.string.new_game_ttt))) {
            title.setText(R.string.playing_ttt);
            setupTTT();
        }
        return mMain;
    }

    private void setupTTT() {
        ImageButton local = (ImageButton) mMain.findViewById(R.id.ttt_local_button);
        ImageButton online = (ImageButton) mMain.findViewById(R.id.ttt_online_button);
        ImageButton computer = (ImageButton) mMain.findViewById(R.id.ttt_computer_button);

        local.setOnClickListener(new View.OnClickListener() {
            @Override public void onClick(View v) {
                GameManager.instance.sendNewGame(GameManager.TTT_L_INDEX, getActivity());
            }
        });
        online.setOnClickListener(new View.OnClickListener() {
            @Override public void onClick(View v) {
                if(isValidUser) {
                    GameManager.instance.sendNewGame(GameManager.TTT_O_INDEX, getActivity());
                }
            }
        });
        computer.setOnClickListener(new View.OnClickListener() {
            @Override public void onClick(View v) {
                //GameManager.instance.sendNewGame(GameManager.TTT_C_INDEX, getActivity());
            }
        });
    }

}
