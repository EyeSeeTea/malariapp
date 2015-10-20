/*
 * Copyright (c) 2015.
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

package org.eyeseetea.malariacare.database.model;

public interface IConvertToSDKVisitor {
    void visit(Answer answer);

    void visit(CompositeScore compositeScore);

    void visit(Header header);

    void visit(Match match);

    void visit(Option option);

    void visit(OrgUnit orgUnit);

    void visit(OrgUnitLevel orgUnitLevel);

    void visit(Program program);

    void visit(Question question);

    void visit(QuestionOption questionOption);

    void visit(QuestionRelation questionRelation);

    void visit(Score score);

    void visit(Survey survey);

    void visit(Tab tab);

    void visit(TabGroup tabGroup);

    void visit(User user);

    void visit(Value value);
}
