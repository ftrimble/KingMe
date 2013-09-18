/**
 * Copyright 2013 Forest Trimble
 *
 * Licensed under the Apache Licesnse, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * http://www.apache.org/licenses/LICENSE-2.0

 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package ftrimble.kingme.device.record;

import ftrimble.kingme.device.file.KingMeGPX;
import ftrimble.kingme.device.R;

import android.app.NotificationManager;
import android.app.Service;
import android.content.Intent;
import android.location.Location;
import android.os.Binder;
import android.os.Bundle;
import android.os.IBinder;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.NotificationCompat.Builder;
import android.text.format.Time;
import android.util.Log;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesClient;
import com.google.android.gms.location.LocationClient;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;

import java.util.LinkedList;
import java.io.File;
import java.io.IOException;

public class RideRecordingService
    extends Service
    implements LocationListener,
               GooglePlayServicesClient.ConnectionCallbacks,
               GooglePlayServicesClient.OnConnectionFailedListener {

    // TODO #18 create a user setting to alter the granularity of data
    private static final int TIME_BETWEEN_POLLING = 1000;
    private static final int NOTIFICATION = R.string.recording_service_started;
    private static final int CONNECTION_FAILURE_RESOLUTION_REQUEST = 9000;

    public static final String RIDE_DATA = "ride_data";
    public static final String BROADCAST = "ftrimble.kingme.device.KingMe";

    private static final long MAX_TIME_BETWEEN_REQUESTS = 1000;
    private static final long MIN_TIME_BETWEEN_REQUESTS = 500;

    private NotificationManager mNotificationManager;
    private NotificationCompat.Builder mNotificationBuilder;

    private final IBinder mBinder = new RideRecordingBinder();

    private LocationClient mLocationClient;
    private LocationRequest mLocationRequest;

    private String mRideName;
    private KingMeGPX mRideFile;

    private boolean mIsRecording = false;
    private boolean mFileBegan = false;

    // Data
    private Time mTime;
    private Location mCurrentLocation;
    private Location mLastLocation = null;
    private Time mLastTime;
    private LinkedList<RecordingData> mLapData;
    private RecordingData mAllData;

    public class RideRecordingBinder extends Binder {
        public RideRecordingService getService() {
            return RideRecordingService.this;
        }
    }

    @Override
    public void onCreate() {
        mLocationRequest = LocationRequest.create();
        mLocationRequest.setInterval(MAX_TIME_BETWEEN_REQUESTS);
        mLocationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
        mLocationRequest.setFastestInterval(MIN_TIME_BETWEEN_REQUESTS);

        mLocationClient = new LocationClient(getApplicationContext(),this,this);
        mLocationClient.connect();

        mNotificationManager =
            (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
        mNotificationBuilder =  new NotificationCompat.Builder(this)
            .setContentTitle(getResources().getString(R.string.app_name))
            .setContentText(getResources().getString(R.string.recording_service_started))
            .setSmallIcon(R.drawable.ic_launcher)
            .setOngoing(true);
    }

    /**
     * Called by Location Services when the request to connect the
     * client finishes successfully. At this point, you can
     * request the current location or start periodic updates
     */
    @Override
    public void onConnected(Bundle dataBundle) {
        // Display the connection status
        Log.i("info", "Location client is Connected");
    }

    /**
     * Called by Location Services if the connection to the
     * location client drops because of an error.
     */
    @Override
    public void onDisconnected() {
        Log.i("info", "Location client is Disconnected");
    }

    /**
     * Called by Location Services if the attempt to
     * Location Services fails.
     */
    @Override
    public void onConnectionFailed(ConnectionResult connectionResult) {
        // Google Play services can resolve some errors it detects.
        // If the error has a resolution, try sending an Intent to
        // start a Google Play services activity that can resolve
        // error.
        if (connectionResult.hasResolution()) {
            /* TODO Start an Activity that tries to resolve the error
            try {
                connectionResult.startResolutionForResult
                    (this, CONNECTION_FAILURE_RESOLUTION_REQUEST);
            } catch (IntentSender.SendIntentException e) {
                // Thrown if Google Play services canceled the original
                // PendingIntent; Log the error
                e.printStackTrace();
            }
            */
        } else {
            // TODO do something if no resolution is available
        }
    }

    /**
     * Initializes the service. We put a notification in the taskbar.
     * We also need access to the location client, in order to provide
     * access to our location data.
     */
    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        return START_STICKY;
    }

    @Override
    public IBinder onBind(Intent intent) {
        return mBinder;
    }

    @Override
    public void onLocationChanged(Location newLocation) {
        mCurrentLocation = newLocation;
        record();
        Intent intent = new Intent(BROADCAST);
        intent.putExtra(RIDE_DATA,mAllData);
        sendBroadcast(intent);
    }

    /**
     * Swaps the recording status of the service. Also updates the
     * notification to reflect the updated status.
     */
    public void start_pause() {
        if ( mIsRecording ) {
            mLocationClient.removeLocationUpdates(this);
            mNotificationBuilder.setContentText("Recording Paused");
            mNotificationManager.notify(NOTIFICATION,
                                        mNotificationBuilder.build());
        } else {
            if ( !mFileBegan ) {
                beginRecording(getApplicationContext().getFilesDir());
            }

            mLocationClient.requestLocationUpdates(mLocationRequest,this);
            // ensure that ridetime only stores time that the clock was running.
            mLastTime.setToNow();
            mLastLocation = null;
            mNotificationBuilder.setContentText("Recording Started");
            mNotificationManager.notify(NOTIFICATION,
                                        mNotificationBuilder.build());
        }
        mIsRecording = !mIsRecording;
    }

    /**
     * Begins a lap if the ride is in progress; ends the ride and terminates
     * the service if it is stopped.
     */
    public void lap_reset() {
        if ( mIsRecording ) {
            mRideFile.endSegment();
            mRideFile.addSegment();

            mLapData.add(new RecordingData(mTime));
        } else if ( mRideFile != null ) {
            mLocationClient.removeLocationUpdates(this);
            mNotificationManager.cancel(NOTIFICATION);
            mRideFile.endDocument();
            mFileBegan = false;
            stopSelf();
        }
    }

    /**
     * Begins an activity recording. This means initializing a new file.
     */
    public void beginRecording(File dir) {
        mTime = new Time();
        mTime.setToNow();
        mLastTime = new Time(mTime);

        // TODO name file based on TOD or other factors.
        mRideName = "test_ride";
        try {
            mRideFile = new KingMeGPX(dir, mRideName, mTime);
            mFileBegan = true;
        } catch ( IOException ioe ) {
            Log.d("KingMeGPX","Could not create a GPX file to record");
        }

        mLapData = new LinkedList<RecordingData>();
        mLapData.add(new RecordingData(mTime));
        mAllData = new RecordingData(mTime);
    }

    /**
     * Polls for data and publishes to a file.
     */
    public void record() {
        mTime.setToNow();

        mRideFile.addPoint(mCurrentLocation, mTime);

        // update information
        mAllData.updateData(mCurrentLocation, mLastLocation,
                            mTime, mLastTime);
        mLapData.getLast().updateData(mCurrentLocation, mLastLocation,
                                      mTime, mLastTime);

        mLastTime.set(mTime);
        mLastLocation = mCurrentLocation;
    }

    public void lap() {
        mRideFile.endSegment();
        mRideFile.addSegment();

        mLapData.add(new RecordingData(mTime));
    }

    /**
     * Stores a final recording. This will reset all data, and can only be
     * called while the mIsRecording has stopped.
     */
    public void storeRecording() {
        mRideFile.endDocument();
    }
}