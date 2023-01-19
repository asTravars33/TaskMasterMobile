package com.wangjessica.taskmaster;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import com.google.firebase.auth.FirebaseAuth;
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
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;

public class MainActivity extends AppCompatActivity implements CreateTodoDialogFragment.CreateTodoListener{

    // Layout variables
    RecyclerView recycler;
    RecyclerAdapter adapter;
    TextView tagSelection;
    EditText dateFilter;
    Button applyDateButton;

    // Firebase variables
    private FirebaseAuth auth;
    private DatabaseReference rootRef;
    private DatabaseReference userRef;
    private String userId;

    // Internal storage
    List<ToDo> todoList;
    Set<String> allTags;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Layout components
        recycler = findViewById(R.id.recycler_view);
        tagSelection = findViewById(R.id.tag_selection);
        applyDateButton = findViewById(R.id.apply_date_button);
        dateFilter = findViewById(R.id.date_filter);
        applyDateButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                applyDate();
            }
        });
        allTags = new HashSet<String>();

        // Firebase info
        auth = FirebaseAuth.getInstance();
        userId = auth.getCurrentUser().getUid();
        rootRef = FirebaseDatabase.getInstance().getReference();
        userRef = rootRef.child("Users").child(userId);

        // Instantiate the journal cards
        showTodos();
        setUpTagSelection();
    }
    public void showTodos(){
        // Get the recycler view
        RecyclerView view = findViewById(R.id.recycler_view);

        // Create the recycler adapter
        userRef.child("ToDos").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                // Create list of the user's current to-do lists
                todoList = new ArrayList<ToDo>();
                todoList.add(new ToDo("Add New", "", new ArrayList<String>(), "", -1));

                // Add the journals to the list
                Iterator iterator = snapshot.getChildren().iterator();
                while(iterator.hasNext()){
                    DataSnapshot todo = (DataSnapshot) iterator.next();
                    // Get this to do's title and date
                    String curTitle = todo.child("Title").getValue().toString();
                    String curDate = todo.child("Date").getValue().toString();
                    int curColor = Integer.parseInt(todo.child("Color").getValue().toString());
                    // Get the tags
                    ArrayList<String> curTags = new ArrayList<String>();
                    getTags(todo.getKey(), curTags);
                    todoList.add(new ToDo(curTitle, curDate, curTags, todo.getKey(), curColor));
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
    public void applyDate(){
        // Get the inputted date
        String date = dateFilter.getText().toString();
        // Is the date blank?
        if(date.equals("")){
            adapter = new RecyclerAdapter(todoList);
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
        // Change the todoLists shown
        ArrayList<ToDo> filteredTodos = new ArrayList<ToDo>();
        for(ToDo todo: todoList){
            if(todo.getDate().equals(date)){
                filteredTodos.add(todo);
            }
        }
        // Update the adapter
        adapter = new RecyclerAdapter(filteredTodos);
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
    public void getTags(String todoKey, ArrayList<String> tags){
        userRef.child("ToDos").child(todoKey).child("Tags").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                Iterator iterator = snapshot.getChildren().iterator();
                while(iterator.hasNext()){
                    tags.add(((DataSnapshot)iterator.next()).getValue().toString());
                    allTags.add(tags.get(tags.size()-1));
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }
    public void setUpTagSelection(){
        // Set up the dialogue box and onClick listener
        tagSelection.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // Get all the tags
                Set<String> allTags = new HashSet<String>();
                for(ToDo card: todoList){
                    ArrayList<String> thisTags = card.getTags();
                    for(String tag: thisTags){
                        allTags.add(tag);
                    }
                }
                String[] curTags = new String[allTags.size()];
                int idx = 0;
                for(String tag: allTags){
                    curTags[idx++] = tag;
                }
                Set<String> selectedTags = new TreeSet<String>();
                boolean[] selectedTag = new boolean[curTags.length];
                // Create the builder
                AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
                builder.setTitle("Select Tags");
                builder.setCancelable(false);
                builder.setMultiChoiceItems(curTags, selectedTag, new DialogInterface.OnMultiChoiceClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i, boolean b) {
                        if(b){
                            // Checked - add to set of selected tags
                            selectedTags.add(curTags[i]);
                        }
                        else{
                            // Not checked
                            selectedTags.remove(curTags[i]);
                        }
                    }
                });
                builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        // Create the display of all selected tags
                        StringBuilder sb = new StringBuilder();
                        for(String tag: selectedTags){
                            sb.append(tag+", ");
                        }
                        tagSelection.setText(sb.substring(0, sb.length()-2));
                        // Change the todoLists shown
                        ArrayList<ToDo> filteredTodos = new ArrayList<ToDo>();
                        for(ToDo todo: todoList){
                            boolean for_display = false;
                            for(String tag: todo.getTags()){
                                if(selectedTags.contains(tag)){
                                    for_display = true;
                                    break;
                                }
                            }
                            if(for_display){
                                filteredTodos.add(todo);
                            }
                        }
                        // Update the adapter
                        adapter = new RecyclerAdapter(filteredTodos);
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
                });
                builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        dialogInterface.dismiss();
                    }
                });
                builder.setNeutralButton("Clear All", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        for(int k=0; k<selectedTag.length; k++){
                            selectedTag[k] = false;
                            selectedTags.clear();
                            tagSelection.setText("");
                        }
                        // Update the adapter
                        adapter = new RecyclerAdapter(todoList);
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
                });
                builder.show();
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
        System.out.println(tags);
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
        System.out.println("Next todo key: "+nextTodoKey);
        HashMap<String, Object> info = new HashMap<String, Object>();
        info.put("Title", title);
        info.put("Date", date);
        info.put("Color", color);
        info.put("Tags", tags);
        userRef.child("ToDos").child(nextTodoKey).setValue(info);
        // Update the list of all to-do pages in Firebase
        HashMap<String, Object> pageInfo = new HashMap<String, Object>();
        pageInfo.put("Tasks", new ArrayList<String>());
        pageInfo.put("Times", new ArrayList<Double>());
        System.out.println("Next todo key, again: "+nextTodoKey);
        rootRef.child("TodoPages").child(nextTodoKey).setValue(pageInfo);
        System.out.println("Next todo key, for the third time: "+nextTodoKey);
        System.out.println("Theoretical reference to todo page: "+rootRef.child("TodoPages").child(nextTodoKey));
    }

    // Moving to other pages
    public void gotoTodos(View view){
        // don't do anything; already on the page
    }
    public void gotoGroups(View view){
        Intent intent = new Intent(MainActivity.this, GroupsActivity.class);
        System.out.println("Starting Groups intent");
        startActivity(intent);
    }
    public void gotoProfile(View view){
        Intent intent = new Intent(MainActivity.this, ProfileActivity.class);
        startActivity(intent);
    }
}