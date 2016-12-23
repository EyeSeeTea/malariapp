/*
 * Copyright (c) 2015.
 *
 * This file is part of QIS Surveillance App.
 *
 *  QIS Surveillance App is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 *
 *  QIS Surveillance App is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with QIS Surveillance App.  If not, see <http://www.gnu.org/licenses/>.
 */


package org.eyeseetea.malariacare.layout.adapters.survey.progress;

import org.eyeseetea.malariacare.database.model.Question;

import java.util.Arrays;
import java.util.List;

/**
 * VO that holds info related to the progress of completion in a DynamicTab
 * Created by arrizabalaga on 23/07/15.
 */
public class ProgressTabStatus {

    /**
     * Ordered list of questions of a tab
     */
    List<Question> questions;

    /**
     * List of flags that indicates if a question has been visited or not
     */
    boolean [] visited;

    /**
     * Based 0 index to the current page in screen
     */
    int currentPage;

    public ProgressTabStatus(List<Question> questions){
        this.questions=questions;
        this.currentPage=0;
        if(questions!=null && questions.size()>0) {
            this.visited = new boolean[questions.size()];
            this.visited[0]=true;
        }
    }

    /**
     * Index of the current question
     * @return
     */
    public int getCurrentPage(){
        return currentPage;
    }

    /**
     * Total number of pages to show (equals number of questions)
     * @return
     */
    public int getTotalPages(){
        if(questions==null){
            return 0;
        }
        return questions.size();
    }

    /**
     * Checks if there is a next question
     * @return
     */
    public boolean hasNextQuestion(){
        int totalPages=getTotalPages();
        return (currentPage+1)<totalPages;
    }

    /**
     * Checks if you are allowed to move forward (only if you get there before)
     * @return
     */
    public boolean isNextAllowed(){
        return hasNextQuestion() && visited[currentPage+1];
    }

    /**
     * Checks if there is a previous question
     * @return
     */
    public boolean hasPreviousQuestion(){
        return (currentPage-1)>=0;
    }

    /**
     * Gets the question in the current position
     * @return
     */
    public Question getCurrentQuestion(){
        if(getTotalPages()==0){
            return null;
        }

        return questions.get(currentPage);
    }

    /**
     * Gets the next question in the list whenever is possible
     * @return
     */
    public Question getNextQuestion(){
        if(getTotalPages()==0 || !hasNextQuestion()){
            return null;
        }

        currentPage++;
        visited[currentPage]=true;
        return questions.get(currentPage);
    }

    /**
     * Gets the previous question in the list whenever is possible
     * @return
     */
    public Question getPreviousQuestion(){
        if(getTotalPages()==0 || !hasPreviousQuestion()){
            return null;
        }

        currentPage--;
        return questions.get(currentPage);
    }

    /**
     * Gets the first question in the list
     * @return
     */
    public Question getFirstQuestion(){
        if(getTotalPages()==0){
            return null;
        }

        currentPage=0;
        return questions.get(currentPage);
    }

    /**
     * Checks if the current question is the first one
     * @return true|false
     */
    public boolean isFirstQuestion(){
        Question currentQuestion=getCurrentQuestion();
        if(currentQuestion==null){
            return false;
        }

        return currentQuestion.equals(questions.get(0));
    }

    /**
     * Returns a formatted string with the current status as follows:
     *  "1 / 5"
     * @return
     */
    public String getStatusAsString(){
        return String.format("%d / %d", Integer.valueOf(getCurrentPage() + 1),Integer.valueOf(getTotalPages()));
    }


    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        ProgressTabStatus that = (ProgressTabStatus) o;

        if (currentPage != that.currentPage) return false;
        if (!questions.equals(that.questions)) return false;
        return Arrays.equals(visited, that.visited);

    }

    @Override
    public int hashCode() {
        int result = questions.hashCode();
        result = 31 * result + Arrays.hashCode(visited);
        result = 31 * result + currentPage;
        return result;
    }
}
