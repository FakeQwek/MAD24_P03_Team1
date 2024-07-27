package sg.edu.np.mad.inkwell;

import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import java.util.Enumeration;
import java.util.Hashtable;
import java.util.Dictionary;
import java.util.regex.Matcher;
import java.util.regex.Pattern;


public class CalculatorActivity extends AppCompatActivity {


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
        Dictionary<String, Integer> calculationSequence = new Hashtable<>();
        TextView calculation = findViewById(R.id.calValue);
        Button calReturn = findViewById(R.id.calReturn);
        Button calClear = findViewById(R.id.calClear);
        Button calOne = findViewById(R.id.calOne);
        Button calTwo = findViewById(R.id.calTwo);
        Button calThree = findViewById(R.id.calThree);
        Button calFour = findViewById(R.id.calFour);
        Button calFive = findViewById(R.id.calFive);
        Button calSix = findViewById(R.id.calSix);
        Button calSeven = findViewById(R.id.calSeven);
        Button calEight = findViewById(R.id.calEight);
        Button calNine = findViewById(R.id.calNine);
        Button calMultiply = findViewById(R.id.multiplyButton);
        Button calSubtract = findViewById(R.id.subtractButton);
        Button calDivide = findViewById(R.id.divisionButton);
        Button calAdd = findViewById(R.id.addButton);
        Button calEquals = findViewById(R.id.equalsButton);

        calculation.setText("0");
        calOne.setText("1");
        calTwo.setText("2");
        calThree.setText("3");
        calFour.setText("4");
        calFive.setText("5");
        calSix.setText("6");
        calSeven.setText("7");
        calEight.setText("8");
        calNine.setText("9");
        calMultiply.setText("X");
        calSubtract.setText("-");
        calDivide.setText("/");
        calAdd.setText("+");
        calEquals.setText("=");
        calReturn.setText("Return");
        calClear.setText("Clear");
        calReturn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                calculationSequence.put("back", 0);

            }
        });
        calClear.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Enumeration<String> k = calculationSequence.keys();
                while (k.hasMoreElements()) {
                    String key = k.nextElement();
                    calculationSequence.remove(key);

                }
                calculation.setText("0");
            }
        });
        calOne.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                calculationSequence.put("1", 0);
                updateDisplay(calculationSequence, calculation);
            }
        });
        calTwo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                calculationSequence.put("2", 0);
                updateDisplay(calculationSequence, calculation);
            }
        });
        calThree.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                calculationSequence.put("3", 0);
                updateDisplay(calculationSequence, calculation);
            }
        });
        calFour.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                calculationSequence.put("4", 0);
                updateDisplay(calculationSequence, calculation);
            }
        });
        calFive.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                calculationSequence.put("5", 0);
                updateDisplay(calculationSequence, calculation);
            }
        });
        calSix.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                calculationSequence.put("6", 0);
                updateDisplay(calculationSequence, calculation);
            }
        });
        calSeven.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                calculationSequence.put("7", 0);
                updateDisplay(calculationSequence, calculation);
            }
        });
        calEight.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                calculationSequence.put("8", 0);
                updateDisplay(calculationSequence, calculation);
            }
        });
        calNine.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                calculationSequence.put("9", 0);
                updateDisplay(calculationSequence, calculation);
            }
        });
        calSubtract.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                calculationSequence.put("-", 0);
                updateDisplay(calculationSequence, calculation);
            }
        });
        calAdd.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                calculationSequence.put("+", 0);
                updateDisplay(calculationSequence, calculation);
            }
        });
        calDivide.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                calculationSequence.put("/", 0);
                updateDisplay(calculationSequence, calculation);
            }
        });
        calMultiply.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                calculationSequence.put("x", 0);
                updateDisplay(calculationSequence, calculation);
            }

        });
        calEquals.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String displayCal = (String)calculation.getText();
                String[] piece = displayCal.split(" ");
                int firstNumber = 0;
                String operator;
                Boolean first = true;
                Boolean error = false;



                if(!isInteger(piece[0]) || !isInteger(piece[2])){
                    error = true;
                }
                else if(!(piece[1] == "-" || piece[1] == "+" || piece[1] == "/" || piece[1] == "x")) {
                    error = true;
                }
                else if (piece.length > 3) {
                    AlertDialog.Builder alertBuilder = new AlertDialog.Builder(CalculatorActivity.this);
                    alertBuilder.setMessage("Can only calculate two numbers at a time");
                    alertBuilder.setCancelable(true);
                    AlertDialog calculationAlert = alertBuilder.create();
                    calculationAlert.show();
                    error = true;
                }
                else {
                    Float no1 = Float.parseFloat(piece[0]);
                    Float no2 = Float.parseFloat(piece[2]);
                    if(piece[1] == "-") {
                        calculation.setText(String.format("%.2f", no1 - no2));
                    }
                    else if(piece[1] == "x") {
                        calculation.setText(String.format("%.2f", no1 * no2));
                    }
                    else if (piece[1] == "/") {
                        calculation.setText(String.format("%.2f", no1 / no2));
                    }
                    else if (piece[1] == "+") {
                        calculation.setText(String.format("%.2f", no1 + no2));
                    }
                }

                if(error) {
                    calculation.setText("Error");
                }
            }
        });


    }

    static Boolean isInteger(String string) {
        try {
            Integer.valueOf(string);
            return true;
        } catch (NumberFormatException e) {
            return false;
        }
    }
    static void updateDisplay(Dictionary<String, Integer> calculationSequence, TextView calculation) {

        Enumeration<String> k = calculationSequence.keys();
        String calculationText = "";
        Boolean first = true;
        String[] calChars = new String[3];
        int count = 0;
        //Iterates through each element of the calculation sequence
        while (k.hasMoreElements()) {
            String key = k.nextElement();
            calChars[count] = key;
            count++;


        }

        int count2 = 0;
        while(count2 < calChars.length -1) {
            if(first) {
                calculationText += calChars[(calChars.length-1) - count2];
                first = false;
                count2++;
                continue;
            }
            /*
            else if(Character.isDigit(calculationText.charAt(calculationText.length()-1))) {
                calculationText += calChars[count2];
                count2++;
                continue;
            }
            */

            calculationText += " " + calChars[count2];
            count2++;

        }

        calculation.setText(calculationText);


    }


}