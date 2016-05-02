package com.gdt.lianxuezhang.galaxydragontravel;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.os.StrictMode;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.animation.AlphaAnimation;
import android.widget.Button;
import android.widget.Toast;

public class MainActivity extends AppCompatActivity {

    private AlphaAnimation buttonClick = new AlphaAnimation(1F, 0.8F);

    private API api = new API(new ErrorHandler() {
        @Override
        public void run() {
            Toast.makeText(MainActivity.this, "INTERNET ERROR", Toast.LENGTH_SHORT).show();
        }
    });

    private SharedPreferenceUtils sharedPreferenceUtils;
    public Button mCompanyLogin;
    public Button mPersonLogin;

    private ConnectivityChangeReceiver connectivityChangeReceiver;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
        StrictMode.setThreadPolicy(policy);

        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        mCompanyLogin = (Button) findViewById(R.id.Btn1);
        mPersonLogin = (Button) findViewById(R.id.Btn2);

    }

    public void login(View v) {
        v.startAnimation(buttonClick);

        Intent intent = new Intent(MainActivity.this, LoginActivity.class);
        intent.putExtra("view_id", v.getId());
        startActivity(intent);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
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

    private void restoreUIState() {
        SharedPreferences sp = getSharedPreferences("this", Activity.MODE_PRIVATE);

        sharedPreferenceUtils = new SharedPreferenceUtils(sp);
        Boolean loginStatus = sharedPreferenceUtils.getLoginStatus();
        int role = sharedPreferenceUtils.getRole();
        int companyID = sharedPreferenceUtils.getCompanyId();
        String email = sharedPreferenceUtils.getUserEmail();
        String password = sharedPreferenceUtils.getUserPassword();

        ConnectivityManager connectivityManager = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetwork = connectivityManager.getActiveNetworkInfo();
        boolean isConnected = activeNetwork != null &&
                activeNetwork.isConnectedOrConnecting();
        if (!isConnected) {
            AlertDialog dialog = new AlertDialog.Builder(MainActivity.this).setTitle("Alert")
                    .setMessage("Device Wi-Fi is disconnected").create();

            dialog.setButton(AlertDialog.BUTTON_NEGATIVE, "Cancel", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                }
            });
            dialog.setButton(AlertDialog.BUTTON_POSITIVE, "OK", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                }
            });

            dialog.show();
        }

        if (loginStatus) {
            if (role == 0) {
                api.httpPost("login/", "email=" + email + "&password=" + password);
                api.httpGet("select_company/" + companyID + "/");
                Intent intent = new Intent(MainActivity.this, TabActivity.class);
                intent.putExtra("companyId", companyID);
                intent.putExtra("email", email);
                intent.putExtra("password", password);
                startActivity(intent);
            } else if (role == 1) {
                api.httpPost("login/", "email=" + email + "&password=" + password);
                api.httpGet("select_company/" + companyID + "/");
                Intent intent = new Intent(MainActivity.this, BtServerController.class);
                intent.putExtra("email", email);
                intent.putExtra("password", password);
                startActivity(intent);
            }
        }
    }

    @Override
    protected void onResume() {
        super.onResume();

        connectivityChangeReceiver = new ConnectivityChangeReceiver(new NotificationHandler() {
            @Override
            public void handle(String status) {
                if (status.equals("Connected")) {
//                    Toast.makeText(MainActivity.this, "You have connected to the network", Toast.LENGTH_SHORT).show();
                    restoreUIState();
                } else if (status.equals("Disconnected")) {
                    AlertDialog dialog = new AlertDialog.Builder(MainActivity.this).setTitle("Exception")
                            .setMessage("Please connect to any network").create();
                    dialog.setButton(AlertDialog.BUTTON_POSITIVE, "OK",
                            new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                }
                            });
                    dialog.show();
                }
            }
        });

        registerReceiver(this.connectivityChangeReceiver,
                new IntentFilter(ConnectivityManager.CONNECTIVITY_ACTION));
//        restoreUIState();
    }

    @Override
    public void onPause() {
        super.onPause();
        unregisterReceiver(connectivityChangeReceiver);
    }

    @Override
    protected void onStop() {
        super.onStop();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
    }

    @Override
    public void onBackPressed() {
    }
}
