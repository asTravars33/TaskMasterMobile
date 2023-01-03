package com.wangjessica.taskmaster;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.os.Handler;
import android.text.Html;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.chaquo.python.PyObject;
import com.chaquo.python.Python;
import com.chaquo.python.android.AndroidPlatform;
import com.devs.vectorchildfinder.VectorChildFinder;
import com.devs.vectorchildfinder.VectorDrawableCompat;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.io.InputStream;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;

import pl.droidsonroids.gif.GifImageView;

public class SingleQuestActivity extends AppCompatActivity {

    // Layout components
    private GifImageView gifView;
    private TextView taskDesc;
    private TextView timerView;
    private ImageView doneButton;

    // Avatar
    private ArrayList<Integer> avatarColors;
    private ImageView avatarImg;

    // Other user info
    private int coinCnt;
    private TextView coinDisplay;

    // Firebase
    private DatabaseReference userRef;
    private DatabaseReference rootRef;
    private DatabaseReference profileRef;
    private String userId;

    // Quest info
    private ArrayList<String> tasks;
    private ArrayList<Double> times; // Times are in minutes
    private ArrayList<String> actionItems;
    private boolean timerDone = true;
    private long secondsLeft = 0;
    private boolean curTaskDone = false;
    private ImageView questImg;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_single_quest);

        // Instantiate layout variables
        gifView = findViewById(R.id.gif_view);
        taskDesc = findViewById(R.id.task_desc);
        timerView = findViewById(R.id.timer);
        questImg = findViewById(R.id.image_view);
        doneButton = findViewById(R.id.done_button);

        // Firebase variables
        rootRef = FirebaseDatabase.getInstance().getReference();
        FirebaseAuth auth = FirebaseAuth.getInstance();
        userId = auth.getCurrentUser().getUid();
        userRef = rootRef.child("Users").child(userId);

        // Retrieve current coin count
        coinDisplay = findViewById(R.id.coin_label);
        profileRef = userRef.child("Profile");
        profileRef.child("Coins").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                coinCnt = Integer.parseInt(snapshot.getValue().toString());
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
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
        //getActionItemsAndStartQuest("Random Preset");
        getActionItemsAndStartQuest("Template Quest");
    }
    // Starting the overall quest
    public void startQuest(){
        // Move through each task
        startTask(0);
    }
    // Running a task
    public void startTask(int i){
        curTaskDone = false;
        doneButton.setBackgroundColor(getColor(R.color.red));
        // Base case? (Quest finished?)
        if(i>=tasks.size()){
            coinCnt+=10;
            Handler handler = new Handler();
            handler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    coinDisplay.setText(""+coinCnt);
                }
            }, 1000);
            return;
        }
        // Show the task
        secondsLeft = (long)(times.get(i)*60);
        String[] nextThing = actionItems.get(i).split(";");
        getImage(nextThing[0]);
        taskDesc.setText(Html.fromHtml(nextThing[0]+". Do <b>"+tasks.get(i)+"</b> for "+times.get(i)+" minutes to "+nextThing[1]+"!"));
        //taskDesc.setText(Html.fromHtml("Do <b>"+tasks.get(i)+"</b> for "+times.get(i)+" minutes to let the dream wake!"));
        Timer timer = new Timer();
        timer.scheduleAtFixedRate(new TimerTask() {
            @Override
            public void run() {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        long minutes = secondsLeft/60;
                        long seconds = secondsLeft%60;
                        String timeDisplay = pad(minutes)+":"+pad(seconds);
                        timerView.setText(timeDisplay);
                        secondsLeft--;
                        if (secondsLeft == -1 || curTaskDone) {
                            doneButton.setBackgroundColor(getColor(R.color.green));
                            coinCnt++;
                            coinDisplay.setText(""+coinCnt);
                            timerDone = true;
                            timer.cancel();
                            Handler handler = new Handler();
                            handler.postDelayed(new Runnable() {
                                @Override
                                public void run() {
                                    startTask(i+1);
                                }
                            }, 500);
                        }
                    }
                });
            }
        }, 0, 1000);
    }
    public void doneTask(View view){
        curTaskDone = true;
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
    // Retrieving image for quest segment
    public void getImage(String line){
        // Find the most important word
        String word = keyword(line);
        System.out.println(word);
        // Download the Bing image
        Python py = Python.getInstance();
        PyObject pyobj = py.getModule("get_img");
        PyObject obj = null;
        obj = pyobj.callAttr("get_image", word);
        System.out.println(obj.toString());
        // Set the new image
        setNewImage(obj.toString());
    }
    private void setNewImage(String imageURL) {
        Thread thread = new Thread(new Runnable() {

            @Override
            public void run() {
                try  {
                    InputStream is = (InputStream) new URL(imageURL).getContent();
                    Bitmap bitmap = BitmapFactory.decodeStream(is);
                    questImg.setImageBitmap(bitmap);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });

        thread.start();
    }
    public String keyword(String line){
        if(!Python.isStarted()){
            Python.start(new AndroidPlatform(this));
        }
        Python py = Python.getInstance();
        PyObject pyobj = py.getModule("is_noun");
        PyObject obj = null;
        obj = pyobj.callAttr("get_noun", line);
        return obj.toString();
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
    /// Get the storyline - changes based on mode!! ///
    public void getActionItemsAndStartQuest(String mode){
        if(mode.equals("Random Preset")){
            getActionItemsAndStartQuestPreset();
        }
        else if(mode.equals("Template Quest")){
            getActionItemsAndStartQuestTemplate();
        }
    }
    public void getActionItemsAndStartQuestPreset(){
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
    public void getActionItemsAndStartQuestTemplate(){
        HashMap<String, Object> magCreatures = new HashMap<String, Object>();
        String[] creatures = new String[] {"arm","heel","hand","leg","foot","shoulder","ear","knees","calf","cheek","hip","chin"};
        for(String creature: creatures){
            magCreatures.put(creature, "");
        }
        rootRef.child("Quest Templates").child("Body Parts").updateChildren(magCreatures);
        doNext();
    }
    public void doNext(){
        HashMap<String, Object> magCreatures = new HashMap<String, Object>();
        String[] creatures = new String[] {"door","box","chest","portal","window","vault"};
        for(String creature: creatures){
            magCreatures.put(creature, "");
        }
        rootRef.child("Quest Templates").child("Locked Items").updateChildren(magCreatures);
        doNext2();
    }
    public void doNext2(){
        HashMap<String, Object> magCreatures = new HashMap<String, Object>();
        String[] creatures = new String[] {"armor","belt","book","boot","slipper","cloak","glove","helm","necklace","bracelet","ring","rod","scroll","shield","staff","wand","weapon"};
        for(String creature: creatures){
            magCreatures.put(creature, "");
        }
        rootRef.child("Quest Templates").child("Magical Items").updateChildren(magCreatures);
    }
    // Save changes

    @Override
    protected void onPause() {
        super.onPause();
        Map<String, Object> coinUpdate = new HashMap<String, Object>();
        coinUpdate.put("Coins", coinCnt);
        profileRef.updateChildren(coinUpdate);
    }
}