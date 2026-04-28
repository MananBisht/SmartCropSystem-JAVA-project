package org.example;

import java.io.*;
import org.datavec.image.loader.NativeImageLoader;
import org.deeplearning4j.nn.multilayer.MultiLayerNetwork;
import org.nd4j.linalg.api.ndarray.INDArray;
import org.nd4j.linalg.dataset.api.preprocessor.ImagePreProcessingScaler;

public class Test {

    static MultiLayerNetwork model;
    public static String predictDisease(File file) {
        try {
            int height = 160, width = 160, channels = 3;

            if (model == null) {
                model = org.deeplearning4j.util.ModelSerializer
                        .restoreMultiLayerNetwork("plant_disease_model.zip");
            }

            NativeImageLoader loader = new NativeImageLoader(height, width, channels);
            INDArray image = loader.asMatrix(file);

            ImagePreProcessingScaler scaler = new ImagePreProcessingScaler(0, 1);
            scaler.transform(image);

            INDArray output = model.output(image);
            int predictedClass = output.argMax(1).getInt(0);

            String[] labels = {
                    "Apple___Apple_scab",
                    "Apple___Black_rot",
                    "Apple___Cedar_apple_rust",
                    "Apple___healthy",
                    "Corn_(maize)___Cercospora_leaf_spot Gray_leaf_spot",
                    "Corn_(maize)___Common_rust_",
                    "Corn_(maize)___Northern_Leaf_Blight",
                    "Corn_(maize)___healthy",
                    "Potato___Early_blight",
                    "Potato___Late_blight",
                    "Potato___healthy",
                    "Tomato___Bacterial_spot",
                    "Tomato___Early_blight",
                    "Tomato___Late_blight",
                    "Tomato___Leaf_Mold",
                    "Tomato___Septoria_leaf_spot",
                    "Tomato___Spider_mites Two-spotted_spider_mite",
                    "Tomato___Target_Spot",
                    "Tomato___healthy"
            };
            double confidence = output.maxNumber().doubleValue() * 100;
            return labels[predictedClass]
                    .replace("___", " ")
                    .replace("_", " ")
                    + " (" + String.format("%.2f", confidence) + "%)";

        } catch (Exception e) {
            e.printStackTrace();
            return "Error";
        }
    }

    public static String solution(String name) {
        name = name.toLowerCase();

        try {
            BufferedReader br = new BufferedReader(new FileReader("src/main/resources/disease.txt"));

            String line;
            boolean found = false;
            String res = "";

            while ((line = br.readLine()) != null) {
                String[] arr = line.split("\\|");
                String disease = arr[0].toLowerCase();

                if (name.contains(disease) || disease.contains(name)) {
                    found = true;
                    res += "Disease type : " + arr[0] + "\n";
                    res += "Prevention   : " + arr[1] + "\n";
                    res += "Treatment    : " + arr[2] + "\n";
                    res += "Pesticide    : " + arr[3] + "\n";
                    break;
                }
            }

            br.close();

            if (!found) {
                res += "\nGeneral Advice:\n" ;
                res += "Prevention: Keep plants dry and ventilated\n";
                res += "Treatment: Remove infected parts\n";
                res += "Pesticide: Use general fungicide\n";
            }

            return res ;

        } catch (Exception e) {
            e.printStackTrace();
            return "Error" ;
        }
    }
}