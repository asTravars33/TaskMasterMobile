package com.wangjessica.taskmaster;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.graphics.Color;
import android.media.AudioAttributes;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Timer;
import java.util.TimerTask;

import pl.droidsonroids.gif.GifImageView;

public class GroupSessionActivity extends AppCompatActivity {

    // Layout components
    LinearLayout chatLayout;
    ScrollView chatScroll;
    EditText messageTxt;
    ScrollView peopleScroll;
    LinearLayout peopleLayout;
    GifImageView gif;
    TextView songNameView;
    Button nextSongButton;
    String[] songNameChoices;
    int songIdx = 0;
    MediaPlayer player;

    // Group info
    ArrayList<String> companions = new ArrayList<String>();
    String groupName;
    int groupCapacity;
    String groupKey;
    private boolean peopleShowing = false;

    // Firebase info
    DatabaseReference groupRef;
    DatabaseReference rootRef;
    DatabaseReference chatRef;
    StorageReference storageRef;
    FirebaseAuth auth;
    String uId;
    String myName;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_group_session);

        // Layout variables
        gif = findViewById(R.id.background_gif);
        chatLayout = findViewById(R.id.chat);
        chatScroll = findViewById(R.id.chat_scroll);
        messageTxt = findViewById(R.id.message_text);
        peopleScroll = findViewById(R.id.people_list);
        peopleLayout = findViewById(R.id.people_list_layout);
        Button peopleButton = findViewById(R.id.people_button);
        peopleButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(!peopleShowing) {
                    peopleScroll.setVisibility(View.VISIBLE);
                    peopleShowing = true;
                }
                else{
                    peopleScroll.setVisibility(View.INVISIBLE);
                    peopleShowing = false;
                }
            }
        });

        // Playing songs
        songNameChoices = getResources().getStringArray(R.array.songs);
        songNameView = findViewById(R.id.song_name);
        nextSongButton = findViewById(R.id.next_button);
        songNameView.setText(songNameChoices[0]);
        playMusic();

        nextSongButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // Increment index
                songIdx++;
                if(songIdx>=songNameChoices.length){
                    songIdx = 0;
                }
                // Update display
                songNameView.setText(songNameChoices[songIdx]);
                // Play the song
                playMusic();
            }
        });

        // Extract room key data
        Intent parentIntent = getIntent();
        groupKey = parentIntent.getStringExtra("groupKey");
        groupName = parentIntent.getStringExtra("groupName");

        // Change the group title
        TextView titleView = findViewById(R.id.title);
        titleView.setText(groupName);

        // Firebase info
        auth = FirebaseAuth.getInstance();
        uId = auth.getCurrentUser().getUid();
        rootRef = FirebaseDatabase.getInstance().getReference();
        groupRef = rootRef.child("Groups").child(groupKey);
        chatRef = rootRef.child("Chats").child(groupKey);
        storageRef = FirebaseStorage.getInstance().getReference().child("Profiles");
        rootRef.child("Users").child(uId).child("Profile").child("Name").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                myName = snapshot.getValue().toString();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

        // Get a list of users in this room
        groupRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                Iterator iterator = snapshot.getChildren().iterator();
                while(iterator.hasNext()){
                    DataSnapshot curData = ((DataSnapshot) iterator.next());
                    String cur = curData.getKey();
                    if(cur.equals("Name")){
                        groupName = curData.getValue().toString();
                    }
                    else if(cur.equals("Capacity")){
                        groupCapacity = Integer.parseInt(curData.getValue().toString());
                    }
                    else if(!cur.equals("User") && !cur.equals("Color")){
                        companions.add(cur);
                    }
                }
                showCompanions();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

        showMessages();
        cycleBackgroundGifs();
    }
    public void playMusic(){
        // Cancel previous song
        if(player!=null)
            player.stop();
        // Get the file name
        String fileName = "song_"+songIdx;
        // Play the song
        player = MediaPlayer.create(this, getResources().getIdentifier(fileName, "raw", "com.wangjessica.taskmaster"));
        player.start();
    }
    /*public void playMusic(String soundLink){
        soundLink = "https://www.soundhelix.com/examples/mp3/SoundHelix-Song-1.mp3";
        player = new MediaPlayer();
        player.setAudioAttributes(
                new AudioAttributes
                        .Builder()
                        .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
                        .build());
        try {
            System.out.println("Playing music....");
            player.setDataSource(soundLink);
            player.prepare();
            player.start();

        } catch (IOException e) {
            e.printStackTrace();
        }
    }*/
    public void cycleBackgroundGifs(){
        Timer timer = new Timer();
        timer.scheduleAtFixedRate(new TimerTask() {
            int idx = 1;
            int max = 4;
            @Override
            public void run() {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        // Change the background
                        String name="study_"+idx;
                        System.out.println(name);
                        int drawId = getResources().getIdentifier(name, "drawable", "com.wangjessica.taskmaster");
                        gif.setBackgroundResource(drawId);
                        // Update background index
                        System.out.println(idx+" "+max);
                        idx++;
                        if(idx>max){
                            idx = 1;
                        }
                    }
                });
            }
        }, 0, 5000); // TODO: Change to 60000 or smth
    }
    public void showMessages(){
        chatRef.addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {
                Iterator iterator = snapshot.getChildren().iterator();
                String content = ((DataSnapshot) iterator.next()).getValue().toString();
                String sender = ((DataSnapshot) iterator.next()).getValue().toString();
                // Add the new message to the layout
                TextView tv = new TextView(GroupSessionActivity.this);
                LinearLayout.LayoutParams lParams = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
                tv.setText("\n"+sender+": "+content);
                tv.setTextColor(Color.WHITE);
                tv.setLayoutParams(lParams);
                chatLayout.addView(tv);
                Handler handler = new Handler();
                handler.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        chatScroll.fullScroll(ScrollView.FOCUS_DOWN);
                    }
                }, 500);
            }

            @Override
            public void onChildChanged(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {

            }

            @Override
            public void onChildRemoved(@NonNull DataSnapshot snapshot) {

            }

            @Override
            public void onChildMoved(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {

            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }
    public void showCompanions(){
        for(String companion: companions){
            TextView tv = new TextView(this);
            LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
            tv.setLayoutParams(lp);
            tv.setGravity(View.TEXT_ALIGNMENT_CENTER);
            tv.setTextSize(17);
            tv.setText(companion);
            peopleLayout.addView(tv);
        }
    }
    public void sendMessage(View view){
        // Get the message from the EditText
        String msg = messageTxt.getText().toString();
        if(msg!=null && !msg.equals("")){
            // Update firebase with the message
            String newKey = chatRef.push().getKey();
            HashMap<String, Object> msgInfo = new HashMap<String, Object>();
            msgInfo.put("Content", msg);
            msgInfo.put("Sender", myName);
            chatRef.child(newKey).updateChildren(msgInfo);
            // Clear message
            messageTxt.setText("");
        }
    }

    // Lifecycle

    @Override
    protected void onDestroy() {
        super.onDestroy();
        // Remove the user from the list of people in the session
        groupRef.child(myName).removeValue();
    }
}