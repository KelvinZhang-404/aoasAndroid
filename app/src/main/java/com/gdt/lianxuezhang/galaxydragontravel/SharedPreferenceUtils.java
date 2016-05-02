package com.gdt.lianxuezhang.galaxydragontravel;

import android.content.SharedPreferences;

/**
 * Created by LianxueZhang on 28/12/2015.
 */
public class SharedPreferenceUtils {

    private SharedPreferences sharedPreferences;
    private SharedPreferences.Editor editor;

    public SharedPreferenceUtils(SharedPreferences sp) {
        this.sharedPreferences = sp;
        this.editor = sharedPreferences.edit();
    }

    public SharedPreferenceUtils clear() {
        this.editor.clear();
        return this;
    }

    public SharedPreferenceUtils setAdvertiseStatus(Boolean advertiseStatus) {
        this.editor.putBoolean("AdvertiseStatus", advertiseStatus);
        return this;
    }

    public SharedPreferenceUtils setUserEmail(String email) {
        this.editor.putString("email", email);
        return this;
    }

    public SharedPreferenceUtils setPassword(String password) {
        this.editor.putString("password", password);
        return this;
    }

    public SharedPreferenceUtils setLoginStatus(Boolean loginStatus) {
        this.editor.putBoolean("login status", loginStatus);
        return this;
    }

    public void commit() {
        this.editor.commit();
    }

    public void apply() {
        this.editor.apply();
    }

    public SharedPreferenceUtils setUser(String email, String password, int role) {
        this.editor.putString("email", email);
        this.editor.putString("password", password);
        this.editor.putInt("role", role);
        return this;
    }

    public SharedPreferenceUtils setCompanyId (int companyId) {
        this.editor.putInt("company id", companyId);
        return this;
    }

    public boolean getAdvertiseStatus() {
        return sharedPreferences.getBoolean("AdvertiseStatus", false);
    }

    public Boolean getLoginStatus() {
        return sharedPreferences.getBoolean("login status", false);
    }

    public String getUserEmail() {
        return sharedPreferences.getString("email", "");
    }

    public String getUserPassword() {
        return sharedPreferences.getString("password", "");
    }

    public int getRole() {
        return sharedPreferences.getInt("role", -1);
    }

    public int getCompanyId() {
        return sharedPreferences.getInt("company id", 0);
    }
}
