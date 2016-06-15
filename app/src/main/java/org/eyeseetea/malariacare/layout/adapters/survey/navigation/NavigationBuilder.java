package org.eyeseetea.malariacare.layout.adapters.survey.navigation;

import android.util.Log;

import org.eyeseetea.malariacare.database.model.Answer;
import org.eyeseetea.malariacare.database.model.Option;
import org.eyeseetea.malariacare.database.model.Question;
import org.eyeseetea.malariacare.database.model.Tab;

/**
 * Created by arrizabalaga on 2/06/16.
 */
public class NavigationBuilder {

    private static String TAG="NavigationBuilder";

    private static NavigationBuilder instance;

    private NavigationBuilder(){

    }

    public static NavigationBuilder getInstance(){
        if(instance==null){
            instance=new NavigationBuilder();
        }
        return instance;
    }

    /**
     * Returns a navigation controller so you can navigate through questions according to answers
     * @param tab
     * @return
     */
    public NavigationController buildController(Tab tab, String module){
        //No tab -> nothing to build
        if(tab==null){
            return null;
        }

        Log.d(TAG,String.format("build(%s)",tab.getName()));
        Question rootQuestion = Question.findRootQuestion(tab);

        //NO first question -> nothing to build
        if(rootQuestion==null){
            return null;
        }
        QuestionNode rootNode = buildNode(rootQuestion);
        return new NavigationController(rootNode, module);
    }

    /**
     * Builds navigation options from the given question
     * @param currentQuestion
     * @return
     */
    private QuestionNode buildNode(Question currentQuestion){
        //No question -> no node
        if(currentQuestion==null){
            return null;
        }
        QuestionNode currentNode = new QuestionNode(currentQuestion);
        //Add children navigation
        buildChildren(currentNode);
        //Add sibling navigation
        buildSibling(currentNode);
        return currentNode;
    }

    /**
     * Adds navigation options according to answers (children questions)
     * @param currentNode
     */
    private void buildChildren(QuestionNode currentNode){
        Question currentQuestion=currentNode.getQuestion();
        //No children questions -> no children to build
        if(currentQuestion==null || !currentQuestion.hasOutputWithOptions() || currentQuestion.getQuestionOption().size()==0){
            return;
        }

        Answer currentAnswer=currentQuestion.getAnswer();
        for(Option option:currentAnswer.getOptions()){
            Question firstChildrenQuestion = currentQuestion.findFirstChildrenByOption(option);

            //No child question for this option -> next
            if(firstChildrenQuestion==null){
                continue;
            }

            Log.d(TAG,String.format("'%s' + '%s' --> '%s'",currentQuestion.getCode(),option.getName(),firstChildrenQuestion.getCode()));
            //Build navigation from there
            QuestionNode childNode=buildNode(firstChildrenQuestion);
            //Add navigation by option to current node
            currentNode.addNavigation(option,childNode);
        }
    }

    /**
     * Adds navigation no matter what answer is given (sibling question)
     * @param currentNode
     */
    private void buildSibling(QuestionNode currentNode){
        Question nextQuestion = currentNode.getQuestion().getSibling();
        //No next question
        if(nextQuestion==null){
            return;
        }
        QuestionNode nextNode = buildNode(nextQuestion);
        currentNode.setSibling(nextNode);
    }

}
