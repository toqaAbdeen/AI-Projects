package application;

import java.io.*;
import java.util.*;

public class CSVLoader {

    public static List<CropData> loadFromCSV(String filename) {
        List<CropData> data = new ArrayList<>();

        try (BufferedReader br = new BufferedReader(new FileReader(filename))) {
            String line;
            br.readLine();  

            while ((line = br.readLine()) != null) {
                String[] parts = line.split(",");
                if (parts.length < 8) continue;

                CropData d = new CropData();
                d.features.put("N", Integer.parseInt(parts[0]));
                d.features.put("P", Integer.parseInt(parts[1]));
                d.features.put("K", Integer.parseInt(parts[2]));
                d.features.put("temperature", Double.parseDouble(parts[3]));
                d.features.put("humidity", Double.parseDouble(parts[4]));
                d.features.put("ph", Double.parseDouble(parts[5]));
                d.features.put("rainfall", Double.parseDouble(parts[6]));
                d.label = parts[7];

                data.add(d);
            }

        } catch (IOException e) {
            System.out.println("File upload error: " + e.getMessage());
        }

        return data;
    }
}
