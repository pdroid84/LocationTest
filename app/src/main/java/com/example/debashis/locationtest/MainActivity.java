package com.example.debashis.locationtest;

import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentSender;
import android.location.Location;
import android.media.browse.MediaBrowser;
import android.os.PersistableBundle;
import android.support.v4.app.DialogFragment;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesUtil;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;

import java.text.DateFormat;
import java.util.Date;


public class MainActivity extends ActionBarActivity implements GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener, LocationListener {

    private static final String REQUESTING_LOCATION_UPDATES_KEY = "updates_key";
    private static final String LOCATION_KEY = "location_key";
    private static final String LAST_UPDATED_TIME_STRING_KEY = "update_time";
    private GoogleApiClient mGoogleApiClient;
    // Request code to use when launching the resolution activity
    private static final int REQUEST_RESOLVE_ERROR = 1001;
    // Unique tag for the error dialog fragment
    private static final String DIALOG_ERROR = "dialog_error";
    // Bool to track whether the app is already resolving an error
    private boolean mResolvingError = false;
    //To keep track of the boolean across activity restarts
    private static final String STATE_RESOLVING_ERROR = "resolving_error";
    //Location variable
    private Location mLastLocation;
    //Current location
    private Location mCurrLocation;
    private String mLastUpdateTime;
    //LocationRequest
    LocationRequest mLocationRequest;
    //flag is set as true, so update will happen
    private boolean mRequestingLocationUpdates = true;
    private TextView mTextView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        mTextView = (TextView) findViewById(R.id.textBox);
        Log.d("DEB","onCreate is called");
        if (savedInstanceState != null) {
          mResolvingError = savedInstanceState.getBoolean(STATE_RESOLVING_ERROR,false);
        }
        updateValuesFromBundle(savedInstanceState);
        //create an instance of GoogleApiClient using the GoogleApiClient.Builder APIs in your activity's onCreate() method
        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .addApi(LocationServices.API)
                .build();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onStart() {
        super.onStart();
        Log.d("DEB", "onStart is called");
        //To gracefully manage the lifecycle of the connection, you should call connect() during the activity's onStart()
        if(!mResolvingError) {
            mGoogleApiClient.connect();
        }
    }

    @Override
    protected void onStop() {
        Log.d("DEB", "onStop is called");
        //call disconnect() during the onStop() method
        mGoogleApiClient.disconnect();
        //make sure super is called at the end
        super.onStop();
    }

    @Override
    protected void onPause() {
        super.onPause();
        Log.d("DEB", "onPause is called");
        stopLocationUpdates();
    }

    //Use a boolean, mRequestingLocationUpdates, to track whether location updates are currently turned o
    @Override
    public void onResume() {
        super.onResume();
        Log.d("DEB", "onResume is called");
        if (mGoogleApiClient.isConnected() && !mRequestingLocationUpdates) {
            startLocationUpdates();
        }
    }

