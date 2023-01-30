package com.wangjessica.taskmaster;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.database.ChildEventListener;
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
    private int lastAddedIdx = 0;
    private boolean submitted=false;

    // Layout variables
    Button addTaskButton;
    Button startQuestButton;
    LinearLayout todoPage;

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

        // Instantiate layout components
        todoPage = findViewById(R.id.todo_page);
        addTaskButton = findViewById(R.id.add_task_button);
        startQuestButton = findViewById(R.id.start_quest_button);
        addTaskButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showAddTaskDialog();
            }
        });

        // Display the todolist's title
        String title = intent.getStringExtra("title");
        TextView titleView = findViewById(R.id.title);
        titleView.setText(title);
    }

    // Get the existing list of tasks and times
    public void instantiateTasksTimes(){
        todoRef.child("Tasks").addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {
                tasks.add(snapshot.getValue().toString());
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
        todoRef.child("Times").addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {
                times.add(Double.parseDouble(snapshot.getValue().toString()));
                displayNewTaskTimes();
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

    // Starting a quest with the tasks
    public void startQuest(View view){
        Intent intent = new Intent(ToDoActivity.this, QuestMenuActivity.class);
        // Make String array version of times
        ArrayList<String> timesString = new ArrayList<String>();
        for(double time: times){
            timesString.add(""+time);
        }
        // Make intent
        intent.putStringArrayListExtra("tasks", tasks);
        intent.putStringArrayListExtra("times", timesString);
        // Move to next activity
        startActivity(intent);
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

        submitted = true;
    }

    // Displaying tasks and times
    public void displayNewTaskTimes(){
        // Remove extra
        if(submitted){
            times.remove(times.size()-1);
            tasks.remove(tasks.size()-1);
            submitted = false;
        }
        // Add the layouts of tasks not yet done
        for(int i=lastAddedIdx; i<times.size(); i++){
            displayNewTaskTime(i);
        }
        lastAddedIdx = times.size();
    }
    public void displayNewTaskTime(int idx){
        // Container for task and time
        LinearLayout ll = new LinearLayout(this);
        ll.setLayoutParams(new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));
        ll.setOrientation(LinearLayout.HORIZONTAL);
        ll.setWeightSum(1f);
        // Add task
        TextView taskTv = new TextView(this);
        LinearLayout.LayoutParams lp1 = new LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 0.6f);
        taskTv.setLayoutParams(lp1);
        taskTv.setText(tasks.get(idx));
        taskTv.setPadding(30, 10, 0, 10);
        taskTv.setTextColor(getResources().getColor(R.color.white, null));
        ll.addView(taskTv);
        // Add time
        TextView timeTv = new TextView(this);
        LinearLayout.LayoutParams lp2 = new LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 0.1f);
        timeTv.setLayoutParams(lp2);
        timeTv.setText(times.get(idx).toString());
        timeTv.setPadding(0, 10, 0, 10);
        timeTv.setTextColor(getResources().getColor(R.color.white, null));
        ll.addView(timeTv);
        // Add button controls
        Button delButton = new Button(this);
        delButton.setTag(idx);
        LinearLayout.LayoutParams buttonLayout = new LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 0.1f);
        delButton.setText("-");
        delButton.setLayoutParams(buttonLayout);
        delButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                delTask(view);
            }
        });
        ll.addView(delButton);
        // Up button
        Button upButton = new Button(this);
        upButton.setTag(idx);
        upButton.setText("U");
        upButton.setLayoutParams(buttonLayout);
        upButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                moveTaskUp(view);
            }
        });
        ll.addView(upButton);
        // Down button
        Button downButton = new Button(this);
        downButton.setTag(idx);
        downButton.setText("D");
        downButton.setLayoutParams(buttonLayout);
        downButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                moveTaskDown(view);
            }
        });
        ll.addView(downButton);
        // Add ll to main view
        todoPage.addView(ll);
    }

    // Rearranging tasks
    public void delTask(View view){
        // Update local arrayLists
        int idx = (Integer) view.getTag();
        tasks.remove(idx);
        times.remove(idx);
        // Update Firebase
        recalFirebase();
        // Update the layout
        lastAddedIdx = 0;
        todoPage.removeViews(1, todoPage.getChildCount()-1);
        displayNewTaskTimes();
    }
    public void moveTaskUp(View view){
        int idx = (Integer) view.getTag();
        swapTasks(idx, idx-1);
        recalFirebase();
    }
    public void moveTaskDown(View view){
        int idx = (Integer) view.getTag();
        swapTasks(idx, idx+1);
        recalFirebase();
    }
    public void swapTasks(int idx1, int idx2){
        // Error check
        if(idx1<0 || idx2 <0 || idx1>=tasks.size() || idx2>=tasks.size()){
            return;
        }
        // Swap the times
        String task1 = tasks.get(idx1);
        double time1 = times.get(idx1);
        tasks.set(idx1, tasks.get(idx2));
        times.set(idx1, times.get(idx2));
        tasks.set(idx2, task1);
        times.set(idx2, time1);
        System.out.println("After swap:");
        System.out.println(tasks);
        System.out.println(times);
        // Update the layout
        lastAddedIdx = 0;
        todoPage.removeViews(1, todoPage.getChildCount()-1);
        displayNewTaskTimes();
    }
    public void recalFirebase(){
        HashMap<String, Object> info = new HashMap<String, Object>();
        info.put("Tasks", tasks);
        info.put("Times", times);
        todoRef.setValue(info);
    }
}
