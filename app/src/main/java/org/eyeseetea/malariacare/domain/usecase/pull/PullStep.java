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

package org.eyeseetea.malariacare.domain.usecase.pull;

public enum PullStep {
    PROGRAMS, EVENTS, PREPARING_PROGRAMS, PREPARING_ANSWERS, PREPARING_QUESTIONS,
    PREPARING_RELATIONSHIPS, PREPARING_ORGANISATION_UNITS, PREPARING_ORGANISATION_UNIT_HIERARCHY,
    PREPARING_USER,
    PREPARING_SCORES,
    VALIDATE_COMPOSITE_SCORES,
    PREPARING_SURVEYS,
    PREPARING_PLANNING_SURVEYS,
    COMPLETE,
    DEMO
}
