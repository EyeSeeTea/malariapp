# App overview

A quick explanation of the layouts of the app.


## Summary

| name                  | type     | class                | layout                     | fragments                                                                                                             | other |
|-----------------------|----------|----------------------|----------------------------|-----------------------------------------------------------------------------------------------------------------------|-------|
| .LoginActivity        | Activity | LoginActivity        | login_layout.xml           |                                                                                                                       |       |
| .DashboardActivity    | Activity | DashboardActivity    | dashboard.xml              | AssesmentFragment  FeedbackFragment  FutureAssessmentPlanningFragment  PerformancePlanningFragment  AnalyticsFragment |       |
| .CreateSurveyActivity | Activity | CreateSurveyActivity | activity_create_survey.xml |               
| .SurveyActivity (*)       | Activity | SurveyActivity       | survey.xml                 | | main_header.xml form_footer.xml | 
| .AssessmentFragment   | Fragment | AssessmentFragment       | | | assessment_header.xml  fragment_assessment.xml | 
| .FeedbackFragment   | Fragment | FeedbackFragment       | | | feedback_header.xml  fragment_feedback.xml | 
| .FutureAssessmentPlanningFragment   | Fragment | FutureAssessmentPlanningFragment       | | | future_assessment_planning_header.xml  fragment_future_assessment_planning.xml | 
| .PerformancePlanningFragment   | Fragment | PerformancePlanningFragment       | | | performance_planning_header.xml  fragment_performance_planning.xml | 


(*) The content of the survey is created on runtime programatically.


`TODO:	document *runtime* layouts`
