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

import android.app.Activity;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
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
                // Send start button clicked signal
                Intent intent =
                    new Intent(KingMe.this,RideRecordingService.class);

                startService(intent);
            }
        };

    private OnClickListener mLapListener = new OnClickListener() {
            public void onClick(View v) {
                Intent intent =
                    new Intent(KingMe.this,RideRecordingService.class);

                stopService(intent);
            }
        };

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
    }

}