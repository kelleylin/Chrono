package edu.dartmouth.cs.chrono;

import android.util.Log;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;

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

        Calendar calendar = Calendar.getInstance();

        int completeCount, overOneHour, countBreaks, countOverlaps;
        double averageExtraTime, totalExtraTime, priorityScore, continuousTime, averageBreak, longestContinuous, score;
        boolean sleep, meals, timeTravel;
        double scoreMultiplier = 10.0;

        ArrayList<Double> beforeDeadlineList = new ArrayList<>();
        ArrayList<Double> breakList = new ArrayList<>();
        ArrayList<Double> continuousWorkingList = new ArrayList<>();

        completeCount = overOneHour = countBreaks = countOverlaps = 0;
        averageExtraTime = totalExtraTime = priorityScore = continuousTime = averageBreak = longestContinuous = score = 0.0;
        sleep = meals = timeTravel = false;

        if (entries != null) {
            for (int i = 0; i < entries.size(); i++) {

                Task currentTask = entries.get(i);

                if (currentTask.getStartTime() < calendar.getTimeInMillis())
                    timeTravel = true;

                if (currentTask.getStartTime() + (currentTask.getDuration() * 60000.0) <= currentTask.getDeadline())
                    completeCount++;

                double timeBeforeDeadline = (currentTask.getDeadline() - (currentTask.getStartTime() + (currentTask.getDuration() * 60000.0))) / 60000.0;

                if (timeBeforeDeadline >= 0) {
                    beforeDeadlineList.add(timeBeforeDeadline);
                    averageExtraTime += timeBeforeDeadline;
                }

                priorityScore += currentTask.getTaskImportance() * timeBeforeDeadline * scoreMultiplier / 10.0;

                if (i == 0)
                    continuousTime += (currentTask.getDuration());

                if ((i > 0) && (currentTask.getStartTime() <= entries.get(i - 1).getStartTime() + (entries.get(i - 1).getDuration() * 60000.0)))
                    continuousTime += (currentTask.getDuration());

                if (continuousTime > longestContinuous)
                    longestContinuous = continuousTime;

                if (((continuousTime / 60.0) > 1.0) || (currentTask.getTaskName().equals("Break"))) {
                    if ((continuousTime / 60.0) > 1.0) {
                        overOneHour++;
                    }
                    continuousWorkingList.add(continuousTime);
                    continuousTime = 0.0;
                }

                if (currentTask.getTaskName().equals("Break")){
                    averageBreak += currentTask.getDuration();
                    countBreaks++;
                    breakList.add((double) currentTask.getDuration());
                }

                for (int j = 0; j < entries.size(); j++) {
                    if ((i != j) && (entries.get(i).getStartTime() > entries.get(j).getStartTime())
                            && (entries.get(i).getStartTime() < (entries.get(j).getStartTime() + (entries.get(j).getDuration() * 60000.0)))) {
                        countOverlaps++;
                    }
                }
            }
            totalExtraTime = averageExtraTime;
            averageExtraTime = averageExtraTime / entries.size();
            if (countBreaks > 0)
                averageBreak = averageBreak / countBreaks;

            // Calculate the total score
            score = (completeCount * scoreMultiplier * scoreMultiplier) + (averageExtraTime * scoreMultiplier)
                    + priorityScore - (overOneHour * scoreMultiplier / 2.5) + (averageBreak * scoreMultiplier / 5.0)
                    + (scoreMultiplier / (1 + standardDeviation(beforeDeadlineList) + standardDeviation(breakList)
                    + standardDeviation(continuousWorkingList))) - (longestContinuous * scoreMultiplier / 10.0)
                    + (totalExtraTime * scoreMultiplier / 20.0) - (countOverlaps * scoreMultiplier * scoreMultiplier);

            if (timeTravel == true)
                if (score > 0)
                    score *= -1.0;
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
        Log.d("SCORE", "Overlaps: " + countOverlaps);
        */

        return -score;
    }

    public static void optimize(ArrayList<Task> entries) {
        if (entries.size() < 1) {
            return;
        }

        ArrayList<Task> testTasks = new ArrayList<>(entries);
        ArrayList<Long> x0 = new ArrayList<>();
        ArrayList<ArrayList<Long>> simplex = new ArrayList<>();

        for (int i = 0; i < entries.size(); i++) {
            x0.add(entries.get(i).getStartTime());
        }

        simplex.add(x0);

        for (int j = 0; j < entries.size(); j++) {
            simplex.add(new ArrayList<>(x0));
        }

        for (int k = 1; k < simplex.size(); k++) {
            long val = (long) (simplex.get(k).get(k-1)*1.0000005);
            simplex.get(k).set(k-1, val);
        }

        Log.d("OPTIMIZE", "Simplex size: " + simplex.size());
        for (int l = 0; l < simplex.size(); l++) {
            Log.d("OPTIMIZE", "Simplex " + l + " :" + simplex.get(l).toString());
        }

        int n = simplex.size();
        ArrayList<Double> simplexScores = scoreSimplex(entries, simplex);

        // while some terminating condition isn't met yet

        int worstIndex = maxArrayIndex(simplexScores);
        int bestIndex = minArrayIndex(simplexScores);
        Log.d("OPTIMIZE", "Worst simplex index: " + worstIndex);
        ArrayList<Long> r = new ArrayList<>(Collections.nCopies(simplex.get(worstIndex).size(), 0L));
        ArrayList<Long> m = new ArrayList<>(Collections.nCopies(simplex.get(worstIndex).size(), 0L));
        double rScore, x_1Score, x_nScore;

        // calculate reflected point r and average point m
        for (int x = 0; x < n; x++) {
            if (x != worstIndex) {
                for (int y = 0; y < simplex.get(x).size(); y++) {
                    long temp = r.get(y) + simplex.get(x).get(y);
                    r.set(y, temp);
                }
            }
        }

        for (int z = 0; z < r.size(); z++) {
            long avg = r.get(z) / (n - 1);
            long reflect = (2*avg) - simplex.get(worstIndex).get(z);
            r.set(z, reflect);
            m.set(z, avg);
            testTasks.get(z).setStartTime(r.get(z));
        }

        rScore = scoreSchedule(testTasks);
        x_1Score = simplexScores.get(bestIndex);
        x_nScore = simplexScores.get(maxArrayIndex2(simplexScores, worstIndex));

        Log.d("OPTIMIZE", "r score: " + rScore + " x1 score: " + x_1Score + " xn score: " + x_nScore);
        //Log.d("OPTIMIZE", "r: " + r.toString());
        //Log.d("OPTIMIZE", "m: " + m.toString());

        if ((rScore < x_nScore) && (x_1Score <= rScore)) {
            Log.d("OPTIMIZE", "REFLECT");
            simplex.set(worstIndex, r);
            //continue;
        }

        // calculate expansion point s
        if (rScore < x_1Score) {
            ArrayList<Long> s = new ArrayList<>(Collections.nCopies(simplex.get(worstIndex).size(), 0L));

            for (int d = 0; d < m.size(); d++) {
                long exp = m.get(d) + 2*(m.get(d) - simplex.get(worstIndex).get(d));
                s.set(d, exp);
                testTasks.get(d).setStartTime(s.get(d));
            }

            double sScore = scoreSchedule(testTasks);
            Log.d("OPTIMIZE", "s score: " + sScore);
            Log.d("OPTIMIZE", "s: " + s.toString());

            if (sScore < rScore) {
                Log.d("OPTIMIZE", "EXPAND");
                simplex.set(worstIndex, s);
                //continue;
            }
            else {
                Log.d("OPTIMIZE", "REFLECT");
                simplex.set(worstIndex, r);
                //continue;
            }
        }

        // calculate contraction points c and cc
        if (rScore >= x_nScore) {
            if (rScore < simplexScores.get(worstIndex)) {
                ArrayList<Long> c = new ArrayList<>(Collections.nCopies(simplex.get(worstIndex).size(), 0L));

                for (int e = 0; e < c.size(); e++) {
                    long contractOut = m.get(e) + ((r.get(e) - m.get(e)) / 2);
                    c.set(e, contractOut);
                    testTasks.get(e).setStartTime(c.get(e));
                }
                double cScore = scoreSchedule(testTasks);
                if (cScore < rScore) {
                    Log.d("OPTIMIZE", "CONTRACT OUTSIDE");
                    simplex.set(worstIndex, c);
                    //continue;
                }
            }

            if (rScore >= simplexScores.get(worstIndex)) {
                ArrayList<Long> cc = new ArrayList<>(Collections.nCopies(simplex.get(worstIndex).size(), 0L));

                for (int f = 0; f < cc.size(); f++) {
                    long contractIn = m.get(f) + ((simplex.get(worstIndex).get(f) - m.get(f)) / 2);
                    cc.set(f, contractIn);
                    testTasks.get(f).setStartTime(cc.get(f));
                }
                double ccScore = scoreSchedule(testTasks);
                if (ccScore < rScore) {
                    Log.d("OPTIMIZE", "CONTRACT IN");
                    simplex.set(worstIndex, cc);
                    //continue;
                }
            }
        }

        // calculate shrink points v
        ArrayList<Long> x1 = simplex.get(bestIndex);
        for (int g = 0; g < simplex.size(); g++) {
            if (g != bestIndex) {
                ArrayList<Long> v = new ArrayList<>(Collections.nCopies(simplex.get(bestIndex).size(), 0L));

                for (int h = 0; h < v.size(); h++) {
                    long shrinkVal = x1.get(h) + ((simplex.get(g).get(h) - x1.get(h)) / 2);
                    v.set(h, shrinkVal);
                }
                simplex.set(g, v);
            }
        }
    }

    public static ArrayList<Double> scoreSimplex(ArrayList<Task> entries, ArrayList<ArrayList<Long>> simplex) {
        ArrayList<Task> testTasks = new ArrayList<>(entries);
        ArrayList<Double> simplexScores = new ArrayList<>();

        for (int i = 0; i < simplex.size(); i++) {

            ArrayList<Long> schedule = simplex.get(i);

            for (int j = 0; j < schedule.size(); j++) {
                testTasks.get(j).setStartTime(schedule.get(j));
            }

            simplexScores.add(scoreSchedule(testTasks));
        }

        Log.d("OPTIMIZE", "Simplex scores: " + simplexScores.toString());
        return simplexScores;
    }

    public static int maxArrayIndex (ArrayList<Double> myList) {
        int maxIndex = -1;
        double maxVal = Double.NEGATIVE_INFINITY;

        for (int i = 0; i < myList.size(); i++) {
            if (myList.get(i) > maxVal) {
                maxIndex = i;
                maxVal = myList.get(i);
            }
        }

        return maxIndex;
    }

    public static int minArrayIndex (ArrayList<Double> myList) {
        int minIndex = -1;
        double minVal = Double.POSITIVE_INFINITY;

        for (int i = 0; i < myList.size(); i++) {
            if (myList.get(i) < minVal) {
                minIndex = i;
                minVal = myList.get(i);
            }
        }

        return minIndex;
    }

    public static int maxArrayIndex2 (ArrayList<Double> myList, int exclude) {
        int maxIndex = -1;
        double maxVal = Double.NEGATIVE_INFINITY;

        for (int i = 0; i < myList.size(); i++) {
            if ((myList.get(i) > maxVal) && (i != exclude)) {
                maxIndex = i;
                maxVal = myList.get(i);
            }
        }

        return maxIndex;
    }
}
