/*
 * Copyright (c) 2016.
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

package org.eyeseetea.malariacare.fragments;

import android.app.Fragment;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;

import org.eyeseetea.malariacare.R;
import org.eyeseetea.malariacare.layout.Module;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by idelcano on 15/02/2016.
 */
public class DashboardConteinerFragment extends Fragment{

    public static final String TAG = ".DashboardConteiner";



    private List<Module> modules;

    public DashboardConteinerFragment() {
        this.modules = new ArrayList();
    }

    FragmentActivity faActivity;

    /**
     * Parent layout
     */
    LinearLayout llLayout;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        faActivity  = (FragmentActivity)    super.getActivity();
        // Replace LinearLayout by the type of the root element of the layout you're trying to load
        llLayout = (LinearLayout) inflater.inflate(R.layout.vertical_main, container, false);
        prepareUI();

        return llLayout; // We must return the loaded Layout
    }

    public static DashboardConteinerFragment newInstance(int index) {
        DashboardConteinerFragment f = new DashboardConteinerFragment();
        // Supply index input as an argument.
        Bundle args = new Bundle();
        args.putInt("index",index);
        f.setArguments(args);

        return f;
    }
    @Override
    public void onCreate(Bundle savedInstanceState){
        Log.d(TAG,"onCreate");
        super.onCreate(savedInstanceState);

    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        Log.d(TAG,"onActivityCreated");
        super.onActivityCreated(savedInstanceState);
    }

    @Override
    public void onResume() {
        Log.d(TAG, "onResume");
        super.onResume();
    }
    @Override
    public void onPause(){
        super.onPause();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle presses on the action bar items
        switch (item.getItemId()) {
            case R.id.overall_score:
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }


    /**
     * Gets a reference to the progress view in order to stop it later
     */
    private void prepareUI(){
    }
}