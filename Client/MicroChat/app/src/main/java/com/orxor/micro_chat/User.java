package com.orxor.micro_chat;

import android.os.Parcel;
import android.os.Parcelable;

public class User implements Parcelable {
    private String userName, Password;

    public User(String userName, String password) {
        this.userName = userName;
        Password = password;
    }

    protected User(Parcel in) {
        userName = in.readString();
        Password = in.readString();
    }

    public static final Creator<User> CREATOR = new Creator<User>() {
        @Override
        public User createFromParcel(Parcel in) {
            return new User(in);
        }

        @Override
        public User[] newArray(int size) {
            return new User[size];
        }
    };

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public String getPassword() {
        return Password;
    }

    public void setPassword(String password) {
        Password = password;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(userName);
        dest.writeString(Password);
    }
}
