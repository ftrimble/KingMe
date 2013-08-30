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
import java.io.File;
import java.io.IOException;

public class RideRecorder extends AsyncTask<Void, RecordingData, Void> {

    private static final int TIME_BETWEEN_POLLING = 1000;

    private final LocationClient mLocationClient;
    private Location mCurrentLocation;

    private String mRideName;
    private KingMeGPX mRideFile;

    private TextView[] mViews;
    private Time mTime;
    private boolean mIsRecording;

    private Location mLastLocation;
    private Time mLastTime;

    private LinkedList<RecordingData> mLapData;
    private RecordingData mAllData;

    public boolean getIsRecording() { return mIsRecording; }


    public RideRecorder(LocationClient locationClient, TextView[] views) {
        super();
        mLocationClient = locationClient;
        mIsRecording = false;
        mViews = views;
    }

    @Override
    protected Void doInBackground(Void... args) {
        mIsRecording = true;
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
        return null;
    }

    @Override
    protected void onProgressUpdate(RecordingData... data) {
        for ( RecordingData datum : data ) {
            for ( TextView view : mViews ) {
                switch ( view.getId() ) {
                case R.id.speed_value:
                    view.setText(Float.toString(datum.getCurrentSpeed()));
                    break;
                case R.id.distance_value:
                    view.setText(Float.toString(datum.getDistanceTravelled()));
                    break;
                case R.id.time_value:
                    view.setText(Float.toString(datum.getElapsedTime()));
                    break;
                default:
                    view.setText("0.0");
                    break;
                }
            }
        }
    }

    /**
     * Begins an activity recording. This means initializing a new file
     * and beginning polling of data.
     */
    public void beginRecording(File dir) {
        mTime = new Time();
        mLastTime = new Time();
        mTime.setToNow();
        mLastTime.set(mTime);

        // TODO name file based on TOD or other factors.
        mRideName = "test_ride";
        try {
            mRideFile = new KingMeGPX(dir, mRideName, mTime);
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
            mLastLocation = mCurrentLocation;

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