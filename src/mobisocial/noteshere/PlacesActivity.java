package mobisocial.noteshere;

import mobisocial.noteshere.R;

import java.io.IOException;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.google.android.gms.maps.model.LatLng;

import android.os.AsyncTask;
import android.os.Bundle;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.Filter;
import android.widget.Filterable;

public class PlacesActivity extends Activity implements OnItemClickListener, OnClickListener {
    
    public static final String TAG = "PlacesActivity";
    
    public static final String EXTRA_LATITUDE = "latitude";
    public static final String EXTRA_LONGITUDE = "longitude";
    public static final String EXTRA_DESCRIPTION = "description";
    
    private static final String PLACES_API_BASE = "https://maps.googleapis.com/maps/api/place";
    private static final String TYPE_AUTOCOMPLETE = "/autocomplete";
    private static final String TYPE_DETAILS = "/details";
    private static final String OUT_JSON = "/json";

    private static final String API_KEY = "AIzaSyCBW_poSriiYj_MQBzsXC_pfKpzMXjnhVc";
    
    private Button mContinueButton;
    private Button mClearButton;
    private AutoCompleteTextView mTextView;
    
    private Map<String, String> mReferences;
    private String mDescription;
    private LatLng mResult;
    private LatLng mInitial;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        mReferences = new HashMap<String, String>();
        
        Intent intent = getIntent();
        if (intent != null) {
            if (intent.hasExtra(EXTRA_LATITUDE) &&
                    intent.hasExtra(EXTRA_LONGITUDE)) {
                double latitude = intent.getDoubleExtra(EXTRA_LATITUDE, -500.0);
                double longitude = intent.getDoubleExtra(EXTRA_LONGITUDE, -500.0);
                mInitial = new LatLng(latitude, longitude);
            }
        }
        
        setContentView(R.layout.activity_places);
        
        mTextView = (AutoCompleteTextView)findViewById(R.id.place_complete);
        mTextView.setAdapter(new PlacesAutoCompleteAdapter(this, android.R.layout.simple_list_item_1));
        mTextView.setOnItemClickListener(this);
        
        mContinueButton = (Button)findViewById(R.id.continue_button);
        mContinueButton.setOnClickListener(this);
        
