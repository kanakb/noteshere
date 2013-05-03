package mobisocial.noteshere;

import java.io.File;
import java.io.IOException;
import java.util.Random;

import mobisocial.noteshere.db.FollowerManager;
import mobisocial.noteshere.db.MNote;
import mobisocial.noteshere.db.NoteManager;
import mobisocial.noteshere.social.SocialClient;
import mobisocial.noteshere.util.UriImage;
import mobisocial.socialkit.musubi.Musubi;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
//import com.google.android.gms.maps.model.LatLng;
//import com.google.android.gms.maps.model.MarkerOptions;

import android.location.Location;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.app.Activity;
import android.content.Intent;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.TextView;
import android.widget.Toast;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.NavUtils;

public class NewNoteActivity extends FragmentActivity {
    public static final String TAG = "NewNoteActivity";
    
    private static final int GALLERY_REQUEST_CODE = 1;
    private static final int CAMERA_REQUEST_CODE = 2;
    private static final int PREVIEW_REQUEST_CODE = 3;
    
    public static final int MAX_IMAGE_WIDTH = 1280;
    public static final int MAX_IMAGE_HEIGHT = 720;
    public static final int MAX_IMAGE_SIZE = 40 * 1024;
    
    private String mFilename;
    private Uri mFileUri;
    
    private TextView mTextView;
    
    //private LocationHelper mLocHelper;
    //private Location mLocation;
    
    private GoogleMap mMap;
    
    private boolean mMovedOnce;
    
    private NoteManager mNoteManager;
    private FollowerManager mFollowerManager;
    
    private Musubi mMusubi;
    
    private MenuItem mAttachmentItem;
    
    private LatLng mLatLng;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_new_note);
        
        mMovedOnce = false;
        
        mLatLng = new LatLng(0.0, 0.0);
        
        mTextView = (TextView)findViewById(R.id.noteText);
        
        Intent intent = getIntent();
        if (intent != null) {
            // Show the Up button in the action bar.
            boolean showUp = intent.getBooleanExtra(NotesActivity.FROM_HOME, false);
            getActionBar().setDisplayHomeAsUpEnabled(showUp);
        }
        
        Random random = new Random();
        long identifier = random.nextLong();
        identifier = (identifier >= 0) ? identifier : -identifier;
        mFilename = "notes_" + random.nextLong() + ".jpg";
        
        //mLocHelper = new LocationHelper(this);
        /*mLocation = mLocHelper.requestLocation(new LocationResult() {
            @Override
            public void onLocation(Location location) {
                mLocation = location;
            }
        });*/
        
        mNoteManager = new NoteManager(App.getDatabaseSource(this));
        mFollowerManager = new FollowerManager(App.getDatabaseSource(this));
        
        if (Musubi.isMusubiInstalled(this)) {
            mMusubi = Musubi.getInstance(this);
        }
        
        setUpMapIfNeeded();
    }
    
    @Override
    protected void onResume() {
        super.onResume();
        
        //mMovedOnce = false;
        if (mFileUri != null && mAttachmentItem != null) {
            mAttachmentItem.setVisible(true);
        }
        
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
                    mLatLng = new LatLng(location.getLatitude(), location.getLongitude());
                    mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(mLatLng, 14.0f));
                    mMap.clear();
                    mMap.addMarker(new MarkerOptions().position(mLatLng).title("Location"));
                    mMovedOnce = true;
                }
            }
        });
        mMap.setOnMapClickListener(new GoogleMap.OnMapClickListener() {
            @Override
            public void onMapClick(LatLng latlng) {
                mMap.clear();
                mLatLng = latlng;
                mMap.addMarker(new MarkerOptions().position(latlng).title("Location"));
            }
        });
        mMap.setMyLocationEnabled(true);
        
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.activity_new_note, menu);
        mAttachmentItem = menu.findItem(R.id.view_attach);
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
            mFileUri = Uri.fromFile(photo);
            cameraIntent.putExtra(MediaStore.EXTRA_OUTPUT, mFileUri);
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
            String text = mTextView.getText().toString();
            if ((text == null || text.length() == 0) && mFileUri == null) {
                Toast.makeText(this, "Please write something or take a picture first.", Toast.LENGTH_SHORT).show();
                return true;
            }
            new NoteSaveTask().execute();
            return true;
        case R.id.view_attach:
            Intent viewAttachmentIntent = new Intent(this, ViewAttachmentActivity.class);
            viewAttachmentIntent.setData(mFileUri);
            startActivityForResult(viewAttachmentIntent, PREVIEW_REQUEST_CODE);
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
    
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == GALLERY_REQUEST_CODE && resultCode == Activity.RESULT_OK) {
            mFileUri = data.getData();
            mAttachmentItem.setVisible(true);
        }
        if (requestCode == CAMERA_REQUEST_CODE && resultCode == Activity.RESULT_OK) {
            mAttachmentItem.setVisible(true);
        }
        if (requestCode == CAMERA_REQUEST_CODE && resultCode != Activity.RESULT_OK) {
            mFileUri = null;
        }
        super.onActivityResult(requestCode, resultCode, data);
    }
    
    private class NoteSaveTask extends AsyncTask<Void, Void, byte[]> {
        @Override
        protected byte[] doInBackground(Void... params) {
            if (mFileUri == null) return null;
            UriImage image = new UriImage(NewNoteActivity.this, mFileUri);
            try {
                return image.getResizedImageData(MAX_IMAGE_WIDTH, MAX_IMAGE_HEIGHT, MAX_IMAGE_SIZE);
            } catch (IOException e) {
                Log.w(TAG, "image conversion failed", e);
                return null;
            }
        }
        
        @Override
        protected void onPostExecute(byte[] data) {
            MNote note = new MNote();
            String text = mTextView.getText().toString();
            if (text != null && text.length() > 0) {
                note.text = text;
            }
            if (data != null && data.length > 0) {
                note.attachment = data;
            }
            note.latitude = mLatLng.latitude;
            note.longitude = mLatLng.longitude;
            note.timestamp = System.currentTimeMillis();
            note.owned = true;
            note.senderId = "";
            note.senderName = "";
            mNoteManager.insertNote(note);
            
            if (mMusubi != null) {
                SocialClient sc = new SocialClient(NewNoteActivity.this, mMusubi);
                sc.sendToFollowers(note, mFollowerManager.getFollowers(), null);
            }
            
            Toast.makeText(NewNoteActivity.this, "Note saved.", Toast.LENGTH_SHORT).show();
            NewNoteActivity.this.finish();
        }
    }

}
