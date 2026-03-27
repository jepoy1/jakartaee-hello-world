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
- Purchase order UI page: `http://localhost:8080/emw/`.
- UI filters are sent as query parameters to the API (example: `http://localhost:8080/emw/?paymentStatus=ONGOING&customer=Centro`).
- Build/redeploy command: `./mvnw package` (or `mvnw.cmd package` on Windows).
- Local run command after a clean build: `./mvnw clean package wildfly:run` (or `mvnw.cmd clean package wildfly:run` on Windows).
- If you already have a provisioned `target/server`, you can restart it with `./mvnw wildfly:run`, but after `clean` you need the combined command so Maven recreates the server and datasource first.
- If `mvn clean` fails with file-lock errors under `target/server`, stop WildFly first, then run clean/package again.
- Maven now applies the local MySQL datasource automatically during `./mvnw package` by running `wildfly/create-purchase-order-datasource.cli` against the provisioned `target/server` tree.
- Maven also copies the MySQL JDBC driver into `target/wildfly-drivers` before that CLI script runs, so the WildFly module can be created locally during the same build.
- The datasource name is `java:jboss/datasources/PurchaseOrderDS`, and the default JDBC URL is `jdbc:mysql://localhost:3306/emw?useSSL=false&allowPublicKeyRetrieval=true&serverTimezone=UTC`.
- Override the defaults with Maven properties if needed: `-Dmysql.url=... -Dmysql.user=... -Dmysql.password=...`.
- The MySQL JDBC driver is downloaded through Maven and installed into the provisioned WildFly server by the same workflow.
- `src/main/resources/META-INF/initial-data.sql` remains the seed script for the sample rows.

### Repository Tests
- Repository tests run against an in-memory H2 database configured in MySQL compatibility mode.
- Run them with `./mvnw test` or `mvnw.cmd test` on Windows.
- This keeps repository tests fast, but MySQL-specific SQL behavior should still be covered separately with integration tests against MySQL.
