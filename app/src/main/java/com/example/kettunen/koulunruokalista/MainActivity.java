package com.example.kettunen.koulunruokalista;

import android.app.ListActivity;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.SimpleAdapter;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;

public class MainActivity extends ListActivity {

    private static final String TAG_RESTAURANTNAME = "RestaurantName";
    private static final String TAG_MENUSFORDAYS = "MenusForDays";
    private static final String TAG_SETMENUS = "SetMenus";
    private static final String TAG_NAME = "Name";
    private static final String TAG_COMPONENTS = "Components";
    private static final String TAG_DATE = "Date";

    Button buttonPreviousDay;
    Button buttonNextDay;

    JSONArray menusfordays = null;
    ListView lv;

    int dayToGet = 0;

    SimpleDateFormat sdf;
    String currentDate;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        buttonPreviousDay = (Button) findViewById(R.id.buttonPreviousDay);
        buttonNextDay = (Button) findViewById(R.id.buttonNextDay);

        sdf = new SimpleDateFormat("yyyy-MM-dd");
        currentDate = sdf.format(new Date());

        buttonPreviousDay.setEnabled(false);

        buttonPreviousDay.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                if (dayToGet > 0) {
                    dayToGet -= 1;

                    if (dayToGet == 0){
                        buttonPreviousDay.setEnabled(false);
                    }

                    // Starting the task. Pass an url as the parameter.
                    String url = "http://www.amica.fi/modules/json/json/Index?costNumber=0235&firstDay=" + currentDate + "&language=fi";
                    new ParseTask().execute(url);
                }
            }
        });

        buttonNextDay.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {

                if (!buttonPreviousDay.isEnabled()){
                    buttonPreviousDay.setEnabled(true);
                }

                dayToGet += 1;
                lv = getListView();

                // Starting the task. Pass an url as the parameter.
                String url = "http://www.amica.fi/modules/json/json/Index?costNumber=0235&firstDay=" + currentDate + "&language=fi";
                new ParseTask().execute(url);
            }
        });

        lv = getListView();

        // Starting the task. Pass an url as the parameter.
        String url = "http://www.amica.fi/modules/json/json/Index?costNumber=0235&firstDay=" + currentDate + "&language=fi";
        new ParseTask().execute(url);
    }

    private class ParseTask extends AsyncTask<String, Void, ArrayList<HashMap<String, String>>> {
        @Override
        protected void onPreExecute() {
            ProgressBar bar=(ProgressBar)findViewById(R.id.progressbar);
            bar.setVisibility(View.VISIBLE);
        }

        @Override
        protected ArrayList<HashMap<String, String>> doInBackground(String... params) {
            String url = params[0];

            // Creating JSON Parser instance
            JSONParser jParser = new JSONParser();

            // getting JSON string from URL
            JSONObject json = jParser.getJSONFromUrl(url);

            // Hashmap for ListView
            ArrayList<HashMap<String, String>> menuList = new ArrayList<HashMap<String, String>>();

            try {
                String restaurantname = json.getString(TAG_RESTAURANTNAME);

                menusfordays = json.getJSONArray(TAG_MENUSFORDAYS);
                JSONObject object = menusfordays.getJSONObject(dayToGet);
                String date = object.getString(TAG_DATE);
                String[] dateParse = date.split("T");
                date = dateParse[0];

                HashMap<String, String> map = new HashMap<String, String>();
                map.put(TAG_RESTAURANTNAME, restaurantname);
                map.put(TAG_NAME, date);
                menuList.add(map);

                JSONArray setmenus = object.getJSONArray(TAG_SETMENUS);
                for (int i = 0; i < setmenus.length(); i++) {
                    JSONObject object2 = setmenus.getJSONObject(i);
                    String name = object2.getString(TAG_NAME);
                    String components = object2.getString(TAG_COMPONENTS);
                    String[] separated = components.split("\"");
                    components = separated[1];

                    // creating new HashMap
                    map = new HashMap<String, String>();

                    // adding each child node to HashMap key => value
                    map.put(TAG_NAME, name);
                    map.put(TAG_COMPONENTS, components);

                     // adding HashList to ArrayList
                        menuList.add(map);
                    }

            } catch (JSONException e) {
                e.printStackTrace();
            }

            return menuList;
        }

        @Override
        protected void onPostExecute(ArrayList<HashMap<String, String>> menuList) {
            /**
             * Updating parsed JSON data into ListView
             * */
            ListAdapter adapter = new SimpleAdapter(
                    MainActivity.this, menuList,
                    R.layout.list_item, new String[] {  TAG_RESTAURANTNAME, TAG_NAME, TAG_COMPONENTS}, new int[] { R.id.restaurant_name ,R.id.name,
                    R.id.components });

            lv.setAdapter(adapter);

            ProgressBar bar=(ProgressBar)findViewById(R.id.progressbar);
            bar.setVisibility(View.INVISIBLE);
        }
    }

}
