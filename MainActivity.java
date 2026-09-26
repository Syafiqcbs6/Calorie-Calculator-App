package com.example.caloriecalculator;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.activity.SystemBarStyle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.chip.ChipGroup;
import com.google.android.material.textfield.MaterialAutoCompleteTextView;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;

/**
 * MainActivity
 * ------------
 * Input screen. Collects the user's details, validates them,
 * then sends them to ResultActivity for calculation.
 */
public class MainActivity extends AppCompatActivity {

    // Keys used to pass data to ResultActivity
    public static final String EXTRA_NAME = "extra_name";
    public static final String EXTRA_AGE = "extra_age";
    public static final String EXTRA_WEIGHT = "extra_weight";
    public static final String EXTRA_HEIGHT = "extra_height";
    public static final String EXTRA_IS_MALE = "extra_is_male";
    public static final String EXTRA_ACTIVITY = "extra_activity";
    public static final String EXTRA_GOAL = "extra_goal";

    private TextInputLayout tilName, tilAge, tilWeight, tilHeight;
    private TextInputEditText etName, etAge, etWeight, etHeight;
    private ChipGroup chipGroupGender, chipGroupGoal;
    private MaterialAutoCompleteTextView actvActivity;
    private TextView tvGoalHint;

    private String[] activityLevels;
    private int selectedActivity = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this, SystemBarStyle.dark(Color.TRANSPARENT));
        setContentView(R.layout.activity_main);

        applyWindowInsets();
        initViews();
        setupActivityDropdown();

        // Update the hint text whenever a different goal chip is tapped
        chipGroupGoal.setOnCheckedStateChangeListener((group, checkedIds) -> updateGoalHint());
        updateGoalHint();

        MaterialButton btnCalculate = findViewById(R.id.btnCalculate);
        MaterialButton btnClear = findViewById(R.id.btnClear);

        btnCalculate.setOnClickListener(v -> pressAnimation(v, this::onCalculateClicked));
        btnClear.setOnClickListener(v -> pressAnimation(v, this::clearForm));

        findViewById(R.id.btnBack).setOnClickListener(v -> finish());

        // Fill the form with the details from last time, so the user doesn't retype them
        if (savedInstanceState == null) {
            prefillFromProfile();
        }
    }

    private void prefillFromProfile() {
        FoodLogStorage storage = new FoodLogStorage(this);
        if (!storage.hasProfile()) {
            return;
        }

        etName.setText(storage.getName());
        etAge.setText(String.valueOf(storage.getAge()));
        etWeight.setText(FoodLogActivity.formatAmount(Math.round(storage.getWeight() * 10) / 10.0));
        etHeight.setText(FoodLogActivity.formatAmount(Math.round(storage.getHeight() * 10) / 10.0));
        chipGroupGender.check(storage.isMale() ? R.id.chipMale : R.id.chipFemale);

        int goal = storage.getGoal();

        if (goal == CalorieCalculator.GOAL_LOSE) {
            chipGroupGoal.check(R.id.chipLose);
        } else if (goal == CalorieCalculator.GOAL_GAIN) {
            chipGroupGoal.check(R.id.chipGain);
        } else {
            chipGroupGoal.check(R.id.chipMaintain);
        }

        selectedActivity = storage.getActivity();

        if (selectedActivity >= 0 && selectedActivity < activityLevels.length) {
            actvActivity.setText(activityLevels[selectedActivity], false);
        }
    }

    /** Keeps content clear of the status bar, navigation bar and keyboard. */
    private void applyWindowInsets() {
        View root = findViewById(R.id.main);
        LinearLayout content = findViewById(R.id.content);
        int baseBottom = content.getPaddingBottom();

        ViewCompat.setOnApplyWindowInsetsListener(root, (v, insets) -> {
            Insets bars = insets.getInsets(
                    WindowInsetsCompat.Type.systemBars() | WindowInsetsCompat.Type.ime());

            v.setPadding(bars.left, bars.top, bars.right, 0);

            content.setPadding(
                    content.getPaddingLeft(),
                    content.getPaddingTop(),
                    content.getPaddingRight(),
                    baseBottom + bars.bottom
            );

            return insets;
        });
    }

    private void initViews() {
        tilName = findViewById(R.id.tilName);
        tilAge = findViewById(R.id.tilAge);
        tilWeight = findViewById(R.id.tilWeight);
        tilHeight = findViewById(R.id.tilHeight);

        etName = findViewById(R.id.etName);
        etAge = findViewById(R.id.etAge);
        etWeight = findViewById(R.id.etWeight);
        etHeight = findViewById(R.id.etHeight);

        chipGroupGender = findViewById(R.id.chipGroupGender);
        chipGroupGoal = findViewById(R.id.chipGroupGoal);
        actvActivity = findViewById(R.id.actvActivity);
        tvGoalHint = findViewById(R.id.tvGoalHint);
    }

    /** Fills the activity-level dropdown and remembers which item is chosen. */
    private void setupActivityDropdown() {
        activityLevels = getResources().getStringArray(R.array.activity_levels);

        ArrayAdapter<String> adapter = new ArrayAdapter<>(
                this, android.R.layout.simple_list_item_1, activityLevels);

        actvActivity.setAdapter(adapter);
        actvActivity.setText(activityLevels[0], false);

        actvActivity.setOnItemClickListener(
                (parent, view, position, id) -> selectedActivity = position
        );
    }

    private int getSelectedGoal() {
        int id = chipGroupGoal.getCheckedChipId();

        if (id == R.id.chipLose) {
            return CalorieCalculator.GOAL_LOSE;
        } else if (id == R.id.chipGain) {
            return CalorieCalculator.GOAL_GAIN;
        }

        return CalorieCalculator.GOAL_MAINTAIN;
    }

    private void updateGoalHint() {
        int goal = getSelectedGoal();

        if (goal == CalorieCalculator.GOAL_LOSE) {
            tvGoalHint.setText(R.string.goal_hint_lose);
        } else if (goal == CalorieCalculator.GOAL_GAIN) {
            tvGoalHint.setText(R.string.goal_hint_gain);
        } else {
            tvGoalHint.setText(R.string.goal_hint_maintain);
        }
    }

    /** Validates every input. If all are valid, opens the result screen. */
    private void onCalculateClicked() {
        clearErrors();
        boolean valid = true;

        // ---- Name ----
        String name = getText(etName);

        if (TextUtils.isEmpty(name)) {
            tilName.setError("Please enter your name");
            valid = false;
        }

        // ---- Age (Mifflin-St Jeor is intended for adults) ----
        int age = (int) parseNumber(getText(etAge));

        if (age < 0) {
            tilAge.setError("Please enter your age");
            valid = false;
        } else if (age < 15 || age > 80) {
            tilAge.setError("Age must be between 15 and 80");
            valid = false;
        }

        // ---- Weight ----
        double weight = parseNumber(getText(etWeight));

        if (weight < 0) {
            tilWeight.setError("Required");
            valid = false;
        } else if (weight < 30 || weight > 300) {
            tilWeight.setError("30 – 300 kg");
            valid = false;
        }

        // ---- Height ----
        double height = parseNumber(getText(etHeight));

        if (height < 0) {
            tilHeight.setError("Required");
            valid = false;
        } else if (height < 100 || height > 250) {
            tilHeight.setError("100 – 250 cm");
            valid = false;
        }

        // ---- Gender ----
        if (chipGroupGender.getCheckedChipId() == View.NO_ID) {
            Toast.makeText(this, "Please select your gender", Toast.LENGTH_SHORT).show();
            valid = false;
        }

        if (!valid) {
            return;
        }

        boolean isMale = chipGroupGender.getCheckedChipId() == R.id.chipMale;

        // Remember the user's details for next time and for the exercise calculation
        new FoodLogStorage(this).saveProfile(
                name,
                age,
                weight,
                height,
                isMale,
                selectedActivity,
                getSelectedGoal()
        );

        // Send the inputs to the result screen
        Intent intent = new Intent(MainActivity.this, ResultActivity.class);

        intent.putExtra(EXTRA_NAME, name);
        intent.putExtra(EXTRA_AGE, age);
        intent.putExtra(EXTRA_WEIGHT, weight);
        intent.putExtra(EXTRA_HEIGHT, height);
        intent.putExtra(EXTRA_IS_MALE, isMale);
        intent.putExtra(EXTRA_ACTIVITY, selectedActivity);
        intent.putExtra(EXTRA_GOAL, getSelectedGoal());

        startActivity(intent);
    }

    /** Resets the form to its starting state. */
    private void clearForm() {
        etName.setText("");
        etAge.setText("");
        etWeight.setText("");
        etHeight.setText("");

        chipGroupGender.clearCheck();
        chipGroupGoal.check(R.id.chipMaintain);

        selectedActivity = 0;
        actvActivity.setText(activityLevels[0], false);

        clearErrors();
        etName.requestFocus();

        Toast.makeText(this, "Form cleared", Toast.LENGTH_SHORT).show();
    }

    /** Small "squish" animation when a button is pressed, then runs the action. */
    private void pressAnimation(View button, Runnable action) {
        button.setEnabled(false);

        button.animate()
                .scaleX(0.95f)
                .scaleY(0.95f)
                .setDuration(70)
                .withEndAction(() ->
                        button.animate()
                                .scaleX(1f)
                                .scaleY(1f)
                                .setDuration(90)
                                .withEndAction(() -> {
                                    button.setEnabled(true);
                                    action.run();
                                })
                                .start()
                )
                .start();
    }

    private void clearErrors() {
        tilName.setError(null);
        tilAge.setError(null);
        tilWeight.setError(null);
        tilHeight.setError(null);
    }

    private String getText(TextInputEditText editText) {
        return editText.getText() == null
                ? ""
                : editText.getText().toString().trim();
    }

    /** Returns the number typed in, or -1 if the box is empty or not a valid number. */
    private double parseNumber(String text) {
        if (TextUtils.isEmpty(text)) {
            return -1;
        }

        try {
            return Double.parseDouble(text);
        } catch (NumberFormatException e) {
            return -1;
        }
    }
}
