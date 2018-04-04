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

package org.eyeseetea.malariacare.layout.dashboard.controllers;

import android.app.AlertDialog;
import android.app.Fragment;
import android.app.FragmentTransaction;
import android.app.ListFragment;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.view.View;

import org.eyeseetea.malariacare.DashboardActivity;
import org.eyeseetea.malariacare.R;
import org.eyeseetea.malariacare.data.database.model.ProgramDB;
import org.eyeseetea.malariacare.data.database.model.SurveyDB;
import org.eyeseetea.malariacare.data.database.utils.Session;
import org.eyeseetea.malariacare.fragments.IModuleFragment;
import org.eyeseetea.malariacare.layout.dashboard.config.ModuleSettings;
import org.eyeseetea.malariacare.layout.utils.LayoutUtils;

/**
 * Created by idelcano on 25/02/2016.
 */
public abstract class ModuleController {

    /**
     * Reference that points to the dashboard activity to resolve context stuff
     */
    DashboardActivity dashboardActivity;

    DashboardController dashboardController;

    /**
     * Reference to the vertical title (only assess, improve)
     */
    int idVerticalTitle;

    /**
     * Reference to the module properties
     */
    ModuleSettings moduleSettings;

    int tabLayout;

    Fragment fragment;
    boolean visible;

    protected ModuleController() {
    }

    public void setDashboardController(DashboardController dashboardController) {
        this.dashboardController = dashboardController;
    }

    public ModuleController(ModuleSettings moduleSettings) {
        this.visible = true;
        this.moduleSettings = moduleSettings;
    }

    public void init(DashboardActivity activity) {
        this.dashboardActivity = activity;
    }

    public String getName() {
        return moduleSettings.getController();
    }

    public String getTitle() {
        return dashboardActivity.getResources().getString(moduleSettings.getResTitle());
    }

    public String getActionBarTitleBySurvey(SurveyDB survey) {
        String title = "";
        if (survey.getOrgUnit().getName() != null) {
            title = survey.getOrgUnit().getName();
        }
        return title;
    }

    public String getActionBarSubTitleBySurvey(SurveyDB survey) {
        ProgramDB program = survey.getProgram();
        if (program.getName() != null) {
            return program.getName();
        }
        return "";
    }

    public Drawable getIcon() {
        return dashboardActivity.getResources().getDrawable(moduleSettings.getResIcon());
    }

    public int getBackgroundColor() {
        return dashboardActivity.getResources().getColor(moduleSettings.getResBackgroundColor());
    }


    public int getLayout() {
        return moduleSettings.getResLayout();
    }

    public int getTabLayout() {
        return tabLayout;
    }

    public Fragment getFragment() {
        return fragment;
    }

    public boolean isVisible() {
        return visible;
    }

    public void setVisible(boolean visible) {
        this.visible = visible;
    }

    public void reloadData() {
        if (fragment == null) {
            return;
        }

        ((IModuleFragment) fragment).reloadData();
    }

    /**
     * Hides part of this module (useful for vertical orientation)
     */
    public void hideVerticalTitle() {
        if (idVerticalTitle == 0) {
            return;
        }
        View activeAssessmentsLabel = dashboardActivity.findViewById(idVerticalTitle);
        activeAssessmentsLabel.setVisibility(View.GONE);
    }

    /**
     * Show the vertical title for this module (only vertical orientation)
     */
    public void showVerticalTitle() {
        if (idVerticalTitle == 0) {
            return;
        }
        View activeAssessmentsLabel = dashboardActivity.findViewById(idVerticalTitle);
        activeAssessmentsLabel.setVisibility(View.VISIBLE);
    }

    /**
     * Inits the module (inside a responsability chain (dashboardActivity.onCreate ->
     * dashboardController.onCreate -> here))
     */
    public void onCreate(DashboardActivity dashboardActivity) {
        if (!isVisible()) {
            return;
        }
        init(dashboardActivity);
        replaceFragment(getLayout(), getFragment());
    }

    /**
     * Invoked whenever a tab loses its focus
     */
    public void onExitTab() {

    }

    /**
     * Invoked whenever a tab gains focus
     */
    public void onTabChanged() {
        if (fragment == null || !fragment.isAdded()) {
            reloadFragment();
        }
        reloadData();
    }

    /**
     * Invoked whenever back is pressed.
     * Asks before leaving the app by default.
     */
    public void onBackPressed() {
        new AlertDialog.Builder(dashboardActivity)
                .setTitle(R.string.survey_title_exit)
                .setMessage(R.string.survey_info_exit)
                .setNegativeButton(android.R.string.no, null)
                .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {

                    public void onClick(DialogInterface arg0, int arg1) {
                        Intent intent = new Intent(Intent.ACTION_MAIN);
                        intent.addCategory(Intent.CATEGORY_HOME);
                        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                        dashboardActivity.startActivity(intent);
                    }
                }).create().show();
    }

    public void setActionBarDashboard() {
        LayoutUtils.setActionBarDashboard(dashboardActivity, getTitle());
    }

    public void setActionBarDashboardWithProgram() {
        LayoutUtils.setActionBarDashboard(dashboardActivity, getTitle()+" - "+Session.getSurveyByModule(getName()).getProgram().getName());
    }

    public void replaceFragment(int layout, Fragment fragment) {
        if (fragment instanceof ListFragment) {
            try {
                //fix some visual problems
                View vg = dashboardActivity.findViewById(layout);
                vg.invalidate();
            } catch (Exception e) {
            }
        }

        FragmentTransaction ft = getFragmentTransaction();
        ft.replace(layout, fragment);
        ft.commit();
    }

    public FragmentTransaction getFragmentTransaction() {
        FragmentTransaction ft = dashboardActivity.getFragmentManager().beginTransaction();
        if (dashboardController.isNavigatingBackwards()) {
            ft.setCustomAnimations(R.animator.anim_slide_in_right, R.animator.anim_slide_out_right);
        } else {
            ft.setCustomAnimations(R.animator.anim_slide_in_left, R.animator.anim_slide_out_left);
        }
        ft.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_FADE);
        return ft;
    }

    /**
     * Checks if the given container contains a fragment of the given class
     */
    protected boolean isFragmentActive(Class fragmentClass) {
        Fragment currentFragment = dashboardActivity.getFragmentManager().findFragmentById(
                getLayout());
        if (fragmentClass.isInstance(currentFragment)) {
            return true;
        }
        return false;
    }

    protected void reloadFragment() {
        Fragment fragment = getFragment();
        if (fragment == null) {
            return;
        }

        init(dashboardActivity);
        replaceFragment(getLayout(), getFragment());
        reloadData();
    }

    /**
     * Hides this module (useful for vertical orientation)
     */
    public void hide() {

        if (fragment == null) {
            return;
        }

        FragmentTransaction ft = getFragmentTransaction();
        ft.hide(fragment);
        ft.commit();
    }

}
