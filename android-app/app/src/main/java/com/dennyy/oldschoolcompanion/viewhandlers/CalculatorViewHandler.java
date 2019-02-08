package com.dennyy.oldschoolcompanion.viewhandlers;

import android.content.Context;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import com.dennyy.oldschoolcompanion.R;
import com.dennyy.oldschoolcompanion.helpers.Constants;
import com.dennyy.oldschoolcompanion.helpers.Utils;

import net.objecthunter.exp4j.Expression;
import net.objecthunter.exp4j.ExpressionBuilder;

import java.math.BigDecimal;
import java.text.DecimalFormat;
import java.text.NumberFormat;

public class CalculatorViewHandler extends BaseViewHandler {
    public String equation;
    public String answer;
    public boolean lastNumeric;
    public boolean stateError;
    public boolean lastDot;
    public boolean calculated;

    private EditText equationDisplay;
    private TextView answerDisplay;
    private NumberFormat numberFormat = new DecimalFormat("##,###.##########", Constants.LOCALE);
    private NumberFormat scientificFormat = new DecimalFormat("0.###E0", Constants.LOCALE);

    public CalculatorViewHandler(Context context, View view) {
        super(context, view);
        updateView(view);
    }

    @Override
    public boolean wasRequesting() {
        return false;
    }

    public void updateView(View view) {
        equationDisplay = view.findViewById(R.id.calc_display);
        answerDisplay = view.findViewById(R.id.calc_answer);
        setNumericOnClickListener(view);
        setOperatorOnClickListener(view);
    }

    public void reloadData() {
        equationDisplay.setText(equation);
        answerDisplay.setText(answer);
        updateAnswers();
    }

    private void setNumericOnClickListener(View view) {
        // Create a common OnClickListener
        View.OnClickListener listener = new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Just append/set the text of clicked button
                Button button = (Button) v;
                int length = equationDisplay.getText().length();
                equationDisplay.setSelection(length, length);
                if (calculated) {
                    clearScreen();
                    equationDisplay.setText(button.getText());
                }
                else if (stateError) {
                    // If current state is Error, replace the error message
                    equationDisplay.setText(button.getText());
                    stateError = false;
                }
                else {
                    // If not, already there is a valid expression so append to it
                    equationDisplay.append(button.getText());
                }
                updateAnswers();
                // Set the flag
                lastNumeric = true;
            }
        };
        int[] numericIds = { R.id.zero, R.id.one, R.id.two, R.id.three, R.id.four, R.id.five, R.id.six, R.id.seven, R.id.eight, R.id.nine, R.id.bracket_open, R.id.bracket_close };

        // Assign the listener to all the numeric buttons
        for (int id : numericIds) {
            view.findViewById(id).setOnClickListener(listener);
        }
    }

    private void setOperatorOnClickListener(View view) {
        // Create a common OnClickListener for operators
        View.OnClickListener listener = new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // If the current state is Error do not append the operator
                // If the last input is number only, append the operator
                if (calculated) {
                    Button button = (Button) v;
                    continueWithAnswer(button.getText().toString());
                }
                else if (lastNumeric && !stateError) {
                    Button button = (Button) v;
                    equationDisplay.append(button.getText());
                    lastNumeric = false;
                    lastDot = false;    // Reset the DOT flag
                    updateAnswers();
                }

            }
        };

        int[] operatorIds = { R.id.plus, R.id.minus, R.id.multiply, R.id.divide };
        // Assign the listener to all the operator buttons
        for (int id : operatorIds) {
            view.findViewById(id).setOnClickListener(listener);
        }
        // Decimal point
        view.findViewById(R.id.dot).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (lastNumeric && !stateError && !lastDot) {
                    equationDisplay.append(".");
                    lastNumeric = false;
                    lastDot = true;
                    updateAnswers();
                }
            }
        });
        // Clear button
        view.findViewById(R.id.ac).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                clearScreen();
                updateAnswers();
            }
        });
        // Back button
        view.findViewById(R.id.calc_back).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Reset all the states and flags
                int length = equationDisplay.getText().length();
                if (length < 1)
                    return;
                equationDisplay.setText(equationDisplay.getText().toString().substring(0, length - 1));
                length = equationDisplay.getText().length();
                boolean lastNumber = length < 1 || Utils.isNumeric(equationDisplay.getText().toString().substring(length - 1));
                lastNumeric = lastNumber;
                stateError = false;
                lastDot = !lastNumber;
                updateAnswers();
            }
        });
        // Equal button
        view.findViewById(R.id.equal).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onEqual();
            }
        });
    }

    private void onEqual() {
        // If the current state is error, nothing to do.
        // If the last input is a number only, solution can be found.
        if (lastNumeric && !stateError) {
            // Read the expression
            String txt = fixExpression(equationDisplay.getText().toString().replace('x', '*').replace(",", ""));
            try {
                Expression expression = new ExpressionBuilder(fixExpression(txt)).build();
                answerDisplay.setTextColor(resources.getColor(R.color.text));
                BigDecimal answer = new BigDecimal(expression.evaluate());
                if (answer.compareTo(new BigDecimal(Integer.MAX_VALUE)) == 1)
                    answerDisplay.setText(scientificFormat.format(new BigDecimal(expression.evaluate())));
                else
                    answerDisplay.setText(numberFormat.format(new BigDecimal(expression.evaluate())));
                calculated = true;
            }
            catch (ArithmeticException | IllegalArgumentException ignored) {
                answerDisplay.setTextColor(resources.getColor(R.color.red));
                answerDisplay.setText(resources.getString(R.string.invalid_input));
            }
            finally {
                updateAnswers();
            }
        }
    }

    private void continueWithAnswer(String operand) {
        String answer = answerDisplay.getText().toString();
        clearScreen();
        equationDisplay.setText(answer + operand);
        updateAnswers();
    }

    private void clearScreen() {
        equationDisplay.setText("");
        answerDisplay.setText("");
        lastNumeric = false;
        lastDot = false;
        calculated = false;
    }

    private void updateAnswers() {
        answer = answerDisplay.getText().toString();
        equation = equationDisplay.getText().toString();
    }

    //Handles fixing the expression before parsing. Adding parens, making sure parens can multiply with each other,
    private String fixExpression(String exp) {
        int openParens = 0;
        int closeParens = 0;
        char openP = '(';
        char closeP = ')';
        String expr = exp;
        for (int i = 0; i < exp.length(); i++) {
            if (exp.charAt(i) == openP)
                openParens++;
            else if (exp.charAt(i) == closeP)
                closeParens++;
        }
        while (openParens > 0) {
            expr += closeP;
            openParens--;
        }
        while (closeParens > 0) {
            expr = openP + expr;
            closeParens--;
        }
        expr = multiplicationForParens(expr);
        return expr;
    }

    //Used to fix multiplication between parentheses
    private String multiplicationForParens(String s) {
        String fixed = "";
        for (int position = 0; position < s.length(); position++) {
            fixed += s.charAt(position);
            if (position == s.length() - 1)
                continue;
            if (s.charAt(position) == ')' && s.charAt(position + 1) == '(')
                fixed += '*';
            if (s.charAt(position) == '(' && s.charAt(position + 1) == ')')
                fixed += '1';
        }
        return fixed;
    }

    @Override
    public void cancelRunningTasks() {

    }
}
