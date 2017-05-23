package com.sofi.knittimer.data;

// Knitting project class
public class Project {

    public int id;
    public String name;
    public int timeSpentInMillis;
    public int percentageDone;

    public Project(int id, String name, int timeSpentInMillis, int percentageDone) {
        this.id = id;
        this.name = name;
        this.timeSpentInMillis = timeSpentInMillis;
        this.percentageDone = percentageDone;
    }

    public int timeLeftInMillis() {
        if (percentageDone == 0) {
            return 0;
        } else {
            return Integer.parseInt(((100 - percentageDone) * (timeSpentInMillis / percentageDone)) + "");
        }
    }

}
