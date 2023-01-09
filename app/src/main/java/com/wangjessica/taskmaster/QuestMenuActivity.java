package com.wangjessica.taskmaster;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;

import androidx.appcompat.app.AppCompatActivity;

import java.util.ArrayList;

public class QuestMenuActivity extends AppCompatActivity {

    ArrayList<String> tasks;
    ArrayList<String> times;

    @Override
    protected void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_quest_menu);

        // Get the information passed in
        Intent intent = getIntent();
        tasks = intent.getStringArrayListExtra("tasks");
        times = intent.getStringArrayListExtra("times");
    }
    // Moving to the quest types
    public void questPreset(View view){
        Intent intent = new Intent(QuestMenuActivity.this, SingleQuestActivity.class);
        intent.putStringArrayListExtra("tasks", tasks);
        intent.putStringArrayListExtra("times", times);
        intent.putExtra("type", "Random Preset");
        startActivity(intent);
    }
    public void questTemplate(View view){
        Intent intent = new Intent(QuestMenuActivity.this, SingleQuestActivity.class);
        intent.putStringArrayListExtra("tasks", tasks);
        intent.putStringArrayListExtra("times", times);
        intent.putExtra("type", "Template Quest");
        startActivity(intent);
    }
    public void questAI(View view){
        Intent intent = new Intent(QuestMenuActivity.this, SingleQuestActivity.class);
        intent.putStringArrayListExtra("tasks", tasks);
        intent.putStringArrayListExtra("times", times);
        intent.putExtra("type", "ML-Generated");
        startActivity(intent);
    }
}
