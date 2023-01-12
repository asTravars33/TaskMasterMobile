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

public class GroupSessionActivity extends AppCompatActivity {

    // Layout components
    LinearLayout chatLayout;
    EditText messageTxt;
    ScrollView peopleScroll;
    LinearLayout peopleLayout;
    TextView link;
    Button addButton;
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
        chatLayout = findViewById(R.id.chat);
        messageTxt = findViewById(R.id.message_text);
        peopleScroll = findViewById(R.id.people_list);
        peopleLayout = findViewById(R.id.people_list_layout);
        link = findViewById(R.id.link);
        addButton = findViewById(R.id.add_button);
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
        addButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String linkText = link.getText().toString();
                playMusic(linkText);
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
    }
    public void playMusic(String soundLink){
        soundLink = "https://www.soundhelix.com/examples/mp3/SoundHelix-Song-1.mp3";
        player = new MediaPlayer();
        player.setAudioAttributes(
                new AudioAttributes
                        .Builder()
                        .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
                        .build());
        try {
            player.setDataSource(soundLink);
            player.prepare();
            player.start();

        } catch (IOException e) {
            e.printStackTrace();
        }
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
        }
    }
}