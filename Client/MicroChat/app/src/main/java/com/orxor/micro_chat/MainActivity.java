package com.orxor.micro_chat;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;

import com.google.firebase.FirebaseApp;
import com.google.gson.Gson;

public class MainActivity extends Activity implements LoginRegisterDialogFragment.OnLoginRegisterSuccess {

    public static final String SETTINGS = "settings";
    public static final String USER = "user";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        SharedPreferences preferences=getSharedPreferences(SETTINGS,MODE_PRIVATE);
        if (preferences.contains(USER)){
            Intent intent = new Intent(this,ChatActivity.class);
            startActivity(intent);
            finish();
        }
        setContentView(R.layout.activity_main);
    }

    public void onClick(View view) {
        LoginRegisterDialogFragment fragment=new LoginRegisterDialogFragment();
        switch (view.getId()){
            case R.id.btn_login:
                fragment.setTitle(getString(R.string.login));
                fragment.setLogin(true);
                fragment.setOnLoginRegisterSuccess(this);
                break;
            case R.id.btn_register:
                fragment.setTitle(getString(R.string.register));
                fragment.setLogin(false);
                fragment.setOnLoginRegisterSuccess(this);
                break;
        }
        fragment.show(getFragmentManager(),"loginRegisterDialog");
    }

    @Override
    public void onSuccess(User user) {
        findViewById(R.id.btn_register).setEnabled(false);
        findViewById(R.id.btn_login).setEnabled(false);
        Gson gson = new Gson();
        getSharedPreferences(SETTINGS,MODE_PRIVATE).edit().putString(USER,gson.toJson(user)).apply();
        Intent intent = new Intent(this,ChatActivity.class);
        intent.putExtra(USER,user);
        startActivity(intent);
        finish();
    }
}
