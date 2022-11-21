package com.wangjessica.taskmaster;

import android.os.Bundle;
import android.os.PersistableBundle;

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
import java.util.Iterator;
import java.util.List;

public class GroupsActivity extends AppCompatActivity {

    // Layout variables
    RecyclerView recycler;
    GroupsRecyclerAdapter adapter;

    // Firebase variables
    private FirebaseAuth auth;
    private DatabaseReference rootRef;
    private DatabaseReference groupsRef;
    private String userId;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState, @Nullable PersistableBundle persistentState) {
        super.onCreate(savedInstanceState, persistentState);

        // Layout components
        recycler = findViewById(R.id.recycler_view);

        // Firebase stuff
        auth = FirebaseAuth.getInstance();
        userId = auth.getCurrentUser().getUid();
        rootRef = FirebaseDatabase.getInstance().getReference();
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

                // Add journals
                Iterator iterator = snapshot.getChildren().iterator();
                while(iterator.hasNext()){
                    DataSnapshot group = (DataSnapshot) iterator.next();
                    // Get the group's information
                    String groupName = group.child("Name").getValue().toString();
                    String userName = group.child("User").getValue().toString();
                    int capacity = Integer.parseInt(group.child("Capacity").getValue().toString());
                    // Add in the group
                    groupsList.add(new Group(groupName, userName, snapshot.getKey(), capacity));
                }

                // Add to layout with adapter
                adapter = new GroupsRecyclerAdapter(groupsList);
                RecyclerView.LayoutManager layoutManager = new GridLayoutManager(GroupsActivity.this, 3);
                recycler.setLayoutManager(layoutManager);
                adapter.setOnItemClickListener(new ClickListener<Group>() {
                    @Override
                    public void onItemClick(Group target) {
                        gotoGroup(target);
                    }
                });
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }
    public void gotoGroup(Group group){
        // TODO: fill in this section
    }
}
