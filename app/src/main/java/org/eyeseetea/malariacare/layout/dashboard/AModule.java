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
public abstract class AModule implements IModule {

    int layout;
    int tabLayout;
    int animatorInRight;
    int animatorOutRight;
    int animatorInLeft;
    int animatorOutLeft;
    FragmentTransaction fragmentTransactionLeft;
    FragmentTransaction fragmentTransactionRight;
    Drawable icon;
    int color;
    String name;
    Fragment fragment;
    ListFragment listFragment;
    boolean visible;

    @Override
    public int getLayout() {
        return layout;
    }

    @Override
    public void setLayout(int layout) {
        this.layout = layout;
    }

    @Override
    public int getTabLayout() {
        return tabLayout;
    }

    @Override
    public void setTabLayout(int tabLayout) {
        this.tabLayout = tabLayout;
    }

    @Override
    public FragmentTransaction getFragmentTransactionLeft() {
        return fragmentTransactionLeft;
    }

    @Override
    public void setFragmentTransactionLeft(FragmentTransaction fragmentTransactionLeft) {
        this.fragmentTransactionLeft = fragmentTransactionLeft;
    }

    public FragmentTransaction getFragmentTransactionRight() {
        return fragmentTransactionRight;
    }

    @Override
    public void setFragmentTransactionRight(FragmentTransaction fragmentTransactionRight) {
        this.fragmentTransactionRight = fragmentTransactionRight;
    }

    @Override
    public Drawable getIcon() {
        return icon;
    }

    @Override
    public void setIcon(Drawable icon) {
        this.icon = icon;
    }

    @Override
    public void setBackgroundColor(int color) {

    }

    @Override
    public int getBackgroundColor() {
        return color;
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public void setName(String name) {
        this.name = name;
    }

    @Override
    public Fragment getFragment() {
        return fragment;
    }

    @Override
    public void setFragment(Fragment fragment) {
        this.fragment = fragment;
    }

    @Override
    public ListFragment getListFragment() {
        return listFragment;
    }

    @Override
    public void setListFragment(ListFragment listFragment) {
        this.listFragment = listFragment;
    }

    @Override
    public boolean isVisible() {
        return visible;
    }

    @Override
    public void setVisible(boolean visible) {
        this.visible = visible;
    }

    @Override
    public int getAnimatorInRight() {
        return animatorInRight;
    }

    @Override
    public void setAnimatorInRight(int animatorInRight) {
        this.animatorInRight = animatorInRight;
    }

    @Override
    public int getAnimatorOutRight() {
        return animatorOutRight;
    }

    @Override
    public void setAnimatorOutRight(int animatorOutRight) {
        this.animatorOutRight = animatorOutRight;
    }

    @Override
    public int getAnimatorInLeft() {
        return animatorInLeft;
    }

    @Override
    public void setAnimatorInLeft(int animatorInLeft) {
        this.animatorInLeft = animatorInLeft;
    }

    @Override
    public int getAnimatorOutLeft() {
        return animatorOutLeft;
    }

    @Override
    public void setAnimatorOutLeft(int animatorOutLeft) {
        this.animatorOutLeft = animatorOutLeft;
    }

    @Override
    public abstract void init(Activity activity);
}
