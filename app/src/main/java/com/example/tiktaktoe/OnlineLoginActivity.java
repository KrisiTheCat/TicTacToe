package com.example.tiktaktoe;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;

import com.google.firebase.database.DataSnapshot;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class OnlineLoginActivity extends AppCompatActivity {
    ListView lv_loginUsers;
    ArrayList<String> list_loginUsers = new ArrayList<String>();
    ArrayAdapter adpt;

    ListView lv_requestedUsers;
    ArrayList<String> list_requestedUsers = new ArrayList<String>();
    ArrayAdapter reqUsersAdpt;

    TextView tvUserID, tvSendRequest;
    String LoginUserID, UserName, LoginUID;

    //Button btnLogin;
    //EditText etEmail;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_online_login);

        DataBaseOp.setLoginActivity(this);
        JoinOnlineGame();

        tvSendRequest = (TextView) findViewById(R.id.tvSendRequest);
        tvSendRequest.setText("Please wait...");

        adpt = new ArrayAdapter(this, android.R.layout.simple_list_item_1, list_loginUsers){

            @Override
            public View getView(int position, View convertView, ViewGroup parent) {

                View view = super.getView(position, convertView, parent);
                TextView text = (TextView) view.findViewById(android.R.id.text1);
                text.setTextColor(Color.WHITE);

                return view;
            }
        };
        lv_loginUsers = (ListView) findViewById(R.id.lv_loginUsers);
        lv_loginUsers.setAdapter(adpt);

        lv_loginUsers.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                final String requestToUser = ((TextView)view).getText().toString();
                sendInvite(requestToUser);
            }
        });

    }


    public void userChanges(DataSnapshot dataSnapshot) {
        String key = "";
        Object val="";
        Class<?> type;
        Set<String> set = new HashSet<String>();

        if(dataSnapshot.hasChildren()) {
            for (DataSnapshot child : dataSnapshot.getChildren()) {
                key = child.getKey();
                val = child.getValue();
                assert val != null;
                type = val.getClass();
                if ((val.toString().equals("open") || type.isArray() || val instanceof List<?>) && !key.equalsIgnoreCase(MainActivity.userMe.getUsername())) {
                    set.add(key);
                }
            }
        }

        adpt.clear();
        adpt.addAll(set);
        adpt.notifyDataSetChanged();
        tvSendRequest.setText("Send request to");
    }

    void sendInvite(final String OtherPlayer){
        AlertDialog.Builder b = new AlertDialog.Builder(this);
        LayoutInflater inflater = this.getLayoutInflater();
        final View dialogView = inflater.inflate(R.layout.connect_player_dialog, null);
        b.setView(dialogView);

        b.setTitle("Outgoing Invitation");
        b.setMessage("Do you wish to play with " + OtherPlayer + "?");
        b.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                DataBaseOp.sendInvitation(MainActivity.userMe.getUsername(), OtherPlayer);
            }
        });
        b.setNegativeButton("Back", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        });
        b.show();
    }

    void confirmInvite(final String OtherPlayer){
        AlertDialog.Builder b = new AlertDialog.Builder(this);
        LayoutInflater inflater = this.getLayoutInflater();
        final View dialogView = inflater.inflate(R.layout.connect_player_dialog, null);
        b.setView(dialogView);

        b.setTitle("Incoming Invitation");
        b.setMessage("Do you wish to play with " + OtherPlayer + "?");
        b.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                DataBaseOp.createGame(MainActivity.userMe.getUsername(), OtherPlayer);
            }
        });
        b.setNegativeButton("Back", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        });
        b.show();
    }

    void startGame(String gameId){
        MainActivity.userMe.setGameId(gameId);
        Intent i = new Intent(getApplicationContext(), OnlineGameActivity.class);
        i.putExtra("game_id", gameId);
        startActivity(i);
        finish();
    }

    void JoinOnlineGame(){
        final AlertDialog.Builder b = new AlertDialog.Builder(this);
        LayoutInflater inflater = this.getLayoutInflater();
        final View dialogView = inflater.inflate(R.layout.login_dialog, null);
        b.setView(dialogView);

        final EditText etUsername = (EditText) dialogView.findViewById(R.id.etUsername);
        final TextView tvError = (TextView) dialogView.findViewById(R.id.tvError);
        final Button btnRegister = (Button) dialogView.findViewById(R.id.btnRegister);
        final Button btnBack = (Button) dialogView.findViewById(R.id.btnBack);

        b.setTitle("Username pick");
        b.setCancelable(false);
        etUsername.addTextChangedListener(new TextWatcher() {

            public void afterTextChanged(Editable s) {
                if(checkUsername(s.toString())){
                    tvError.setVisibility(View.INVISIBLE);
                    btnRegister.setEnabled(true);
                }
                else{
                    tvError.setVisibility(View.VISIBLE);
                    btnRegister.setEnabled(false);
                }
            }

            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            public void onTextChanged(CharSequence s, int start, int before, int count) {}
        });

        final AlertDialog alert = b.create();
        alert.show();
        btnRegister.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                alert.dismiss();
                MainActivity.userMe.setUsername(etUsername.getText().toString());

                TextView tvLoginUser = (TextView) findViewById(R.id.tvLoginUser);
                tvLoginUser.setText("ID: " + (etUsername.getText().toString()));

                DataBaseOp.playerStatus(etUsername.getText().toString(), true, "");
                DataBaseOp.onlineUserChanges();
                DataBaseOp.onlinePlayerStatusUpd(MainActivity.userMe.getUsername());
            }
        });
        btnBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent i = new Intent(getApplicationContext(), MenuActivity.class);
                startActivity(i);
                finish();
            }
        });
    }

    private boolean checkUsername(String s) {
        if (s == null){
            return false;
        }
        if (s.length()==0){
            return false;
        }
        int len = s.length();
        for (int i = 0; i < len; i++) {
            if (!(Character.isLetter(s.charAt(i)))) {
                return false;
            }
        }
        return true;
    }

    @Override
    public void onStop() {
        super.onStop();
        if(MainActivity.userMe.getGameId().length() == 0)
            DataBaseOp.playerStatus(MainActivity.userMe.getUsername(), false, "");
    }

}
