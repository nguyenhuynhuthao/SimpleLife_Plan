package com.example.simplelife.fragments;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.simplelife.R;
import com.example.simplelife.activities.AlarmReceiverPlan;
import com.example.simplelife.database.PlansDBOpenHelper;
import com.example.simplelife.database.PlansDBStructure;
import com.example.simplelife.entities.Events;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

public class PlanRecycleAdapter extends RecyclerView.Adapter<PlanRecycleAdapter.MyViewHolder> {

    Context context;
    ArrayList<Events> arrayList;
    PlansDBStructure plansDBStructure;
    PlansDBOpenHelper plansDBOpenHelper;

    public PlanRecycleAdapter(Context context, ArrayList<Events> arrayList) {
        this.context = context;
        this.arrayList = arrayList;
    }

    @NonNull
    @Override
    public MyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.fragment_show_event_plan_rowlayout, parent,false);
        return new MyViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull MyViewHolder holder, int position) {

        Events events = arrayList.get(position);
        holder.Event.setText(events.getEVENT());
        holder.DateText.setText(events.getDATE());
        holder.Place.setText(events.getPLACE());
        holder.Bring.setText(events.getBRING());
        holder.Time.setText(events.getTIME());
        holder.delete.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                deleteCalendarEvent(events.getEVENT(),events.getPLACE(),events.getDATE(),events.getTIME(),events.getBRING());
                // xóa plan trong array list
                arrayList.remove(position);
                // lưu thay đổi
                notifyDataSetChanged();

            }
        });


        if(isAlarmMe(events.getEVENT(),events.getPLACE(),events.getDATE(),events.getTIME(),events.getBRING())){
            holder.setAlarm.setImageResource(R.drawable.ic_action_notification_on);
        }
        else {
            holder.setAlarm.setImageResource(R.drawable.ic_action_notification_off);
        }

        Calendar dateCalendar = Calendar.getInstance();
        dateCalendar.setTime(ConvertStringToDate(events.getDATE()));
        int alarmYear = dateCalendar.get(Calendar.YEAR);
        int alarmMonth = dateCalendar.get(Calendar.MONTH);
        int alarmDay = dateCalendar.get(Calendar.DAY_OF_MONTH);
        Calendar timeCalendar = Calendar.getInstance();
        timeCalendar.setTime(ConvertStringToTime(events.getTIME()));
        int alarmHour = timeCalendar.get(Calendar.HOUR_OF_DAY);
        int alarmMinute = timeCalendar.get(Calendar.MINUTE);

        holder.setAlarm.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(isAlarmMe(events.getEVENT(),events.getPLACE(),events.getDATE(),events.getTIME(),events.getBRING())){
                    holder.setAlarm.setImageResource(R.drawable.ic_action_notification_off);
                    cancelAlarm(getRequestCode(events.getDATE(),events.getEVENT(),events.getTIME(),events.getPLACE(),events.getBRING()));
                    updateEvent(events.getDATE(),events.getEVENT(),events.getTIME(),events.getPLACE(),events.getBRING(),"off");
                    notifyDataSetChanged();
                }
                else {
                    holder.setAlarm.setImageResource(R.drawable.ic_action_notification_on);
                    Calendar alarmCalendar = Calendar.getInstance();
                    alarmCalendar.set(alarmYear,alarmMonth,alarmDay,alarmHour,alarmMinute);
                    setAlarm(alarmCalendar,events.getEVENT(),events.getTIME(),getRequestCode(events.getDATE(),events.getEVENT(),events.getTIME(),events.getPLACE(),events.getBRING()));
                    updateEvent(events.getDATE(),events.getEVENT(),events.getTIME(),events.getPLACE(),events.getBRING(),"on");
                    notifyDataSetChanged();
                }
            }
        });

    }

    @Override
    public int getItemCount() {
        return arrayList.size();
    }

    public class MyViewHolder extends RecyclerView.ViewHolder{

        TextView DateText, Event, Place, Bring, Time;
        Button delete;
        ImageButton setAlarm;

        public MyViewHolder(@NonNull View itemView) {
            super(itemView);

            DateText = itemView.findViewById(R.id.event_date);
            Event = itemView.findViewById(R.id.event_title);
            Place = itemView.findViewById(R.id.event_place);
            Bring = itemView.findViewById(R.id.event_bring);
            Time = itemView.findViewById(R.id.event_time);
            delete = itemView.findViewById(R.id.btndelete);
            //set click nhắc nhở
            setAlarm = itemView.findViewById(R.id.btnAlarmme);

        }
    }

    //TODO:
    private Date ConvertStringToDate(String eventDate){
        SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd", Locale.ENGLISH);
        Date date = null;

        try {
            date = format.parse(eventDate);
        } catch (ParseException e) {
            e.printStackTrace();
        }

        return date;
    }

    //TODO:
    private Date ConvertStringToTime(String eventDate){
        SimpleDateFormat format = new SimpleDateFormat("kk:mm", Locale.ENGLISH);
        Date date = null;

        try {
            date = format.parse(eventDate);
        } catch (ParseException e) {
            e.printStackTrace();
        }

        return date;
    }

    //TODO: delete plan
    public void deleteCalendarEvent(String event, String place, String date, String time, String bring){

        plansDBOpenHelper = new PlansDBOpenHelper(context);
        SQLiteDatabase database = plansDBOpenHelper.getWritableDatabase();
        plansDBOpenHelper.deleteEvent(event,place,date,time,bring,database);
        plansDBOpenHelper.close();

    }

    private boolean isAlarmMe(String event, String place, String date, String time, String bring){
        boolean alarmed = false;
        plansDBOpenHelper = new PlansDBOpenHelper(context);
        SQLiteDatabase database = plansDBOpenHelper.getReadableDatabase();
        Cursor cursor = plansDBOpenHelper.ReadIDEvent(event,place,date,time,bring,database);
        while (cursor.moveToNext()){
            String notify = cursor.getString(cursor.getColumnIndex(PlansDBStructure.Notify));

            // kiểm tra checkbox plan
            if(notify.equals("on")){
                alarmed = true;
            }
            else {
                alarmed = false;
            }
        }
        cursor.close();
        plansDBOpenHelper.close();

        return alarmed;
    }

    //TODO: Chức năng nhắc nhở plan: nhắc nhở
    private void setAlarm(Calendar calendar, String event, String time, int RequestCode){
        Intent intent = new Intent(context.getApplicationContext(), AlarmReceiverPlan.class);
        intent.putExtra("event",event);
        intent.putExtra("time",time);
        intent.putExtra("id",RequestCode);
        PendingIntent pendingIntent = PendingIntent.getBroadcast(context,RequestCode,intent,PendingIntent.FLAG_ONE_SHOT);
        AlarmManager alarmManager = (AlarmManager)context.getApplicationContext().getSystemService(Context.ALARM_SERVICE);
        alarmManager.set(AlarmManager.RTC_WAKEUP,calendar.getTimeInMillis(),pendingIntent);
    }

    //TODO: Chức năng nhắc nhở plan: chọn không nhắc nhở
    private void cancelAlarm(int RequestCode){
        Intent intent = new Intent(context.getApplicationContext(), AlarmReceiverPlan.class);
        PendingIntent pendingIntent = PendingIntent.getBroadcast(context,RequestCode,intent,PendingIntent.FLAG_ONE_SHOT);
        AlarmManager alarmManager = (AlarmManager)context.getApplicationContext().getSystemService(Context.ALARM_SERVICE);
        alarmManager.cancel(pendingIntent);
    }

    //TODO: lấy id của plan để xác định nó là cái nào
    private int getRequestCode(String date, String event, String time, String place, String bring){
        int code = 0;
        plansDBOpenHelper = new PlansDBOpenHelper(context);
        SQLiteDatabase database = plansDBOpenHelper.getReadableDatabase();
        Cursor cursor = plansDBOpenHelper.ReadIDEvent(date,event,time,place,bring,database);
        while (cursor.moveToNext()){
            code = cursor.getInt(cursor.getColumnIndex(PlansDBStructure.ID));
        }
        cursor.close();
        plansDBOpenHelper.close();

        return code;
    }

    //TODO: Update plan
    private void updateEvent(String date, String event, String time, String place, String bring, String notify){
        plansDBOpenHelper = new PlansDBOpenHelper(context);
        SQLiteDatabase database = plansDBOpenHelper.getWritableDatabase();
        plansDBOpenHelper.updateEvent(date,event,time,place,bring,notify,database);
        plansDBOpenHelper.close();
    }
}
