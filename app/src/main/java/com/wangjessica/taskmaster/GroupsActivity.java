package com.wangjessica.taskmaster;

import android.content.Intent;
import android.os.Bundle;
import android.os.PersistableBundle;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

public class GroupsActivity extends AppCompatActivity implements CreateGroupDialogFragment.CreateGroupListener{

    // Layout variables
    RecyclerView recycler;
    GroupsRecyclerAdapter adapter;

    // Firebase variables
    private FirebaseAuth auth;
    private DatabaseReference rootRef;
    private DatabaseReference groupsRef;
    private String userName;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_groups);
        System.out.println("On create");

        // Layout components
        recycler = findViewById(R.id.recycler_view);

        // Firebase stuff
        auth = FirebaseAuth.getInstance();
        String userId = auth.getCurrentUser().getUid();
        rootRef = FirebaseDatabase.getInstance().getReference();
        rootRef.child("Users").child(userId).child("Profile").child("Name").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                userName = snapshot.getValue().toString();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
        groupsRef = rootRef.child("Groups");

        // Instantiate the groups
        showGroups();
    }
    public void showGroups(){
        groupsRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                // Create list of the groups
                List<Group> groupsList = new ArrayList<Group>();
                groupsList.add(new Group("Add Group", "", "", -1, -1));

                // Add journals
                Iterator iterator = snapshot.getChildren().iterator();
                while(iterator.hasNext()){
                    DataSnapshot group = (DataSnapshot) iterator.next();
                    // Get the group's information
                    String groupName = group.child("Name").getValue().toString();
                    String userName = group.child("User").getValue().toString();
                    int capacity = Integer.parseInt(group.child("Capacity").getValue().toString());
                    int color = Integer.parseInt(group.child("Color").getValue().toString());
                    // Add in the group
                    groupsList.add(new Group(groupName, userName, snapshot.getKey(), capacity, color));
                }

                // Add to layout with adapter
                adapter = new GroupsRecyclerAdapter(groupsList);
                RecyclerView.LayoutManager layoutManager = new GridLayoutManager(GroupsActivity.this, 3);
                recycler.setLayoutManager(layoutManager);
                adapter.setOnItemClickListener(new ClickListener<Group>() {
                    @Override
                    public void onItemClick(Group target) {
                        if(target.getTitle().equals("Add Group")){
                            makeGroup();
                        }
                        gotoGroup(target);
                    }
                });
                recycler.setAdapter(adapter);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }
    // Creating a new group
    public void makeGroup(){
        CreateGroupDialogFragment fragment = new CreateGroupDialogFragment();
        fragment.setCancelable(true);
        fragment.show(getSupportFragmentManager(), "New Group");
    }
    @Override
    public void onDialogPositiveClick(String title, int capacity, int color) {
        // Create a new group
        String nextGroupKey = groupsRef.push().getKey();
        // Add the group to Firebase
        Map<String, Object> info = new HashMap<String, Object>();
        info.put("Name", title);
        info.put("User", userName);
        info.put("Capacity", capacity);
        info.put("Color", color);
        groupsRef.child(nextGroupKey).setValue(info);
    }

    // Entering a created group
    public void gotoGroup(Group group){
        // TODO: fill in this section
    }

    // Moving to other pages
    public void gotoTodos(View view){
        Intent intent = new Intent(GroupsActivity.this, MainActivity.class);
        startActivity(intent);
    }
    public void gotoGroups(View view){
        // Don't do anything; already on the page
    }
    public void gotoProfile(View view){
        Intent intent = new Intent(GroupsActivity.this, ProfileActivity.class);
        startActivity(intent);
    }
}
