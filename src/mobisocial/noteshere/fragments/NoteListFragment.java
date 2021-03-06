package mobisocial.noteshere.fragments;

import java.text.DateFormat;
import java.text.DecimalFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import com.google.android.gms.maps.model.LatLng;

import mobisocial.noteshere.App;
import mobisocial.noteshere.R;
import mobisocial.noteshere.ViewNoteActivity;
import mobisocial.noteshere.db.MNote;
import mobisocial.noteshere.db.NoteManager;
import mobisocial.noteshere.location.LocationHelper;
import mobisocial.noteshere.util.SimpleCursorLoader;
import mobisocial.socialkit.musubi.DbIdentity;
import mobisocial.socialkit.musubi.Musubi;
import android.content.Context;
import android.content.Intent;
import android.database.ContentObserver;
import android.database.Cursor;
import android.location.Location;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.Loader;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.CursorAdapter;
import android.widget.ListView;
import android.widget.TextView;

public class NoteListFragment extends Fragment
    implements LoaderManager.LoaderCallbacks<Cursor>, OnItemClickListener {
    
    public static final String ARG_IS_MINE = "is_mine";
    
    private boolean mIsMine;
    private int mLoaderId;
    
    private FragmentActivity mActivity;
    
    private ListView mNoteView;
    
    private NoteListCursorAdapter mNotesAdapter;
    
    private NoteManager mNoteManager;
    
    private Musubi mMusubi;
    
    private Map<View, Long> mNoteMap;
    
    private LocationHelper mLocHelper;
    private Location mLocation;
    
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState) {
        mActivity = getActivity();
        
        mNoteManager = new NoteManager(App.getDatabaseSource(mActivity));
        
        mIsMine = getArguments().getBoolean(ARG_IS_MINE);
        mLoaderId = mIsMine ? 1 : 0;
        
        if (Musubi.isMusubiInstalled(mActivity)) {
            mMusubi = Musubi.getInstance(mActivity);
        }
        
        mNoteMap = new HashMap<View, Long>();
        
        mLocHelper = new LocationHelper(mActivity);
        
        View v = inflater.inflate(R.layout.note_list, container, false);
        mNoteView = (ListView)v.findViewById(R.id.entry_list);
        
        mNoteView.setOnItemClickListener(this);
        
        mActivity.getSupportLoaderManager().initLoader(mLoaderId, null, this);
        mActivity.getContentResolver().registerContentObserver(App.URI_NOTE_AVAILABLE, false, new ContentObserver(new Handler(getActivity().getMainLooper())) {
            @Override
            public void onChange(boolean selfChange) {
                mActivity.getSupportLoaderManager().restartLoader(mLoaderId, null, NoteListFragment.this);
            }
        });
        
        return v;
    }
    
    @Override
    public void onResume() {
        super.onResume();
        mLocation = mLocHelper.requestLocation(new LocationHelper.LocationResult() {
            @Override
            public void onLocation(Location location) {
                mLocation = location;
                mActivity.getContentResolver().notifyChange(App.URI_NOTE_AVAILABLE, null);
            }
        });
        mActivity.getSupportLoaderManager().restartLoader(mLoaderId, null, this);
    }

    @Override
    public void onItemClick(AdapterView<?> adapterView, View view, int position, long id) {
        long noteId = mNoteMap.get(view);
        Intent intent = new Intent(mActivity, ViewNoteActivity.class);
        intent.setData(App.getNoteUri(noteId));
        mActivity.startActivity(intent);
    }

    @Override
    public Loader<Cursor> onCreateLoader(int arg0, Bundle arg1) {
        LatLng location = null;
        if (mLocation != null) {
            location = new LatLng(mLocation.getLatitude(), mLocation.getLongitude());
        }
        return new NoteListLoader(mActivity, location, mIsMine);
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor cursor) {
        if (mNotesAdapter == null) {
            mNotesAdapter = new NoteListCursorAdapter(mActivity, cursor);
        } else {
            mNotesAdapter.changeCursor(cursor);
        }
        mNoteView.setAdapter(mNotesAdapter);
    }

    @Override
    public void onLoaderReset(Loader<Cursor> arg0) {
        
    }
    
    private class NoteListCursorAdapter extends CursorAdapter {
        public NoteListCursorAdapter(Context context, Cursor c) {
            super(context, c, 0);
        }

        @Override
        public void bindView(View view, Context context, Cursor cursor) {
            TextView nameText = (TextView)view.findViewById(android.R.id.text1);
            TextView detailsText = (TextView)view.findViewById(android.R.id.text2);
            
            MNote note = mNoteManager.fillInLimitedFields(cursor);
            
            // update the sender name if possible
            if (note.owned) {
                note.senderName = "Me";
            } else if (mMusubi != null) {
                String id = note.senderId;
                String feedString = mActivity.getSharedPreferences(App.PREFS_NAME, 0).getString(App.PREF_FEED_URI, null);
                if (feedString != null) {
                    Uri feedUri = Uri.parse(feedString);
                    DbIdentity ident = mMusubi.userForGlobalId(feedUri, id);
                    if (ident != null) {
                        // update the note entry if there's a name change
                        String newName = ident.getName();
                        if (!note.senderName.equals(newName)) {
                            note.senderName = newName;
                            mNoteManager.updateSenderName(note);
                        }
                    }
                }
            }
            String nameFormat = note.senderName;
            nameText.setText(nameFormat);
            
            Date date = new Date(note.timestamp);
            DateFormat format = DateFormat.getDateTimeInstance();
            String detailsFormat = format.format(date);
            if (mLocation != null) {
                float[] results = new float[1];
                Location.distanceBetween(
                        note.latitude, note.longitude, mLocation.getLatitude(), mLocation.getLongitude(), results);
                double resultMiles = results[0] * 0.00062137119;
                DecimalFormat df = new DecimalFormat("#.###");
                detailsFormat += " � " + df.format(resultMiles) + " mi";
            }
            detailsText.setText(detailsFormat);
            
            mNoteMap.put(view, note.id);
        }

        @Override
        public View newView(Context context, Cursor cursor, ViewGroup parent) {
            final LayoutInflater inflater = LayoutInflater.from(context);
            View v = inflater.inflate(android.R.layout.simple_list_item_2, parent, false);
            bindView(v, context, cursor);
            return v;
        }
    }
    
    public static class NoteListLoader extends SimpleCursorLoader {
        private final NoteManager mNoteManager;
        private final LatLng mLatLng;
        private final boolean mIsMine;
        
        public NoteListLoader(Context context, LatLng latLng, boolean isMine) {
            super(context);
            mNoteManager = new NoteManager(App.getDatabaseSource(context));
            if (latLng == null) {
                mLatLng = new LatLng(0.0, 0.0);
            } else {
                mLatLng = latLng;
            }
            mIsMine = isMine;
        }
        
        @Override
        public Cursor loadInBackground() {
            Cursor c;
            if (mIsMine) {
                c = mNoteManager.getMyNotesCursor();
            } else {
                c = mNoteManager.getNearbyNoteCursor(mLatLng.latitude, mLatLng.longitude);
            }
            c.setNotificationUri(getContext().getContentResolver(), App.URI_NOTE_AVAILABLE);
            return c;
        }
    }

}
