package com.sofi.knittimer.utils;

import com.sofi.knittimer.data.Project;

/**
 * Created by sofvanh on 23/02/18.
 */

public final class StringUtils {

    public static String createDetailsString(Project project) {
        if (project.timerRunning) {
            return "Working...";
        }

        if (project.percentageDone == 100) {
            return "100% done. Project finished! Yay!!";
        }

        long timeRemaining = project.timeLeftInMillis();

        if (timeRemaining == 0) { // Project hasn't been started (% is still set to 0)
            return "0% done. Let's get to work!";
        }

        long totalSeconds = timeRemaining / 1000;
        long totalMinutes = totalSeconds / 60;
        long littleMinutes = totalMinutes % 60; // total minutes - whole hours
        long totalHours = totalMinutes / 60;

        String details = project.percentageDone + "% done, " + totalHours;
        if (totalHours == 1) {
            details += " hour and ";
        } else {
            details += " hours and ";
        }
        details += littleMinutes;
        if (littleMinutes == 1) {
            details += " minute left";
        } else {
            details += " minutes left";
        }
        return details;
    }

    public static String createTimeString(Project project) {
        long timeSpent = project.timeSpentInMillis;
        long totalSeconds = timeSpent / 1000;
        long littleSeconds = totalSeconds % 60;
        long totalMinutes = totalSeconds / 60;
        long littleMinutes = totalMinutes % 60;
        long totalHours = totalMinutes / 60;

        String returnString;
        if (totalHours < 10) {
            returnString = "0" + totalHours + ":";
        } else {
            returnString = "" + totalHours + ":";
        }

        if (littleMinutes < 10) {
            returnString += "0" + littleMinutes + ":";
        } else {
            returnString += littleMinutes + ":";
        }

        if (littleSeconds < 10) {
            returnString += "0" + littleSeconds;
        } else {
            returnString += littleSeconds;
        }
        return returnString;
    }

}