        mClearButton = (Button)findViewById(R.id.clear_button);
        mClearButton.setOnClickListener(this);
        // Show the Up button in the action bar.
        setupActionBar();
    }

    /**
     * Set up the {@link android.app.ActionBar}.
     */
    private void setupActionBar() {

        getActionBar().setDisplayHomeAsUpEnabled(true);

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.places, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
        case android.R.id.home:
            // This ID represents the Home or Up button. In the case of this
            // activity, the Up button is shown. Use NavUtils to allow users
            // to navigate up one level in the application structure. For
            // more details, see the Navigation pattern on Android Design:
            //
            // http://developer.android.com/design/patterns/navigation.html#up-vs-back
            //
            setResult(RESULT_CANCELED);
            finish();
            //NavUtils.navigateUpFromSameTask(this);
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
    
    private ArrayList<String> autocomplete(String input) {
        ArrayList<String> resultList = null;
        
        StringBuilder sb = new StringBuilder(PLACES_API_BASE + TYPE_AUTOCOMPLETE + OUT_JSON);
        sb.append("?sensor=false&key=" + API_KEY);
        if (mInitial == null) {
            sb.append("&components=country:us");
        } else {
            sb.append("&location=" + mInitial.latitude + "," + mInitial.longitude);
            sb.append("&radius=100000");
        }
        try {
            sb.append("&input=" + URLEncoder.encode(input, "utf8"));
        } catch(UnsupportedEncodingException e) {
            Log.e(TAG, "Error encoding utf8", e);
            return null;
        }
        StringBuilder jsonResults = getJsonResults(sb);
        if (jsonResults == null) {
            return null;
        }

        try {
            // Create a JSON object hierarchy from the results
            JSONObject jsonObj = new JSONObject(jsonResults.toString());
            JSONArray predsJsonArray = jsonObj.getJSONArray("predictions");
            
            // Extract the Place descriptions from the results
            resultList = new ArrayList<String>(predsJsonArray.length());
            for (int i = 0; i < predsJsonArray.length(); i++) {
                JSONObject prediction = predsJsonArray.getJSONObject(i);
                String description = prediction.getString("description");
                String reference = prediction.getString("reference");
                resultList.add(description);
                mReferences.put(description, reference);
            }
        } catch (JSONException e) {
            Log.e(TAG, "Cannot process JSON results", e);
        }
        
        return resultList;
    }
    
    private LatLng getCoordinates(String reference) {
        StringBuilder sb = new StringBuilder(PLACES_API_BASE + TYPE_DETAILS + OUT_JSON);
        sb.append("?reference=" + reference);
        sb.append("&sensor=false&key=" + API_KEY);
        StringBuilder jsonResults = getJsonResults(sb);
        if (jsonResults == null) {
            return null;
        }
        try {
            JSONObject jsonObj = new JSONObject(jsonResults.toString());
            JSONObject result = jsonObj.getJSONObject("result");
            JSONObject geometry = result.getJSONObject("geometry");
            JSONObject location = geometry.getJSONObject("location");
            LatLng latlng = new LatLng(location.getDouble("lat"), location.getDouble("lng"));
            return latlng;
        } catch (JSONException e) {
            Log.e(TAG, "Cannot process JSON results", e);
            return null;
        }
    }
    
    private StringBuilder getJsonResults(StringBuilder sb) {
        HttpURLConnection conn = null;
        StringBuilder jsonResults = new StringBuilder();
        try {
            URL url = new URL(sb.toString());
            conn = (HttpURLConnection) url.openConnection();
            InputStreamReader in = new InputStreamReader(conn.getInputStream());
            
            // Load the results into a StringBuilder
            int read;
            char[] buff = new char[1024];
            while ((read = in.read(buff)) != -1) {
                jsonResults.append(buff, 0, read);
            }
        } catch (MalformedURLException e) {
            Log.e(TAG, "Error processing Places API URL", e);
            return null;
        } catch (IOException e) {
            Log.e(TAG, "Error connecting to Places API", e);
            return null;
        } finally {
            if (conn != null) {
                conn.disconnect();
            }
        }
        return jsonResults;
    }
    
    private class PlacesAutoCompleteAdapter extends ArrayAdapter<String> implements Filterable {
        private ArrayList<String> resultList;
        
        public PlacesAutoCompleteAdapter(Context context, int textViewResourceId) {
            super(context, textViewResourceId);
        }
        
        @Override
        public int getCount() {
            return resultList.size();
        }

        @Override
        public String getItem(int index) {
            return resultList.get(index);
        }

        @Override
        public Filter getFilter() {
            Filter filter = new Filter() {
                @Override
                protected FilterResults performFiltering(CharSequence constraint) {
                    FilterResults filterResults = new FilterResults();
                    if (constraint != null) {
                        // Retrieve the autocomplete results.
                        resultList = autocomplete(constraint.toString());
                        
                        // Assign the data to the FilterResults
                        filterResults.values = resultList;
                        filterResults.count = resultList.size();
                    }
                    return filterResults;
                }

                @Override
                protected void publishResults(CharSequence constraint, FilterResults results) {
                    if (results != null && results.count > 0) {
                        notifyDataSetChanged();
                    }
                    else {
                        notifyDataSetInvalidated();
                    }
                }};
            return filter;
        }
    }

    @Override
    public void onItemClick(AdapterView<?> adapterView, View view, int position, long id) {
        String str = (String) adapterView.getItemAtPosition(position);
        mDescription = str;
        Log.d(TAG, "tapped " + str);
        String reference = mReferences.get(str);
        Log.d(TAG, "reference: " + reference);
        new LocationDetailsTask().execute(reference);
    }

    @Override
    public void onClick(View v) {
        if (v == mContinueButton) {
            Intent resultIntent = new Intent();
            resultIntent.putExtra(EXTRA_LATITUDE, mResult.latitude);
            resultIntent.putExtra(EXTRA_LONGITUDE, mResult.longitude);
            resultIntent.putExtra(EXTRA_DESCRIPTION, mDescription);
            setResult(RESULT_OK, resultIntent);
            finish();
        } else if (v == mClearButton) {
            mResult = null;
            mDescription = null;
            mTextView.dismissDropDown();
            mTextView.clearListSelection();
            mTextView.setText("");
            mContinueButton.setEnabled(false);
        }
    }
    
    private class LocationDetailsTask extends AsyncTask<String, Void, LatLng> {

        @Override
        protected LatLng doInBackground(String... params) {
            if (params.length == 0) return null;
            String reference = params[0];
            return getCoordinates(reference);
        }
        
        @Override
        protected void onPostExecute(LatLng result) {
            if (result == null) return;
            mContinueButton.setEnabled(true);
            Log.d(TAG, "found: " + result.toString());
            mResult = result;
        }
    }

}
