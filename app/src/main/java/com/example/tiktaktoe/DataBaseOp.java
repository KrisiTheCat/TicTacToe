package com.example.tiktaktoe;

import android.util.Log;

import androidx.annotation.NonNull;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.Objects;

public class DataBaseOp {

    private static FirebaseDatabase database = FirebaseDatabase.getInstance("https://tik-tak-toe-7f9c3-default-rtdb.firebaseio.com/");
    private static DatabaseReference myRef = database.getReference();

    private static OnlineGameActivity gameActivity;
    public static void setGameActivity(OnlineGameActivity gameActivity1){
        gameActivity = gameActivity1;
    }
    private static OnlineLoginActivity loginActivity;
    public static void setLoginActivity(OnlineLoginActivity loginActivity1){
        loginActivity = loginActivity1;
    }
    private static WinActivity winActivity;
    public static void setWinActivity(WinActivity winActivity1){
        winActivity = winActivity1;
    }

    public static void playerStatus(String playerName, boolean status, String waiting){
        if(status){
            if(waiting.length() == 0)
                myRef.child("online").child(playerName).setValue("open");
            else {
                myRef.child("online").child(playerName).setValue('_' + waiting);
                myRef.child("online").child(waiting).addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        if(Objects.requireNonNull(snapshot.getValue()).toString().equals('_' + playerName)){
                            createGame(playerName, waiting);
                        }
                        else if(Objects.requireNonNull(snapshot.getValue()).toString().equals("open")){
                            myRef.child("online").child(playerName).setValue("open");
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });
            }
        }
        else {
            myRef.child("online").child(playerName).removeValue();
        }
    }

    public static void onlineUserChanges() {
        myRef.child("online").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                loginActivity.userChanges(snapshot);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    public static void onlinePlayerStatusUpd(String playerName){

        myRef.child("online").child(playerName+"").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if(snapshot.hasChildren()) {
                    for (DataSnapshot child : snapshot.getChildren()) {
                        loginActivity.confirmInvite(child.getValue().toString());
                    }
                }
                else if(!snapshot.getValue().toString().equals("open") && snapshot.getValue().toString().charAt(0) != '_'){
                    loginActivity.startGame(snapshot.getValue().toString());
                    //myRef.child("online").child(playerName+"").removeEventListener(this);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }
    public static void sendInvitation(String playerFrom, String playerTo){
        myRef.child("online").child(playerTo).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                myRef.child("online").child(playerTo).child(snapshot.getChildrenCount()+"").setValue(playerFrom);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    public static void createGame(String player1, String player2){
        DatabaseReference newGame = myRef.child("games").push();
        myRef.child("online").child(player1).setValue(newGame.getKey());
        myRef.child("online").child(player2).setValue(newGame.getKey());
        GameInfo gameInfo = new GameInfo(player1, player2);
        newGame.setValue(gameInfo);
    }
    public static void deleteGame(String gameId){
        myRef.child("games").child(gameId).removeValue();
    }

    public static void gameUpdates(String gameId){
        myRef.child("games").child(gameId).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if(snapshot.getValue()!=null) {
                    gameActivity.gameUpdate(snapshot);
                }
                else {
                    myRef.child("games").child(gameId).removeEventListener(this);
                }
            }
            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }
    public static void getGameInfo(String gameId){
        myRef.child("games").child(gameId).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if(snapshot.getValue()!=null) {
                    winActivity.becomeGameInfo(snapshot);
                }
                else {
                    myRef.child("games").child(gameId).removeEventListener(this);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    public static void updateState(String gameId, GameStates newState) {
        myRef.child("games").child(gameId)
                .child("gameState").setValue(newState);
    }
    public static void movePawn(String gameId, int playerId, int pawnId, int position) {
        myRef.child("games").child(gameId)
                .child("pawns").child(playerId + "")
                .child(pawnId+"").child("pos").setValue(position);
        myRef.child("games").child(gameId)
                .child("turn").setValue((1-playerId));

    }
}
