[<p align="center"><img src="https://support.crowdin.com/assets/logos/crowdin-dark-symbol.png" data-canonical-src="https://support.crowdin.com/assets/logos/crowdin-dark-symbol.png" width="150" height="150" align="center"/></p>](https://crowdin.com)

# Crowdin Test App

## Configuration
Open [app.properties](resources/app.properties) file and set parameter for your app


## Installation
Run the following command to build crowdin-test.jar
```console
./gradlew shadowJar
```
After successful installation, the file will be placed into the root folder

## Running

Use the following method to run the app:

```console
java -jar crowdin-test.jar "projectId" "token" "wildcard"
```
- projectId - is id of existing project in crowdin
- token - Crowdin Personal Access Token
- wildcard - expression to detect files for upload
