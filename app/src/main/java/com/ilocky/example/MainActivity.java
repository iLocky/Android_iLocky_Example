package com.ilocky.example;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.SharedPreferences;
import android.os.Handler;
import android.os.IBinder;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AbsListView;
import android.widget.ListView;

import com.daimajia.androidanimations.library.Techniques;
import com.daimajia.androidanimations.library.YoYo;
import com.ilocky.ILocky;
import com.ilocky.ILockyPassport;
import com.ilocky.ILockyService;

public class MainActivity extends AppCompatActivity {

    ILockyKeyAdapter mAdapter=null;
    ListView mListView=null;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ILockyPassport.initialize(this); //initialize the iLocky Passport(Keys)
        ILockyPassport.removeAllInvalidPassports();

        startService(new Intent(getBaseContext(), ILockyService.class));
        doBindService();
        /** CAUTION: any passport should be genetered in our global server.
         ** this iLocky passport(key) generator is only for testing. it will be removed in near future.
         ********************************************************************************************/
        ILockyPassport passport = ILockyPassport.Builder()
                .setActionType(ILockyPassport.ACTION_TYPE_LOW_SECURITY_OPEN)
                .setDeviceId(ILockyPassport.getDeviceUuid())
                .setILockyId("99fe71d1")
                .setStartTime(System.currentTimeMillis())
                .setEndTime(System.currentTimeMillis() + 36000000)
                .setTimes(0)
                .setRevokePast(true);
        /********************************************************************************************/
        String p=passport.toJSONString();
        ILockyPassport.importPassport(p);

        mListView=(ListView)findViewById(R.id.listView);
        mAdapter =new ILockyKeyAdapter(this,ILockyPassport.getAlliLockyPassports());
        mListView.setAdapter(mAdapter);
    }

    public View getViewByPosition(int pos, AbsListView listView) {
        final int firstListItemPosition = listView.getFirstVisiblePosition();
        final int lastListItemPosition = firstListItemPosition + listView.getChildCount() - 1;

        if (pos < firstListItemPosition || pos > lastListItemPosition ) {
            return listView.getAdapter().getView(pos, null, listView);
        } else {
            final int childIndex = pos - firstListItemPosition;
            return listView.getChildAt(childIndex);
        }
    }
    ILockyService myService=null;
    boolean bound=false;
    void doBindService() {
        boolean bound = bindService( new Intent( this, ILockyService.class ), serviceConnection, Context.BIND_AUTO_CREATE );
        if ( bound ) {
            new Handler().postDelayed(new Runnable() {
                @Override
                public void run() {
                    myService.setDoInBackground(true); // the iLocky service will run in background during the app open.
                    myService.setBackgroundSound(R.raw.open); //set background open door sound(raw resource sound id)
                    myService.setNotCloseEnoughVibrateAndSound(true, R.raw.notclose);//set not close enough vibrate and sound (true/false for vibrate, not close engough of raw resource sound id)
                    myService.setILockyEventCallback(new ILocky.ILockyEventCallback() {
                        @Override
                        public void onPassportUsed(final ILockyPassport iLockyPassport) {
                            runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    for(int i=0;i<mAdapter.getCount();i++) {
                                        if(iLockyPassport.isSame((ILockyPassport)mAdapter.getItem(i))) {
                                            YoYo.with(Techniques.Swing)
                                                    .duration(700)
                                                    .playOn(getViewByPosition(i, mListView));

                                            break;
                                        }
                                    }
                                }
                            });
                        }

                        @Override
                        public void onILockyAccessSuccess(ILockyPassport iLockyPassport) {
                            runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    mAdapter.passports=ILockyPassport.getAlliLockyPassports();
                                    mAdapter.notifyDataSetChanged();
                                }
                            });

                        }

                        @Override
                        public void onILockyAccessFail(ILockyPassport iLockyPassport) {

                        }

                        @Override
                        public void onNotCloseEnough() {

                        }
                    });
                }
            }, 1000); // not run immdeiately , delay 1 sec to set this service.
        }
        else {
            Log.d("ILocky", "Failed to bind service");
        }
    }

    //doUnbindService
    void doUnbindService() {
        unbindService(serviceConnection);
    }
    //Service Connection
    private final ServiceConnection serviceConnection = new ServiceConnection() {
        public void onServiceConnected( ComponentName className, IBinder service ) {
            myService = ( (ILockyService.ServiceBinder) service ).getService();
            bound = true;
        }
        public void onServiceDisconnected( ComponentName className ) {
            myService = null;
            bound = false;
        }
    };

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }
    boolean doInBackground=false;
    @Override
    protected void onResume() {
        super.onResume();
        SharedPreferences prefs = getSharedPreferences("iLocky", Context.MODE_PRIVATE);
        doInBackground=prefs.getBoolean(ILockyService.PREFS_DO_IN_BACKGROUND,false);
        if(myService!=null)
            myService.setDoInBackground(true);
    }
    @Override
    protected void onPause() {
        super.onPause();
        SharedPreferences prefs = getSharedPreferences("iLocky", Context.MODE_PRIVATE);
        prefs.edit().putBoolean(ILockyService.PREFS_DO_IN_BACKGROUND,doInBackground).apply();
        if(myService!=null) {
            myService.setDoInBackground(doInBackground);  // will keep doinbackground setting in service
        }
    }
    @Override
    protected void onDestroy() {
        super.onDestroy();
        doUnbindService();
    }
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }
}