    //Once the user completes the resolution provided by startResolutionForResult() or GooglePlayServicesUtil.getErrorDialog(),
    //your activity receives the onActivityResult() callback with the RESULT_OK result code. You can then call connect() again.
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        Log.d("DEB", "onActivityResult is called");
        if (requestCode == REQUEST_RESOLVE_ERROR) {
            mResolvingError = false;
            if (resultCode == RESULT_OK) {
                // Make sure the app is not already connected or attempting to connect
                if (!mGoogleApiClient.isConnecting() &&
                        !mGoogleApiClient.isConnected()) {
                    mGoogleApiClient.connect();
                }
            }
        }
    }



    @Override
    public void onSaveInstanceState(Bundle outState, PersistableBundle outPersistentState) {

        Log.d("DEB", "onSaveInstanceState is called");
        outState.putBoolean(STATE_RESOLVING_ERROR, mResolvingError);
        outState.putBoolean(REQUESTING_LOCATION_UPDATES_KEY,
                mRequestingLocationUpdates);
        outState.putParcelable(LOCATION_KEY, mCurrLocation);
        outState.putString(LAST_UPDATED_TIME_STRING_KEY, mLastUpdateTime);
        super.onSaveInstanceState(outState, outPersistentState);
    }



    private void updateValuesFromBundle(Bundle savedInstanceState) {
        Log.d("DEB","updateValuesFromBundle is called");
        if (savedInstanceState != null) {
            // Update the value of mRequestingLocationUpdates from the Bundle, and
            // make sure that the Start Updates and Stop Updates buttons are
            // correctly enabled or disabled.
            if (savedInstanceState.keySet().contains(REQUESTING_LOCATION_UPDATES_KEY)) {
                mRequestingLocationUpdates = savedInstanceState.getBoolean(
                        REQUESTING_LOCATION_UPDATES_KEY);
               // setButtonsEnabledState();
            }

            // Update the value of mCurrentLocation from the Bundle and update the
            // UI to show the correct latitude and longitude.
            if (savedInstanceState.keySet().contains(LOCATION_KEY)) {
                // Since LOCATION_KEY was found in the Bundle, we can be sure that
                // mCurrentLocationis not null.
                mCurrLocation = savedInstanceState.getParcelable(LOCATION_KEY);
            }

            // Update the value of mLastUpdateTime from the Bundle and update the UI.
            if (savedInstanceState.keySet().contains(LAST_UPDATED_TIME_STRING_KEY)) {
                mLastUpdateTime = savedInstanceState.getString(
                        LAST_UPDATED_TIME_STRING_KEY);
            }
            updateLocationInfo();
        }
    }

    @Override
    public void onConnected(Bundle bundle) {
        Log.d("DEB","onConnected is called.");
        Log.d("DEB", "Just a test for Git barch - FristBranch");
        // Connected to Google Play services!
        mLastLocation = LocationServices.FusedLocationApi.getLastLocation(mGoogleApiClient);
        if(mLastLocation != null) {
            String mLatitude = String.valueOf(mLastLocation.getLatitude());
            String mLongitude = String.valueOf(mLastLocation.getLongitude());
            Log.d("DEB", "The values are -> Laitude= "+mLatitude + " and Longitude= "+mLongitude);
            Toast.makeText(this,"The values are -> Laitude= "+mLatitude + " and Longitude= "+mLongitude,Toast.LENGTH_LONG).show();
            mTextView.setText("The values are -> Laitude= "+mLatitude + " and Longitude= "+mLongitude);
        }

        if (mRequestingLocationUpdates) {
            startLocationUpdates();
        }

    }

    @Override
    public void onConnectionSuspended(int i) {
        // The connection has been interrupted.
        Log.d("DEB","onConnectionSuspended is called");

    }

    @Override
    public void onConnectionFailed(ConnectionResult connectionResult) {
        Log.d("DEB","onConectionFailed is called");
        // This callback is important for handling errors that
        // may occur while attempting to connect with Google.
        if (mResolvingError) {
            // Already attempting to resolve an error.
            return;
        } else if (connectionResult.hasResolution()) {
            try {
                mResolvingError = true;
                connectionResult.startResolutionForResult(this, REQUEST_RESOLVE_ERROR);
            } catch (IntentSender.SendIntentException e) {
                // There was an error with the resolution intent. Try again.
                mGoogleApiClient.connect();
            }
        } else {
            // Show dialog using GooglePlayServicesUtil.getErrorDialog()
            showErrorDialog(connectionResult.getErrorCode());
            mResolvingError = true;
        }
    }

    protected void startLocationUpdates() {
        Log.d("DEB","startLocationUpdates is called");
        createLocationRequest();
        LocationServices.FusedLocationApi.requestLocationUpdates(
                mGoogleApiClient, mLocationRequest, this);
    }

    //Create the location request and set the parameters to get periodic location update
    protected void createLocationRequest() {
        Log.d("DEB","createLocationRequest is called");
        mLocationRequest = new LocationRequest();
        mLocationRequest.setInterval(10000);
        mLocationRequest.setFastestInterval(5000);
        mLocationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
    }

    @Override
    public void onLocationChanged(Location location) {
        Log.d("DEB","onLocationChanged is called");
        mCurrLocation = location;
        mLastUpdateTime = DateFormat.getTimeInstance().format(new Date());
        updateLocationInfo();

    }

    private void updateLocationInfo() {
        Log.d("DEB","updateLocationInfo is called");
        String mLatitude = String.valueOf(mCurrLocation.getLatitude());
        String mLongitude = String.valueOf(mCurrLocation.getLongitude());
        Log.d("DEB", "The updated location values are -> Laitude= "+mLatitude + " and Longitude= "+mLongitude);
        Toast.makeText(this,"The updated location values are -> Laitude= "+mLatitude + " and Longitude= "+mLongitude,Toast.LENGTH_LONG).show();
        mTextView.setText("The values are -> Laitude= "+mLatitude + " and Longitude= "+mLongitude);

    }

    //To stop location updates, call removeLocationUpdates(). And call this from onPause()
    protected void stopLocationUpdates() {
        Log.d("DEB","stopLocationUpdates is called");
        LocationServices.FusedLocationApi.removeLocationUpdates(
                mGoogleApiClient, this);
    }

        // The rest of this code is all about building the error dialog
        /* Creates a dialog for an error message */
        private void showErrorDialog(int errorCode) {
            Log.d("DEB","showError is called");
            // Create a fragment for the error dialog
            ErrorDialogFragment dialogFragment = new ErrorDialogFragment();
            // Pass the error that should be displayed
            Bundle args = new Bundle();
            args.putInt(DIALOG_ERROR, errorCode);
            dialogFragment.setArguments(args);
            dialogFragment.show(getSupportFragmentManager(), "errordialog");
        }

        /* Called from ErrorDialogFragment when the dialog is dismissed. */
        public void onDialogDismissed() {
            Log.d("DEB","onDialogDismissed is called");
            mResolvingError = false;
        }


    /* A fragment to display an error dialog */
        public static class ErrorDialogFragment extends DialogFragment {
            public ErrorDialogFragment() { }

            @Override
            public Dialog onCreateDialog(Bundle savedInstanceState) {
                Log.d("DEB","onCreateDialog is called");
                // Get the error code and retrieve the appropriate dialog
                int errorCode = this.getArguments().getInt(DIALOG_ERROR);
                return GooglePlayServicesUtil.getErrorDialog(errorCode,
                        this.getActivity(), REQUEST_RESOLVE_ERROR);
            }

            @Override
            public void onDismiss(DialogInterface dialog) {
                Log.d("DEB","onDismiss is called");
                ((MainActivity)getActivity()).onDialogDismissed();
            }
        }
}
