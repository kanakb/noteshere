package mobisocial.noteshere;

import java.text.DateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import mobisocial.noteshere.db.MNote;
import mobisocial.noteshere.db.NoteManager;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.GoogleMap.OnInfoWindowClickListener;
import com.google.android.gms.maps.GoogleMap.OnMyLocationChangeListener;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

import android.content.Intent;
import android.database.Cursor;
import android.location.Location;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.NavUtils;

public class NearbyActivity extends FragmentActivity {
    
    private GoogleMap mMap;
    private boolean mFoundMe;
    private NoteManager mNoteManager;
    private Map<String, Long> mMarkerNotes;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_nearby);
        // Show the Up button in the action bar.
        setupActionBar();
        
        mFoundMe = false;
        mNoteManager = new NoteManager(App.getDatabaseSource(this));
        mMarkerNotes = new HashMap<String, Long>();
        
        setUpMapIfNeeded();
    }
    
    @Override
    protected void onResume() {
        super.onResume();
        setUpMapIfNeeded();
    }
    
    private void setUpMapIfNeeded() {
        if (mMap == null) {
            mMap = ((SupportMapFragment)getSupportFragmentManager().findFragmentById(R.id.nearbyMapFragment)).getMap();
            if (mMap != null) {
                setUpMap();
            }
        }
    }
    
    private void setUpMap() {
        mMap.setOnMyLocationChangeListener(new OnMyLocationChangeListener() {
            @Override
            public void onMyLocationChange(Location location) {
                if (!mFoundMe) {
                    mFoundMe = true;
                    mMap.clear();
                    LatLng latlng = new LatLng(location.getLatitude(), location.getLongitude());
                    mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(latlng, 8.0f));
                    Cursor c = mNoteManager.getNearbyNoteCursor(latlng.latitude, latlng.longitude);
                    try {
                        while (c.moveToNext()) {
                            MNote note = mNoteManager.fillInLimitedFields(c);
                            Marker m = mMap.addMarker(new MarkerOptions()
                                .position(new LatLng(note.latitude, note.longitude)));
                            String title;
                            if (note.senderName == null || note.senderName.equals("")) {
                                title = "Me";
                            } else {
                                title = note.senderName;
                            }
                            Date date = new Date(note.timestamp);
                            DateFormat format = DateFormat.getDateTimeInstance();
                            String detailsFormat = format.format(date);
                            title += " on " + detailsFormat;
                            m.setTitle(title);
                            mMarkerNotes.put(m.getId(), note.id);
                        }
                    } finally {
                        c.close();
                    }
                }
            }
        });
        mMap.setMyLocationEnabled(true);
        mMap.setOnInfoWindowClickListener(new OnInfoWindowClickListener() {
            @Override
            public void onInfoWindowClick(Marker marker) {
                String markerId = marker.getId();
                if (mMarkerNotes.containsKey(markerId)) {
                    long noteId = mMarkerNotes.get(markerId);
                    Intent intent = new Intent(NearbyActivity.this, ViewNoteActivity.class);
                    intent.setData(App.getNoteUri(noteId));
                    NearbyActivity.this.startActivity(intent);
                }
            }
        });
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
        getMenuInflater().inflate(R.menu.nearby, menu);
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
            NavUtils.navigateUpFromSameTask(this);
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

}
