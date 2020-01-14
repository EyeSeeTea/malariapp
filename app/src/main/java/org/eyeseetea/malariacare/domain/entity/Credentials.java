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

package org.eyeseetea.malariacare.domain.entity;

import static org.eyeseetea.malariacare.domain.common.RequiredChecker.required;

public class Credentials {
    private static final String DEMO_USER = "demo";
    private static final String DEMO_SERVER = "demo.server";

    private String username;
    private String userUid;
    private String password;
    private String serverURL;

    public Credentials(String serverURL, String username, String password) {
        this.serverURL = required(serverURL, "Server URL is required");
        this.username = required(username, "Username is required");
        this.password = required(password, "Password is required");
    }

    public static Credentials createDemoCredentials() {
        Credentials credentials = new Credentials(DEMO_SERVER, DEMO_USER, DEMO_USER);

        return credentials;
    }

    public String getServerURL() {
        return serverURL;
    }

    public String getUsername() {
        return username;
    }

    public String getPassword() {
        return password;
    }

    public String getUserUid() {
        return userUid;
    }

    public void setUserUid(String userUid) {
        this.userUid = userUid;
    }

    public boolean isDemoCredentials() {
        return this.equals(Credentials.createDemoCredentials());
    }

    public boolean equals(Object o) {
        if (o == null) return false;
        if (!(o instanceof Credentials)) return false;

        Credentials other = (Credentials) o;
        if (!this.serverURL.equals(other.getServerURL())) return false;
        if (!this.username.equals(other.getUsername())) return false;
        if (!this.password.equals(other.getPassword())) return false;

        return true;
    }

    public int hashCode() {
        return (int) serverURL.hashCode() *
                username.hashCode() *
                password.hashCode();
    }

}
