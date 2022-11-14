package com.wangjessica.taskmaster;

import androidx.appcompat.app.AppCompatActivity;
import androidx.vectordrawable.graphics.drawable.VectorDrawableCompat;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.sdsmdg.harjot.vectormaster.VectorMasterView;
import com.sdsmdg.harjot.vectormaster.models.PathModel;

import java.util.ArrayList;
import java.util.HashMap;

import yuku.ambilwarna.AmbilWarnaDialog;

public class ProfileActivity extends AppCompatActivity{

    // Input variables
    private Button colorButton;
    private EditText nameInput;
    private View colorPreview;
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
        setContentView(R.layout.activity_main);

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

        // Avatar vectors
        VectorMasterView avatarVector = (VectorMasterView) findViewById(R.id.profile_img);
        for(int i=0; i<4; i++){
            PathModel path = avatarVector.getPathModelByName("path"+i);
            path.setStrokeColor(curColor); // TODO: Yes but do this on click! worst case scenario: make invisible buttons
        }

    }
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