package com.gdt.lianxuezhang.galaxydragontravel;

import android.content.Intent;
import android.os.Bundle;
import android.widget.TabHost;

public class TabActivity extends android.app.TabActivity {

    private String mEmail;
    private String mPassword;
    private int mCompanyID;

    @Override
    public void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_tab);

        // create the TabHost that will contain the Tabs
        TabHost tabHost = (TabHost)findViewById(android.R.id.tabhost);

        Bundle extras = getIntent().getExtras();
        mEmail = extras.getString("email");
        mPassword = extras.getString("password");
        mCompanyID = extras.getInt("companyId");

        TabHost.TabSpec tab1 = tabHost.newTabSpec("First Tab");
        TabHost.TabSpec tab2 = tabHost.newTabSpec("Second Tab");
        TabHost.TabSpec tab3 = tabHost.newTabSpec("Third tab");

        Intent intent1 = new Intent(this,Punch.class);
        intent1.putExtra("email", mEmail);
        intent1.putExtra("password", mPassword);
        intent1.putExtra("companyId", mCompanyID);

        // Set the Tab name and Activity
        // that will be opened when particular Tab will be selected
        tab1.setIndicator("Punch");
        tab1.setContent(intent1);

        tab2.setIndicator("Timesheet");
        tab2.setContent(new Intent(this,TimeSheet.class));

        tab3.setIndicator("Payroll");
        tab3.setContent(new Intent(this,Payroll.class));

        /** Add the tabs  to the TabHost to display. */
        tabHost.addTab(tab1);
        tabHost.addTab(tab2);
        tabHost.addTab(tab3);
    }
}
