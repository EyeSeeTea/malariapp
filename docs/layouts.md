# App overview

A quick explanation of the layouts of the app.


## Summary

| name                  | type     | class                | layout                     | fragments                                                                                                             | other |
|-----------------------|----------|----------------------|----------------------------|-----------------------------------------------------------------------------------------------------------------------|-------|
| .LoginActivity        | Activity | LoginActivity        | login_layout.xml           |                                                                                                                       |       |
| .DashboardActivity    | Activity | DashboardActivity    | dashboard.xml              | AssesmentFragment  FeedbackFragment  FutureAssessmentPlanningFragment  PerformancePlanningFragment  AnalyticsFragment |       |
| .CreateSurveyActivity | Activity | CreateSurveyActivity | activity_create_survey.xml |               
| .SurveyActivity (*)       | Activity | SurveyActivity       | survey.xml                 | | main_header.xml form_footer.xml | 


(*) The content of the survey is created on runtime programatically.


`TODO:	document *runtime* layouts`
