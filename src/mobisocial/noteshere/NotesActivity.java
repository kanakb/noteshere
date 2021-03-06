package mobisocial.noteshere;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.json.JSONException;
import org.json.JSONObject;

import mobisocial.noteshere.R;
import mobisocial.noteshere.db.FollowingManager;
import mobisocial.noteshere.db.MFollowing;
import mobisocial.noteshere.fragments.NoteListFragment;
import mobisocial.noteshere.services.FirstLoadProcessor;
import mobisocial.noteshere.social.SocialClient;
import mobisocial.socialkit.musubi.DbFeed;
import mobisocial.socialkit.musubi.DbIdentity;
import mobisocial.socialkit.musubi.Musubi;
import mobisocial.socialkit.obj.MemObj;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;

public class NotesActivity extends FragmentActivity {
    private static final String TAG = "NotesActivity";
    
    public static final String FROM_HOME = "from_home";

    private static final String ACTION_CREATE_FEED = "musubi.intent.action.CREATE_FEED";
    private static final String ACTION_EDIT_FEED = "musubi.intent.action.EDIT_FEED";
    
    private static final int REQUEST_CREATE_FEED = 1;
    private static final int REQUEST_EDIT_FEED = 2;
    
    private static final String ADD_TITLE = "member_header";
    private static final String ADD_HEADER = "Following";
    
    private Musubi mMusubi;

    /**
     * The {@link android.support.v4.view.PagerAdapter} that will provide
     * fragments for each of the sections. We use a
     * {@link android.support.v4.app.FragmentPagerAdapter} derivative, which
     * will keep every loaded fragment in memory. If this becomes too memory
     * intensive, it may be best to switch to a
     * {@link android.support.v4.app.FragmentStatePagerAdapter}.
     */
    SectionsPagerAdapter mSectionsPagerAdapter;

