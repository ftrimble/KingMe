package ftrimble.kingme.device;

import android.app.Activity;
import android.app.Dialog;
import android.os.Bundle;
import android.widget.Toast;
import android.support.v4.app.FragmentActivity;

import com.google.android.gms.location.LocationClient;
import com.google.android.gms.location.Location;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesClient;


public class MainActivity extends Activity
    implements GooglePlayServicesClient.ConnectionCallbacks,
               GooglePlayServicesClient.OnConnectionFailedListener {

    private final LocationClient mLocationClient;
    private Location mCurrentLocation;

    private final static int CONNECTION_FAILURE_RESOLUTION_REQUEST = 9000;

    /**
     * Displays error results to the user when Google Play
     * Services are unavailable
     */
    public static class ErrorDialogFragment extends DialogFragment {

        private Dialog mDialog;

        public ErrorDialogFragment() {
            super();
            mDialog = null;
        }

        public void setDialog(Dialog dialog) { mDialog = dialog; }

        @Override
        public Dialog onCreateDialog(Bundle savedInstanceState) {
            return mDialog;
        }
    }

    /**
     * Checks to ensure that Google Play Services are available, and that we
     * can get user location data; this is necessary for the application to
     * function.
     */
    private boolean areServicesConnected() {
        int resultCode =
            GooglePlayServicesUtil.isGooglePlayServicesAvailable(this);

        if (ConnectionResult.SUCCESS == resultCode) {
            Log.d("Location Updates",
                    "Google Play services is available.");
            return true;
        } else { // Google Play services was not available for some reason
            int errorCode = connectionResult.getErrorCode();
            Dialog errorDialog = GooglePlayServicesUtil.getErrorDialog
                (errorCode, this, CONNECTION_FAILURE_RESOLUTION_REQUEST);

            if (errorDialog != null) {
                // Show the returned error dialog in the DialogFragment
                ErrorDialogFragment errorFragment =
                        new ErrorDialogFragment();
                errorFragment.setDialog(errorDialog);
                errorFragment.show(getSupportFragmentManager(),
                        "Location Updates");
            }
        }
    }

    /**
     * Called by Location Services when the request to connect the
     * client finishes successfully. At this point, you can
     * request the current location or start periodic updates
     */
    @Override
    public void onConnected(Bundle dataBundle) {
        Toast.makeText(this, "Connected", Toast.LENGTH_SHORT).show();
    }

    /**
     * Called by Location Services if the connection to the
     * location client drops because of an error.
     */
    @Override
    public void onDisconnected() {
        Toast.makeText(this, "Disconnected. Please re-connect.",
                Toast.LENGTH_SHORT).show();
    }

    /**
     * Called by Location Services if the attempt to
     * Location Services fails.
     */
    @Override
    public void onConnectionFailed(ConnectionResult connectionResult) {
        /*
         * Google Play services can resolve some errors it detects.
         * If the error has a resolution, try sending an Intent to
         * start a Google Play services activity that can resolve
         * error.
         */
        if ( connectionResult.hasResolution() ) {
            try {
                // Start an Activity that tries to resolve the error
                connectionResult.startResolutionForResult
                    (this, CONNECTION_FAILURE_RESOLUTION_REQUEST);
            } catch (IntentSender.SendIntentException e) {
                // Thrown if Google Play services canceled
                // the original PendingIntent
                e.printStackTrace();
            }
        } else {
            // no resolution is available; display an error dialog
            showErrorDialog(connectionResult.getErrorCode());
        }
    }

    /**
     * Called when the activity is first created.
     */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);
        mLocationClient = new LocationClient(this, this, this);

    }

    /**
     * Called when the activity becomes visible.
     */
    @Override
    protected void onStart() {
        super.onStart();
        mLocationClient.connect();
    }

    /**
     * Called when the activity is no longer visible.
     */
    @Override
    protected void onStart() {
        mLocationClient.disconnect();
        super.onStart();
    }
}
