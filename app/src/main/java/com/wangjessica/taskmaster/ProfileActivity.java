package com.wangjessica.taskmaster;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
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
import java.util.HashMap;
import java.util.Iterator;

import yuku.ambilwarna.AmbilWarnaDialog;

public class ProfileActivity extends AppCompatActivity{

    // Input variables
    private Button colorButton;
    private EditText nameInput;
    private View colorPreview;
    private TextView coinsLabel;
    VectorChildFinder vector;

    private ImageView profileImg;
    int curColor = 0;
    private ArrayList<Integer> curColors;
    private int coinCnt;


    // Firebase variables
    private FirebaseAuth auth;
    private DatabaseReference rootRef;
    private DatabaseReference userRef;
    private DatabaseReference profileRef;
    private String userId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        AppCompatDelegate.setCompatVectorFromResourcesEnabled(true);
        setContentView(R.layout.activity_profile);

        // Instantiate firebase
        rootRef = FirebaseDatabase.getInstance().getReference();
        auth = FirebaseAuth.getInstance();
        userId = auth.getCurrentUser().getUid();
        userRef = rootRef.child("Users").child(userId);
        profileRef = userRef.child("Profile");

        // Instantiate layout components
        colorButton = findViewById(R.id.pick_color_button);
        nameInput = findViewById(R.id.name_field);
        colorPreview = findViewById(R.id.pick_color_view);
        coinsLabel = findViewById(R.id.coins_label);

        colorButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                openColorPicker();
            }
        });

        // Avatar vector
        profileImg = findViewById(R.id.profile_img);
        //avatarVector = (VectorMasterView) findViewById(R.id.profile_img);
        curColors = new ArrayList<Integer>();
        curColors.add(-1);
        curColors.add(-1);
        curColors.add(-1);
        curColors.add(-1);

        // Fill in the profile from saved information
        fillCurrent();
    }
    // Populate the profile entries with existing settings
    public void fillCurrent(){
        userRef.child("Profile").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                // User's coin count
                coinCnt = Integer.parseInt(snapshot.child("Coins").getValue().toString());
                coinsLabel.setText(""+coinCnt);
                // Avatar colors
                for(int i=0; i<4; i++){
                    curColors.set(i, Integer.parseInt(snapshot.child("Color "+i).getValue().toString()));
                }
                updateAvatarAll();
                // User's name
                String name = snapshot.child("Name").getValue().toString();
                nameInput.setText(name);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }
    // Picking colors
    public void openColorPicker(){
        final AmbilWarnaDialog colorPicker = new AmbilWarnaDialog(this, 0, new AmbilWarnaDialog.OnAmbilWarnaListener() {
            @Override
            public void onCancel(AmbilWarnaDialog dialog) {

            }

            @Override
            public void onOk(AmbilWarnaDialog dialog, int color) {
                curColor = color;
                colorPreview.setBackgroundColor(curColor);
            }
        });
        colorPicker.show();
    }

    // Updating avatar appearance
    public void updateAvatarAll(){ // All at once
        VectorChildFinder vector = new VectorChildFinder(this, R.drawable.avatar, profileImg);
        for(int i=0; i<curColors.size(); i++){
            VectorDrawableCompat.VFullPath path = vector.findPathByName("path"+i);
            path.setFillColor(curColors.get(i));
        }
    }
    public void updateAvatar(View view){ // When a path is clicked
        // Which part of avatar?
        Button clicked = (Button) view;
        String pathName = clicked.getTag().toString();
        // Store the changed color
        int idx = Integer.parseInt(pathName.substring(pathName.length()-1));
        curColors.set(idx, curColor);
        // Update the path colors
        updateAvatarAll();
    }
    public void saveUpdates(View view) {
        String name = nameInput.getText().toString();
        HashMap<String, Object> info = new HashMap<String, Object>();
        info.put("Name", name);
        for(int i=0; i<curColors.size(); i++){
            info.put("Color "+i, curColors.get(i));
        }
        info.put("Coins", coinCnt);
        profileRef.setValue(info);
    }
}