package mobisocial.noteshere;

import java.io.IOException;

import mobisocial.noteshere.util.UriImage;
import android.net.Uri;
import android.os.Bundle;
import android.app.Activity;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.ImageView;
import android.widget.Toast;

public class ViewAttachmentActivity extends Activity {
    
    private static final String TAG = "ViewAttachmentActivity";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_view_attachment);
        // Show the Up button in the action bar.
        setupActionBar();
        
        if (getIntent() == null || getIntent().getData() == null) {
            Toast.makeText(this, "No Image!", Toast.LENGTH_SHORT).show();
            setResult(RESULT_CANCELED);
            finish();
        }
        
        Uri data = getIntent().getData();
        UriImage image = new UriImage(this, data);
        byte[] raw_img = null;
        try {
            raw_img = image.getResizedImageData(NewNoteActivity.MAX_IMAGE_WIDTH, NewNoteActivity.MAX_IMAGE_HEIGHT, NewNoteActivity.MAX_IMAGE_SIZE);
        } catch (IOException e) {
            Log.w(TAG, "image conversion failed", e);
            Toast.makeText(this, "Image load failed!", Toast.LENGTH_SHORT).show();
            setResult(RESULT_CANCELED);
            finish();
        }
        
        if (raw_img == null) {
            Toast.makeText(this, "No Image!", Toast.LENGTH_SHORT).show();
            setResult(RESULT_CANCELED);
            finish();
        }
        ImageView imageView = (ImageView)findViewById(R.id.image_preview);
        Bitmap bm = BitmapFactory.decodeByteArray(raw_img, 0, raw_img.length);
        imageView.setImageBitmap(bm);
    }
    
    @Override
    protected void onDestroy() {
        ImageView imageView = (ImageView)findViewById(R.id.image_preview);
        BitmapDrawable bd = (BitmapDrawable)imageView.getDrawable();
        bd.getBitmap().recycle();
        imageView.setImageBitmap(null);
        super.onDestroy();
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
        getMenuInflater().inflate(R.menu.view_attachment, menu);
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
            //NavUtils.navigateUpFromSameTask(this);
            setResult(RESULT_CANCELED);
            finish();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

}
