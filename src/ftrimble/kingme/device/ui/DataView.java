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
package ftrimble.kingme.device.ui;

import ftrimble.kingme.device.record.RecordingData;
import ftrimble.kingme.device.R;

import android.content.Context;
import android.content.res.TypedArray;
import android.preference.PreferenceManager;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.widget.LinearLayout;
import android.widget.TextView;

public class DataView extends LinearLayout {

    // enum values for the data type attribute
    public static final int AVERAGE_SPEED = 0;
    public static final int INSTANTANEOUS_SPEED = 1;
    public static final int LAP_SPEED = 2;
    public static final int DISTANCE = 3;
    public static final int LAP_DISTANCE = 4;
    public static final int ELAPSED_TIME = 5;
    public static final int RIDE_TIME = 6;
    public static final int LAP_TIME = 7;

    // enum values for the units type
    private static final String METRIC_SETTING_VALUE = "0";
    private static final String STATUTE_SETTING_VALUE = "1";

    // useful conversions
    private static final String METRIC_SPEED_UNITS="kmh";
    private static final String STATUTE_SPEED_UNITS="mph";
    private static final String METRIC_DISTANCE_UNITS="km";
    private static final String STATUTE_DISTANCE_UNITS="mi";
    private static final float METERS_TO_MILES = 3.28084f/5280;
    private static final float METERS_TO_KILOMETERS = 1f/1000;
    private static final float MS_TO_SECONDS = 1.0f/1000;
    private static final int SECONDS_PER_MINUTE = 60;
    private static final int MINUTES_PER_HOUR = 60;
    private static final float MS_TO_MINUTES = MS_TO_SECONDS/60;
    private static final float MS_TO_HOURS = MS_TO_MINUTES/60;


    // Views contained in the layout
    private TextView mDescription;
    private TextView mData;
    private TextView mUnits;

    private int mDataGathered;

    private float mDistanceConversion;
    private float mSpeedConversion;

    public DataView(Context context) {
        this(context, null);
    }

    public DataView(Context context, AttributeSet attrs) {
        super(context, attrs);

        TypedArray a = context.obtainStyledAttributes
            (attrs,R.styleable.DataView, 0, 0);

        mDataGathered = a.getInt(R.styleable.DataView_data_type,0);

        setOrientation(LinearLayout.VERTICAL);

        LayoutInflater inflater = (LayoutInflater) context
            .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        inflater.inflate(R.layout.data_view, this, true);

        mDescription = (TextView) getChildAt(0);
        LinearLayout childLayout = (LinearLayout) getChildAt(1);
        mData = (TextView) childLayout.getChildAt(0);
        mUnits = (TextView) childLayout.getChildAt(1);

        setNewDataGathered();
    }

    public void setNewDataGathered() {
        // populate default text
        if ( mDataGathered == AVERAGE_SPEED )
            mDescription.setText(getResources().getString(R.string.avg_speed));
        else if ( mDataGathered == INSTANTANEOUS_SPEED )
            mDescription.setText(getResources().getString(R.string.curr_speed));
        else if ( mDataGathered == LAP_SPEED )
            mDescription.setText(getResources().getString(R.string.lap_speed));
        else if ( mDataGathered == DISTANCE )
            mDescription.setText(getResources().getString(R.string.distance));
        else if ( mDataGathered == LAP_DISTANCE )
            mDescription.setText(getResources().getString(R.string.lap_distance));
        else if ( mDataGathered == ELAPSED_TIME )
            mDescription.setText(getResources().getString(R.string.elapsed_time));
        else if ( mDataGathered == RIDE_TIME )
            mDescription.setText(getResources().getString(R.string.ride_time));
        else if ( mDataGathered == LAP_TIME )
            mDescription.setText(getResources().getString(R.string.lap_time));

        if ( mDataGathered == AVERAGE_SPEED ||
             mDataGathered == INSTANTANEOUS_SPEED ||
             mDataGathered == LAP_SPEED ) {
            mData.setText(getResources().getString(R.string.speed_default));
        }
        else if ( mDataGathered == DISTANCE || mDataGathered == LAP_DISTANCE )
            mData.setText(getResources().getString(R.string.distance_default));
        else if ( mDataGathered == ELAPSED_TIME ||
                  mDataGathered == RIDE_TIME ||
                  mDataGathered == LAP_TIME ) {
            mData.setText(getResources().getString(R.string.time_default));
        }
        changeUnits(PreferenceManager.getDefaultSharedPreferences(context)
                    .getString("units_category_key",METRIC_SETTING_VALUE));
    }

    public void changeUnits(String newUnit) {
        if ( newUnit.equals(METRIC_SETTING_VALUE) ) {
            mDistanceConversion = METERS_TO_KILOMETERS;
            mSpeedConversion = METERS_TO_KILOMETERS*MS_TO_SECONDS;
            switch ( mDataGathered ) {
            case AVERAGE_SPEED:
            case INSTANTANEOUS_SPEED:
            case LAP_SPEED:
                mUnits.setText(getResources().getString(R.string.speed_units_metric));
                break;
            case DISTANCE:
            case LAP_DISTANCE:
                mUnits.setText(getResources().getString(R.string.distance_units_metric));
                break;
            default: break;
            }
        }
        else {
            mDistanceConversion = METERS_TO_MILES;
            mSpeedConversion = METERS_TO_MILES/MS_TO_SECONDS;
            switch ( mDataGathered ) {
            case AVERAGE_SPEED:
            case INSTANTANEOUS_SPEED:
            case LAP_SPEED:
                mUnits.setText(getResources().getString(R.string.speed_units_statute));
                break;
            case DISTANCE:
            case LAP_DISTANCE:
                mUnits.setText(getResources().getString(R.string.distance_units_statute));
                break;
            default: break;
            }
        }
    }

    public void updateData(RecordingData data) {
        switch ( mDataGathered ) {
        case AVERAGE_SPEED:
        case INSTANTANEOUS_SPEED:
        case LAP_SPEED:
            float speed = 0;
            if ( mDataGathered == AVERAGE_SPEED )
                speed = data.getDistanceTravelled()/data.getRideTime();
            else if ( mDataGathered == INSTANTANEOUS_SPEED ) speed = data.getCurrentSpeed();
            else if ( mDataGathered == LAP_SPEED ) speed = 0;
            mData.setText(String.format("%1.2f",speed*mSpeedConversion));
            break;
        case DISTANCE:
        case LAP_DISTANCE:
            float distance = 0;
            if ( mDataGathered == DISTANCE ) distance = data.getDistanceTravelled();
            else if ( mDataGathered == LAP_DISTANCE ) distance = 0;
            mData.setText(String.format("%1.2f",distance*mDistanceConversion));
            break;
        case ELAPSED_TIME:
        case RIDE_TIME:
        case LAP_TIME:
            int timeInMS = 0;
            if ( mDataGathered == ELAPSED_TIME ) timeInMS = data.getElapsedTime();
            else if ( mDataGathered == RIDE_TIME ) timeInMS = data.getRideTime();
            else if ( mDataGathered == LAP_TIME ) timeInMS = 0;
            mData.setText
                (String.format("%d:%02d:%02d",
                               Math.round(timeInMS*MS_TO_HOURS),
                               Math.round(timeInMS*MS_TO_MINUTES) % MINUTES_PER_HOUR,
                               Math.round(timeInMS*MS_TO_SECONDS) % SECONDS_PER_MINUTE));
            break;
        default: return;
        }
    }

}