/*
 * Copyright (c) 2017.
 *
 * This file is part of QA App.
 *
 *  Health Network QIS App is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 *
 *  Health Network QIS App is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with Foobar.  If not, see <http://www.gnu.org/licenses/>.
 */

package org.eyeseetea.malariacare.utils;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.pm.PackageManager;
import android.os.Build;
import androidx.core.app.ActivityCompat;

import java.util.HashMap;

/**
 * Created by nacho on 23/11/16.
 */

public class Permissions {

    public static final int FINE_LOCATION_REQUEST_CODE = 8;

    public static final int PHONE_STATE_REQUEST_CODE = 9;

    public static final int READ_ACCOUNTS_STATE_REQUEST_CODE = 10;

    private static Permissions permissionsInstance;
    private static Activity activity;
    HashMap<Integer, Permission> permissions;

    public static Permissions getInstance(Activity callingActivity) {
        if (permissionsInstance == null) {
            activity = callingActivity;
            permissionsInstance = new Permissions();
            permissionsInstance.init();
        }
        return permissionsInstance;
    }

    /**
     * Remove from permissions Map the granted permission
     *
     * @return true on permission granted, false otherwise
     */
    public static boolean processAnswer(int requestCode,
            String permissions[], int[] grantResults) {
        if (grantResults.length > 0
                && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            permissionsInstance.removePermission(requestCode);
            return true;
        }
        return false;
    }

    public void init() {
        permissions = new HashMap<>();
        addPermission(new Permission(FINE_LOCATION_REQUEST_CODE,
                android.Manifest.permission.ACCESS_FINE_LOCATION));
        addPermission(new Permission(READ_ACCOUNTS_STATE_REQUEST_CODE,
                Manifest.permission.GET_ACCOUNTS));

        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.Q){
            addPermission(new Permission(PHONE_STATE_REQUEST_CODE,
                    android.Manifest.permission.READ_PHONE_STATE));
        }
    }

    public void addPermission(Permission permission) {
        permissions.put(permission.getCode(), permission);
    }

    public void removePermission(int code) {
        permissions.remove(code);
    }

    public Permission getPermission(int code) {
        return permissions.get(code);
    }

    public void requestNextPermission() {
        if (permissions.size() > 0) {
            Integer code = (Integer) permissions.keySet().toArray()[0];
            Permission permission = getPermission(code);
            requestPermission(permission.getDefinition(), permission.getCode());
        }
    }

    public boolean hasNextPermission() {
        return (permissions.size() > 0);
    }
    public void requestPermission(String permission, int code) {
        if(!hasPermissions(activity, new String[]{permission})) {
            ActivityCompat.requestPermissions(activity, new String[]{permission}, code);
        }
    }
    public static boolean hasPermissions(Context context, String... permissions) {
        if (android.os.Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && context != null && permissions != null) {
            for (String permission : permissions) {
                if (ActivityCompat.checkSelfPermission(context, permission) != PackageManager.PERMISSION_GRANTED) {
                    return false;
                }
            }
        }
        return true;
    }

    public boolean areAllPermissionsGranted() {
        return permissions.isEmpty();
    }

    private class Permission {
        private int code;
        private String definition;

        public Permission(int code, String permission) {
            this.code = code;
            this.definition = permission;
        }

        public int getCode() {
            return code;
        }

        public void setCode(int code) {
            this.code = code;
        }

        public String getDefinition() {
            return definition;
        }

        public void setDefinition(String definition) {
            this.definition = definition;
        }
    }
}
