package com.example.caloriecalculator;

import android.content.ActivityNotFoundException;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.activity.SystemBarStyle;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.PickVisualMediaRequest;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.chip.Chip;
import com.google.android.material.chip.ChipGroup;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.progressindicator.LinearProgressIndicator;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;
import com.google.mlkit.vision.common.InputImage;

import java.io.IOException;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

/**
 * AddFoodActivity
 * ---------------
 * Lets the user add a food to a meal in two ways:
 *  1. Take / choose a photo -> the AI model suggests what the food is
 *  2. Search the built-in food list
 * Either way, the user then enters the amount (g or ml) and the calories are calculated.
 */
public class AddFoodActivity extends AppCompatActivity {

    public static final String EXTRA_MEAL = "extra_meal";
    public static final String EXTRA_MODE = "extra_mode";
    public static final String MODE_SEARCH = "search";
    public static final String MODE_CAMERA = "camera";

    private String meal;
    private String selectedCategory = FoodDatabase.ALL;
    private FoodLogStorage storage;
    private FoodRecognizer recognizer;

    private View cardPreview;
    private android.widget.ImageView ivPreview;
    private LinearProgressIndicator progressRecognize;
    private TextView tvStatus, tvModelGuesses, tvResultCount;
    private ChipGroup chipSuggestions, chipCategories;
    private TextInputEditText etSearch;
    private LinearLayout resultsContainer;
    private MaterialButton btnTakePhoto, btnGallery;

    // Opens the phone's camera app and returns a small photo (no camera permission needed)
    private final ActivityResultLauncher<Void> takePhotoLauncher =
            registerForActivityResult(new ActivityResultContracts.TakePicturePreview(), bitmap -> {
                if (bitmap != null) {
                    ivPreview.setImageBitmap(bitmap);
                    runRecognition(InputImage.fromBitmap(bitmap, 0));
                }
            });

