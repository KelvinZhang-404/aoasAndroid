package com.gdt.lianxuezhang.galaxydragontravel;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.gdt.lianxuezhang.models.Company;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class CompanySelectActivity extends AppCompatActivity {
    private API api = new API(new ErrorHandler() {
        @Override
        public void run() {
            Toast.makeText(CompanySelectActivity.this, "INTERNET ERROR", Toast.LENGTH_SHORT).show();
        }
    });

    private int role;
    private String mEmail;
    private String mPassword;

    private List<Company> companyList = new ArrayList<>();
    private ListView listView;
    private ListViewAdapter listViewAdapter;
    private List<Map<String, Object>> listItems;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_company_select);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        toolbar.setTitle("Please select your company");
        setSupportActionBar(toolbar);
        setUp();
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                int companyId = companyList.get(position).getId();
                JSONObject object = api.httpGet("select_company/" + companyId + "/");

                if (object == null) return;

                Intent intent = null;
                if (role == 0) {
                    intent = new Intent(CompanySelectActivity.this, TabActivity.class);
                } else if (role == 1) {
                    intent = new Intent(CompanySelectActivity.this, BtServerController.class);
                }
                if (intent != null) {
                    intent.putExtra("companyId", companyId);
                    intent.putExtra("email", mEmail);
                    intent.putExtra("password", mPassword);
                    CompanySelectActivity.this.startActivity(intent);
                } else {
                    Toast.makeText(CompanySelectActivity.this,
                            "something goes wrong",
                            Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    private void setUp() {
        role = getIntentValue();
        setCompanies(role);
        mEmail = getIntentValue("email");
        mPassword = getIntentValue("password");

        listView = (ListView) findViewById(R.id.listView);
        listItems = getListItems();
        listViewAdapter = new ListViewAdapter(this, listItems);
        listView.setAdapter(listViewAdapter);
    }

    private int getIntentValue() {
        int id;
        Bundle extras = getIntent().getExtras();
        if (extras == null) {
            id = -1;
        } else {
            id = extras.getInt("role");
        }
        return id;
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

    private void setCompanies(int role) {
        JSONObject object = api.httpGet("display_company/" + role + "/");
        try {
            if (object == null) return;
            JSONArray companies = object.getJSONArray("companies");
            for (int i = 0; i < companies.length(); i++) {
                int companyID = companies.getJSONObject(i).getInt("company_id");
                String company_name = companies.getJSONObject(i).getString("company");
                Company company = new Company(company_name, companyID);
                companyList.add(company);
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    private List<Map<String, Object>> getListItems() {
        List<Map<String, Object>> listItems = new ArrayList<>();
        for (int i = 0; i < companyList.size(); i++) {
            Map<String, Object> map = new HashMap<>();
            map.put("cmpName", companyList.get(i).getName());
            listItems.add(map);
        }
        return listItems;
    }

    /**
     * ListViewAdapter class
     *
     * @author LianxueZhang
     */
    public class ListViewAdapter extends BaseAdapter {
        private List<Map<String, Object>> listItems;
        private LayoutInflater listContainer;

        public final class ListItemView {
            public TextView textView;
        }

        public ListViewAdapter(Context context, List<Map<String, Object>> listItems) {
            listContainer = LayoutInflater.from(context);
            this.listItems = listItems;
        }

        public int getCount() {
            return listItems.size();
        }

        public Object getItem(int arg0) {
            return null;
        }

        public long getItemId(int arg0) {
            return 0;
        }

        public View getView(int position, View convertView, ViewGroup parent) {
            ListItemView listItemView;
            if (convertView == null) {
                listItemView = new ListItemView();
                convertView = listContainer.inflate(R.layout.company_list_item, parent, false);
                listItemView.textView = (TextView) convertView.findViewById(R.id.company_name);
                convertView.setMinimumHeight(150);
                convertView.setTag(listItemView);
            } else {
                listItemView = (ListItemView) convertView.getTag();
            }
            listItemView.textView.setText((String) listItems.get(position).get("cmpName"));

            return convertView;
        }
    }

    @Override
    public void onBackPressed() {
    }
}