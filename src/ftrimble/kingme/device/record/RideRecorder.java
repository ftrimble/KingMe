package ftrimble.kingme.device.record;

import ftrimble.kingme.device.file.KingMeGPX;
import ftrimble.kingme.device.R;

import android.app.Activity;
import android.location.Location;
import android.os.AsyncTask;
import android.text.format.Time;
import android.util.Log;
import android.widget.TextView;

import com.google.android.gms.location.LocationClient;

import java.util.LinkedList;
import java.io.IOException;

public class RideRecorder extends AsyncTask<Activity, RecordingData, Void> {

    private static final int TIME_BETWEEN_POLLING = 1000;

    private Activity mActivity;

    private final LocationClient mLocationClient;
    private Location mCurrentLocation;

    private String mRideName;
    private KingMeGPX mRideFile;

    private Time mTime;
    private boolean mIsRecording;

    private Location mLastLocation;
    private Time mLastTime;

    private LinkedList<RecordingData> mLapData;
    private RecordingData mAllData;


    public RideRecorder(LocationClient locationClient) {
        super();
        mLocationClient = locationClient;
        mIsRecording = false;
    }

    @Override
    protected Void doInBackground(Activity... args) {
        mActivity = args[0];
        beginRecording();
        while ( mIsRecording) {
            record();
            publishProgress();

            try {
                // wait between polling for data
                Thread.sleep(TIME_BETWEEN_POLLING);
            } catch ( InterruptedException ie ) {
                ie.printStackTrace();
            }
        }
        stopRecording();

        return null;
    }

    @Override
    protected void onProgressUpdate(RecordingData... data) {
        for ( RecordingData datum : data ) {
            // update speed data
            TextView view = (TextView) mActivity.findViewById(R.id.speed_value);
            view.setText(Float.toString(datum.getCurrentSpeed()));

            // update distance datum
            view = (TextView) mActivity.findViewById(R.id.distance_value);
            view.setText(Float.toString(datum.getDistanceTravelled()));

            // update time datum
            view = (TextView) mActivity.findViewById(R.id.time_value);
            view.setText(Float.toString(datum.getElapsedTime()));
        }
    }

    /**
     * Begins an activity recording. This means initializing a new file
     * and beginning polling of data.
     */
    public void beginRecording() {
        mTime = new Time();
        mTime.setToNow();

        try {
            mRideFile = new KingMeGPX(mActivity.getApplicationContext()
                                      .getFilesDir(), mRideName, mTime);
        } catch ( IOException ioe ) {
            Log.d("KingMeGPX","Could not create a GPX file to record");
        }

        mIsRecording = true;

        mLapData = new LinkedList<RecordingData>();
        mAllData = new RecordingData(mTime);
    }

    /**
     * Polls for data and publishes to a file.
     */
    public void record() {
        if ( mIsRecording ) {
            mCurrentLocation = mLocationClient.getLastLocation();
            mTime.setToNow();

            mRideFile.addPoint(mCurrentLocation, mTime);

            // update information
            mAllData.updateData(mCurrentLocation, mLastLocation,
                                mTime, mLastTime);
            mLapData.getLast().updateData(mCurrentLocation, mLastLocation,
                                          mTime, mLastTime);

            mLastTime.set(mTime);
            mLastLocation.set(mCurrentLocation);

        }
    }

    public void lap() {
        mRideFile.endSegment();
        mRideFile.addSegment();

        mLapData.add(new RecordingData(mTime));
    }

    public void resume() {
        mIsRecording = true;

        // ensure that ridetime only stores time that the clock was running.
        mLastTime.setToNow();

        record();
    }

    /**
     * Pauses an activity recording. This means that location polling will
     * stop, and metrics will no longer be updated.
     */
    public void stopRecording() {
        mIsRecording = false;
    }

    /**
     * Stores a final recording. This will reset all data, and can only be
     * called while the mIsRecording has stopped.
     */
    public void storeRecording() {
        mRideFile.endDocument();
    }
}