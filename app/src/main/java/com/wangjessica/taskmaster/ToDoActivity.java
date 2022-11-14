package com.wangjessica.taskmaster;

import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.ArrayList;

public class ToDoActivity extends AppCompatActivity {

    // Firebase Variables
    private DatabaseReference rootRef;
    private DatabaseReference todoRef;

    // Task and time information
    private ArrayList<String> tasks;
    private ArrayList<Double> times;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.todo_list);

        // Get the Firebase Database references
        rootRef = FirebaseDatabase.getInstance().getReference();
        todoRef = rootRef.child("TodoPages");


    }
}