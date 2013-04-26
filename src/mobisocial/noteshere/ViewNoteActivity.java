package mobisocial.noteshere;

import java.util.Date;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;

import mobisocial.noteshere.db.MNote;
import mobisocial.noteshere.db.NoteManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.NavUtils;

public class ViewNoteActivity extends FragmentActivity {
    
    private MNote mNote;
    
    private GoogleMap mMap;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_view_note);
        // Show the Up button in the action bar.
        setupActionBar();
        
        if (getIntent() == null || getIntent().getData() == null) return;
        
        int noteId = Integer.parseInt(getIntent().getData().getLastPathSegment());
        
        NoteManager nm = new NoteManager(App.getDatabaseSource(this));
        mNote = nm.getNote(noteId);
        
        if (mNote == null) return;
        
        TextView title = (TextView)findViewById(R.id.viewNoteTitle);
        TextView text = (TextView)findViewById(R.id.viewNoteText);
        ImageView attachment = (ImageView)findViewById(R.id.attachmentView);
        
        title.setText(mNote.senderName + " on " + new Date(mNote.timestamp));
        if (mNote.text != null) {
            text.setText(mNote.text);
        } else {
            text.setVisibility(View.GONE);
        }
        
        if (mNote.attachment != null) {
            Bitmap bm = BitmapFactory.decodeByteArray(mNote.attachment, 0, mNote.attachment.length);
            attachment.setImageBitmap(bm);
        }
        
        setUpMapIfNeeded();
    }
    
    @Override
    protected void onResume() {
        super.onResume();
        
        setUpMapIfNeeded();
    }
    
    private void setUpMapIfNeeded() {
        if (mMap == null) {
            mMap = ((SupportMapFragment)getSupportFragmentManager().findFragmentById(R.id.viewMapFragment)).getMap();
            if (mMap != null) {
                setUpMap();
            }
        }
    }
    
    private void setUpMap() {
        if (mNote == null) return;
        
        LatLng latlng = new LatLng(mNote.latitude, mNote.longitude);
        mMap.addMarker(new MarkerOptions().position(latlng).title(mNote.senderName));
        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(latlng, 14.0f));
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
        getMenuInflater().inflate(R.menu.view_note, menu);
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
