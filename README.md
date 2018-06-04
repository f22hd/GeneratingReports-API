# GeneratingReports-API
An example of how to create API by java spring boot and generating pdf files by itextpdf and uploud it to AWS S3 with supporting arabic language.

### Application Sequence

with this application you can use generate reports by sending the details as json request.
- request handled by controller.
- validate and pass data to services.
- services generate new file with data as pdf by itextpdf library.
- after created the pdf file , application will use aws library to upload that file into specific bucket in S3.
- result object will return as response.

### Prerequisites:
- Java 8
- Maven > 3.0

### Configurations:
make sure add your credentials (bucket name , access key , secret key) in application.properties inside this path 
```sh
/src/main/resources
```

## Usage
### Using the terminal
Go on the project's root folder, then type: 
```sh
mvn spring-boot:run
```
### Using eclipse IDE or Spring Tool Suite
Import as Existing Maven Project and run it as Spring Boot App.


you can use [Postman](https://www.getpostman.com/) and sending the requests ( POST , GET ..etc) to application 
   new report request:
 - POST  http://localhost:5000/report/ 
 - Header: Content-Type   Application/json
 - initial request body : 
 > { "title" : "", "body" : "" } 
 
 list reports request :
 - GET http://localhost:5000/report/
 - Header:  Content-Type  Application/json

 
### development:
Want to contribute? Great!

after cloned this repository, open your IDE and open that folder.
- right click on the project name and choose maven then update project.

Done :)
now you are ready to change the code and add more features and fixing the bugs.
don't forget to do pull request to get your changes and open issue if you have any issue with.


#### Resources:
 - Spring Boot http://spring.io/projects/spring-boot
 - itextpdf https://developers.itextpdf.com/
 - aws sdk documentation https://docs.aws.amazon.com/sdk-for-java/v1/developer-guide/welcome.html
 
 
