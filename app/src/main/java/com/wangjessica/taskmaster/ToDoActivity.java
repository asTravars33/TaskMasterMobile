package com.wangjessica.taskmaster;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;

public class ToDoActivity extends AppCompatActivity implements AddTaskDialogFragment.AddTaskListener{

    // Firebase Variables
    private DatabaseReference rootRef;
    private DatabaseReference todoRef;
    private String todoKey;

    // Task and time information
    private ArrayList<String> tasks;
    private ArrayList<Double> times;

    // Layout variables
    Button addTaskButton;
    Button startQuestButton;

    @Override
    protected void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);
        setContentView(R.layout.todo_list);

        // Get the Firebase Database references
        rootRef = FirebaseDatabase.getInstance().getReference();
        Intent intent = getIntent();
        todoKey = intent.getStringExtra("key");
        todoRef = rootRef.child("TodoPages").child(todoKey);

        // Get the current tasks and times
        tasks = new ArrayList<String>();
        times = new ArrayList<Double>();
        instantiateTasksTimes();

        System.out.println(tasks);
        System.out.println(times);

        // Instantiate layout components
        addTaskButton = findViewById(R.id.add_task_button);
        startQuestButton = findViewById(R.id.start_quest_button);
        addTaskButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showAddTaskDialog();
            }
        });
    }

    // Get the existing list of tasks and times
    public void instantiateTasksTimes(){
        todoRef.child("Tasks").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                Iterator iterator = snapshot.getChildren().iterator();
                while(iterator.hasNext()){
                    DataSnapshot curTask = (DataSnapshot) iterator.next();
                    tasks.add(curTask.getValue().toString());
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
        todoRef.child("Times").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                Iterator iterator = snapshot.getChildren().iterator();
                while(iterator.hasNext()){
                    DataSnapshot curTime = (DataSnapshot) iterator.next();
                    times.add(Double.parseDouble(curTime.getValue().toString()));
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    // Adding a new task
    public void showAddTaskDialog(){
        AddTaskDialogFragment fragment = new AddTaskDialogFragment();
        fragment.setCancelable(true);
        fragment.show(getSupportFragmentManager(), "Create Task");
    }

    @Override
    public void onDialogPositiveClick(String name, double time) {
        // Update task and time lists
        tasks.add(name);
        times.add(time);

        // Push changes to Firebase
        HashMap<String, Object> info = new HashMap<String, Object>();
        info.put("Tasks", tasks);
        info.put("Times", times);
        todoRef.setValue(info);
    }
}