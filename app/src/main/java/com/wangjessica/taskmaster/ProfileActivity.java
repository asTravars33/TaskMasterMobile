package com.wangjessica.taskmaster;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;

import com.devs.vectorchildfinder.VectorChildFinder;
import com.devs.vectorchildfinder.VectorDrawableCompat;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.ArrayList;
import java.util.HashMap;

import yuku.ambilwarna.AmbilWarnaDialog;

public class ProfileActivity extends AppCompatActivity{

    // Input variables
    private Button colorButton;
    private EditText nameInput;
    private View colorPreview;
    VectorChildFinder vector;

    private ImageView profileImg;
    int curColor = 0;
    private ArrayList<Integer> curColors;

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
    }
    // Populate the profile entries with existing settings
    public void fillCurrent(){

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
    public void updateAvatar(View view){
        // Which part of avatar?
        Button clicked = (Button) view;
        String pathName = clicked.getTag().toString();
        // Store the changed color
        int idx = Integer.parseInt(pathName.substring(pathName.length()-1, pathName.length()));
        curColors.set(idx, curColor);
        // Update the path colors
        VectorChildFinder vector = new VectorChildFinder(this, R.drawable.avatar, profileImg);
        for(int i=0; i<curColors.size(); i++){
            VectorDrawableCompat.VFullPath path = vector.findPathByName("path"+i);
            path.setFillColor(curColors.get(i));
        }
    }
    public void saveUpdates(View view) {
        String name = nameInput.getText().toString();
        HashMap<String, Object> info = new HashMap<String, Object>();
        info.put("Name", name);
        for(int i=0; i<curColors.size(); i++){
            info.put("Color "+i, curColors.get(i));
        }
        profileRef.setValue(info);
    }
}