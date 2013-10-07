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
package ftrimble.kingme.device;

import ftrimble.kingme.device.preferences.KingMeSettings;
import ftrimble.kingme.device.record.RideRecordingService;
import ftrimble.kingme.device.record.RideRecordingService.RideRecordingBinder;
import ftrimble.kingme.device.record.RecordingData;
import ftrimble.kingme.device.ui.DataView;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.IBinder;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MenuInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.TextView;

import java.io.File;
import java.lang.Math;

public class KingMe extends Activity {

    // enum values for the units type
    private static final String METRIC_SETTING_VALUE = "0";
    private static final String STATUTE_SETTING_VALUE = "1";

    private static final int RESULT_SETTINGS = 1;

    private SharedPreferences mPreferences;

    private RideRecordingService mRecorder;

    private boolean mIsBound;

    private OnClickListener mStartListener = new OnClickListener() {
            public void onClick(View v) {
                if ( mIsBound && mRecorder != null ) {
                    mRecorder.start_pause();
                }
            }
        };

    private OnClickListener mLapListener = new OnClickListener() {
            public void onClick(View v) {
                // no need to do anything if recording hasn't started
                if ( mIsBound && mRecorder != null ) {
                    mRecorder.lap_reset();
                }
            }
        };

    /**
     * This receiver will handle the information gathered by the recording
     * service. Every time new information is gathered in the service and
     * the user is in the application, this will refresh the views to show
     * the impact of the new data.
     */
    private BroadcastReceiver mReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                Bundle bundle = intent.getExtras();
                if ( bundle != null ) {
                    RecordingData rideData = (RecordingData)
                        bundle.get(RideRecordingService.RIDE_DATA);
                    // TODO use settings based refreshing.
                    DataView speed = (DataView) findViewById(R.id.speed_view),
                        distance = (DataView) findViewById(R.id.distance_view),
                        time = (DataView) findViewById(R.id.time_view);
                    speed.updateData(rideData);
                    distance.updateData(rideData);
                    time.updateData(rideData);
                }
            }
        };

    /**
     * Forges a connection between the ride recording service and the activity.
     */
    private ServiceConnection mConnection = new ServiceConnection() {
            @Override
            public void onServiceConnected(ComponentName className,
                                           IBinder service) {
                mRecorder = ((RideRecordingBinder)service).getService();
                mIsBound = true;
            }
            public void onServiceDisconnected(ComponentName arg0) {
                mRecorder = null;
                mIsBound = false;
            }
        };

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.main_menu,menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch ( item.getItemId() ) {
        case R.id.settings:
            startActivityForResult(new Intent(this, KingMeSettings.class),
                                   RESULT_SETTINGS);
            return true;
        default:
            return super.onOptionsItemSelected(item);
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode,resultCode,data);

        switch ( requestCode ) {
        case RESULT_SETTINGS:
            DataView speed = (DataView) findViewById(R.id.speed_view),
                distance = (DataView) findViewById(R.id.distance_view),
                time = (DataView) findViewById(R.id.time_view);
            String units = PreferenceManager.getDefaultSharedPreferences(this)
                .getString("units_category_key", METRIC_SETTING_VALUE);
            speed.changeUnits(units);
            distance.changeUnits(units);
            time.changeUnits(units);
        }
    }

    /**************************************************************************/
    /**************************************************************************/
    /***** LIFECYCLE FUNCTIONS ************************************************/
    /**************************************************************************/
    /**************************************************************************/

    @Override
    public void onStart() {
        super.onStart();
        mPreferences = PreferenceManager.getDefaultSharedPreferences(this);

        Intent intent = new Intent(KingMe.this,RideRecordingService.class);
        bindService(intent, mConnection, Context.BIND_AUTO_CREATE);
    }

    @Override
    public void onResume() {
        super.onResume();
        mPreferences = PreferenceManager.getDefaultSharedPreferences(this);
        registerReceiver(mReceiver,
                         new IntentFilter(RideRecordingService.BROADCAST));
    }

    @Override
    public void onPause() {
        super.onPause();
        unregisterReceiver(mReceiver);
    }

    @Override
    public void onStop() {
        super.onStop();

        if ( mIsBound ) {
            unbindService(mConnection);
            mIsBound = false;
        }
    }

    /**
     * Called when the activity is first created.
     */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);

        Button start_pause = (Button) findViewById(R.id.start_pause);
        Button lap_reset = (Button) findViewById(R.id.lap_reset);

        start_pause.setOnClickListener(mStartListener);
        lap_reset.setOnClickListener(mLapListener);

        Intent intent =
            new Intent(KingMe.this,RideRecordingService.class);
        startService(intent);
    }

}