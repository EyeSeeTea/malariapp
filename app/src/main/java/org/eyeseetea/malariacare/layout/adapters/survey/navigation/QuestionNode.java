package org.eyeseetea.malariacare.layout.adapters.survey.navigation;

import org.eyeseetea.malariacare.database.model.Option;
import org.eyeseetea.malariacare.database.model.Question;

import java.util.HashMap;
import java.util.Map;

/**
 * POJO that represents a node in the graph that tells where to go next
 * Created by arrizabalaga on 2/06/16.
 */
public class QuestionNode {
    /**
     * Question here
     */
    private Question question;
    /**
     * Where to go given an option (children questions)
     */
    private Map<Long,QuestionNode> navigation;

    /**
     * Parent node
     */
    private QuestionNode parentNode;

    /**
     * Next question (sibling)
     */
    private QuestionNode sibling;

    public QuestionNode(Question question){
        this.question = question;
        this.navigation = new HashMap<>();
    }

    public void setQuestion(Question question){
        this.question=question;
    }

    public Question getQuestion(){
        return this.question;
    }

    /**
     * From this question given the option you will move to
     * @param option
     */
    public void addNavigation(Option option, QuestionNode nextNode){
        //something wrong -> nothing to add
        if(option==null || nextNode==null){
            return;
        }

        //Add parentNode to children
        nextNode.setParentNode(this);
        //Annotate navigation
        this.navigation.put(option.getId_option(),nextNode);
    }

    /**
     * Sets the parent of this questionnode
     * @param parentNode
     */
    public void setParentNode(QuestionNode parentNode){
        this.parentNode=parentNode;
    }

    /**
     * Sets the next sibling (same parent question)
     * @param sibling
     */
    public void setSibling(QuestionNode sibling){
        this.sibling=sibling;
        //Siblings share same parent
        this.sibling.parentNode=this.parentNode;
    }

    /**
     * Returns next question given an option
     * @param option
     * @return
     */
    public QuestionNode next(Option option){

        //Try children
        QuestionNode nextNode = nextByOption(option);
        if(nextNode!=null){
            return nextNode;
        }

        //Try sibling same level
        nextNode = nextBySibling();
        if(nextNode!=null){
            return nextNode;
        }

        //No parent -> no where to go
        if(parentNode==null){
            return null;
        }

        //Parent -> Try parent's sibling
        return this.parentNode.nextBySibling();
    }

    /**
     * Returns navigation from here with the given option
     * @param option
     * @return
     */
    private QuestionNode nextByOption(Option option){
        if(option==null){
            return null;
        }

        return this.navigation.get(option.getId_option());
    }

    /**
     * Returns next question no matter what (moves to sibling)
     * @return
     */
    public QuestionNode nextBySibling(){
        //NO sibling ->nowhere to go
        if(this.sibling==null){
            return null;
        }

        return this.sibling;
    }


}
