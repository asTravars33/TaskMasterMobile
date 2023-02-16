package com.wangjessica.taskmaster;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;

import com.google.android.material.snackbar.Snackbar;

import java.util.ArrayList;

public class QuestMenuActivity extends AppCompatActivity {

    private ArrayList<String> tasks;
    private ArrayList<String> times;
    private ConstraintLayout layout;

    @Override
    protected void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_quest_menu);

        // Get the information passed in
        Intent intent = getIntent();
        tasks = intent.getStringArrayListExtra("tasks");
        times = intent.getStringArrayListExtra("times");
        layout = findViewById(R.id.layout);
    }
    // Moving to the quest types
    public void questPreset(View view){
        if(tasks==null || times==null){
            Snackbar snackbar = Snackbar.make(layout, "Please start quest from an open to-do list!", Snackbar.LENGTH_LONG);
            snackbar.show();
            return;
        }
        Intent intent = new Intent(QuestMenuActivity.this, SingleQuestActivity.class);
        intent.putStringArrayListExtra("tasks", tasks);
        intent.putStringArrayListExtra("times", times);
        intent.putExtra("type", "Random Preset");
        startActivity(intent);
    }
    public void questTemplate(View view){
        if(tasks==null || times==null){
            Snackbar snackbar = Snackbar.make(layout, "Please start quest from an open to-do list!", Snackbar.LENGTH_LONG);
            snackbar.show();
            return;
        }
        Intent intent = new Intent(QuestMenuActivity.this, SingleQuestActivity.class);
        intent.putStringArrayListExtra("tasks", tasks);
        intent.putStringArrayListExtra("times", times);
        intent.putExtra("type", "Template Quest");
        startActivity(intent);
    }
    public void questAI(View view){
        if(tasks==null || times==null){
            Snackbar snackbar = Snackbar.make(layout, "Please start quest from an open to-do list!", Snackbar.LENGTH_LONG);
            snackbar.show();
            return;
        }
        Intent intent = new Intent(QuestMenuActivity.this, SingleQuestActivity.class);
        intent.putStringArrayListExtra("tasks", tasks);
        intent.putStringArrayListExtra("times", times);
        intent.putExtra("type", "ML-Generated");
        startActivity(intent);
    }
}
