package com.gdt.lianxuezhang.galaxydragontravel;

import android.content.Context;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.gdt.lianxuezhang.models.TimesheetRow;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class TimeSheet extends AppCompatActivity {
    private API api = new API(new ErrorHandler() {
        @Override
        public void run() {
            Toast.makeText(TimeSheet.this, "INTERNET ERROR", Toast.LENGTH_SHORT).show();
        }
    });

    private ListView timeSheetListView;
    private ListViewAdapter listViewAdapter;
    private List<Map<String, Object>> listItems;

    private List<TimesheetRow> timesheetRowList = new ArrayList<>();

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_time_sheet);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        toolbar.setTitle("Time Sheet");
        setSupportActionBar(toolbar);

        setListView();

        timeSheetListView = (ListView) findViewById(R.id.timeSheetListView);
        listItems = getListItems();
        listViewAdapter = new ListViewAdapter(this, listItems);
        timeSheetListView.setAdapter(listViewAdapter);

    }

    @Override
    public void onResume() {
        super.onResume();
        updateData();
    }

    private void updateData() {
        setListView();
        listItems = getListItems();
        listViewAdapter = new ListViewAdapter(this, listItems);
        listViewAdapter.notifyDataSetChanged();
        timeSheetListView.setAdapter(listViewAdapter);
    }

    private void setListView() {
        timesheetRowList.clear();
        JSONObject object = api.httpGet("timesheet/");
        if (object == null) return;
        try {
            JSONArray timesheets = object.getJSONArray("timesheet_list");
            for (int i = 0; i < timesheets.length(); i++) {
                String comment = timesheets.getJSONObject(i).getString("comment");
                String overtime = timesheets.getJSONObject(i).getString("overtime");
                String timeStamp = timesheets.getJSONObject(i).getString("time_stamp");
                String timeStampType = timeStampType(timesheets.getJSONObject(i).getString("time_stamp_type"));
                TimesheetRow timesheetRow = new TimesheetRow(comment, overtime, timeStamp, timeStampType);
                timesheetRowList.add(timesheetRow);
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    private String timeStampType(String timeStampType) {
        switch (timeStampType) {
            case "WS":
                return "Start working";
            case "WE":
                return "End working";
            case "LO":
                return "Lunch out";
            case "LB":
                return "Lunch back";
            default:
                return "No Type";
        }
    }

    private List<Map<String, Object>> getListItems() {
        List<Map<String, Object>> listItems = new ArrayList<>();
        for (int i = 0; i < timesheetRowList.size(); i++) {
            Map<String, Object> map = new HashMap<>();
            map.put("comment", timesheetRowList.get(i).getComment());
            map.put("overtime", timesheetRowList.get(i).getOvertime());
            map.put("time_stamp", timesheetRowList.get(i).getTimeStamp());
            map.put("time_stamp_type", timesheetRowList.get(i).getTimeStampType());
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
            public TextView workType;
            public TextView workTypeText;
            public TextView time;
            public TextView timeText;
            public TextView overtime;
            public TextView overtimeText;
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
                convertView = listContainer.inflate(R.layout.timesheet_list_item, parent, false);
                listItemView.workType = (TextView) convertView.findViewById(R.id.work_type);
                listItemView.workTypeText = (TextView) convertView.findViewById(R.id.work_type_text);
                listItemView.time = (TextView) convertView.findViewById(R.id.time);
                listItemView.timeText = (TextView) convertView.findViewById(R.id.time_text);
                listItemView.overtime = (TextView) convertView.findViewById(R.id.overtime);
                listItemView.overtimeText = (TextView) convertView.findViewById(R.id.overtime_text);
                convertView.setMinimumHeight(150);
                convertView.setTag(listItemView);
            } else {
                listItemView = (ListItemView) convertView.getTag();
            }
            //Set text info
            listItemView.workTypeText.setText((String) listItems.get(position).get("time_stamp_type"));
            listItemView.timeText.setText((String) listItems.get(position).get("time_stamp"));
            listItemView.overtimeText.setText((String) listItems.get(position).get("overtime"));

            return convertView;
        }
    }

    @Override
    public void onBackPressed() {
    }
}
