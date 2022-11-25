package com.wangjessica.taskmaster;

import com.google.firebase.database.DatabaseReference;

import java.util.ArrayList;

public class ToDo {

    // Basic info
    private String title;
    private String date; // date label - default = date created
    private ArrayList<String> tags;
    private int color;

    // Firebase
    private String todoKey;
    private DatabaseReference journalRef;

    // Constructor
    public ToDo(String title, String date, ArrayList<String> tags, String todoKey, int color){
        this.title = title;
        this.date = date;
        this.tags = tags;
        this.todoKey = todoKey;
        this.color = color;
    }

    // Getters and setters
    public String getTitle(){
        return title;
    }
    public String getDate(){
        return date;
    }
    public ArrayList<String> getTags(){
        return tags;
    }
    public String getTodoKey(){
        return todoKey;
    }
    public int getColor(){
        return color;
    }
}