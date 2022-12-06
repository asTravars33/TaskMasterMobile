package com.wangjessica.taskmaster;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.text.Html;
import android.widget.ImageView;
import android.widget.TextView;

import com.devs.vectorchildfinder.VectorChildFinder;
import com.devs.vectorchildfinder.VectorDrawableCompat;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.Timer;
import java.util.TimerTask;

import pl.droidsonroids.gif.GifImageView;

public class SingleQuestActivity extends AppCompatActivity {

    // Layout components
    private GifImageView gifView;
    private TextView taskDesc;
    private TextView timerView;

    // Avatar
    private ArrayList<Integer> avatarColors;
    private ImageView avatarImg;

    // Firebase
    private DatabaseReference userRef;
    private DatabaseReference rootRef;
    private String userId;

    // Quest info
    private ArrayList<String> tasks;
    private ArrayList<Double> times; // Times are in minutes
    private ArrayList<String> actionItems;
    private boolean timerDone = true;
    private long secondsLeft = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_single_quest);

        // Instantiate layout variables
        gifView = findViewById(R.id.gif_view);
        taskDesc = findViewById(R.id.task_desc);
        timerView = findViewById(R.id.timer);

        // Firebase variables
        rootRef = FirebaseDatabase.getInstance().getReference();
        FirebaseAuth auth = FirebaseAuth.getInstance();
        userId = auth.getCurrentUser().getUid();
        userRef = rootRef.child("Users").child(userId);

        // Fill in avatar
        avatarImg = findViewById(R.id.profile_img);
        avatarColors = new ArrayList<Integer>();
        avatarColors();

        // Quest programming
        Intent intent = getIntent();
        tasks = intent.getStringArrayListExtra("tasks");
        ArrayList<String> timesString = intent.getStringArrayListExtra("times");
        times = new ArrayList<Double>();
        for(String time: timesString){
            times.add(Double.parseDouble(time));
        }

        // Get the action items
        getActionItemsAndStartQuest();
    }
    // Starting the overall quest
    public void startQuest(){
        // Move through each task
        startTask(0);
    }
    // Running a task
    public void startTask(int i){
        // Base case? (Quest finished?)
        if(i>=tasks.size()){
            return;
        }
        // Show the task
        secondsLeft = (long)(times.get(i)*60);
        String[] nextThing = actionItems.get(i).split(";");
        taskDesc.setText(Html.fromHtml(nextThing[0]+". Do <b>"+tasks.get(i)+"</b> for "+times.get(i)+" minutes to "+nextThing[1]+"!"));
        Timer timer = new Timer();
        timer.scheduleAtFixedRate(new TimerTask() {
            @Override
            public void run() {
                long minutes = secondsLeft/60;
                long seconds = secondsLeft%60;
                String timeDisplay = pad(minutes)+":"+pad(seconds);
                timerView.setText(timeDisplay);
                secondsLeft--;
                if (secondsLeft == -1) {
                    timerDone = true;
                    timer.cancel();
                    startTask(i+1);
                }
            }
        }, 0, 1000);
    }
    public String pad(long l){
        String newS = "";
        String s = ""+l;
        if(s.length()<2){
            for(int i=0; i<2-s.length(); i++){
                newS += "0";
            }
            newS+=s;
        }
        else{
            newS = s;
        }
        return newS;
    }
    // Getting avatar colors
    public void avatarColors(){
        userRef.child("Profile").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                for(int i=0; i<4; i++){
                    avatarColors.add(Integer.parseInt(snapshot.child("Color "+i).getValue().toString()));
                }
                updateAvatarAll();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }
    // Updating avatar appearance
    public void updateAvatarAll(){ // All at once
        VectorChildFinder vector = new VectorChildFinder(this, R.drawable.avatar, avatarImg);
        for(int i=0; i<avatarColors.size(); i++){
            VectorDrawableCompat.VFullPath path = vector.findPathByName("path"+i);
            path.setFillColor(avatarColors.get(i));
        }
    }
    // Get the storyline
    public void getActionItemsAndStartQuest(){
        actionItems = new ArrayList<String>();
        rootRef.child("Quest Presets").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                Iterator iterator = snapshot.getChildren().iterator();
                while(iterator.hasNext()){
                    actionItems.add(((DataSnapshot)iterator.next()).getKey());
                    System.out.println(((DataSnapshot)iterator.next()).getKey());
                }
                startQuest();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }
}