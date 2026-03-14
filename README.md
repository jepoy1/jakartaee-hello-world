# Jakarta EE Purchase Order Tracking API
This is a sample Jakarta EE application for Purchase Order tracking.

* You can build the application by executing the following command from the directory where this file resides. 
Please ensure you have installed a [Java SE implementation](https://adoptium.net) appropriate for your 
Jakarta EE version (this sample assumes Java SE 21). Note, 
the [Maven Wrapper](https://maven.apache.org/wrapper/) is already included in the project, so a Maven install 
is not actually needed. You may first need to execute `chmod +x mvnw`.

  ```
  ./mvnw clean package
  ```
 
This will generate a file named `jakartaee-purchase-order-tracking.war`. You should be able to run the application by deploying 
the file to a [Jakarta EE compatible runtime](https://jakarta.ee/compatibility).

### WildFly Deployment Note
- This app is configured with context root `/emw` via `src/main/webapp/WEB-INF/jboss-web.xml`.
- Expected endpoint after deployment: `GET http://localhost:8080/emw/purchase-orders`.
- Build/redeploy command: `./mvnw package` (or `mvnw.cmd package` on Windows).
- If `mvn clean` fails with file-lock errors under `target/server`, stop WildFly first, then run clean/package again.

### Repository Tests
- Repository tests run against an in-memory H2 database configured in MySQL compatibility mode.
- Run them with `./mvnw test` or `mvnw.cmd test` on Windows.
- This keeps repository tests fast, but MySQL-specific SQL behavior should still be covered separately with integration tests against MySQL.
