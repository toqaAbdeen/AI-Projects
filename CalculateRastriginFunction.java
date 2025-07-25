package finalSol;

import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.chart.LineChart;
import javafx.scene.chart.NumberAxis;
import javafx.scene.chart.XYChart;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.stage.Stage;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class CalculateRastriginFunction extends Application {

    // Constants
    final int DIMENSIONS = 15;
    final double MIN = -2.0;
    final double MAX = 2.0;
    final int MAX_ITERATIONS = 3000;

    // Formatting results
    static DecimalFormat df = new DecimalFormat("0.0000");

    // Random number generator
    Random rand = new Random();

    public static void main(String[] args) {
        launch(args);
    }

    @Override
    public void start(Stage stage) {
        // Run the simulated annealing optimization
        OptimizationResult result = optimize();

        // UI layout
        VBox root = new VBox(10);
        root.setStyle("-fx-padding: 10; -fx-background-color: #f5f5f5;");

        // Text area showing summary
        TextArea resultText = new TextArea(result.toString());
        resultText.setEditable(false);

        // Create charts
        LineChart<Number, Number> scoreChart = createChart("Score Over Time", "Iteration", "Score", result.scoreList, Color.MEDIUMBLUE);
        LineChart<Number, Number> distanceChart = createChart("Distance Over Time", "Iteration", "Distance", result.distanceList, Color.HOTPINK);
        LineChart<Number, Number> temperatureChart = createChart("Temperature Over Time", "Iteration", "Temperature", result.temperatureList, Color.GREENYELLOW);

        // Add everything to the screen
        root.getChildren().addAll(
                new Label("Simple Simulated Annealing on Rastrigin Function"),
                resultText,
                scoreChart,
                distanceChart,
                temperatureChart
        );

        stage.setScene(new Scene(root, 900, 1000));
        stage.setTitle("COMP338 Project - Rastrigin Optimizer");
        stage.show();
    }

    // Optimization logic
    private OptimizationResult optimize() {
        long startTime = System.currentTimeMillis(); // Start timing

        double[] current = getRandomSolution(); // Starting point
        double currentScore = evaluate(current);

        double[] best = current.clone();
        double bestScore = currentScore;

        // Simulated Annealing parameters
        double temp = 1000;
        double coolingRate = 0.95;
        double stepSize = 0.1;

        // Lists to store progress
        List<Double> scoreList = new ArrayList<>();
        List<Double> distanceList = new ArrayList<>();
        List<Double> temperatureList = new ArrayList<>();

        // Save initial values
        scoreList.add(currentScore);
        distanceList.add(distanceToZero(current));
        temperatureList.add(temp);

        for (int i = 1; i <= MAX_ITERATIONS; i++) {
            double[] neighbor = getNeighbor(current, stepSize);
            double neighborScore = evaluate(neighbor);

            double difference = neighborScore - currentScore;

            if (difference < 0 || Math.exp(-difference / temp) > rand.nextDouble()) {
                current = neighbor;
                currentScore = neighborScore;

                if (currentScore < bestScore) {
                    best = current.clone();
                    bestScore = currentScore;
                }
            }

            temp *= coolingRate;

            scoreList.add(bestScore);
            distanceList.add(distanceToZero(best));
            temperatureList.add(temp);
        }

        long endTime = System.currentTimeMillis(); // End timing
        long runtimeMillis = endTime - startTime;

        return new OptimizationResult(best, bestScore, scoreList, distanceList, temperatureList, runtimeMillis);
    }

    private double[] getRandomSolution() {
        double[] solution = new double[DIMENSIONS];
        for (int i = 0; i < DIMENSIONS; i++) {
            solution[i] = MIN + rand.nextDouble() * (MAX - MIN);
        }
        return solution;
    }

    private double[] getNeighbor(double[] solution, double step) {
        double[] neighbor = new double[DIMENSIONS];
        for (int i = 0; i < DIMENSIONS; i++) {
            double change = rand.nextGaussian() * step;
            neighbor[i] = clamp(solution[i] + change);
        }
        return neighbor;
    }

    private double clamp(double value) {
        return Math.max(MIN, Math.min(MAX, value));
    }

    private double evaluate(double[] x) {
        double sum = 10 * DIMENSIONS;
        for (int i = 0; i < DIMENSIONS; i++) {
            sum += x[i] * x[i] - 10 * Math.cos(2 * Math.PI * x[i]);
        }
        return sum;
    }

    private double distanceToZero(double[] x) {
        double sum = 0;
        for (int i = 0; i < DIMENSIONS; i++) {
            sum += x[i] * x[i];
        }
        return Math.sqrt(sum);
    }

    private LineChart<Number, Number> createChart(String title, String xLabel, String yLabel, List<Double> data, Color color) {
        NumberAxis xAxis = new NumberAxis();
        NumberAxis yAxis = new NumberAxis();
        xAxis.setLabel(xLabel);
        yAxis.setLabel(yLabel);

        LineChart<Number, Number> chart = new LineChart<>(xAxis, yAxis);
        chart.setTitle(title);
        chart.setLegendVisible(false);
        chart.setCreateSymbols(false);

        XYChart.Series<Number, Number> series = new XYChart.Series<>();
        for (int i = 0; i < data.size(); i++) {
            series.getData().add(new XYChart.Data<>(i, data.get(i)));
        }

        chart.getData().add(series);

        // Apply CSS color based on passed color
        String rgb = String.format("%d, %d, %d",
                (int) (color.getRed() * 255),
                (int) (color.getGreen() * 255),
                (int) (color.getBlue() * 255));
        series.getNode().lookup(".chart-series-line").setStyle("-fx-stroke: rgb(" + rgb + "); -fx-stroke-width: 2px;");

        return chart;
    }

    // Class to store results
    static class OptimizationResult {
        double[] bestSolution;
        double bestScore;
        List<Double> scoreList;
        List<Double> distanceList;
        List<Double> temperatureList;
        long runtimeMillis;

        public OptimizationResult(double[] solution, double score, List<Double> scores,
                                  List<Double> distances, List<Double> temps, long runtime) {
            bestSolution = solution;
            bestScore = score;
            scoreList = scores;
            distanceList = distances;
            temperatureList = temps;
            runtimeMillis = runtime;
        }

        @Override
        public String toString() {
            StringBuilder sb = new StringBuilder();
            sb.append("=== Optimization Result ===\n");
            sb.append("Final Score: ").append(df.format(bestScore)).append("\n");
            sb.append("Final Distance to Global Minimum: ").append(df.format(distanceList.get(distanceList.size() - 1))).append("\n");
            sb.append("Runtime: ").append(runtimeMillis).append(" ms\n\n");

            sb.append("Best Solution:\n");
            for (int i = 0; i < bestSolution.length; i++) {
                sb.append("x").append(i + 1).append(" = ").append(df.format(bestSolution[i])).append("\n");
            }

            sb.append("\nInitial Values:\n");
            sb.append("Initial Score: ").append(df.format(scoreList.get(0))).append("\n");
            sb.append("Initial Distance: ").append(df.format(distanceList.get(0))).append("\n");
            sb.append("Initial Temperature: ").append(df.format(temperatureList.get(0))).append("\n");

            return sb.toString();
        }
    }
}