package com.wangjessica.taskmaster;

public class Group {
    // Basic info
    private String title;
    private String userName;
    private int capacity;

    // Firebase
    private String groupKey;

    // Constructor
    public Group(String title, String userName, String groupKey, int capacity){
        this.title = title;
        this.userName = userName;
        this.groupKey = groupKey;
        this.capacity = capacity;
    }

    // Getters and setters
    public String getTitle(){
        return title;
    }
    public String getUserName(){
        return userName;
    }
    public String getGroupKey(){
        return groupKey;
    }
    public int getCapacity(){
        return capacity;
    }

}