    // Opens the photo picker so the user can choose an existing picture
    private final ActivityResultLauncher<PickVisualMediaRequest> pickImageLauncher =
            registerForActivityResult(new ActivityResultContracts.PickVisualMedia(), this::onImagePicked);

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this, SystemBarStyle.dark(Color.TRANSPARENT));
        setContentView(R.layout.activity_add_food);
        applyWindowInsets();

        meal = getIntent().getStringExtra(EXTRA_MEAL);
        if (meal == null) {
            meal = FoodEntry.MEAL_BREAKFAST;
        }
        String mode = getIntent().getStringExtra(EXTRA_MODE);
        storage = new FoodLogStorage(this);

        initViews();
        ((TextView) findViewById(R.id.tvAddTitle)).setText("Add to " + FoodEntry.getMealName(meal));
        findViewById(R.id.btnBack).setOnClickListener(v -> finish());

        setupCategories();
        setupSearch();
        refreshResults();

        // Load the AI model
        try {
            recognizer = new FoodRecognizer(this);
        } catch (IOException e) {
            recognizer = null;
            btnTakePhoto.setEnabled(false);
            btnGallery.setEnabled(false);
            showStatus("Food recognition is unavailable: food_model.tflite was not found in the assets folder. You can still search below.");
        }

        btnTakePhoto.setOnClickListener(v -> openCamera());
        btnGallery.setOnClickListener(v -> openGallery());

        // Opened from the "Camera" button: go straight to the camera
        if (MODE_CAMERA.equals(mode) && savedInstanceState == null && recognizer != null) {
            getWindow().getDecorView().post(this::openCamera);
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (recognizer != null) {
            recognizer.close();
        }
    }

    private void applyWindowInsets() {
        View root = findViewById(R.id.main);
        LinearLayout content = findViewById(R.id.content);
        int baseBottom = content.getPaddingBottom();
        ViewCompat.setOnApplyWindowInsetsListener(root, (v, insets) -> {
            Insets bars = insets.getInsets(
                    WindowInsetsCompat.Type.systemBars() | WindowInsetsCompat.Type.ime());
            v.setPadding(bars.left, bars.top, bars.right, 0);
            content.setPadding(content.getPaddingLeft(), content.getPaddingTop(),
                    content.getPaddingRight(), baseBottom + bars.bottom);
            return insets;
        });
    }

    private void initViews() {
        cardPreview = findViewById(R.id.cardPreview);
        ivPreview = findViewById(R.id.ivPreview);
        progressRecognize = findViewById(R.id.progressRecognize);
        tvStatus = findViewById(R.id.tvRecognizeStatus);
        tvModelGuesses = findViewById(R.id.tvModelGuesses);
        tvResultCount = findViewById(R.id.tvResultCount);
        chipSuggestions = findViewById(R.id.chipSuggestions);
        chipCategories = findViewById(R.id.chipCategories);
        etSearch = findViewById(R.id.etSearch);
        resultsContainer = findViewById(R.id.resultsContainer);
        btnTakePhoto = findViewById(R.id.btnTakePhoto);
        btnGallery = findViewById(R.id.btnGallery);
    }

    // =====================================================================
    //  METHOD 1: CAMERA + AI RECOGNITION
    // =====================================================================

    private void openCamera() {
        try {
            takePhotoLauncher.launch(null);
        } catch (ActivityNotFoundException e) {
            Toast.makeText(this, "No camera app found on this device", Toast.LENGTH_SHORT).show();
        }
    }

    private void openGallery() {
        pickImageLauncher.launch(new PickVisualMediaRequest.Builder()
                .setMediaType(ActivityResultContracts.PickVisualMedia.ImageOnly.INSTANCE)
                .build());
    }

    private void onImagePicked(Uri uri) {
        if (uri == null) {
            return;
        }
        ivPreview.setImageURI(uri);
        try {
            runRecognition(InputImage.fromFilePath(this, uri));
        } catch (IOException e) {
            showStatus("Could not open that picture. Please try another one.");
        }
    }

    private void runRecognition(InputImage image) {
        if (recognizer == null) {
            return;
        }
        cardPreview.setVisibility(View.VISIBLE);
        progressRecognize.setVisibility(View.VISIBLE);
        chipSuggestions.removeAllViews();
        tvModelGuesses.setVisibility(View.GONE);
        showStatus("Recognising your food...");

        recognizer.recognize(image, new FoodRecognizer.Callback() {
            @Override
            public void onResult(List<FoodRecognizer.Guess> guesses) {
                progressRecognize.setVisibility(View.GONE);
                showSuggestions(guesses);
            }

            @Override
            public void onError(String message) {
                progressRecognize.setVisibility(View.GONE);
                showStatus("Recognition failed: " + message);
            }
        });
    }

    /** Matches the AI's guesses to foods in our list and shows them as tappable chips. */
    private void showSuggestions(List<FoodRecognizer.Guess> guesses) {
        if (guesses.isEmpty()) {
            showStatus("Sorry, no food was recognised. Try a clearer, closer photo, or search below.");
            return;
        }

        // Show what the AI model actually predicted (top 3)
        StringBuilder sb = new StringBuilder("AI model guesses: ");
        for (int i = 0; i < Math.min(3, guesses.size()); i++) {
            FoodRecognizer.Guess g = guesses.get(i);
            if (i > 0) {
                sb.append(", ");
            }
            sb.append(String.format(Locale.getDefault(), "%s (%.0f%%)", g.label, g.confidence * 100));
        }
        tvModelGuesses.setText(sb.toString());
        tvModelGuesses.setVisibility(View.VISIBLE);

        // Find foods in our list that match each guess (keeping the highest confidence)
        Map<FoodItem, Float> matches = new LinkedHashMap<>();
        for (FoodRecognizer.Guess guess : guesses) {
            for (FoodItem food : FoodDatabase.findByModelLabel(guess.label)) {
                if (!matches.containsKey(food)) {
                    matches.put(food, guess.confidence);
                }
            }
        }

        if (matches.isEmpty()) {
            showStatus(String.format("The AI thinks this is \"%s\", but it isn't in our food list yet. Please search for the closest food below.",
                    guesses.get(0).label));
            return;
        }

        showStatus("Is it one of these? Tap the correct food:");
        LayoutInflater inflater = getLayoutInflater();
        int shown = 0;
        for (Map.Entry<FoodItem, Float> entry : matches.entrySet()) {
            if (shown == 4) {
                break;
            }
            FoodItem food = entry.getKey();
            Chip chip = (Chip) inflater.inflate(R.layout.item_chip, chipSuggestions, false);
            chip.setCheckable(false);
            chip.setText(String.format(Locale.getDefault(), "%s · %.0f%%", food.name, entry.getValue() * 100));
            chip.setOnClickListener(v -> showAmountDialog(food));
            chipSuggestions.addView(chip);
            shown++;
        }
    }

    private void showStatus(String message) {
        tvStatus.setVisibility(View.VISIBLE);
        tvStatus.setText(message);
    }

    // =====================================================================
    //  METHOD 2: SEARCH THE FOOD LIST
    // =====================================================================

    private void setupCategories() {
        LayoutInflater inflater = getLayoutInflater();
        for (String category : FoodDatabase.CATEGORIES) {
            Chip chip = (Chip) inflater.inflate(R.layout.item_chip, chipCategories, false);
            chip.setId(View.generateViewId());
            chip.setText(category);
            chip.setTag(category);
            chipCategories.addView(chip);
            if (FoodDatabase.ALL.equals(category)) {
                chip.setChecked(true);
            }
        }
        chipCategories.setOnCheckedStateChangeListener((group, checkedIds) -> {
            if (!checkedIds.isEmpty()) {
                Chip checked = group.findViewById(checkedIds.get(0));
                selectedCategory = (String) checked.getTag();
                refreshResults();
            }
        });
    }

    private void setupSearch() {
        etSearch.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
            }

            @Override
            public void afterTextChanged(Editable s) {
                refreshResults();
            }
        });
    }

    private void refreshResults() {
        String query = etSearch.getText() == null ? "" : etSearch.getText().toString();
        List<FoodItem> results = FoodDatabase.search(query, selectedCategory);

        resultsContainer.removeAllViews();
        tvResultCount.setText(results.isEmpty()
                ? "No foods found. Try another word or category."
                : results.size() + " foods");

        LayoutInflater inflater = getLayoutInflater();
        for (FoodItem food : results) {
            View row = inflater.inflate(R.layout.item_food_result, resultsContainer, false);
            ((TextView) row.findViewById(R.id.tvResultName)).setText(food.name);
            ((TextView) row.findViewById(R.id.tvResultDetail)).setText(String.format(Locale.getDefault(),
                    "%s · %d kcal per 100 %s", food.category, food.getRoundedPer100(), food.unit));
            row.setOnClickListener(v -> showAmountDialog(food));
            resultsContainer.addView(row);
        }
    }

    // =====================================================================
    //  ENTER AMOUNT + CALCULATE CALORIES
    // =====================================================================

    private void showAmountDialog(FoodItem food) {
        View view = getLayoutInflater().inflate(R.layout.dialog_add_amount, null);
        TextView tvPer100 = view.findViewById(R.id.tvDialogPer100);
        TextView tvServing = view.findViewById(R.id.tvServingHint);
        TextView tvCalc = view.findViewById(R.id.tvDialogCalc);
        TextInputLayout tilAmount = view.findViewById(R.id.tilAmount);
        TextInputEditText etAmount = view.findViewById(R.id.etAmount);

        tvPer100.setText(String.format(Locale.getDefault(),
                "%d kcal per 100 %s", food.getRoundedPer100(), food.unit));
        tvServing.setText(String.format(Locale.getDefault(),
                "Typical serving: %s ≈ %d %s", food.servingLabel, food.servingAmount, food.unit));
        tilAmount.setSuffixText(food.unit);
        etAmount.setText(String.valueOf(food.servingAmount));

        // Live calculation: calories = amount ÷ 100 × kcal per 100
        Runnable updateCalc = () -> {
            double amount = parseAmount(etAmount);
            if (amount <= 0) {
                tvCalc.setText("Enter an amount to see the calories");
            } else {
                tvCalc.setText(String.format(Locale.getDefault(),
                        "Calories = amount ÷ 100 × kcal per 100\n= %s ÷ 100 × %d\n= %,d kcal",
                        FoodLogActivity.formatAmount(amount), food.getRoundedPer100(), food.caloriesFor(amount)));
            }
        };
        etAmount.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
            }

            @Override
            public void afterTextChanged(Editable s) {
                tilAmount.setError(null);
                updateCalc.run();
            }
        });
        updateCalc.run();

        AlertDialog dialog = new MaterialAlertDialogBuilder(this)
                .setTitle(food.name)
                .setView(view)
                .setNegativeButton("Cancel", null)
                .setPositiveButton("Add to " + FoodEntry.getMealName(meal), null)
                .create();

        // Custom click handler so the dialog stays open if the amount is invalid
        dialog.setOnShowListener(d -> dialog.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener(v -> {
            double amount = parseAmount(etAmount);
            if (amount <= 0 || amount > 5000) {
                tilAmount.setError("Enter 1 – 5000 " + food.unit);
                return;
            }
            int calories = food.caloriesFor(amount);
            storage.addEntry(new FoodEntry(System.currentTimeMillis(), meal, food.name,
                    amount, food.unit, food.kcalPer100, calories));
            Toast.makeText(this, String.format(Locale.getDefault(), "Added %s (%,d kcal) to %s",
                    food.name, calories, FoodEntry.getMealName(meal)), Toast.LENGTH_SHORT).show();
            dialog.dismiss();
            finish(); // back to the food log
        }));
        dialog.show();
    }

    private double parseAmount(TextInputEditText editText) {
        String text = editText.getText() == null ? "" : editText.getText().toString().trim();
        try {
            return Double.parseDouble(text);
        } catch (NumberFormatException e) {
            return -1;
        }
    }
}
