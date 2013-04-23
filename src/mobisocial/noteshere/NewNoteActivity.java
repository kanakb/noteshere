package mobisocial.noteshere;

import java.io.File;
import java.util.Random;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
//import com.google.android.gms.maps.model.LatLng;
//import com.google.android.gms.maps.model.MarkerOptions;

import android.location.Location;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.content.Intent;
import android.view.Menu;
import android.view.MenuItem;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.NavUtils;

public class NewNoteActivity extends FragmentActivity {
    public static final String TAG = "NewNoteActivity";
    
    private static final int GALLERY_REQUEST_CODE = 1;
    private static final int CAMERA_REQUEST_CODE = 2;
    
    private String mFilename;
    
    //private LocationHelper mLocHelper;
    //private Location mLocation;
    
    private GoogleMap mMap;
    
    private boolean mMovedOnce;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_new_note);
        
        mMovedOnce = false;
        
        Intent intent = getIntent();
        if (intent != null) {
            // Show the Up button in the action bar.
            boolean showUp = intent.getBooleanExtra(NotesActivity.FROM_HOME, false);
            getActionBar().setDisplayHomeAsUpEnabled(showUp);
        }
        
        Random random = new Random();
        mFilename = "notes/" + random.nextLong() + ".jpg";
        
        // TODO: this should be a shared instance
        //mLocHelper = new LocationHelper(this);
        /*mLocation = mLocHelper.requestLocation(new LocationResult() {
            @Override
            public void onLocation(Location location) {
                // TODO: save the location
                mLocation = location;
            }
        });*/
        
        setUpMapIfNeeded();
    }
    
    @Override
    protected void onResume() {
        super.onResume();
        
        mMovedOnce = false;
        
        setUpMapIfNeeded();
    }
    
    private void setUpMapIfNeeded() {
        if (mMap == null) {
            mMap = ((SupportMapFragment)getSupportFragmentManager().findFragmentById(R.id.mapFragment)).getMap();
            if (mMap != null) {
                setUpMap();
            }
        }
    }
    
    private void setUpMap() {
        /*LatLng marker = null;
        if (mLocation == null) {
            marker = new LatLng(0, 0);
        } else {
            marker = new LatLng(mLocation.getLatitude(), mLocation.getLongitude());
        }
        mMap.addMarker(new MarkerOptions().position(marker).title("Marker"));*/
        mMap.setOnMyLocationChangeListener(new GoogleMap.OnMyLocationChangeListener() {
            @Override
            public void onMyLocationChange(Location location) {
                if (!mMovedOnce) {
                    LatLng position = new LatLng(location.getLatitude(), location.getLongitude());
                    mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(position, 14.0f));
                    mMap.clear();
                    mMap.addMarker(new MarkerOptions().position(position).title("Location"));
                    mMovedOnce = true;
                }
            }
        });
        mMap.setOnMapClickListener(new GoogleMap.OnMapClickListener() {
            @Override
            public void onMapClick(LatLng latlng) {
                mMap.clear();
                mMap.addMarker(new MarkerOptions().position(latlng).title("Location"));
            }
        });
        mMap.setMyLocationEnabled(true);
        
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.activity_new_note, menu);
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
        case R.id.camera:
            // Get a picture from the camera
            Intent cameraIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
            File photo = new File(Environment.getExternalStorageDirectory(), mFilename);
            Uri outputFileUri = Uri.fromFile(photo);
            cameraIntent.putExtra(MediaStore.EXTRA_OUTPUT, outputFileUri);
            startActivityForResult(cameraIntent, CAMERA_REQUEST_CODE);
            return true;
        case R.id.gallery:
            // Get a picture from the gallery
            Intent galleryIntent = new Intent();
            galleryIntent.setType("image/*");
            galleryIntent.setAction(Intent.ACTION_GET_CONTENT);
            startActivityForResult(
                    Intent.createChooser(galleryIntent, null), GALLERY_REQUEST_CODE);
            return true;
        case R.id.save:
            // Need to collect all relevant data and save it in the database
            // Also need to post an object if sharing was enabled
            return super.onContextItemSelected(item);
        }
        return super.onOptionsItemSelected(item);
    }
    
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
    }

}
