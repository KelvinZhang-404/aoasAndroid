package com.gdt.lianxuezhang.galaxydragontravel;

import android.annotation.TargetApi;
import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.le.AdvertiseCallback;
import android.bluetooth.le.AdvertiseData;
import android.bluetooth.le.AdvertiseSettings;
import android.bluetooth.le.BluetoothLeAdvertiser;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Build;
import android.os.Bundle;
import android.os.ParcelUuid;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.view.animation.AlphaAnimation;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.UUID;

public class BtServerController extends AppCompatActivity {

    private AlphaAnimation buttonClick = new AlphaAnimation(1F, 0.8F);

    private API api = new API(new ErrorHandler() {
        @Override
        public void run() {
            Toast.makeText(BtServerController.this,
                    "INTERNET ERROR",
                    Toast.LENGTH_SHORT).show();
        }
    });

    private static final String LOW_SDK_ERROR = "Device Does Not Support Bluetooth Server Mode";
    private static final String NOT_MULTIPLE_ADVERTISE = "Device Does Not Support Multiple Advertisement";

    private Button mSwitch;
    private TextView mAdvertiseStatus;
    private TextView mCompanyName;
    private TextView mCompanyUUID;

    private BluetoothAdapter mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
    private BluetoothLeAdvertiser mBluetoothLeAdvertiser = null;
    private Boolean mAdvertising;

    private SharedPreferenceUtils sharedPreferenceUtils;
    private String companyName;
    private int companyID;
    private String mEmail;
    private String mPassword;
    private Boolean loginStatus = false;

    private AdvertiseSettings adSettings = null;
    private AdvertiseData adData = null;
    private AdvertiseCallback advertiseCallback = null;

    private ParcelUuid mParcelUuid = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_bt_server_controller);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        setUp();

        JSONObject object = api.httpGet("get_company_info/");
        parseCompany(object);

        if (Build.VERSION.SDK_INT < 21) {
            mAdvertiseStatus.setText(LOW_SDK_ERROR);
            mSwitch.setClickable(false);
            return;
        } else if (mBluetoothAdapter != null && mBluetoothAdapter.isMultipleAdvertisementSupported()) {
            mBluetoothLeAdvertiser = mBluetoothAdapter.getBluetoothLeAdvertiser();
        } else {
            mAdvertiseStatus.setText(NOT_MULTIPLE_ADVERTISE);
            mSwitch.setClickable(false);
        }
        setUpAdvertise();
    }

    private void parseCompany(JSONObject object) {
        try {
            companyID = object.getInt("company_id");
            companyName = object.getString("company_name");
            String companyUUID = object.getString("company_uuid");
            mParcelUuid = new ParcelUuid(UUID.fromString(companyUUID));
            mCompanyName.setText(companyName);
            mCompanyUUID.setText(companyUUID);
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    private void setUp() {
        mCompanyName = (TextView) findViewById(R.id.company_name);
        mCompanyUUID = (TextView) findViewById(R.id.company_uuid);
        mSwitch = (Button) findViewById(R.id.btn_bt_signal);
        mAdvertiseStatus = (TextView) findViewById(R.id.advertise_status);
        mAdvertising = false;
        loginStatus = true;

        mEmail = getIntentValue("email");
        mPassword = getIntentValue("password");
    }

    public void btnClick(View v) {
        v.startAnimation(buttonClick);
        api.httpGet("logout/");

        loginStatus = false;
        SharedPreferences sp = getSharedPreferences("this", Activity.MODE_PRIVATE);

        sharedPreferenceUtils = new SharedPreferenceUtils(sp);
        sharedPreferenceUtils.setLoginStatus(false).apply();

        Intent intent = new Intent(BtServerController.this, MainActivity.class);
        startActivity(intent);
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

    @TargetApi(21)
    private void setUpAdvertise() {
        adSettings = new AdvertiseSettings.Builder()
                .setAdvertiseMode(AdvertiseSettings.ADVERTISE_MODE_BALANCED).build();
        adData = new AdvertiseData.Builder().addServiceUuid(mParcelUuid).build();

        advertiseCallback = new AdvertiseCallback() {
            @Override
            public void onStartSuccess(AdvertiseSettings settingsInEffect) {
                super.onStartSuccess(settingsInEffect);
                Toast.makeText(BtServerController.this,
                        "Successfully turn on broadcasting",
                        Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onStartFailure(int errorCode) {
                super.onStartFailure(errorCode);
                Toast.makeText(BtServerController.this,
                        "Failed to turn on broadcasting",
                        Toast.LENGTH_SHORT).show();
            }
        };
    }

    @TargetApi(21)
    private void checkAdvertisingStatus() {
        if (!mAdvertising) {
            mBluetoothLeAdvertiser.stopAdvertising(advertiseCallback);
            mSwitch.setText("Turn On");
            mAdvertiseStatus.setText("Advertising: Off");
        } else {
            mBluetoothLeAdvertiser.startAdvertising(adSettings, adData, advertiseCallback);
            mSwitch.setText("Turn Off");
            mAdvertiseStatus.setText("Advertising: On");
        }
    }

    @TargetApi(21)
    public void switchSignal(View v) {
        v.startAnimation(buttonClick);
        if (!mAdvertising) {
            mAdvertising = true;
            mBluetoothLeAdvertiser.startAdvertising(adSettings, adData, advertiseCallback);
            mSwitch.setText("Turn Off");
            mAdvertiseStatus.setText("Advertising: On");
        } else {
            mAdvertising = false;
            mBluetoothLeAdvertiser.stopAdvertising(advertiseCallback);
            mSwitch.setText("Turn On");
            mAdvertiseStatus.setText("Advertising: Off");
        }
    }

    /**
     * Save current data using an Editor
     */

    private void savePreferences() {
        SharedPreferences sharedPreferences = getSharedPreferences("this", Activity.MODE_PRIVATE);

        sharedPreferenceUtils = new SharedPreferenceUtils(sharedPreferences);
        sharedPreferenceUtils.setAdvertiseStatus(mAdvertising).setLoginStatus(loginStatus)
                .setCompanyId(companyID).setUser(mEmail, mPassword, 1).commit();
    }

    private void restoreUIState() {
        SharedPreferences sharedPreferences = getSharedPreferences("this", Activity.MODE_PRIVATE);

        sharedPreferenceUtils = new SharedPreferenceUtils(sharedPreferences);
        mAdvertising = sharedPreferenceUtils.getAdvertiseStatus();

        checkAdvertisingStatus();
    }

    @Override
    public void onPause() {
        super.onPause();
        savePreferences();
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (mBluetoothLeAdvertiser != null) restoreUIState();
    }

    @TargetApi(21)
    @Override
    protected void onStop() {
        super.onStop();
        if (mBluetoothLeAdvertiser != null)
            mBluetoothLeAdvertiser.stopAdvertising(advertiseCallback);
    }

    @Override
    public void onBackPressed() {
    }
}
