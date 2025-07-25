package application;

import java.util.*;
import java.util.stream.Collectors;

public class DecisionTree {

    private final List<String> features = Arrays.asList("N", "P", "K", "temperature", "humidity", "ph", "rainfall");

    public Node buildTree(List<CropData> data) {
        return id3(data, new ArrayList<>(features));
    }

    private Node id3(List<CropData> data, List<String> remainingFeatures) {
        Node node = new Node();

        // If all samples have the same label
        Set<String> labels = data.stream().map(d -> d.label).collect(Collectors.toSet());
        if (labels.size() == 1) {
            node.label = data.get(0).label;
            return node;
        }

        // If no features left, return majority class
        if (remainingFeatures.isEmpty()) {
            node.label = majorityClass(data);
            return node;
        }

        // Choose best feature using information gain
        String bestFeature = chooseBestFeature(data, remainingFeatures);
        node.feature = bestFeature;
        remainingFeatures.remove(bestFeature);

        // Build branches
        Map<String, List<Double>> featureValuesMap = new HashMap<>();

        // Collect unique values for each sample
        for (CropData crop : data) {
            Object valObj = crop.features.get(bestFeature);
            String valStr = valObj.toString();
            double valNum = Double.parseDouble(valStr);

            featureValuesMap.computeIfAbsent(bestFeature, k -> new ArrayList<>()).add(valNum);
        }

        // Get average as threshold
        double threshold = featureValuesMap.get(bestFeature).stream()
                .mapToDouble(Double::doubleValue)
                .average()
                .orElseThrow();

        // Split into <= and > branches
        List<CropData> leftSubset = data.stream()
                .filter(crop -> Double.parseDouble(crop.features.get(bestFeature).toString()) <= threshold)
                .collect(Collectors.toList());

        List<CropData> rightSubset = data.stream()
                .filter(crop -> Double.parseDouble(crop.features.get(bestFeature).toString()) > threshold)
                .collect(Collectors.toList());

        // Recursively build subtrees
        if (!leftSubset.isEmpty()) {
            Node leftNode = id3(leftSubset, new ArrayList<>(remainingFeatures));
            node.branches.put(bestFeature + " <= " + threshold, leftNode);
        } else {
            Node leaf = new Node();
            leaf.label = majorityClass(data);
            node.branches.put(bestFeature + " <= " + threshold, leaf);
        }

        if (!rightSubset.isEmpty()) {
            Node rightNode = id3(rightSubset, new ArrayList<>(remainingFeatures));
            node.branches.put(bestFeature + " > " + threshold, rightNode);
        } else {
            Node leaf = new Node();
            leaf.label = majorityClass(data);
            node.branches.put(bestFeature + " > " + threshold, leaf);
        }

        return node;
    }

    private String majorityClass(List<CropData> data) {
        Map<String, Integer> counts = new HashMap<>();
        for (CropData d : data) {
            counts.put(d.label, counts.getOrDefault(d.label, 0) + 1);
        }
        return counts.entrySet().stream()
                .max(Map.Entry.comparingByValue())
                .get().getKey();
    }

    // Calculates entropy of a dataset
    private double calculateEntropy(List<CropData> data) {
        long total = data.size();
        if (total == 0) return 0;

        Map<String, Long> labelCounts = data.stream()
                .collect(Collectors.groupingBy(d -> d.label, Collectors.counting()));

        double entropy = 0;
        for (Long count : labelCounts.values()) {
            double prob = (double) count / total;
            entropy -= prob * Math.log(prob) / Math.log(2);
        }

        return entropy;
    }

    // Calculates information gain for a feature
    private double informationGain(List<CropData> data, String feature) {
        double baseEntropy = calculateEntropy(data);

        Map<Double, List<CropData>> grouped = data.stream()
                .collect(Collectors.groupingBy(
                        crop -> Double.parseDouble(crop.features.get(feature).toString()),
                        Collectors.toList()
                ));

        double avgEntropy = 0;
        double total = data.size();

        for (List<CropData> subset : grouped.values()) {
            double weight = (double) subset.size() / total;
            avgEntropy += weight * calculateEntropy(subset);
        }

        return baseEntropy - avgEntropy;
    }

    // Choose best feature based on information gain
    private String chooseBestFeature(List<CropData> data, List<String> features) {
        return features.stream()
                .max((f1, f2) -> Double.compare(informationGain(data, f1), informationGain(data, f2)))
                .orElseThrow();
    }

    public String predict(Node root, CropData sample) {
        Node current = root;

        while (!current.isLeaf()) {
            boolean matched = false;
            for (Map.Entry<String, Node> entry : current.branches.entrySet()) {
                String branchKey = entry.getKey();
                Node next = entry.getValue();

                if (branchKey.contains("<=")) {
                    String[] parts = branchKey.split("<=");
                    double threshold = Double.parseDouble(parts[1]);
                    double inputVal = Double.parseDouble(sample.features.get(current.feature).toString());

                    if (inputVal <= threshold) {
                        current = next;
                        matched = true;
                        break;
                    }
                } else if (branchKey.contains(">")) {
                    String[] parts = branchKey.split(">");
                    double threshold = Double.parseDouble(parts[1]);
                    double inputVal = Double.parseDouble(sample.features.get(current.feature).toString());

                    if (inputVal > threshold) {
                        current = next;
                        matched = true;
                        break;
                    }
                } else {
                    // For categorical features
                    if (entry.getKey().equalsIgnoreCase(sample.features.get(current.feature).toString())) {
                        current = next;
                        matched = true;
                        break;
                    }
                }
            }

            if (!matched) {
                return "Unknown (no matching path)";
            }
        }

        return current.label;
    }

    public void printTree(Node node, String indent, StringBuilder builder) {
        if (node.isLeaf()) {
            builder.append(indent).append("Label: ").append(node.label).append("\n");
        } else {
            for (Map.Entry<String, Node> entry : node.branches.entrySet()) {
                builder.append(indent).append(node.feature).append(" = ").append(entry.getKey()).append(":\n");
                printTree(entry.getValue(), indent + "  ", builder);
            }
        }
    }
}
