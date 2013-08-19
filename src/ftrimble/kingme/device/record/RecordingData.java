package ftrimble.kingme.device.record;

import android.text.format.Time;

import java.lang.Math;

class RecordingData {

    private double mDistanceTravelled;

    private Time mBeginTime;
    private int mElapsedTime;
    private int mRideTime;

    private float mMaxSpeed;
    private float mCurrentSpeed;

    private float mTotalAscent;
    private float mTotalDescent;

    public RecordingData(Time time) {
        mBeginTime = new Time(time);

        mDistanceTravelled = 0;
        mElapsedTime = 0;
        mRideTime = 0;
        mMaxSpeed = 0;
        mCurrentSpeed = 0;
        mTotalAscent = 0;
        mTotalDescent = 0;
    }

    public void updateData(Location newLoc, Location oldLoc,
                           Time now, Time then) {
        // update elevation information
        float elevationChange = newLoc.getAltitude() - oldLoc.getAltitude();
        if ( elevationChange < 0 ) mTotalDescent -= elevationChange;
        else mTotalAscent += elevationChange;

        // update time information
        mElapsedTime = (int)(now.toMillis(false) - mBeginTime.toMillis(false));
        int timeDiff = (int)(now.toMillis(false) - then.toMillis(false));
        mRideTime += timeDiff;

        // update distance
        double distanceDiff = oldLoc.distanceTo(newLoc);
        mDistanceTravelled += distanceDiff;

        // update speeds
        mCurrentSpeed = (float)(distanceDiff / timeDiff);
        mMaxSpeed = Math.max(mCurrentSpeed,mMaxSpeed);
    }

}
