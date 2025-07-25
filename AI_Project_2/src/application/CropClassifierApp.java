package application;

import javafx.application.Application;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.stage.Stage;

import java.util.*;
import java.util.stream.Collectors;

public class CropClassifierApp extends Application {

    private DecisionTree tree = new DecisionTree();
    private Node decisionTreeRoot;
    private TextArea resultArea = new TextArea();

    @Override
    public void start(Stage primaryStage) {
        VBox root = new VBox(10);
        root.setPadding(new Insets(10));

        Label title = new Label("🌾 Crop Recommendation System");

        GridPane inputs = new GridPane();
        inputs.setHgap(10);
        inputs.setVgap(10);

        TextField[] fields = new TextField[7];
        String[] labels = {"N", "P", "K", "Temperature", "Humidity", "pH", "Rainfall"};
        for (int i = 0; i < labels.length; i++) {
            inputs.add(new Label(labels[i] + ":"), 0, i);
            fields[i] = new TextField();
            inputs.add(fields[i], 1, i);
        }

        Button trainBtn = new Button("Train Model");
        trainBtn.setOnAction(e -> trainModel());

        Button predictBtn = new Button("Predict Crop");
        predictBtn.setOnAction(e -> {
            if (decisionTreeRoot == null) {
                showAlert("Error", "Model not trained yet.");
                return;
            }
            try {
                CropData input = new CropData();
                input.features.put("N", Integer.parseInt(fields[0].getText()));
                input.features.put("P", Integer.parseInt(fields[1].getText()));
                input.features.put("K", Integer.parseInt(fields[2].getText()));
                input.features.put("temperature", Double.parseDouble(fields[3].getText()));
                input.features.put("humidity", Double.parseDouble(fields[4].getText()));
                input.features.put("ph", Double.parseDouble(fields[5].getText()));
                input.features.put("rainfall", Double.parseDouble(fields[6].getText()));

                String prediction = tree.predict(decisionTreeRoot, input);
                showAlert("Prediction", "Recommended Crop: " + prediction);
            } catch (NumberFormatException ex) {
                showAlert("Input Error", "Please enter valid numeric values.");
            }
        });

        HBox buttons = new HBox(10, trainBtn, predictBtn);

        resultArea.setEditable(false);
        resultArea.setPrefHeight(250);

        root.getChildren().addAll(title, inputs, buttons, new Label("Results:"), resultArea);

        Scene scene = new Scene(root, 550, 500);
        primaryStage.setScene(scene);
        primaryStage.setTitle("Crop Recommendation - Decision Tree");
        primaryStage.show();
    }

    private void trainModel() {
    	List<CropData> data = CSVLoader.loadFromCSV("C:/Users/abdee/Downloads/archive/Crop_recommendation.csv");
        decisionTreeRoot = tree.buildTree(data);

        List<List<CropData>> folds = splitData(data, 5);
        StringBuilder results = new StringBuilder();

        for (int i = 0; i < 5; i++) {
            List<CropData> test = folds.get(i);
            List<CropData> train = new ArrayList<>();
            for (int j = 0; j < 5; j++) if (j != i) train.addAll(folds.get(j));

            Node model = tree.buildTree(train);
            List<String> predictions = test.stream().map(d -> tree.predict(model, d)).collect(Collectors.toList());
            List<String> actuals = test.stream().map(d -> d.label).collect(Collectors.toList());

            Set<String> allLabels = new HashSet<>(actuals);
            double acc = Evaluator.accuracy(predictions, actuals);
            double totalPrec = 0, totalRec = 0;
            for (String label : allLabels) {
                totalPrec += Evaluator.precision(predictions, actuals, label);
                totalRec += Evaluator.recall(predictions, actuals, label);
            }
            double avgPrec = totalPrec / allLabels.size();
            double avgRec = totalRec / allLabels.size();

            results.append(String.format("Fold %d: Acc=%.2f%%, Prec=%.2f%%, Rec=%.2f%%\n",
                    i + 1, acc * 100, avgPrec * 100, avgRec * 100));
        }

        StringBuilder treeText = new StringBuilder();
        tree.printTree(decisionTreeRoot, "", treeText);
        resultArea.setText("Decision Tree:\n" + treeText + "\n\nResults:\n" + results);
    }

    private List<List<CropData>> splitData(List<CropData> data, int k) {
        List<List<CropData>> folds = new ArrayList<>();
        int foldSize = data.size() / k;
        Collections.shuffle(data);
        for (int i = 0; i < k; i++) {
            int start = i * foldSize;
            int end = Math.min(start + foldSize, data.size());
            folds.add(new ArrayList<>(data.subList(start, end)));
        }
        return folds;
    }

    private void showAlert(String title, String content) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.showAndWait();
    }

    public static void main(String[] args) {
        launch(args);
    }
}
