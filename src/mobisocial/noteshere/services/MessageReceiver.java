package mobisocial.noteshere.services;

import org.json.JSONObject;

import mobisocial.noteshere.social.SocialClient;
import mobisocial.socialkit.musubi.DbObj;
import mobisocial.socialkit.musubi.Musubi;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.util.Log;

public class MessageReceiver extends BroadcastReceiver {
    private static final String TAG = "MessageReceiver";

    @Override
    public void onReceive(Context context, Intent intent) {
        if (intent == null) {
            Log.d(TAG, "no intent");
        }
        
        Log.d(TAG, "message received: " + intent);
        
        Uri objUri = intent.getParcelableExtra("objUri");
        if (objUri == null) {
            Log.d(TAG, "no object found");
            return;
        }
        Log.d(TAG, "obj uri: " + objUri.toString());
        
        Musubi musubi = Musubi.forIntent(context, intent);
        DbObj obj = musubi.objForUri(objUri);
        
        if (obj == null) {
            Log.d(TAG, "obj is null?");
            return;
        }
        
        JSONObject json = obj.getJson();
        if (json == null) {
            Log.d(TAG, "no json attached to obj");
            return;
        }
        Log.d(TAG, "received: " + obj.getType());
        
        if (obj.getSender().isOwned()) {
            Log.d(TAG, "message is owned");
            return; // TODO: maybe do something else with messages I send
        }
        
        // Let the wrappers handle the obj
        SocialClient sc = new SocialClient(context, musubi);
        sc.handleIncomingObj(obj);
    }

}
