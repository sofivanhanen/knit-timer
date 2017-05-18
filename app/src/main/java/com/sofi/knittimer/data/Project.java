package com.sofi.knittimer.data;

// Knitting project class
public class Project {

    public int _id;
    public String _name;
    public int _timeSpentInMillis;
    public int _percentageDone;

    public Project(int id, String name, int timeSpentInMillis, int percentageDone) {
        _id = id;
        _name = name;
        _timeSpentInMillis = timeSpentInMillis;
        _percentageDone = percentageDone;
    }

    public int timeLeftInMillis() {
        return Integer.parseInt(((100 - _percentageDone) * (_timeSpentInMillis / _percentageDone)) + "");
    }

}
