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

import android.text.format.Time;
import android.location.Location;
import android.os.Parcel;
import android.os.Parcelable;
import android.util.Log;

import java.io.Serializable;
import java.lang.Math;

public class RecordingData implements Parcelable {

    private float mDistanceTravelled;

    private Time mBeginTime;
    private int mElapsedTime;
    private int mRideTime;

    private float mMaxSpeed;
    private float mCurrentSpeed;

    private float mTotalAscent;
    private float mTotalDescent;

    public float getDistanceTravelled() { return mDistanceTravelled; }
    public Time getBeginTime() { return mBeginTime; }
    public int getElapsedTime() { return mElapsedTime; }
    public int getRideTime() { return mRideTime; }
    public float getMaxSpeed() { return mMaxSpeed; }
    public float getCurrentSpeed() { return mCurrentSpeed; }
    public float getTotalAscent() { return mTotalAscent; }
    public float getTotalDescent() { return mTotalDescent; }

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

    public RecordingData(Parcel in) {
        mDistanceTravelled = in.readFloat();
        mElapsedTime = in.readInt();
        mRideTime = in.readInt();
        mMaxSpeed = in.readFloat();
        mCurrentSpeed = in.readFloat();
        mTotalAscent = in.readFloat();
        mTotalDescent = in.readFloat();
    }

    @Override
    public void writeToParcel(Parcel out, int flags) {
        out.writeFloat(mDistanceTravelled);
        out.writeInt(mElapsedTime);
        out.writeInt(mRideTime);
        out.writeFloat(mMaxSpeed);
        out.writeFloat(mCurrentSpeed);
        out.writeFloat(mTotalAscent);
        out.writeFloat(mTotalDescent);
    }

    @Override
    public int describeContents() {
        return 0;
    }

    public static final Parcelable.Creator<RecordingData> CREATOR =
        new Parcelable.Creator<RecordingData>() {
        public RecordingData createFromParcel(Parcel in) {
            return new RecordingData(in);
        }

        public RecordingData[] newArray(int size) {
            return new RecordingData[size];
        }
    };


    public void updateData(Location newLoc, Location oldLoc,
                           Time now, Time then) {
        // on first update set 0 change
        if ( oldLoc == null ) oldLoc = newLoc;
        if ( then == null ) then = now;

        // update elevation information
        float elevationChange =
            (float)(newLoc.getAltitude() - oldLoc.getAltitude());
        if ( elevationChange < 0 ) mTotalDescent -= elevationChange;
        else mTotalAscent += elevationChange;

        // update time information
        mElapsedTime = (int)(now.toMillis(false) - mBeginTime.toMillis(false));
        int timeDiff = (int)(now.toMillis(false) - then.toMillis(false));
        mRideTime += timeDiff;

        // update distance (in meters)
        double distanceDiff = oldLoc.distanceTo(newLoc);
        mDistanceTravelled += distanceDiff;

        // update speeds (in meters / ms)
        // TODO investigate using a trailing average, perhaps of three speeds?
        mCurrentSpeed = (float) (distanceDiff / timeDiff);
        mMaxSpeed = Math.max(mCurrentSpeed,mMaxSpeed);
    }

}
