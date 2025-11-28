/*  Starter project for Mobile Platform Development - 1st diet 25/26
    You should use this project as the starting point for your assignment.
    This project simply reads the data from the required URL and displays the
    raw data in a TextField
*/

//
// Name                 Euan Scott
// Student ID           S2132201
// Programme of Study   Software Development
//

// UPDATE THE PACKAGE NAME to include your Student Identifier
package org.me.gcu.scott_euan_s2132201;

import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.content.Context;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.EditText;
import android.view.View.OnClickListener;
import androidx.appcompat.app.AppCompatActivity;
import android.widget.ListView;
import android.widget.AdapterView;
import android.widget.Toast;
import android.content.Intent;
import androidx.core.view.WindowCompat;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import android.os.Handler;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;
import org.xmlpull.v1.XmlPullParserFactory;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.StringReader;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.List;
import java.util.Date;
import java.text.DateFormat;


public class MainActivity extends AppCompatActivity implements OnClickListener {
    private TextView rawDataDisplay;
    private Button startButton;
    private String result;
    private ListView ratesListView;
    private List<CurrencyRate> currencyRates = new ArrayList<>();
    private List<CurrencyRate> fullCurrencyRates = new ArrayList<>();
    private RateAdapter ratesAdapter;
    private static final long REFRESH_INTERVAL_MS = 60 * 1000; // 5 minutes
    private Handler refreshHandler = new Handler();
    private Runnable refreshRunnable;