    /**
     * The {@link ViewPager} that will host the section contents.
     */
    ViewPager mViewPager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_notes);

        // Create the adapter that will return a fragment for each of the three
        // primary sections of the app.
        mSectionsPagerAdapter = new SectionsPagerAdapter(
                getSupportFragmentManager());

        // Set up the ViewPager with the sections adapter.
        mViewPager = (ViewPager) findViewById(R.id.pager);
        mViewPager.setAdapter(mSectionsPagerAdapter);

        if (Musubi.isMusubiInstalled(this)) {
            mMusubi = Musubi.getInstance(this);
        }
        
        FirstLoadProcessor processor = FirstLoadProcessor.newInstance(this);
        getContentResolver().registerContentObserver(App.URI_APP_SETUP, false, processor);
        /*getContentResolver().registerContentObserver(App.URI_APP_SETUP_COMPLETE, false, new ContentObserver(new Handler(getMainLooper()) {
            @Override
            public void onChange(boolean selfChange) {
                
            }
        });*/
        runSetup();
    }
    
    private void runSetup() {
        boolean setupComplete = getSharedPreferences(App.PREFS_NAME, 0)
                .getBoolean(App.PREF_APP_SETUP_COMPLETE, false);
        if (!setupComplete) {
            getContentResolver().notifyChange(App.URI_APP_SETUP, null);
        } 
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.activity_notes, menu);
        return true;
    }
    
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle item selection
        switch (item.getItemId()) {
        case R.id.menu_new:
            Intent newNoteIntent = new Intent(this, NewNoteActivity.class);
            newNoteIntent.putExtra(FROM_HOME, true);
            NotesActivity.this.startActivity(newNoteIntent);
            return true;
        case R.id.menu_settings:
            Intent settingsIntent = new Intent(this, SettingsActivity.class);
            NotesActivity.this.startActivity(settingsIntent);
            return true;
        case R.id.share:
            if (mMusubi == null) {
                // get people to the market to install Musubi
                Log.d(TAG, "Musubi not installed");
                new InstallMusubiDialogFragment().show(getSupportFragmentManager(), null);
                return super.onOptionsItemSelected(item);
            }
            SharedPreferences p = getSharedPreferences(App.PREFS_NAME, 0);
            String feedEntry = p.getString(App.PREF_FEED_URI, null);
            Uri feedUri = (feedEntry != null) ? Uri.parse(feedEntry) : null;
            Log.d(TAG, "trying to add followers ");
            String action = ACTION_CREATE_FEED;
            int request = REQUEST_CREATE_FEED;
            DbFeed feed = null;
            if (feedUri != null) {
                feed = mMusubi.getFeed(feedUri);
                if (feed != null) {
                    action = ACTION_EDIT_FEED;
                    request = REQUEST_EDIT_FEED;
                }
            }
            Intent intent = new Intent(action);
            if (feed != null) {
                intent.setData(feedUri);
                intent.putExtra(ADD_TITLE, ADD_HEADER);
            }
            startActivityForResult(intent, request);
            return true;
        case R.id.nearby_notes:
            Intent nearbyIntent = new Intent(this, NearbyActivity.class);
            NotesActivity.this.startActivity(nearbyIntent);
            return true;
        default:
            return super.onContextItemSelected(item);
        }
    }

    /**
     * A {@link FragmentPagerAdapter} that returns a fragment corresponding to
     * one of the sections/tabs/pages.
     */
    public class SectionsPagerAdapter extends FragmentPagerAdapter {

        public SectionsPagerAdapter(FragmentManager fm) {
            super(fm);
        }

        @Override
        public Fragment getItem(int position) {
            // getItem is called to instantiate the fragment for the given page.
            // Return a DummySectionFragment (defined as a static inner class
            // below) with the page number as its lone argument.
            Fragment fragment = new NoteListFragment();
            Bundle args = new Bundle();
            boolean isMine = (position == 0) ? false : true;
            args.putBoolean(NoteListFragment.ARG_IS_MINE, isMine);
            fragment.setArguments(args);
            return fragment;
        }

        @Override
        public int getCount() {
            // Show 2 total pages.
            return 2;
        }

        @Override
        public CharSequence getPageTitle(int position) {
            switch (position) {
            case 0:
                return getString(R.string.title_section1).toUpperCase();
            case 1:
                return getString(R.string.title_section2).toUpperCase();
            }
            return null;
        }
    }
    
    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        FollowingManager fm = new FollowingManager(App.getDatabaseSource(this));
        SharedPreferences p = getSharedPreferences(App.PREFS_NAME, 0);
        if (requestCode == REQUEST_CREATE_FEED && resultCode == RESULT_OK) {
            if (data == null || data.getData() == null) {
                return;
            }
            
            Uri feedUri = data.getData();
            Log.d(TAG, "feedUri: " + feedUri);
            
            // save the feed uri
            p.edit().putString(App.PREF_FEED_URI, feedUri.toString()).commit();
            
            DbFeed feed = mMusubi.getFeed(feedUri);
            Log.d(TAG, "me: " + feed.getLocalUser().getId() + ", " + feed.getLocalUser().getName());
            
            JSONObject json = new JSONObject();
            try {
                json.put("working", true);
            } catch (JSONException e) {
                Log.e(TAG, "json issue", e);
                return;
            }
            
            feed.postObj(new MemObj("noteshere_test", json));
            
            // Save members (these are the people I follow)
            List<DbIdentity> members = feed.getMembers();
            Set<String> following = new HashSet<String>();
            for (DbIdentity member : members) {
                if (!member.isOwned()) {
                    Log.d(TAG, "member: " + member.getId() + ", " + member.getName());
                    following.add(member.getId());
                    MFollowing follow = new MFollowing();
                    follow.userId = member.getId();
                    fm.insertFollowing(follow);
                }
            }
            
            SocialClient sc = new SocialClient(this, mMusubi);
            sc.sendHello(following);
            runSetup();
        } else if (requestCode == REQUEST_EDIT_FEED && resultCode == RESULT_OK) {
            if (data == null || data.getData() == null) {
                return;
            }
            Uri feedUri = data.getData();
            Log.d(TAG, "feedUri: " + feedUri);
            Set<String> userIds = fm.getFollowing();
            DbFeed feed = mMusubi.getFeed(feedUri);
            List<DbIdentity> members = feed.getMembers();
            Set<String> toNotify = new HashSet<String>();
            for (DbIdentity member : members) {
                if (!member.isOwned()) {
                    Log.d(TAG, "member: " + member.getId() + ", " + member.getName());
                    if (!userIds.contains(member.getId())) {
                        Log.d(TAG, "added: " + member.getId() + ", " + member.getName());
                        toNotify.add(member.getId());
                        MFollowing follow = new MFollowing();
                        follow.userId = member.getId();
                        fm.insertFollowing(follow);
                    }
                }
            }
            
            SocialClient sc = new SocialClient(this, mMusubi);
            sc.sendHello(toNotify);
        }
    }
    
    private class InstallMusubiDialogFragment extends DialogFragment {
        @Override
        public Dialog onCreateDialog(Bundle savedInstanceState) {
            // Use the Builder class for convenient dialog construction
            AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
            builder.setMessage(R.string.install_musubi)
                   .setTitle(R.string.no_musubi)
                   .setIcon(R.drawable.musubi_icon)
                   .setPositiveButton(R.string.google_play, new DialogInterface.OnClickListener() {
                       public void onClick(DialogInterface dialog, int id) {
                           Intent market = Musubi.getMarketIntent();
                           getActivity().startActivity(market);
                       }
                   })
                   .setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
                       public void onClick(DialogInterface dialog, int id) {
                       }
                   });
            // Create the AlertDialog object and return it
            return builder.create();
        }
    }

}
