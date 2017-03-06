package edu.dartmouth.cs.chrono;

import android.util.Log;

import java.util.ArrayList;

/**
 * Created by Bill on 3/5/2017.
 */

public class ScoreFunction {
    public static double standardDeviation(ArrayList<Double> list) {
        if (list.size() == 0)
            return 0;

        double mean = 0;
        for (int i = 0; i < list.size(); i++) {
            mean += list.get(i);
        }
        mean = mean / list.size();
        double variance = 0;

        for (int j = 0; j < list.size(); j++) {
            double res = list.get(j) - mean;
            variance = variance + (res * res);
        }
        variance = variance / list.size();

        return Math.sqrt(variance);
    }

    public static double scoreSchedule(ArrayList<Task> entries) {
        Log.d("SCORE", "Computing score of schedule: " + entries.size() + " tasks");

        int completeCount, overOneHour, countBreaks;
        double averageExtraTime, totalExtraTime, priorityScore, continuousTime, averageBreak, longestContinuous, score;
        boolean sleep, meals;
        double scoreMultiplier = 1000.0;

        ArrayList<Double> beforeDeadlineList = new ArrayList<>();
        ArrayList<Double> breakList = new ArrayList<>();
        ArrayList<Double> continuousWorkingList = new ArrayList<>();

        completeCount = overOneHour = countBreaks = 0;
        averageExtraTime = totalExtraTime = priorityScore = continuousTime = averageBreak = longestContinuous = score = 0.0;
        sleep = meals = false;

        if (entries != null) {
            for (int i = 0; i < entries.size(); i++) {

                Task currentTask = entries.get(i);

                if (currentTask.getStartTime() + (currentTask.getDuration() * 60000.0) <= currentTask.getDeadline())
                    completeCount++;

                double timeBeforeDeadline = (currentTask.getDeadline() - (currentTask.getStartTime() + (currentTask.getDuration() * 60000.0)));

                if (timeBeforeDeadline >= 0) {
                    beforeDeadlineList.add(timeBeforeDeadline);
                    averageExtraTime += timeBeforeDeadline;
                }

                priorityScore += currentTask.getTaskImportance() * timeBeforeDeadline * scoreMultiplier / 10.0;

                if (i == 0)
                    continuousTime += (currentTask.getDuration() * 60000.0);

                if ((i > 0) && (currentTask.getStartTime() <= entries.get(i - 1).getStartTime() + (entries.get(i - 1).getDuration() * 60000.0)))
                    continuousTime += (currentTask.getDuration() * 60000.0);

                if (continuousTime > longestContinuous)
                    longestContinuous = continuousTime;

                if (((continuousTime / 3600000.0) > 1.0) || (currentTask.getTaskName().equals("Break"))) {
                    if ((continuousTime / 3600000.0) > 1.0) {
                        overOneHour++;
                    }
                    continuousWorkingList.add(continuousTime);
                    continuousTime = 0.0;
                }

                if (currentTask.getTaskName().equals("Break")){
                    averageBreak += currentTask.getDuration();
                    countBreaks ++;
                    breakList.add((double) currentTask.getDuration());
                }
            }
            totalExtraTime = averageExtraTime;
            averageExtraTime = averageExtraTime / entries.size();
            if (countBreaks > 0)
                averageBreak = averageBreak / countBreaks;

            // Calculate the total score
            score = (completeCount * scoreMultiplier) + (averageExtraTime * scoreMultiplier)
                    + priorityScore + (overOneHour * scoreMultiplier / 2.5) + (averageBreak * scoreMultiplier / 5.0)
                    + (scoreMultiplier / (1 + standardDeviation(beforeDeadlineList) + standardDeviation(breakList)
                    + standardDeviation(continuousWorkingList))) + (longestContinuous * scoreMultiplier / 10.0)
                    + (totalExtraTime * scoreMultiplier / 20.0);
        }

        /*
        Log.d("TEST", ""+completeCount);
        Log.d("TEST", ""+overOneHour);
        Log.d("TEST", ""+countBreaks);
        Log.d("TEST", ""+averageExtraTime);
        Log.d("TEST", ""+totalExtraTime);
        Log.d("TEST", ""+priorityScore);
        Log.d("TEST", ""+continuousTime);
        Log.d("TEST", ""+averageBreak);
        Log.d("TEST", ""+longestContinuous);
        */

        return score / (scoreMultiplier * scoreMultiplier);
    }
}
