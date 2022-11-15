package com.wangjessica.taskmaster;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.DialogFragment;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;

public class MainActivity extends AppCompatActivity implements CreateTodoDialogFragment.CreateTodoListener{

    // Layout variables
    RecyclerView recycler;
    RecyclerAdapter adapter;

    // Firebase variables
    private FirebaseAuth auth;
    private DatabaseReference rootRef;
    private DatabaseReference userRef;
    private String userId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Layout components
        recycler = findViewById(R.id.recycler_view);

        // Firebase info
        auth = FirebaseAuth.getInstance();
        userId = auth.getCurrentUser().getUid();
        rootRef = FirebaseDatabase.getInstance().getReference();
        userRef = rootRef.child("Users").child(userId);

        // Instantiate the journal cards
        showTodos();
    }
    public void showTodos(){
        // Get the recycler view
        RecyclerView view = findViewById(R.id.recycler_view);

        // Create the recycler adapter
        userRef.child("ToDos").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                // Create list of the user's current to-do lists
                List<ToDo> todoList = new ArrayList<ToDo>();
                todoList.add(new ToDo("Add New", "", new ArrayList<String>(), ""));

                // Add the journals to the list
                Iterator iterator = snapshot.getChildren().iterator();
                while(iterator.hasNext()){
                    DataSnapshot todo = (DataSnapshot) iterator.next();
                    // Get this to do's title and date
                    String curTitle = todo.child("Title").getValue().toString();
                    String curDate = todo.child("Date").getValue().toString();
                    // Get the tags
                    ArrayList<String> curTags = new ArrayList<String>();
                    getTags(todo.getKey(), curTags);
                    todoList.add(new ToDo(curTitle, curDate, curTags, snapshot.getKey()));
                }

                // Display journals on RecyclerView
                adapter = new RecyclerAdapter(todoList);
                for(ToDo todo: todoList)
                    System.out.println(todo.getTitle());
                RecyclerView.LayoutManager layoutManager = new GridLayoutManager(MainActivity.this, 3);
                recycler.setLayoutManager(layoutManager);
                adapter.setOnItemClickListener(new ClickListener<ToDo>() {
                    @Override
                    public void onItemClick(ToDo target) {
                        if(target.getTitle().equals("Add New")){
                            addTodo();
                        }
                        else{
                            showTodo(target);
                        }
                    }
                });
                recycler.setAdapter(adapter);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }
    public void getTags(String todoKey, ArrayList<String> tags){
        userRef.child("ToDos").child(todoKey).child("Tags").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                Iterator iterator = snapshot.getChildren().iterator();
                while(iterator.hasNext()){
                    tags.add(((DataSnapshot)iterator.next()).getValue().toString());
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    // Adding a new to-do list
    public void addTodo(){
        CreateTodoDialogFragment fragment = new CreateTodoDialogFragment();
        fragment.setCancelable(true);
        fragment.show(getSupportFragmentManager(), "Create ToDo");
    }

    // Going to a to-do list
    public void showTodo(ToDo target){
        System.out.println(target.getTags());
        Intent intent = new Intent(MainActivity.this, ToDoActivity.class);
        intent.putExtra("key", target.getTodoKey());
        startActivity(intent);
    }

    @Override
    public void onDialogPositiveClick(String title, String date, int color, ArrayList<String> tags) {
        // Error control - no name and/or date entered
        if(title==null || title.equals("")){
            title = "Journal";
        }
        if(date==null || date.equals("")){
            Date curDate = Calendar.getInstance().getTime();
            SimpleDateFormat dateFormat = new SimpleDateFormat("MM/dd/yy");
            date = dateFormat.format(curDate);
        }
        // Update the user's list of todos in Firebase
        String nextTodoKey = userRef.child("ToDos").push().getKey();
        HashMap<String, Object> info = new HashMap<String, Object>();
        info.put("Title", title);
        info.put("Date", date);
        info.put("Color", color);
        tags.add("Thing");
        tags.add("Thing2"); // TODO: Remove thing and thing2
        info.put("Tags", tags);
        userRef.child("ToDos").child(nextTodoKey).setValue(info);
        // Update the list of all to-do pages in Firebase
        HashMap<String, Object> pageInfo = new HashMap<String, Object>();
        pageInfo.put("Tasks", new ArrayList<String>());
        pageInfo.put("Times", new ArrayList<Double>());
        rootRef.child("TodoPages").child(nextTodoKey).setValue(pageInfo);
    }

    // Moving to other pages
    public void gotoTodos(){
        // don't do anything; already on the page
    }
    public void gotoGroups(){

    }
    public void gotoProfile(){

    }
}