# QoC Android App

QoC Android App is an open-source quality assessments tool connected with DHIS2 Health Information Servers. It eases the process of analyzing the data with a wide range of features such as automatic surveys generation, retreiving and pushing information to DHIS2 servers. Its highly detailed graphical reports allows to extract the best of your data.

Depending on the content of its database, this app is in fact divided into 2 different builds:

* EDS (Electronic Data System)
* HNQIS (Health Network Quality Improvement System)

Both are essentially the same application with different data.

## Compatibility

This app is compatible with SDK API version 15+, and is being tested for 16+. If you find any bug for these versions, please let us know using github or writing to [hello@eyeseetea.com](mailto:hello@eyeseetea.com)

## How to customize the app

This app is a survey app that logs agains a DHIS2 server and push data to it. In order to adapt its content to a different scenario, you need to:

1. Prepare the DHIS2 server creating the data in it
2. Create a user with access to every organization unit we need to access
3. Modify CSV files in assets to reflect your specific case. These CSV will be populated into the database after the user has logged into the system

## License

This app is licensed under GPLv3. Please respect the terms of that license.
