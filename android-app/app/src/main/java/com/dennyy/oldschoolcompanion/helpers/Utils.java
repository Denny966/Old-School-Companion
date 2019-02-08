package com.dennyy.oldschoolcompanion.helpers;

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
import android.webkit.WebView;
import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.dennyy.oldschoolcompanion.AppController;
import com.dennyy.oldschoolcompanion.customviews.ChangelogDialog;
import com.dennyy.oldschoolcompanion.customviews.InfoDialog;
import com.dennyy.oldschoolcompanion.models.Changelog.Changelogs;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.xmlpull.v1.XmlPullParserException;

import java.io.*;
import java.math.RoundingMode;
import java.net.URLEncoder;
import java.text.DecimalFormat;
import java.text.Format;
import java.text.SimpleDateFormat;
import java.util.*;

public class Utils {

    public interface VolleyCallback {
        void onSuccess(String result);

        void onError(VolleyError error);

        void always();
    }

    public static void getString(String url, String tag, final VolleyCallback callback) {
        getString(url, tag, true, callback);
    }

    public static void getString(String url, String tag, boolean increaseTimeout, final VolleyCallback callback) {
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
        }) {
            @Override
            public Map<String, String> getHeaders() {
                Map<String, String> headers = new HashMap<>();
                headers.put("User-Agent", Constants.USER_AGENT);
                return headers;
            }
        };

        AppController.getInstance().addToRequestQueue(strReq, tag, increaseTimeout);
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

    public static int occurrences(String haystack, char needle) {
        int count = 0;
        for (int i = 0; i < haystack.length(); i++) {
            if (haystack.charAt(i) == needle) {
                count++;
            }
        }
        return count;
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
            imm.hideSoftInputFromWindow(view.getApplicationWindowToken(), 0);
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
        if (imm != null && view != null)
            imm.showSoftInput(view, InputMethodManager.SHOW_IMPLICIT);
    }

    public static boolean isNumeric(String str) {
        return str.matches("-?\\d+(\\.\\d+)?");  //match a number with optional '-' and decimal.
    }

    public static boolean isMyServiceRunning(Context context, Class<?> serviceClass) {
        if (context == null) return false;
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

    public static void showChangelogs(Context context) throws IOException, XmlPullParserException {
        if (!(context instanceof AppCompatActivity)) {
            return;
        }
        Changelogs changelogs = new ChangelogParser(context).parse();
        ChangelogDialog dialog = new ChangelogDialog();
        Bundle args = new Bundle();
        args.putSerializable(ChangelogDialog.ARG_CHANGELOGS, changelogs);
        dialog.setArguments(args);
        dialog.show(((AppCompatActivity) context).getSupportFragmentManager(), Constants.FIRST_VERSION_STARTUP);
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
        catch (IOException ex) {
            if (!(ex instanceof FileNotFoundException) || !fileName.equals(Constants.ITEMIDLIST_FILE_NAME)) {
                Logger.log(ex);
            }
        }

        return ret;
    }

    public static String writeToFile(Context context, String fileName, String data) {
        try {
            OutputStreamWriter outputStreamWriter = new OutputStreamWriter(context.openFileOutput(fileName, Context.MODE_PRIVATE));
            outputStreamWriter.write(data);
            outputStreamWriter.close();
        }
        catch (IOException ex) {
            Logger.log(fileName, ex);
        }
        return data;
    }
    public static boolean isNullOrEmpty(String s) {
        return s == null || s.trim().length() == 0;
    }

    public static String readFromAssets(Context context, String filename) {
        InputStream is;
        try {
            is = context.getAssets().open(filename);
            int size = is.available();
            byte[] buffer = new byte[size];
            is.read(buffer);
            is.close();
            return new String(buffer, "UTF-8");
        }
        catch (IOException ex) {
            Logger.log(ex);
            return "";
        }
    }

    @SuppressWarnings("unchecked")
    public static <T> ArrayList<T> jsonArrayToList(Class<T> type, JSONArray jsonArray) throws JSONException {
        ArrayList<T> list = new ArrayList<>();
        if (jsonArray == null) {
            return list;
        }
        for (int i = 0; i < jsonArray.length(); i++) {
            Object o = jsonArray.get(i);
            if (type.isInstance(o)) {
                list.add((T) jsonArray.get(i));
            }
        }
        return list;
    }

    public static void clearWebView(WebView webView) {
        if (webView != null) {
            webView.clearHistory();
            webView.clearCache(true);
            webView.loadUrl("about:blank");
            webView.removeAllViews();
            webView.destroyDrawingCache();
            webView.destroy();
            webView = null;
        }
    }

    public static int tryParseInt(String number, int defaultVal) {
        try {
            return Integer.parseInt(number);
        }
        catch (NumberFormatException e) {
            return defaultVal;
        }
    }

    public static boolean between(int i, int minValueInclusive, int maxValueInclusive) {
        return (i >= minValueInclusive && i <= maxValueInclusive);
    }

    public static void executeJavaScript(WebView webView, String javaScript) {
        webView.loadUrl("javascript:(function() { " + javaScript + " })()");
    }

    public static boolean isValidContextForGlide(final Context context) {
        if (context == null) {
            return false;
        }
        if (context instanceof Activity) {
            final Activity activity = (Activity) context;
            if (activity.isDestroyed() || activity.isFinishing()) {
                return false;
            }
        }
        return true;
    }

    public static String getEncodedString(String url) {
        try {
            url = URLEncoder.encode(url, "utf-8");
        }
        catch (UnsupportedEncodingException e) {
            url = url.replace(" ", "+");
            Logger.log(e, "failed to encode url", url);
        }
        return url;
    }

    public static String capitalize(String input) {
        String[] words = input.toLowerCase().split(" ");
        StringBuilder builder = new StringBuilder();
        for (int i = 0; i < words.length; i++) {
            String word = words[i];

            if (i > 0 && word.length() > 0) {
                builder.append(" ");
            }

            String cap = word.substring(0, 1).toUpperCase() + word.substring(1);
            builder.append(cap);
        }
        return builder.toString();
    }

    public static float getDistance(float x1, float x2, float y1, float y2) {
        float dx = Math.abs(x1 - x2);
        float dy = Math.abs(y1 - y2);
        return (float) Math.sqrt(dx * dx + dy * dy);
    }
}