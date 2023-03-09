package com.wangjessica.taskmaster;

import android.app.VoiceInteractor;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.os.Handler;
import android.text.Html;
import android.text.method.ScrollingMovementMethod;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

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

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.TimeUnit;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import pl.droidsonroids.gif.GifImageView;

public class SingleQuestActivity extends AppCompatActivity implements PromptDialogFragment.StartQuestListener {

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
        taskDesc.setMovementMethod(new ScrollingMovementMethod());

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
                coinDisplay.setText(""+coinCnt);
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
        // getSegment();
        String type = intent.getStringExtra("type");
        getActionItemsAndStartQuest(type);
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
            coinCnt+=25;
            Handler handler = new Handler();
            doneButton.setBackgroundColor(getColor(R.color.green));
            // Notify user of quest finished
            taskDesc.setText("Quest Complete!");
            // Coins update
            coinDisplay.setText(""+coinCnt);
            updateCoins();
            handler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    /*coinDisplay.setText(""+coinCnt);
                    updateCoins();
                    // Go back to main activity
                    System.out.println("Going back babes");
                    Intent intent = new Intent(SingleQuestActivity.this, MainActivity.class);
                    startActivity(intent);*/
                }
            }, 1000);
        }
        else{
            // Show the task
            secondsLeft = (long)(times.get(i)*60);
            String[] nextThing = actionItems.get(i).split(";");
            getImage(nextThing[0]);
            taskDesc.setText(Html.fromHtml(nextThing[0]+". <b>"+tasks.get(i)+"</b> for "+times.get(i)+" minutes to "+nextThing[1]+"!"));

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
                                coinCnt+=5;
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
            if(path!=null)
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
        else{
            getActionItemsAndStartQuestAI();
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
                }
                startQuest();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }
    public void getActionItemsAndStartQuestTemplate(){
        // Template lists
        String[] magicalCreatures = new String[] {"dragon","chimera","mermaid","yeti","basilisk","sphinx","medusa","griffin","centaur","hippogriff","fairy","kappa","pegasus","ghoul","pixie","cyclops","redcap","manticore","typhon","leprechaun","fenrir","cipactli","imp","minotaur","hydra","fomorians","charybdis","behemoth","cerberus","echidna","adlet","cacus","geryon","fachan","ogre","humbaba","scylla","hadhayosh","kee-wakw","abaia","phoenix","tarasque","cockatrice","harpy","makara","ammit","garuda","leviathan","wyvern","namazu","elf","serpopard","indus worm","ahuizotl","psoglav","sirin","ekek","hellhound","monocerus"};
        String[] landforms = new String[] {"anabranch","arroyo","badlands","barchan","basin","bay","bayou","beach","bight","bluff","bornharuence","continent","cove","crevasse","cuesta","current","dam","dell","delta","dirt cone","desert","divide","dome","dry lake","dune","erg","estuary","escarpment","fjord","floodplain","forest","foe","hill","hogback","hoodoo","iceberg","inlet","island","islet","isthmus","karst","lake","latitude","lava dome","lava field","lava lake","lava spine","lava tube","longitude","lowland","marsh","mr","plain","plateau","pond","port","pothole","prairie","rapids","ravine","reef","reservoir","ria","riffle","river","sea","sound","strait","sandbar","sinkhole","swamp","terrace","tide","upstream","valley","volcano","waterfall"};
        String[] bodyParts = new String[] {"arm","heel","hand","leg","foot","shoulder","ear","knees","calf","cheek","hip","chin"};
        String[] lockedItems = new String[] {"door","box","chest","portal","window","vault"};
        String[] magicalItems = new String[] {"armor","belt","book","boot","slipper","cloak","glove","helm","necklace","bracelet","ring","rod","scroll","shield","staff","wand","weapon"};

        String[][] templateLists = new String[][]{magicalCreatures, landforms, bodyParts, lockedItems, magicalItems};
        String[] templateText = new String[] {"A XX appears in your path;fight it", "You encounter a XX;cross it", "You fall and injure your XX;heal it", "You encounter a locked XX;unlock it", "You find a XX;activate it"};

        // Populate actionItems
        actionItems = new ArrayList<String>();
        for(int i=0; i<tasks.size(); i++){
            int idx = (int)(Math.random()*5);
            String[] list = templateLists[idx];
            int elemIdx = (int)(Math.random()*list.length);
            String questSegment = templateText[idx].replace("XX", list[elemIdx]);
            char c = list[elemIdx].charAt(0);
            if(c=='a'||c=='e'||c=='i'||c=='o'||c=='u'){
                questSegment.replace("a ", "an ");
            }
            actionItems.add(questSegment);
        }

        // Begin the quest
        startQuest();
    }
    public void getActionItemsAndStartQuestAI(){
        PromptDialogFragment fragment = new PromptDialogFragment();
        fragment.setCancelable(true);
        fragment.show(getSupportFragmentManager(), "User Prompt");
    }
    @Override
    public void onDialogPositiveClick(String prompt) {
        getSegment(prompt);
    }
    public void getSegment(String prompt){
        OkHttpClient okHttpClient = new OkHttpClient.Builder()
                .connectTimeout(50, TimeUnit.SECONDS)
                .writeTimeout(50, TimeUnit.SECONDS)
                .readTimeout(50, TimeUnit.SECONDS)
                .build();
        Request request = new Request.Builder().url("http://astravars33.pythonanywhere.com/gentext?prompt="+prompt).build();
                okHttpClient.newCall(request).enqueue(new Callback() {
                    @Override
                    public void onFailure(@NonNull Call call, @NonNull IOException e) {
                        taskDesc.setText("Uh it failed");
                        System.out.println(e.toString());
                    }

                    @Override
                    public void onResponse(@NonNull Call call, @NonNull Response response) throws IOException {
                        String res = response.body().string();
                        System.out.println(res);
                        String[] taskElems = res.split("\\. |\\? |! ");
                        actionItems = new ArrayList<String>();
                        for(String elem: taskElems){
                            if(elem.length()>3 && !elem.substring(0, 2).equals("Go")){
                                actionItems.add(elem+";"+"continue");
                            }
                        }
                        startQuest();
                    }
                });
    }
    public void getSegmentOLD(){
        if(!Python.isStarted()){
            Python.start(new AndroidPlatform(this));
        }
        Python py = Python.getInstance();
        PyObject pyobj = py.getModule("gen_text");
        PyObject obj = null;
        obj = pyobj.callAttr("predict", "You begin your quest to find a rare diamond. ");
        System.out.println(obj.toString());
    }
    // Save changes

    @Override
    protected void onPause() {
        super.onPause();
        // updateCoins();
    }
    private void updateCoins(){
        Map<String, Object> coinUpdate = new HashMap<String, Object>();
        coinUpdate.put("Coins", coinCnt);
        profileRef.updateChildren(coinUpdate);
    }
}