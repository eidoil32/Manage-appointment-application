package com.idohayun.mybusiness;


import android.content.Context;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.TextInputLayout;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

public class ToolsView extends Fragment {
    private TextView password, username, phone, errorText;
    private TextInputLayout inputLayout_phone, inputLayout_username, inputLayout_password;
    private baseUSER baseUSER = new baseUSER();
    private Button btnLogin, btnRegister, btnOK;
    private String input_username = null, input_password = null, input_phone = null;
    private boolean userExist = false;

    private static StringBuilder sb = new StringBuilder();

    private static final String TAG = "ToolsView";

    public ToolsView() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        MainActivity.changeTitlePage(inflater.getContext().getResources().getString(R.string.text_tools_title));
         return inflater.inflate(R.layout.fragment_tools_view, container, false);
    }

    @Override
    public void onViewCreated(@NonNull final View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        view.setBackgroundColor(view.getResources().getColor(R.color.colorBackground,null));
        baseUSER.getUserDetails(view);
        userExist = checkIfUserAlreadyExist();

        if(userExist) {
            Log.d(TAG, "onViewCreated: user exist: " + baseUSER.getName());
            getFragmentManager().popBackStack();
            getFragmentManager().beginTransaction().add(R.id.fragment,new UserPanel()).addToBackStack("UserPanel").commit();
        } else {
            Log.d(TAG, "onViewCreated: user doesn't exist!");
        }

        inputLayout_phone = view.findViewById(R.id.input_layout_phone);
        inputLayout_username = view.findViewById(R.id.input_layout_user_name);
        inputLayout_password = view.findViewById(R.id.input_layout_password);
        password = view.findViewById(R.id.tools_user_password);
        username = view.findViewById(R.id.tools_user_name);
        phone = view.findViewById(R.id.tools_user_phone_number);
        errorText = view.findViewById(R.id.text_error_type);
        btnRegister = view.findViewById(R.id.btn_tools_register);
        btnLogin = view.findViewById(R.id.btn_tools_login);
        btnOK = view.findViewById(R.id.tools_btn_ok);


        btnRegister.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                errorText.clearAnimation();
                errorText.setVisibility(View.GONE);
                phone.setVisibility(View.VISIBLE);
                password.setVisibility(View.VISIBLE);
                inputLayout_phone.setVisibility(View.VISIBLE);
                inputLayout_password.setVisibility(View.VISIBLE);
                inputLayout_username.setVisibility(View.VISIBLE);
                username.setVisibility(View.VISIBLE);
                btnOK.setVisibility(View.VISIBLE);


                btnOK.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        input_username = username.getText().toString();
                        input_phone = phone.getText().toString();
                        input_password = password.getText().toString();

                        if(!input_password.isEmpty() && !input_phone.isEmpty() && !input_username.isEmpty()) {
                            baseUSER.getUserDetails(view);
                            baseUSER.setView(view);
                            baseUSER.setName(input_username);
                            baseUSER.setPassword(input_password);
                            baseUSER.setPhone(Integer.parseInt(input_phone));
                            if(baseUSER.validData(errorText)) {
                                baseUSER.add_newUserToDB();
                                if (baseUSER.isExist()) {
                                    CustomToast.showToast(getContext(), getString(R.string.user_registered_successfully), 1);
                                    getFragmentManager().popBackStack();
                                    getFragmentManager().beginTransaction().add(R.id.fragment, new UserPanel()).addToBackStack("UserPanel").commit();
                                }
                            } else {
                                errorText.setVisibility(View.VISIBLE);
                                errorText.setTextColor(getResources().getColor(R.color.toast_error,null));
                                errorText.setAnimation(AnimationUtils.loadAnimation(view.getContext(),R.anim.bounce));
                            }
                        } else {
                            errorText.setVisibility(View.VISIBLE);
                            errorText.setTextColor(getResources().getColor(R.color.toast_error,null));
                            errorText.setText(getString(R.string.error_need_to_fill_all_fields));
                            errorText.setAnimation(AnimationUtils.loadAnimation(view.getContext(),R.anim.bounce));
                        }
                    }
                });

            }
        });

        btnLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                errorText.clearAnimation();
                errorText.setVisibility(View.GONE);
                phone.setVisibility(View.VISIBLE);
                password.setVisibility(View.VISIBLE);
                username.setVisibility(View.INVISIBLE);
                btnOK.setVisibility(View.VISIBLE);
                inputLayout_phone.setVisibility(View.VISIBLE);
                inputLayout_password.setVisibility(View.VISIBLE);
                inputLayout_username.setVisibility(View.INVISIBLE);

                btnOK.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        String s_phone, s_password;
                        s_password = password.getText().toString();
                        s_phone = phone.getText().toString();
                        Log.d(TAG, "onClick: phone: " + s_phone + " password: " + s_password);
                        if(s_phone.isEmpty() || s_password.isEmpty()) {
                            errorText.setVisibility(View.VISIBLE);
                            errorText.setTextColor(getResources().getColor(R.color.toast_error,null));
                            errorText.setText(getString(R.string.error_need_to_fill_all_fields));
                        } else {
                            errorText.setVisibility(View.INVISIBLE);
                            loginUser(view.getContext(),s_phone,s_password);
                        }
                    }
                });
            }
        });
    }

    private void loginUser(final Context context, String i_phone, String i_password) {
        RequestQueue queue = Volley.newRequestQueue(context);
        Map<String, String> map = new HashMap<>();
        map.put("Phone", i_phone);
        map.put("Password", i_password);
        final JSONObject jsonObject = new JSONObject(map);
        JsonObjectRequest request = new JsonObjectRequest(
                Request.Method.POST, // the request method
                ServerURLSManager.User_manager_login, jsonObject,
                new Response.Listener<JSONObject>() { // the response listener
                    @Override
                    public void onResponse(JSONObject response) {
                        try {
                            baseUSER user = new baseUSER();
                            String responseString = response.getString("status");
                            if(responseString.equals("true")) {
                                String s = response.getString("data");
                                Log.d(TAG, "onResponse: " + response.getString("password"));
                                sb.append(s);
                                JSONArray jsonArray = new JSONArray(s);
                                for (int i =0; i < jsonArray.length() ; i++) {
                                    JSONObject obj = jsonArray.getJSONObject(i);
                                    user.setExist(true);
                                    user.setPhone(obj.getInt("Phone"));
                                    user.setName(obj.getString("UserName"));
                                    user.setPassword(obj.getString("Password"));
                                    user.setId(obj.getInt("UserID"));
                                    Log.d(TAG, "onResponse: " + user.print());
                                }
                                CustomToast.showToast(context,getString(R.string.user_connect_successfully),1);
                                user.createDBHelper(getView());
                                user.updateLocalDB();
                                getFragmentManager().beginTransaction().replace(R.id.fragment,new UserPanel()).commit();
                            } else if (responseString.equals("false")) {
                                String responseMessage = response.getString("message");
                                if(responseMessage.equals("user_not_found")) {
                                    CustomToast.showToast(context,getString(R.string.login_user_not_found),0);
                                } else if (responseMessage.equals("password_incorrect")) {
                                    CustomToast.showToast(context,getString(R.string.login_password_incorrect),0);
                                } else if (responseMessage.equals("error")) {
                                    CustomToast.showToast(context,getString(R.string.login_error_unknown),0);
                                }
                            } else {
                                CustomToast.showToast(context,getString(R.string.login_error_unknown),0);
                            }
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }
                },
                new Response.ErrorListener() { // the error listener
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        Log.d(TAG, "onErrorResponse: error " + error.toString());
                        Toast.makeText(context, "Oops! Got error from server!", Toast.LENGTH_SHORT).show();
                    }
                });

        queue.add(request);
    }


    private boolean checkIfUserAlreadyExist() {
        return baseUSER.isExist();
    }
}
