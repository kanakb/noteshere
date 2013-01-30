package mobisocial.noteshere;

import java.io.File;
import java.util.Random;

import mobisocial.socialkit.musubi.Musubi;

import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.app.Activity;
import android.content.Intent;
import android.view.Menu;
import android.view.MenuItem;
import android.support.v4.app.NavUtils;

public class NewNoteActivity extends Activity {
    public static final String TAG = "NewNoteActivity";
    
    private static final String ACTION_CREATE_FEED = "musubi.intent.action.CREATE_FEED";
    
    private static final int GALLERY_REQUEST_CODE = 1;
    private static final int CAMERA_REQUEST_CODE = 2;
    private static final int REQUEST_CREATE_FEED = 3;
    
    private String mFilename;
    private boolean mMusubiInstalled;
    @SuppressWarnings("unused")
    private Musubi mMusubi;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_new_note);
        
        Intent intent = getIntent();
        if (intent != null) {
            // Show the Up button in the action bar.
            boolean showUp = intent.getBooleanExtra(NotesActivity.FROM_HOME, false);
            getActionBar().setDisplayHomeAsUpEnabled(showUp);
        }
        
        Random random = new Random();
        mFilename = "notes/" + random.nextLong() + ".jpg";
        
        mMusubiInstalled = Musubi.isMusubiInstalled(this);
        if (mMusubiInstalled) {
            mMusubi = Musubi.getInstance(this);
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.activity_new_note, menu);
        if (!mMusubiInstalled) {
            menu.findItem(R.id.share).setVisible(false);
        }
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
        case R.id.share:
            // Need to launch Musubi to start a new feed
            Intent createFeed = new Intent(ACTION_CREATE_FEED);
            startActivityForResult(createFeed, REQUEST_CREATE_FEED);
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
