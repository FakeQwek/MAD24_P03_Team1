package sg.edu.np.mad.inkwell;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.drawerlayout.widget.DrawerLayout;

import com.google.android.material.navigation.NavigationView;

import java.util.ArrayList;
import java.util.List;

public class CalculatorActivity extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener {

    private List<String> calculationSequence = new ArrayList<>();
    private TextView calculation;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_calculator);


        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        //Finds nav bar drawer and nav view before setting listener
        DrawerLayout drawerLayout = findViewById(R.id.drawer_layout);
        NavigationView navigationView = findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);
        //Sets listener to allows for closing and opening of the navbar
        navigationView.bringToFront();
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(this, drawerLayout, toolbar, R.string.open_nav,
                R.string.close_nav);
        drawerLayout.addDrawerListener(toggle);
        toggle.syncState();
        calculation = findViewById(R.id.calValue);

        // Button IDs and Values
        int[] buttonIds = {
                R.id.calOne, R.id.calTwo, R.id.calThree, R.id.calFour, R.id.calFive,
                R.id.calSix, R.id.calSeven, R.id.calEight, R.id.calNine, R.id.multiplyButton,
                R.id.subtractButton, R.id.divisionButton, R.id.addButton, R.id.equalsButton,
                R.id.calClear, R.id.calReturn
        };

        String[] buttonValues = {
                "1", "2", "3", "4", "5", "6", "7", "8", "9", "x", "-", "/", "+", "=", "Clear", "Return"
        };

        for (int i = 0; i < buttonIds.length; i++) {
            Button button = findViewById(buttonIds[i]);
            button.setText(buttonValues[i]);
            button.setOnClickListener(new ButtonClickListener(buttonValues[i]));
        }

        calculation.setText("0");
    }

    private class ButtonClickListener implements View.OnClickListener {
        private String value;

        ButtonClickListener(String value) {
            this.value = value;
        }

        @Override
        public void onClick(View v) {
            switch (value) {
                case "Clear":
                    calculationSequence.clear();
                    calculation.setText("0");
                    break;
                case "Return":
                    String previous = calculationSequence.get(calculationSequence.size()-1);
                    calculationSequence.remove(previous);
                    updateDisplay();
                    break;
                case "=":
                    calculateResult();
                    break;
                default:
                    calculationSequence.add(value);
                    updateDisplay();
                    break;
            }
        }
    }

    private void calculateResult() {
        if (calculationSequence.size() < 3) {
            showError("Incomplete expression");
            return;
        }

        try {
            String expression = String.join(" ", calculationSequence);
            String[] parts = expression.split(" ");
            if (parts.length != 3) {
                showError("Invalid expression");
                return;
            }

            float num1 = Float.parseFloat(parts[0]);
            float num2 = Float.parseFloat(parts[2]);
            float result;

            switch (parts[1]) {
                case "+":
                    result = num1 + num2;
                    break;
                case "-":
                    result = num1 - num2;
                    break;
                case "x":
                    result = num1 * num2;
                    break;
                case "/":
                    if (num2 == 0) {
                        showError("Division by zero");
                        return;
                    }
                    result = num1 / num2;
                    break;
                default:
                    showError("Unknown operator");
                    return;
            }

            calculation.setText(String.format("%.2f", result));
            calculationSequence.clear();
            calculationSequence.add(String.format("%.2f", result));

        } catch (NumberFormatException e) {
            showError("Invalid number format");
        }
    }

    private void showError(String message) {
        AlertDialog.Builder alertBuilder = new AlertDialog.Builder(CalculatorActivity.this);
        alertBuilder.setMessage(message);
        alertBuilder.setCancelable(true);
        AlertDialog alert = alertBuilder.create();
        alert.show();
        calculation.setText("Error");
    }

    private void updateDisplay() {
        String displayText = String.join(" ", calculationSequence);
        calculation.setText(displayText.isEmpty() ? "0" : displayText);
    }
    public boolean onNavigationItemSelected(@NonNull MenuItem menuItem) {
        int id = menuItem.getItemId();
        Navbar navbar = new Navbar(this);
        Intent newActivity = navbar.redirect(id);
        startActivity(newActivity);
        Log.d("Alert", "Starting" + newActivity);
        return true;
    }
}