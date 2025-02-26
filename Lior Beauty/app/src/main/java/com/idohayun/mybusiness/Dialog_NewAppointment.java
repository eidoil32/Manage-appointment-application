package com.idohayun.mybusiness;

import android.app.Activity;
import android.app.Dialog;
import android.app.DialogFragment;
import android.content.Context;
import android.content.DialogInterface;
import android.content.res.ColorStateList;
import android.content.res.Configuration;
import android.graphics.Color;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.constraint.Guideline;
import android.support.v4.view.ViewCompat;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.baoyz.swipemenulistview.SwipeMenuListView;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

public class Dialog_NewAppointment extends DialogFragment {
    private static final String TAG = "Dialog_NewAppointment";
    private SwipeMenuListView listView;
    private static DateArray currentDate;
    private Context context;
    private ProgressBar progressBar;
    private Map<String, String> appointmentDetails = new HashMap<>();
    private TextView username, phoneNumber, dialog_date, dialog_time;
    private Spinner treatmentTypes;
    private int colorBad = Color.RED, colorGood = Color.GRAY;
    private ColorStateList colorStateListBAD = ColorStateList.valueOf(colorBad),
            colorStateListGOOD = ColorStateList.valueOf(colorGood);
    private boolean is_addName, is_addPhone, is_chooseType;
    private int type;
    private baseUSER user = new baseUSER();
    private JsonObjectRequest request;

    public Dialog_NewAppointment() {
    }

    Dialog_NewAppointment(Context context, View view, SwipeMenuListView listView, DateArray currentDate, ProgressBar progressBar) {
        this.listView = listView;
        this.context = context;
        this.currentDate = currentDate;
        this.progressBar = progressBar;
    }

    @Override
    public void onResume() {
        super.onResume();
        ViewGroup.LayoutParams params = getDialog().getWindow().getAttributes();
        params.width = ViewGroup.LayoutParams.MATCH_PARENT;
        params.height = ViewGroup.LayoutParams.MATCH_PARENT;
        getDialog().getWindow().setAttributes((android.view.WindowManager.LayoutParams) params);
    }

    @Override
    public void onDismiss(DialogInterface dialog) {
        super.onDismiss(dialog);
        dialog.cancel();
    }

    @Override
    public View onCreateView(LayoutInflater inflater,ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.dialog_make_new_appointment,container,false);
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        Log.d(TAG, "onCreateView: In Dialog");
        String fullDate, fullTime;
        user.getUserDetails(view);
        Button btnCancel, btnOK;

        if(context == null) {
            context = getContext();
        }

        ViewGroup.LayoutParams params = getDialog().getWindow().getAttributes();
        params.width = ViewGroup.LayoutParams.MATCH_PARENT;

        int displayWidth = params.width;

        String min = currentDate.getMin() < 10 ? currentDate.getMin() + "0" : Integer.toString(currentDate.getMin());
        fullDate = currentDate.getDay() + "/" + currentDate.getMonth() + "/" + currentDate.getYear();
        fullTime = currentDate.getHour() + ":" + min;

        appointmentDetails.put("PersonID", Integer.toString(currentDate.getPersonID()));
        appointmentDetails.put("Day", Integer.toString(currentDate.getDay()));
        appointmentDetails.put("Month", Integer.toString(currentDate.getMonth()));
        appointmentDetails.put("Year", Integer.toString(currentDate.getYear()));
        appointmentDetails.put("Hour", Integer.toString(currentDate.getHour()));
        appointmentDetails.put("Min", Integer.toString(currentDate.getMin()));
        appointmentDetails.put("UserID", Integer.toString(user.getId()));

        dialog_date = (TextView) view.findViewById(R.id.dialog_new_apt_date);
        dialog_time = (TextView) view.findViewById(R.id.dialog_new_apt_time);
        dialog_date.setText(fullDate);
        dialog_time.setText(fullTime);
        treatmentTypes = (Spinner) view.findViewById(R.id.dialog_new_apt_type_list);
        treatmentTypes.setDropDownWidth((int) (displayWidth * 0.5f));
        btnCancel = (Button) view.findViewById(R.id.dialog_new_apt_cancel);
        btnOK = (Button) view.findViewById(R.id.dialog_new_apt_ok);
        username = (TextView) view.findViewById(R.id.dialog_new_apt_edit_name);
        phoneNumber = (TextView) view.findViewById(R.id.dialog_new_apt_edit_phone);

