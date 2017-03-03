package edu.dartmouth.cs.chrono;

import java.util.Calendar;

/**
 * Created by kelle on 3/2/2017.
 */

public class Task {

    private Long id;

    // Start and end times in millisecond
    private Long startTime;
    private Long deadline;

    // If split multiple times; default is 1
    private int blockID;

    // Task name
    private String taskName;

    // Int from 1 to 5, 5 is most urgent
    private int taskUrgency;

    // Int from 1 to 5, 5 is most important
    private int taskImportance;

    // Int in number of minutes
    private int duration;

    // 1 if task to be scheduled, 0 if event that cannot be moved
    private int taskType;

    public Task() {
        Calendar cal = Calendar.getInstance();
        startTime = cal.getTimeInMillis();
        deadline = new Long(-1);
        blockID = 0;
        taskName = "";
        taskUrgency = 1;
        taskImportance = 1;
        duration = 0;
        taskType = 1;
    }

    public Task(String _name, int _urgency, int _importance, int _duration) {
        Calendar cal = Calendar.getInstance();
        startTime = cal.getTimeInMillis();
        blockID = 0;
        taskName = _name;
        taskUrgency = _urgency;
        taskImportance = _importance;
        duration = _duration;
        taskType = 1;
    }

    public Long getStartTime() {
        return startTime;
    }

    public void setStartTime(Long startTime) {
        this.startTime = startTime;
    }

    public Long getDeadline() {
        return deadline;
    }

    public void setDeadline(Long deadline) {
        this.deadline = deadline;
    }

    public String getTaskName() {
        return taskName;
    }

    public void setTaskName(String taskName) {
        this.taskName = taskName;
    }

    public int getTaskUrgency() {
        return taskUrgency;
    }

    public void setTaskUrgency(int taskUrgency) {
        this.taskUrgency = taskUrgency;
    }

    public int getTaskImportance() {
        return taskImportance;
    }

    public void setTaskImportance(int taskImportance) {
        this.taskImportance = taskImportance;
    }

    public int getDuration() {
        return duration;
    }

    public void setDuration(int duration) {
        this.duration = duration;
    }

    public int getTaskType() {
        return taskType;
    }

    public void setTaskType(int taskType) {
        this.taskType = taskType;
    }

    public int getBlockID() {
        return blockID;
    }

    public void setBlockID(int blockID) {
        this.blockID = blockID;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }
}
