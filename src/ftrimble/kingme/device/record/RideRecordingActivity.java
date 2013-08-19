package ftrimble.kingme.device.record;

import ftrimble.kingme.device.file.KingMeGPX;

import android.text.format.Time;

import java.util.LinkedList;


class RideRecordingActivity extends Activity {

    private static final int TIME_BETWEEN_POLLING = 1000;

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


    public RideRecordingActivity(LoactionClient locationClient) {
        super();
        mLocationClient = locationClient;
        mIsRecording = false;
    }

    /**
     * Begins an activity recording. This means initializing a new file
     * and beginning polling of data.
     */
    public void beginRecording() {
        mTime = new Time();
        mTime.setToNow();

        mRideFile = new KingMeGPX(Context.getFilesDir(), mRideName, mTime);
        mIsRecording = true;

        mLapData = new LinkedList<RecordingData>();
        mAllData = new RecordingData(mTime);
        record();
    }

    /**
     * Polls for data and publishes to a file.
     */
    public void record() {
        while ( mIsRecording ) {
            mCurrentLocation = mLocationClient.getCurrentLocation();
            mTime.setToNow();

            mRideFile.addPoint(mCurrentLocation, mTime);

            // update information
            mAllData.updateData(mCurrentLocation, mLastLocation,
                                mTime, mLastTime);
            mLapData.getLast().updateData(mCurrentLocation, mLastLocation,
                                          mTime, mLastTime);

            mLastTime.set(mTime);
            mLastLocation.set(mCurrentLocation);

            // wait between polling for data
            // wait(TIME_BETWEEN_POLLING);
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