package com.gdt.lianxuezhang.galaxydragontravel;

import android.annotation.TargetApi;
import android.app.Activity;
import android.app.AlertDialog;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.view.animation.AlphaAnimation;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;
import java.util.UUID;

public class Punch extends AppCompatActivity {

    private AlphaAnimation buttonClick = new AlphaAnimation(1F, 0.8F);

    private API api = new API(new ErrorHandler() {
        @Override
        public void run() {
            Toast.makeText(Punch.this, "INTERNET ERROR", Toast.LENGTH_SHORT).show();
        }
    });

    private static final int REQUEST_ENABLE_BT = 1;
    private static final int DISCOVER_DURATION = 300;

    private Button punch_in, punch_out, lunch_out, lunch_back;
    private Button log_out;
    private TextView mCompanyName;
    private TextView mSelectedCompany;
    private BluetoothAdapter mBluetoothAdapter;
    private UUID[] uuids = null;

    private SharedPreferenceUtils sharedPreferenceUtils;
    // attributes for sharedPreferences
    private String companyName;
    private int companyID = 0;
    private Boolean login_status = false;
    private String mEmail = "null";
    private String mPassword = "null";

    private JSONObject object;

    @Override
    public void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_punch);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        toolbar.setTitle("Attendance");
        setSupportActionBar(toolbar);

        setUp();

        if (object == null) {
            setClickable(false);
            Toast.makeText(this, "Cannot retrieve company info", Toast.LENGTH_SHORT).show();
            return;
        }

        parseCompany(object);
        turnOnBt();
    }

    private void setUp() {
        punch_in = (Button) findViewById(R.id.punch_in);
        punch_out = (Button) findViewById(R.id.punch_out);
        lunch_out = (Button) findViewById(R.id.lunch_out);
        lunch_back = (Button) findViewById(R.id.lunch_back);
        login_status = true;
        mEmail = getIntentValue("email");
        mPassword = getIntentValue("password");
        log_out = (Button) findViewById(R.id.log_out);
        log_out.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                v.startAnimation(buttonClick);
                api.httpGet("logout/");
                login_status = false;
                SharedPreferences sp = getSharedPreferences("this", Activity.MODE_PRIVATE);

                sharedPreferenceUtils = new SharedPreferenceUtils(sp);
                sharedPreferenceUtils.setLoginStatus(false).apply();

                Intent intent = new Intent(Punch.this, MainActivity.class);
                startActivity(intent);
            }
        });

        mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        mCompanyName = (TextView) findViewById(R.id.company_name);
        mSelectedCompany = (TextView) findViewById(R.id.selected_company);
        object = api.httpGet("get_company_info/");
    }

    private void parseCompany(JSONObject object) {
        try {
            companyID = object.getInt("company_id");
            companyName = object.getString("company_name");
            mCompanyName.setText(companyName);
            mSelectedCompany.setText(object.getString("company_uuid"));
            List<UUID> uuidList = new ArrayList<>();
            uuidList.add(UUID.fromString(object.getString("company_uuid")));
            uuids = new UUID[uuidList.size()];
            uuidList.toArray(uuids);
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    private String getIntentValue(String key) {
        String str;
        Bundle extras = getIntent().getExtras();
        if (extras == null) {
            str = "";
        } else {
            str = extras.getString(key);
        }
        return str;
    }

    public void showLeaveEarlyAlertDialog() {
        JSONObject returnedObject = api.httpGet("is_leaving_early/");
        try {
            boolean isLeavingEarly = returnedObject.getBoolean("is_leaving_early");
            if (isLeavingEarly) {
                AlertDialog dialog = new AlertDialog.Builder(Punch.this).setTitle("Are you sure")
                        .setMessage("Do you really want to leave before the scheduled time?").create();
                dialog.setButton(AlertDialog.BUTTON_NEGATIVE, "Cancel",
                        new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                setClickable(true);
                            }
                        });
                dialog.setButton(AlertDialog.BUTTON_POSITIVE, "OK",
                        new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                startScanDevice("WE");
                            }
                        });
                dialog.show();
            } else {
                startScanDevice("WE");
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    public void btnClick(View v) {

        setClickable(false);
        switch (v.getId()) {
            case R.id.punch_in:
                v.startAnimation(buttonClick);
                startScanDevice("WS");
                break;
            case R.id.punch_out:
                v.startAnimation(buttonClick);
                showLeaveEarlyAlertDialog();
                break;
            case R.id.lunch_out:
                v.startAnimation(buttonClick);
                startScanDevice("LO");
                break;
            case R.id.lunch_back:
                v.startAnimation(buttonClick);
                startScanDevice("LB");
                break;
            default:
                break;
        }

    }

    private void turnOnBt() {
        if (mBluetoothAdapter == null) {
            Toast.makeText(this, "Device does not support Bluetooth",
                    Toast.LENGTH_SHORT).show();
            mSelectedCompany.setText("Button is disabled as your device does not support Bluetooth");
            setClickable(false);
            return;
        }
        if (!mBluetoothAdapter.isEnabled()) {
            Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            enableBtIntent.putExtra(BluetoothAdapter.EXTRA_DISCOVERABLE_DURATION, DISCOVER_DURATION);
            startActivityForResult(enableBtIntent, REQUEST_ENABLE_BT);
        } else {
            Toast.makeText(getApplicationContext(), "Bluetooth Already on",
                    Toast.LENGTH_LONG).show();
        }

        if (android.os.Build.VERSION.SDK_INT < android.os.Build.VERSION_CODES.JELLY_BEAN_MR2) {
            setClickable(false);
            Toast.makeText(Punch.this, "Your device does not support BLE", Toast.LENGTH_SHORT).show();
        }
    }

    private BluetoothAdapter.LeScanCallback leScanCallback = null;

    @TargetApi(18)
    private void startScanDevice(final String str) {

        final Timer timer = new Timer();
        timer.schedule(new scanTask(), 5000);

        leScanCallback = new BluetoothAdapter.LeScanCallback() {
            @Override
            public void onLeScan(BluetoothDevice device, int rssi, byte[] scanRecord) {
                timer.cancel();
                mBluetoothAdapter.stopLeScan(this);
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        try {
                            JSONObject object = api.httpPost("timesheet/add/", "time_stamp_type=" + str);
                            if (object == null) return;
                            if (!object.getBoolean("success")) {
                                Toast.makeText(Punch.this, object.getString("msg"), Toast.LENGTH_SHORT).show();
                            } else {
                                Toast.makeText(Punch.this, "Attendance check success", Toast.LENGTH_SHORT).show();
                            }

                            setClickable(true);
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }
                });
            }
        };

        if (!mBluetoothAdapter.isEnabled()) {
            Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            enableBtIntent.putExtra(BluetoothAdapter.EXTRA_DISCOVERABLE_DURATION, DISCOVER_DURATION);
            startActivityForResult(enableBtIntent, REQUEST_ENABLE_BT);
        } else {
            try {
                UUID[] uuids = {UUID.fromString(object.getString("company_uuid"))};
                mBluetoothAdapter.startLeScan(uuids, leScanCallback);
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
    }

    class scanTask extends TimerTask {
        private Handler handler = new Handler() {
            @TargetApi(18)
            @Override
            public void dispatchMessage(Message msg) {
                super.dispatchMessage(msg);
                Toast.makeText(Punch.this,
                        "scan overtime, make sure you put your device colse to company signal",
                        Toast.LENGTH_SHORT).show();
                mBluetoothAdapter.stopLeScan(leScanCallback);
                setClickable(true);
            }
        };

        @Override
        public void run() {
            handler.sendEmptyMessage(0);
        }
    }

    private void setClickable(Boolean clickable) {
        punch_in.setClickable(clickable);
        punch_out.setClickable(clickable);
        lunch_out.setClickable(clickable);
        lunch_back.setClickable(clickable);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (resultCode == RESULT_OK && requestCode == REQUEST_ENABLE_BT) {
            Toast.makeText(Punch.this,
                    "Successfully enable Bluetooth", Toast.LENGTH_SHORT).show();
        } else {
            Toast.makeText(this, "Bluetooth is cancelled",
                    Toast.LENGTH_SHORT).show();
        }
    }

    private void savePreferences() {
        SharedPreferences sp = getSharedPreferences("this", Activity.MODE_PRIVATE);

        sharedPreferenceUtils = new SharedPreferenceUtils(sp);
        sharedPreferenceUtils.setLoginStatus(login_status).setCompanyId(companyID)
                .setUser(mEmail, mPassword, 0).commit();
    }

    @Override
    public void onPause() {
        super.onPause();
        savePreferences();
    }

    @Override
    protected void onStop() {
        super.onStop();
        savePreferences();
    }

    @Override
    public void onBackPressed() {
    }
}
