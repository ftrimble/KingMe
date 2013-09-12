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

import ftrimble.kingme.device.record.RideRecordingService;
import ftrimble.kingme.device.record.RideRecordingService.RideRecordingBinder;
import ftrimble.kingme.device.record.RecordingData;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.IBinder;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.TextView;

import java.io.File;

public class KingMe extends Activity {

    private RideRecordingService mRecorder;

    private boolean mIsBound;

    private OnClickListener mStartListener = new OnClickListener() {
            public void onClick(View v) {
                if ( mIsBound && mRecorder != null ) {
                    mRecorder.start_pause();
                    TextView speed = (TextView) findViewById(R.id.speed_value);
                    speed.setText("2.0");
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

    private BroadcastReceiver mReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                Bundle bundle = intent.getExtras();
                if ( bundle != null ) {
                    RecordingData rideData = (RecordingData)
                        bundle.get(RideRecordingService.RIDE_DATA);
                    TextView speed = (TextView) findViewById(R.id.speed_value),
                        distance = (TextView) findViewById(R.id.distance_value),
                        time = (TextView) findViewById(R.id.time_value);
                    speed.setText(Float.toString(rideData.getCurrentSpeed()));
                    distance.setText(Float.toString
                                     (rideData.getDistanceTravelled()));
                    time.setText(Float.toString(rideData.getElapsedTime()));
                }
            }
        };

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
    public void onStart() {
        super.onStart();

        Intent intent = new Intent(KingMe.this,RideRecordingService.class);
        bindService(intent, mConnection, Context.BIND_AUTO_CREATE);
    }

    @Override
    public void onResume() {
        super.onResume();
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