        GetTypesOfTreatments getTypesOfTreatments = new GetTypesOfTreatments();
        getTypesOfTreatments.getListOfTypes(this.getContext(), treatmentTypes);
        treatmentTypes.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                Log.d(TAG, "onTouch: ?");
                return false;
            }
        });

        btnCancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dismiss();
            }
        });

        if(user.isExist()) {
            appointmentDetails.put("Name", user.getName());
            appointmentDetails.put("Phone", Integer.toString(user.getPhone()));
            appointmentDetails.put("UserID", Integer.toString(user.getId()));
            username.setText(user.getName());
            username.setEnabled(false);
            phoneNumber.setText(Integer.toString(user.getPhone()));
            phoneNumber.setEnabled(false);

            is_addName = true;
            is_addPhone = true;
        }

        treatmentTypes.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                Log.d(TAG, "onItemSelected: selecting id: " + position);
                if(position > 0) {
                    is_chooseType = true;
                    type = position - 1;
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                is_chooseType = false;
            }
        });
        btnOK.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(!user.isExist()) {
                    if (!username.getText().toString().isEmpty()) {
                        is_addName = true;
                        appointmentDetails.put("Name", username.getText().toString());
                    } else {
                        ViewCompat.setBackgroundTintList(username, colorStateListBAD);
                        Log.d(TAG, "onClick: name empty");
                    }
                    if (!phoneNumber.getText().toString().isEmpty()) {
                        is_addPhone = true;
                        appointmentDetails.put("Phone", phoneNumber.getText().toString());
                    } else {
                        ViewCompat.setBackgroundTintList(phoneNumber, colorStateListBAD);
                        Log.d(TAG, "onClick: phone empty");
                    }
                }
                if (is_chooseType) {
                    appointmentDetails.put("Type", Integer.toString(type));
                } else {
                    ViewCompat.setBackgroundTintList(treatmentTypes, colorStateListBAD);
                    Log.d(TAG, "onClick: type empty");
                }
                if (is_chooseType && is_addPhone && is_addName) {
                    RequestQueue queue = Volley.newRequestQueue(getContext());
                    ViewCompat.setBackgroundTintList(phoneNumber, colorStateListGOOD);
                    ViewCompat.setBackgroundTintList(phoneNumber, colorStateListGOOD);
                    ViewCompat.setBackgroundTintList(treatmentTypes, colorStateListGOOD);
                    final JSONObject jsonObject = new JSONObject(appointmentDetails);
                    Log.d(TAG, "onClick: " + jsonObject.toString());
                    request = new JsonObjectRequest(
                            Request.Method.POST, ServerURLSManager.Appointment_new_appointment, jsonObject,
                            new Response.Listener<JSONObject>() {
                                @Override
                                public void onResponse(JSONObject response) {
                                    try {
                                        if (response.getString("status").equals("true")) {
                                            Log.d(TAG, "onResponse: SUCCESS!!");
                                            if(context == null) {
                                                Log.d(TAG, "onResponse: context is null");
                                            } else {
                                                CustomToast.showToast(context, context.getString(R.string.new_window_added), 1);
                                                listView.setAdapter(null);
                                                GetAppointmentListData.getData(context, currentDate.getDay(), currentDate.getMonth(), currentDate.getYear(), listView, progressBar);
                                                dismiss();
                                            }
                                        } else {
                                            Log.d(TAG, "onResponse: Failed!! " + response.getString("data"));
                                        }
                                    } catch (JSONException e) {
                                        e.printStackTrace();
                                    }
                                }
                            }, new Response.ErrorListener() {
                        @Override
                        public void onErrorResponse(VolleyError error) {
                            Log.d(TAG, "onResponse: Failed!!");
                            Toast.makeText(context, context.getString(R.string.new_window_failed), Toast.LENGTH_SHORT).show();

                        }
                    });
                    queue.add(request);
                }
            }
        });
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }
}
