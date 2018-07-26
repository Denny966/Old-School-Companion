package com.dennyy.osrscompanion.helpers;

import android.app.Activity;
import android.app.ActivityManager;
import android.content.Context;
import android.content.res.Resources;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.support.v7.app.AppCompatActivity;
import android.util.DisplayMetrics;
import android.view.View;
import android.view.inputmethod.InputMethodManager;

import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.dennyy.osrscompanion.AppController;
import com.dennyy.osrscompanion.customviews.InfoDialog;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.math.RoundingMode;
import java.text.DecimalFormat;
import java.text.Format;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Iterator;
import java.util.Locale;

public class Utils {

    public interface VolleyCallback {
        void onSuccess(String result);

        void onError(VolleyError error);

        void always();
    }

    public static void getString(String url, String tag, final VolleyCallback callback) {
        StringRequest strReq = new StringRequest(Request.Method.GET, url, new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                callback.onSuccess(response);
                callback.always();
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                callback.onError(error);
                callback.always();
            }
        });
        AppController.getInstance().addToRequestQueue(strReq, tag);
    }

    public static String formatNumber(long number) {
        DecimalFormat df = new DecimalFormat("#,###", Constants.LOCALE);
        df.setRoundingMode(RoundingMode.CEILING);
        return df.format(number);
    }

    public static String convertTime(long time) {
        return convertTime(time, "EEE, d MMM yyyy HH:mm:ss");
    }

    public static String convertTime(long time, String pattern) {
        Date date = new Date(time);
        Format format = new SimpleDateFormat(pattern, Locale.getDefault());
        return format.format(date);
    }

    public static String repeat(String str, int times) {
        return new String(new char[times]).replace("\0", str);
    }

    public static double eval(String str) {

        str = str.replace('x', '*');
        final String finalStr = str;
        return new Object() {
            int pos = -1, ch;

            void nextChar() {
                ch = (++pos < finalStr.length()) ? finalStr.charAt(pos) : -1;
            }

            boolean eat(int charToEat) {
                while (ch == ' ')
                    nextChar();
                if (ch == charToEat) {
                    nextChar();
                    return true;
                }
                return false;
            }

            double parse() {
                nextChar();
                double x = parseExpression();
                if (pos < finalStr.length())
                    throw new RuntimeException("Unexpected: " + (char) ch);
                return x;
            }

            // Grammar:
            // expression = term | expression `+` term | expression `-` term
            // term = factor | term `*` factor | term `/` factor
            // factor = `+` factor | `-` factor | `(` expression `)`
            //        | number | functionName factor | factor `^` factor

            double parseExpression() {
                double x = parseTerm();
                for (; ; ) {
                    if (eat('+'))
                        x += parseTerm(); // addition
                    else if (eat('-'))
                        x -= parseTerm(); // subtraction
                    else
                        return x;
                }
            }

            double parseTerm() {
                double x = parseFactor();
                for (; ; ) {
                    if (eat('*'))
                        x *= parseFactor(); // multiplication
                    else if (eat('/'))
                        x /= parseFactor(); // division
                    else
                        return x;
                }
            }

            double parseFactor() {
                if (eat('+'))
                    return parseFactor(); // unary plus
                if (eat('-'))
                    return -parseFactor(); // unary minus

                double x;
                int startPos = this.pos;
                if (eat('(')) { // parentheses
                    x = parseExpression();
                    eat(')');
                }
                else if ((ch >= '0' && ch <= '9') || ch == '.') { // numbers
                    while ((ch >= '0' && ch <= '9') || ch == '.')
                        nextChar();
                    x = Double.parseDouble(finalStr.substring(startPos, this.pos));
                }
                else if (ch >= 'a' && ch <= 'z') { // functions
                    while (ch >= 'a' && ch <= 'z')
                        nextChar();
                    String func = finalStr.substring(startPos, this.pos);
                    x = parseFactor();
                    if (func.equals("sqrt"))
                        x = Math.sqrt(x);
                    else if (func.equals("sin"))
                        x = Math.sin(Math.toRadians(x));
                    else if (func.equals("cos"))
                        x = Math.cos(Math.toRadians(x));
                    else if (func.equals("tan"))
                        x = Math.tan(Math.toRadians(x));
                    else
                        throw new RuntimeException("Unknown function: " + func);
                }
                else {
                    throw new RuntimeException("Unexpected: " + (char) ch);
                }

                if (eat('^'))
                    x = Math.pow(x, parseFactor()); // exponentiation

                return x;
            }
        }.parse();
    }


    /**
     * This method converts dp unit to equivalent pixels, depending on device density.
     *
     * @param dp      A value in dp (density independent pixels) unit. Which we need to convert into pixels
     * @param context Context to get resources and device specific display metrics
     * @return A float value to represent px equivalent to dp depending on device density
     */
    public static float convertDpToPixel(float dp, Context context) {
        Resources resources = context.getResources();
        DisplayMetrics metrics = resources.getDisplayMetrics();
        float px = dp * ((float) metrics.densityDpi / DisplayMetrics.DENSITY_DEFAULT);
        return px;
    }

    /**
     * This method converts device specific pixels to density independent pixels.
     *
     * @param px      A value in px (pixels) unit. Which we need to convert into db
     * @param context Context to get resources and device specific display metrics
     * @return A float value to represent dp equivalent to px value
     */
    public static float convertPixelsToDp(float px, Context context) {
        Resources resources = context.getResources();
        DisplayMetrics metrics = resources.getDisplayMetrics();
        float dp = px / ((float) metrics.densityDpi / DisplayMetrics.DENSITY_DEFAULT);
        return dp;
    }

    public static void hideKeyboard(Context context, View view) {
        InputMethodManager imm = (InputMethodManager) context.getSystemService(Context.INPUT_METHOD_SERVICE);
        if (imm != null && view != null)
            imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
    }

    public static void hideKeyboard(Activity activity) {
        InputMethodManager inputManager = (InputMethodManager) activity.getSystemService(Context.INPUT_METHOD_SERVICE);

        // check if no view has focus:
        View currentFocusedView = activity.getCurrentFocus();
        if (currentFocusedView != null && inputManager != null) {
            inputManager.hideSoftInputFromWindow(currentFocusedView.getWindowToken(), InputMethodManager.HIDE_NOT_ALWAYS);
        }
    }


    public static void showKeyboard(Context context, View view) {
        InputMethodManager imm = (InputMethodManager) context.getSystemService(Context.INPUT_METHOD_SERVICE);
        if (imm != null)
            imm.showSoftInput(view, InputMethodManager.SHOW_IMPLICIT);
    }

    public static boolean isNumeric(String str) {
        return str.matches("-?\\d+(\\.\\d+)?");  //match a number with optional '-' and decimal.
    }

    public static boolean isMyServiceRunning(Context context, Class<?> serviceClass) {
        ActivityManager manager = (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);
        for (ActivityManager.RunningServiceInfo service : manager.getRunningServices(Integer.MAX_VALUE)) {
            if (serviceClass.getName().equals(service.service.getClassName())) {
                return true;
            }
        }
        return false;
    }

    public static boolean containsCaseInsensitive(String s, String[] l) {
        for (String string : l) {
            if (string.equalsIgnoreCase(s)) {
                return true;
            }
        }
        return false;
    }

    public static boolean containsCaseInsensitive(String s, String l) {
        return l.toLowerCase().contains(s.toLowerCase());
    }

    public static String trimMessage(String json, String key) {
        String trimmedString;

        try {
            JSONObject obj = new JSONObject(json);
            trimmedString = obj.getString(key);
        }
        catch (JSONException e) {
            return null;
        }

        return trimmedString;
    }

    public static void showDialog(Context context, String title, String message) {
        Utils.showDialog(context, title, message, false);
    }

    public static void showDialog(Context context, String title, String message, boolean showCancelButton) {
        if (!(context instanceof AppCompatActivity)) {
            return;
        }
        DialogFragment dialog = new InfoDialog();
        Bundle args = new Bundle();
        args.putString(InfoDialog.ARG_TITLE, title);
        args.putString(InfoDialog.ARG_MESSAGE, message);
        args.putBoolean(InfoDialog.ARG_SHOW_CANCEL, showCancelButton);
        dialog.setArguments(args);
        dialog.show(((AppCompatActivity) context).getSupportFragmentManager(), "");
    }

    /**
     * Joins array of object to single string by separator
     * <p>
     * Credits to kurellajunior on this post
     * http://snippets.dzone.com/posts/show/91
     *
     * @param iterable  any kind of iterable ex.: <code>["a", "b", "c"]</code>
     * @param separator separetes entries ex.: <code>","</code>
     * @return joined string ex.: <code>"a,b,c"</code>
     */
    public static String join(Iterable<?> iterable, String separator) {
        Iterator<?> oIter;
        if (iterable == null || (!(oIter = iterable.iterator()).hasNext()))
            return "";
        StringBuilder oBuilder = new StringBuilder(String.valueOf(oIter.next()));
        while (oIter.hasNext())
            oBuilder.append(separator).append(oIter.next());
        return oBuilder.toString();
    }

    public static CharSequence[] seperate(CharSequence val, String separator) {
        if (val == null || "".equals(val)) {
            return new CharSequence[0];
        }
        else {
            return ((String) val).split(separator);
        }
    }

    public static String readFromFile(Context context, String fileName) {
        String ret = "";

        try {
            InputStream inputStream = context.openFileInput(fileName);

            if (inputStream != null) {
                InputStreamReader inputStreamReader = new InputStreamReader(inputStream);
                BufferedReader bufferedReader = new BufferedReader(inputStreamReader);
                String receiveString;
                StringBuilder stringBuilder = new StringBuilder();

                while ((receiveString = bufferedReader.readLine()) != null) {
                    stringBuilder.append(receiveString);
                    stringBuilder.append(System.lineSeparator());
                }

                inputStream.close();
                ret = stringBuilder.toString();
            }
        }
        catch (IOException ignored) {
        }

        return ret;
    }

    public static boolean writeToFile(Context context, String fileName, String data) {
        try {
            OutputStreamWriter outputStreamWriter = new OutputStreamWriter(context.openFileOutput(fileName, Context.MODE_PRIVATE));
            outputStreamWriter.write(data);
            outputStreamWriter.close();
            return true;
        }
        catch (IOException e) {
            return false;
        }
    }
    public static boolean isNullOrEmpty(String s) {
        return s == null || s.trim().length() == 0;
    }
}