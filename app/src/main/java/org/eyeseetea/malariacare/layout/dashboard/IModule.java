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

package org.eyeseetea.malariacare.layout.dashboard;

import android.app.Activity;
import android.app.Fragment;
import android.app.FragmentTransaction;
import android.app.ListFragment;
import android.graphics.drawable.Drawable;

/**
 * Created by idelcano on 25/02/2016.
 */
public interface IModule {
    void setLayout(int layout);
    int getLayout();
    void setTabLayout(int tablayout);
    int getTabLayout();
    void setAnimatorInRight(int animatorInRight);
    int getAnimatorInRight();
    void setAnimatorOutRight(int animatorOutRight);
    int getAnimatorOutRight();
    void setAnimatorInLeft(int animatorInLeft);
    int getAnimatorInLeft();
    void setAnimatorOutLeft(int animatorOutLeft);
    int getAnimatorOutLeft();
    void setFragmentTransactionLeft(FragmentTransaction fragmentTransactionLeft);
    FragmentTransaction getFragmentTransactionLeft();
    void setFragmentTransactionRight(FragmentTransaction fragmentTransactionRight);
    FragmentTransaction getFragmentTransactionRight();
    void setIcon(Drawable icon);
    Drawable getIcon();
    void setName(String name);
    String getName();
    void setFragment(Fragment fragment);
    Fragment getFragment();
    void setListFragment(ListFragment fragment);
    ListFragment getListFragment();
    void setVisible(boolean visible);
    boolean isVisible();
    void reloadData();

    void init(Activity activity);
}
