package application;

import java.util.*;

public class Evaluator {

    public static double accuracy(List<String> predicted, List<String> actual) {
        int correct = 0;
        for (int i = 0; i < predicted.size(); i++) {
            if (predicted.get(i).equals(actual.get(i))) correct++;
        }
        return (double) correct / predicted.size();
    }

    public static double precision(List<String> predicted, List<String> actual, String targetClass) {
        int tp = 0, fp = 0;
        for (int i = 0; i < predicted.size(); i++) {
            if (predicted.get(i).equals(targetClass)) {
                if (actual.get(i).equals(targetClass)) tp++;
                else fp++;
            }
        }
        return tp + fp == 0 ? 0 : (double) tp / (tp + fp);
    }

    public static double recall(List<String> predicted, List<String> actual, String targetClass) {
        int tp = 0, fn = 0;
        for (int i = 0; i < predicted.size(); i++) {
            if (actual.get(i).equals(targetClass)) {
                if (predicted.get(i).equals(targetClass)) tp++;
                else fn++;
            }
        }
        return tp + fn == 0 ? 0 : (double) tp / (tp + fn);
    }
}