    private String urlSource="https://www.fx-exchange.com/gbp/rss.xml";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        WindowCompat.setDecorFitsSystemWindows(getWindow(), false);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        View root = findViewById(R.id.rootLayout);
        ViewCompat.setOnApplyWindowInsetsListener(root, (view, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            view.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        rawDataDisplay = findViewById(R.id.rawDataDisplay);
        startButton = findViewById(R.id.startButton);
        startButton.setOnClickListener(this);

        ratesListView = findViewById(R.id.ratesListView);

        // adapter uses currencyRates
        ratesAdapter = new RateAdapter(this, currencyRates);
        ratesListView.setAdapter(ratesAdapter);

        ratesListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                CurrencyRate selected = currencyRates.get(position);
                Toast.makeText(MainActivity.this,
                        "Selected: " + selected.getCode(),
                        Toast.LENGTH_SHORT).show();

                Intent intent = new Intent(MainActivity.this, CurrencyConvertor.class);
                intent.putExtra("code", selected.getCode());
                intent.putExtra("name", selected.getName());
                intent.putExtra("rate", selected.getRateToGbp());
                startActivity(intent);
            }
        });

        EditText searchBar = findViewById(R.id.searchBar);
        searchBar.addTextChangedListener(new android.text.TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                String query = s.toString().toLowerCase();

                currencyRates.clear();

                if (query.isEmpty()) {
                    currencyRates.addAll(fullCurrencyRates);
                } else {
                    for (CurrencyRate cr : fullCurrencyRates) {
                        String text = (cr.getCode() + " " + cr.getName() + " " + cr.getRateToGbp())
                                .toLowerCase();
                        if (text.contains(query)) {
                            currencyRates.add(cr);
                        }
                    }
                }

                ratesAdapter.notifyDataSetChanged();
            }

            @Override
            public void afterTextChanged(android.text.Editable s) {}
        });

        if (savedInstanceState != null) {
            result = savedInstanceState.getString("result", "");

            ArrayList<String> savedList = savedInstanceState.getStringArrayList("rates");
            if (savedList != null) {
                currencyRates.clear();
                fullCurrencyRates.clear();

                for (String item : savedList) {
                    String[] parts = item.split("\\|");
                    if (parts.length == 3) {
                        String code = parts[0];
                        String name = parts[1];
                        double rate = Double.parseDouble(parts[2]);

                        CurrencyRate cr = new CurrencyRate(code, name, rate);
                        currencyRates.add(cr);
                        fullCurrencyRates.add(cr);
                    }
                }

                ratesAdapter.notifyDataSetChanged();
                rawDataDisplay.setText(
                        "Loaded " + currencyRates.size() + " exchange rates @ " + java.time.LocalTime.now().withNano(0)
                );
            }
        }
        // ---- Auto-refresh setup ----
        refreshRunnable = new Runnable() {
            @Override
            public void run() {
             startProgress();

             // Schedule the next refresh
             refreshHandler.postDelayed(this, REFRESH_INTERVAL_MS);
            }
        };

        refreshHandler.postDelayed(refreshRunnable, REFRESH_INTERVAL_MS);
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);

        outState.putString("result", result);

        ArrayList<String> savedList = new ArrayList<>();
        for (CurrencyRate cr : currencyRates) {
            savedList.add(cr.getCode() + "|" + cr.getName() + "|" + cr.getRateToGbp());
        }
        outState.putStringArrayList("rates", savedList);
    }

    private boolean isNetworkAvailable() {
        ConnectivityManager cm =
                (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        if (cm == null) {
            return false;
        }
        NetworkInfo activeNetwork = cm.getActiveNetworkInfo();
        return activeNetwork != null && activeNetwork.isConnected();
    }

    public void onClick(View aview)
    {
        startProgress();
    }

    public void startProgress()
    {
        // Check internet connection before starting download
        if (!isNetworkAvailable()) {
            Toast.makeText(this,
                    "No internet connection. Please connect and try again.",
                    Toast.LENGTH_LONG).show();
            rawDataDisplay.setText("No internet connection.");
            return;
        }

        new Thread(new Task(urlSource)).start();
    }


    private void processItem(String title, String description) {
        if (title == null || description == null) {
            return;
        }

        try {
            // Get the rate from the description
            String[] parts = description.split("=");
            if (parts.length < 2) {
                return;
            }

            String rightSide = parts[1].trim();
            String[] tokens = rightSide.split(" ");
            if (tokens.length < 1) {
                return;
            }

            double rate = Double.parseDouble(tokens[0]);

            String code = "???";
            int open = title.lastIndexOf('(');
            int close = title.lastIndexOf(')');
            if (open != -1 && close != -1 && close > open + 1) {
                code = title.substring(open + 1, close);
            }

            String name = title.trim();

            CurrencyRate cr = new CurrencyRate(code, name, rate);
            currencyRates.add(cr);

        } catch (Exception e) {
            Log.e("Parsing", "Error parsing item: " + e.getMessage());
        }
    }


    private class Task implements Runnable
    {
        private String url;
        public Task(String aurl){
            url = aurl;
        }
        @Override
        public void run(){
            URL aurl;
            URLConnection yc;
            BufferedReader in = null;
            String inputLine = "";

            result = "";

            try
            {
                Log.d("MyTask","in try");
                aurl = new URL(url);
                yc = aurl.openConnection();
                in = new BufferedReader(new InputStreamReader(yc.getInputStream()));
                while ((inputLine = in.readLine()) != null){
                    result = result + inputLine;
                }
                in.close();
            }
            catch (IOException ae) {
                Log.e("MyTask", "ioexception");
            }

            if (result == null || result.isEmpty()) {
                MainActivity.this.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        rawDataDisplay.setText("Error: no data downloaded. Check your internet connection.");
                        Toast.makeText(MainActivity.this,
                                "Error downloading data. Please check your internet connection.",
                                Toast.LENGTH_LONG).show();
                    }
                });
                return; // stop here, don't try to parse
            }

            int i = result.indexOf("<?"); //initial tag
            result = result.substring(i);


            i = result.indexOf("</rss>"); //final tag
            result = result.substring(0, i + 6);


            try {
                XmlPullParserFactory factory = XmlPullParserFactory.newInstance();
                factory.setNamespaceAware(true);
                XmlPullParser xpp = factory.newPullParser();
                xpp.setInput(new StringReader(result));

                // Clears any previous data
                currencyRates.clear();

                boolean insideItem = false;
                String currentTitle = null;
                String currentDescription = null;

                int eventType = xpp.getEventType();
                while (eventType != XmlPullParser.END_DOCUMENT) {
                    if (eventType == XmlPullParser.START_TAG) {
                        String tagName = xpp.getName();

                        if (tagName.equalsIgnoreCase("item")) {
                            insideItem = true;
                            currentTitle = null;
                            currentDescription = null;
                        } else if (insideItem && tagName.equalsIgnoreCase("title")) {
                            currentTitle = xpp.nextText();
                        } else if (insideItem && tagName.equalsIgnoreCase("description")) {
                            currentDescription = xpp.nextText();
                        }

                    } else if (eventType == XmlPullParser.END_TAG) {
                        String tagName = xpp.getName();

                        if (tagName.equalsIgnoreCase("item") && insideItem) {
                            processItem(currentTitle, currentDescription);
                            insideItem = false;
                        }
                    }

                    eventType = xpp.next();
                }

            } catch (XmlPullParserException e) {
                Log.e("Parsing", "EXCEPTION" + e);
            } catch (IOException e) {
                Log.e("Parsing", "I/O EXCEPTION" + e);
            }

            MainActivity.this.runOnUiThread(new Runnable() {
                public void run() {
                    Log.d("UI thread", "I am the UI thread");

                    fullCurrencyRates.clear();
                    fullCurrencyRates.addAll(currencyRates);

                    // notify adapter that the underlying list changed
                    ratesAdapter.notifyDataSetChanged();

                    String time = DateFormat.getTimeInstance(DateFormat.MEDIUM).format(new Date());
                    rawDataDisplay.setText(
                            "Loaded " + currencyRates.size() + " exchange rates @ " + time
                    );
                }
            });
        }

    }
    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (refreshHandler != null && refreshRunnable != null) {
            refreshHandler.removeCallbacks(refreshRunnable);
        }
    }


}