/*
 * Copyright (c) 2016.
 *
 * This file is part of Health Network QIS App.
 *
 *  QA App is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 *
 *  QA App is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with QA App.  If not, see <http://www.gnu.org/licenses/>.
 */

package org.eyeseetea.malariacare.database.utils.metadata;

/**
 * Created by nacho on 03/05/16.
 */
public class PhoneMetaData {
    private String imei;
    private String phone_number;
    private String phone_serial;

    public PhoneMetaData() {

    }

    public PhoneMetaData(String phone_serial, String imei, String phone_number) {
        this.phone_serial = phone_serial;
        this.imei = imei;
        this.phone_number = phone_number;
    }

    public String getImei() {
        return imei;
    }

    public void setImei(String imei) {
        this.imei = imei;
    }

    public String getPhone_number() {
        return phone_number;
    }

    public void setPhone_number(String phone_number) {
        this.phone_number = phone_number;
    }

    public String getPhone_serial() {
        return phone_serial;
    }

    public void setPhone_serial(String phone_serial) {
        this.phone_serial = phone_serial;
    }

    public String getPhone_metaData() {
        String phonemetadata="";
        phonemetadata= "###";
        if(phone_number != null && !phone_number.equals("") && phone_number.length()>0){
            phonemetadata=phonemetadata+phone_number;
        }

        phonemetadata= phonemetadata+"###";
        if(imei != null && !imei.equals("") && imei.length()>0){
            phonemetadata=phonemetadata+imei;
        }

        phonemetadata= phonemetadata+"###";
        if(phone_serial != null && !phone_serial.equals("") && phone_serial.length()>0){
            phonemetadata=phonemetadata+phone_serial;
        }
        return phonemetadata;
    }
}
