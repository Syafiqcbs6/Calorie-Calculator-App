package com.example.caloriecalculator;

import android.content.Context;

import com.google.mlkit.common.model.LocalModel;
import com.google.mlkit.vision.common.InputImage;
import com.google.mlkit.vision.label.ImageLabel;
import com.google.mlkit.vision.label.ImageLabeler;
import com.google.mlkit.vision.label.ImageLabeling;
import com.google.mlkit.vision.label.custom.CustomImageLabelerOptions;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * FoodRecognizer
 * --------------
 * Recognises food in a photo using an on-device AI model
 * (Google AIY Vision Classifier Food V1, 2,023 dishes) through Google ML Kit.
 * Everything runs on the phone: no internet, no API key.
 */
public class FoodRecognizer {

    private static final String MODEL_FILE = "food_model.tflite";
    private static final String LABELS_FILE = "food_labels.txt";

    /** One guess from the AI, e.g. "Nasi lemak" with 0.62 (62%) confidence. */
    public static class Guess {
        public final String label;
        public final float confidence;

        Guess(String label, float confidence) {
            this.label = label;
            this.confidence = confidence;
        }
    }

    public interface Callback {
        void onResult(List<Guess> guesses);

        void onError(String message);
    }

    private final List<String> labels = new ArrayList<>();
    private final ImageLabeler labeler;

    public FoodRecognizer(Context context) throws IOException {
        // Load the English food names. Line number = the model's output index.
        try (BufferedReader reader = new BufferedReader(
                new InputStreamReader(context.getAssets().open(LABELS_FILE)))) {
            String line;
            while ((line = reader.readLine()) != null) {
                labels.add(line.trim());
            }
        }
        // Throws an IOException early if the model file is missing
        context.getAssets().open(MODEL_FILE).close();

        LocalModel localModel = new LocalModel.Builder()
                .setAssetFilePath(MODEL_FILE)
                .build();

        CustomImageLabelerOptions options = new CustomImageLabelerOptions.Builder(localModel)
                .setConfidenceThreshold(0.03f)
                .setMaxResultCount(8)
                .build();

        labeler = ImageLabeling.getClient(options);
    }

    public void recognize(InputImage image, Callback callback) {
        labeler.process(image)
                .addOnSuccessListener(result -> {
                    List<Guess> guesses = new ArrayList<>();
                    for (ImageLabel label : result) {
                        int index = label.getIndex();
                        // Index 0 is "__background__" (not food)
                        if (index > 0 && index < labels.size()) {
                            guesses.add(new Guess(labels.get(index), label.getConfidence()));
                        }
                    }
                    Collections.sort(guesses, (a, b) -> Float.compare(b.confidence, a.confidence));
                    callback.onResult(guesses);
                })
                .addOnFailureListener(e -> callback.onError(e.getMessage()));
    }

    public void close() {
        labeler.close();
    }
}
