package com.orxor.micro_chat;

import android.app.AlertDialog;
import android.app.DialogFragment;
import android.content.DialogInterface;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;

public class LoginRegisterDialogFragment extends DialogFragment implements View.OnClickListener {

    private String title;
    private boolean login;
    private Button action, cancel;
    private EditText username, password, verifyPassword;
    private OnLoginRegisterSuccess onLoginRegisterSuccess;

    public void setOnLoginRegisterSuccess(OnLoginRegisterSuccess onLoginRegisterSuccess) {
        this.onLoginRegisterSuccess = onLoginRegisterSuccess;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public void setLogin(boolean login) {
        this.login = login;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        getDialog().setTitle(title);
        setCancelable(false);
        View view = inflater.inflate(R.layout.fragment_dialog,container,false);
        cancel=view.findViewById(R.id.btn_cancel);
        action = view.findViewById(R.id.btn_login_register);
        username = view.findViewById(R.id.et_username);
        password = view.findViewById(R.id.et_password);
        verifyPassword = view.findViewById(R.id.et_verify_password);

        cancel.setOnClickListener(this);
        action.setOnClickListener(this);
        if (login){
            action.setText(R.string.login);
            view.findViewById(R.id.tv_verify_password).setVisibility(View.GONE);
            verifyPassword.setVisibility(View.GONE);
        }

        return view;
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()){
            case R.id.btn_cancel:
                dismiss();
                break;
            case R.id.btn_login_register:
                doAction();
                break;
        }
    }

    void doAction(){
        if (username.getText().toString().isEmpty()){
            username.setError("Must Enter Username!");
            return;
        }
        if (password.getText().toString().isEmpty()){
            password.setError("Must Enter Password!");
            return;
        }
        ServerConnection serverConnection =null;
        if (login){
            cancel.setEnabled(false);
            action.setEnabled(false);
            serverConnection=new ServerConnection();
            serverConnection.setActualPostExecute(new OnActualPostExecute() {
                @Override
                public void onPostExecute(ServerResponse response) {
                    if (response.isSuccess()){
                        dismiss();
                        if (onLoginRegisterSuccess != null) {
                            onLoginRegisterSuccess.onSuccess(new User(username.getText().toString(),password.getText().toString()));
                        }
                    } else {
                        onError(response);
                    }
                }
            });
            serverConnection.execute(new ServerAction(ServerAction.ACTION_LOGIN,
                    null,
                    new User(username.getText().toString(),password.getText().toString())));
        } else {
            if(password.getText().toString().equals(verifyPassword.getText().toString())){
                cancel.setEnabled(false);
                action.setEnabled(false);
                serverConnection=new ServerConnection();
                serverConnection.setActualPostExecute(new OnActualPostExecute() {
                    @Override
                    public void onPostExecute(ServerResponse response) {
                        if (response.isSuccess()){
                            dismiss();
                            if (onLoginRegisterSuccess != null) {
                                onLoginRegisterSuccess.onSuccess(new User(username.getText().toString(),password.getText().toString()));
                            }
                        } else {
                            onError(response);
                        }
                    }
                });
                serverConnection.execute(new ServerAction(ServerAction.ACTION_REGISTER,
                        null,
                        new User(username.getText().toString(),password.getText().toString())));
            } else {
                verifyPassword.setError("Password Mismatch");
            }
        }
    }

    private void onError(ServerResponse response) {
        AlertDialog dialog = new AlertDialog.Builder(username.getContext())
                .setMessage(response.getResponse())
                .setOnDismissListener(new DialogInterface.OnDismissListener() {
                    @Override
                    public void onDismiss(DialogInterface dialog) {
                        cancel.setEnabled(true);
                        action.setEnabled(true);
                    }
                })
                .create();
        dialog.show();
    }

    public interface OnLoginRegisterSuccess{
        void onSuccess(User user);
    }
}
