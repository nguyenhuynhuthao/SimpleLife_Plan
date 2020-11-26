package com.example.simplelife.fragments;

import android.app.AlarmManager;
import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.app.PendingIntent;
import android.app.TimePickerDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;

import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.util.AttributeSet;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.GridView;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.TimePicker;
import android.widget.Toast;

import com.example.simplelife.R;
import com.example.simplelife.activities.AlarmReceiverPlan;
import com.example.simplelife.database.PlansDBOpenHelper;
import com.example.simplelife.database.PlansDBStructure;
import com.example.simplelife.entities.Events;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.TimeZone;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link PlanFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class PlanFragment extends Fragment {

    private static final String TAG = "PlanFragment";
    private TextView txtDate;
    private DatePickerDialog.OnDateSetListener mDataSetListener;

    //TODO: Khởi tạo nút next và previous của tháng
    ImageButton nextButton, previousButton;
    TextView curentDate;
    GridView gridView;
    private static final int MAX_CALENDAR_DAYS = 42;
    // Locale.ENGLISH: lấy lịch và hiện lên bằng tiếng anh
    Calendar calendar = Calendar.getInstance(Locale.ENGLISH);
    Context context;

    // SimpleDateFormat trong java cung cấp các phương thức để định dạng
    // và phân tích ngày tháng và thời gian trong java.
    // Lưu ý: định dạng (format) có nghĩa là chuyển đổi date thành string
    // và phân tích (parse) có nghĩa là chuyển đổi string thành date.
    SimpleDateFormat dateFormat = new SimpleDateFormat("MMMM yyyy", Locale.ENGLISH);
    SimpleDateFormat monthFormat = new SimpleDateFormat("MMMM", Locale.ENGLISH);
    SimpleDateFormat yearFormat = new SimpleDateFormat("yyyy", Locale.ENGLISH);
    SimpleDateFormat eventDateFormate = new SimpleDateFormat("yyyy-MM-dd", Locale.ENGLISH);


    GridAdapterPlan gridAdapterPlan;
    PlansDBOpenHelper plandbOpenHelper;
    AlertDialog alertDialog;
    // Tạo 1 list các ngày
    List<Date> dates = new ArrayList<>();
    // Tạo 1 list các sự kiện lick
    List<Events> eventsList = new ArrayList<>();
    // Tạo biến nhắc nhớ
    int alarmYear, alarmMonth, alarmDay, alarmHours, alarmMinute;




    public PlanFragment() {
        // Required empty public constructor
    }

    public static PlanFragment newInstance() {
        PlanFragment fragment = new PlanFragment();
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        //Log ra console cua Dev
        Log.d("MY_PLAN", "PlanFragment is opening...");
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View v = inflater.inflate(R.layout.fragment_plan, container, false);

        // TODO: hiện ngày tháng năm
        txtDate = v.findViewById(R.id.calender_tv);
        txtDate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Calendar cal = Calendar.getInstance();
                int year = cal.get(Calendar.YEAR);
                int month = cal.get(Calendar.MONTH);
                int day = cal.get(Calendar.DAY_OF_MONTH);

                DatePickerDialog dialog = new DatePickerDialog(getActivity(), android.R.style.Theme_Holo_Dialog_MinWidth, mDataSetListener, year, month, day);

                dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
                dialog.show();
            }
        });

        //TODO: Chọn và lấy ngày, tháng, năm
        mDataSetListener = new DatePickerDialog.OnDateSetListener() {
            @Override
            public void onDateSet(DatePicker view, int year, int month, int dayOfMonth) {
                month = month + 1;
                Log.d(TAG, "onDateSet: dd/mmmm/yyyy" + dayOfMonth + "/" + month + "/" + year);
                String date = dayOfMonth + "/" + month + "/" + year;
                //Load date lên hệ thống
                txtDate.setText(date);
            }
        };

        //TODO: Khởi tạo các nút bấm cho nextMonth, previousMonth
        nextButton = v.findViewById(R.id.nextBtn);
        previousButton = v.findViewById(R.id.previousBtn);
        curentDate = v.findViewById(R.id.current_Date);
        gridView = v.findViewById(R.id.gridview);

        SetUpCalendar();

        // Bấm nút giảm tháng
        previousButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                calendar.add(Calendar.MONTH, -1);
                SetUpCalendar();
            }
        });

        // Bấm nút tăng tháng
        nextButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                calendar.add(Calendar.MONTH, 1);
                SetUpCalendar();
            }
        });


        //TODO: chức năng thêm plan
        gridView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

                AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
                builder.setCancelable(true);
                View addView = LayoutInflater.from(parent.getContext()).inflate(R.layout.activity_newevent_plan, null);
                EditText eventTitle = addView.findViewById(R.id.edt_title_event);
                EditText eventPlace = addView.findViewById(R.id.edt_place_event);
                TextView eventTime = addView.findViewById(R.id.eventtime);
                ImageButton setTime = addView.findViewById(R.id.seteventtime);
                EditText eventBring = addView.findViewById(R.id.edt_bring_event);
                Button addEventPlan = addView.findViewById(R.id.addevent);

                // lấy biến giá trị của checkbox alarm nhắc nhở
                CheckBox alarmMe = addView.findViewById(R.id.alarme);
                Calendar dateCalendar = Calendar.getInstance();
                dateCalendar.setTime(dates.get(position));
                alarmYear = dateCalendar.get(Calendar.YEAR);
                alarmMonth = dateCalendar.get(Calendar.MONTH);
                alarmDay = dateCalendar.get(Calendar.DAY_OF_MONTH);

                // set thời gian cho plan
                setTime.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Calendar calendar = Calendar.getInstance();
                        int hours = calendar.get(Calendar.HOUR_OF_DAY);
                        int minute = calendar.get(Calendar.MINUTE);
                        TimePickerDialog timePickerDialog = new TimePickerDialog(getActivity(), R.style.Theme_AppCompat_Dialog, new TimePickerDialog.OnTimeSetListener() {
                            @Override
                            public void onTimeSet(TimePicker view, int hourOfDay, int minute) {
                                Calendar c = Calendar.getInstance();
                                c.set(Calendar.HOUR_OF_DAY, hourOfDay);
                                c.set(Calendar.MINUTE, minute);
                                c.setTimeZone(TimeZone.getDefault());
                                SimpleDateFormat hformate = new SimpleDateFormat("K:mm a", Locale.ENGLISH);
                                String event_Time = hformate.format(c.getTime());
                                eventTime.setText(event_Time);

                                // lấy thời gian của plan để get thời gian nhắc nhở
                                alarmHours = c.get(Calendar.HOUR_OF_DAY);
                                alarmMinute = c.get(Calendar.MINUTE);

                            }
                        }, hours, minute, false);
                        // show ra thời gian lấy được
                        timePickerDialog.show();
                    }
                });

                // hàm lưu plan
                String date = eventDateFormate.format(dates.get(position));
                String month = monthFormat.format(dates.get(position));
                String year = yearFormat.format(dates.get(position));

                addEventPlan.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {

                        // kiểm tra checkbox nhắc nhở
                        if(alarmMe.isChecked()){
                            SaveEvent(eventTitle.getText().toString(), eventPlace.getText().toString(), eventTime.getText().toString(), date, month, year, eventBring.getText().toString(),"on");
                            SetUpCalendar();

                            // set values cho hàm nhắc nhở
                            Calendar calendar = Calendar.getInstance();
                            calendar.set(alarmYear,alarmMonth,alarmDay,alarmHours,alarmMinute);
                            setAlarm(calendar,eventTitle.getText().toString(),eventTime.getText().toString(),getRequestCode(date,eventTitle.getText().toString(),eventTime.getText().toString(),eventPlace.getText().toString(),eventBring.getText().toString()));

                            alertDialog.dismiss();
                        }
                        else {
                            SaveEvent(eventTitle.getText().toString(), eventPlace.getText().toString(), eventTime.getText().toString(), date, month, year, eventBring.getText().toString(),"off");
                            SetUpCalendar();
                            alertDialog.dismiss();
                        }


                    }
                });

                builder.setView(addView);
                alertDialog = builder.create();
                alertDialog.show();
            }
        });

        //TODO: hàm hiển thị tất cả các plan
        gridView.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {

                String date = eventDateFormate.format(dates.get(position));

                AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
                builder.setCancelable(true);
                View showView = LayoutInflater.from(parent.getContext()).inflate(R.layout.activity_show_event_plan, null);

                RecyclerView recyclerView = showView.findViewById(R.id.EventsRV);
                RecyclerView.LayoutManager layoutManager = new LinearLayoutManager(showView.getContext());
                recyclerView.setLayoutManager(layoutManager);
                recyclerView.setHasFixedSize(true);
                PlanRecycleAdapter planRecycleAdapter = new PlanRecycleAdapter(showView.getContext(),CollecEventByDate(date));
                recyclerView.setAdapter(planRecycleAdapter);
                planRecycleAdapter.notifyDataSetChanged();

                builder.setView(showView);
                alertDialog = builder.create();
                alertDialog.show();

                //TODO: setup calendar
                alertDialog.setOnCancelListener(new DialogInterface.OnCancelListener() {
                    @Override
                    public void onCancel(DialogInterface dialog) {
                        SetUpCalendar();
                    }
                });

                return true;
            }
        });

        return v;
    }

    //TODO: lấy id của plan để xác định nó là cái nào
    private int getRequestCode(String date, String event, String time, String place, String bring){
        int code = 0;
        plandbOpenHelper = new PlansDBOpenHelper(getActivity());
        SQLiteDatabase database = plandbOpenHelper.getReadableDatabase();
        Cursor cursor = plandbOpenHelper.ReadIDEvent(date,event,time,place,bring,database);
        while (cursor.moveToNext()){
           code = cursor.getInt(cursor.getColumnIndex(PlansDBStructure.ID));
        }
        cursor.close();
        plandbOpenHelper.close();

        return code;
    }

    //TODO: Chức năng nhắc nhở plan
    private void setAlarm(Calendar calendar, String event, String time, int RequestCode){
        Intent intent = new Intent(getActivity().getApplicationContext(), AlarmReceiverPlan.class);
        intent.putExtra("event",event);
        intent.putExtra("time",time);
        intent.putExtra("id",RequestCode);
        PendingIntent pendingIntent = PendingIntent.getBroadcast(getActivity(),RequestCode,intent,PendingIntent.FLAG_ONE_SHOT);
        AlarmManager alarmManager = (AlarmManager)getActivity().getApplicationContext().getSystemService(Context.ALARM_SERVICE);
        alarmManager.set(AlarmManager.RTC_WAKEUP,calendar.getTimeInMillis(),pendingIntent);
    }


    private ArrayList<Events> CollecEventByDate(String date){
        ArrayList<Events> arrayList = new ArrayList<>();
        plandbOpenHelper = new PlansDBOpenHelper(getActivity());
        SQLiteDatabase database = plandbOpenHelper.getReadableDatabase();
        Cursor cursor = plandbOpenHelper.ReadEvent(date,database);
        while (cursor.moveToNext()){

            String event = cursor.getString(cursor.getColumnIndex(PlansDBStructure.EVENT));
            String place = cursor.getString(cursor.getColumnIndex(PlansDBStructure.EVENT_PLACE));
            String time = cursor.getString(cursor.getColumnIndex(PlansDBStructure.TIME));
            String Date = cursor.getString(cursor.getColumnIndex(PlansDBStructure.DATE));
            String month = cursor.getString(cursor.getColumnIndex(PlansDBStructure.MONTH));
            String yearr = cursor.getString(cursor.getColumnIndex(PlansDBStructure.YEAR));
            String bring = cursor.getString(cursor.getColumnIndex(PlansDBStructure.EVENT_BRING));
            Events events = new Events(event,place,time,Date,month,yearr,bring);
            arrayList.add(events);

        }
        cursor.close();
        plandbOpenHelper.close();

        return arrayList;
    }


    public PlanFragment(int contentLayoutId, Context context) {
        super(contentLayoutId);
        this.context = context;
    }

    private void SaveEvent(String event, String place, String time, String date, String month, String year, String bring, String notify){
        plandbOpenHelper = new PlansDBOpenHelper(getActivity());
        SQLiteDatabase database = plandbOpenHelper.getWritableDatabase();
        plandbOpenHelper.SaveEvent(event,place,time,date,month,year,bring,notify,database);
//        plandbOpenHelper.close();
        Toast.makeText(getActivity(), "Event Plan Save", Toast.LENGTH_SHORT).show();
    }

    // TODO: Hàm load lại lịch sau khi tăng, giảm tháng
    private void SetUpCalendar(){
        String currwntDate = dateFormat.format(calendar.getTime());
        curentDate.setText(currwntDate);
        dates.clear();
        Calendar monthCalendar = (Calendar) calendar.clone();
        monthCalendar.set(Calendar.DAY_OF_MONTH,1);
        int FirstDayofMonth = monthCalendar.get(Calendar.DAY_OF_WEEK)-1;
        // add ngày đầu tiên của tháng
        monthCalendar.add(Calendar.DAY_OF_MONTH, -FirstDayofMonth);

        //hàm lấy dữ liệu kế hoạch tháng
        CollectEventsPerMonth(monthFormat.format(calendar.getTime()), yearFormat.format(calendar.getTime()));

        // lấy ngày của 1 tháng
        while(dates.size() < MAX_CALENDAR_DAYS) {
            dates.add(monthCalendar.getTime());
            monthCalendar.add(Calendar.DAY_OF_MONTH,1);
        }

        gridAdapterPlan = new GridAdapterPlan(getActivity(),dates,calendar,eventsList);
        gridView.setAdapter(gridAdapterPlan);
    }

    //TODO: Thu thập sự kiện mỗi tháng
    private void CollectEventsPerMonth(String Month, String year){
        eventsList.clear();
        plandbOpenHelper = new PlansDBOpenHelper(getActivity());
        SQLiteDatabase database = plandbOpenHelper.getReadableDatabase();
        Cursor cursor = plandbOpenHelper.ReadEventsperMonth(Month,year,database);
        while(cursor.moveToNext()){
            String event = cursor.getString(cursor.getColumnIndex(PlansDBStructure.EVENT));
            String place = cursor.getString(cursor.getColumnIndex(PlansDBStructure.EVENT_PLACE));
            String time = cursor.getString(cursor.getColumnIndex(PlansDBStructure.TIME));
            String date = cursor.getString(cursor.getColumnIndex(PlansDBStructure.DATE));
            String month = cursor.getString(cursor.getColumnIndex(PlansDBStructure.MONTH));
            String yearr = cursor.getString(cursor.getColumnIndex(PlansDBStructure.YEAR));
            String bring = cursor.getString(cursor.getColumnIndex(PlansDBStructure.EVENT_BRING));
            Events events = new Events(event,place,time,date,month,year,bring);

            eventsList.add(events);

        }
        cursor.close();
        plandbOpenHelper.close();
    }

}