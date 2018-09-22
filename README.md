appd-exts-commons
=================

A set of utilities to get rid of boilerplate code and to impose convention over configuration.
#SimpleHttpClient
This is a zero configuration Http Client which uses the Apache Http Client 3.x under the hood. The following features will be automatically be configured based on the Task Argument Map which is is the input to the extension class
- Authentication (BASIC)
- Proxy Configuration with proxy authentication
- SSL Configuration
The zero configuration is achieved by standarizing the conventions.

###Usage
Build the SimpleHttpClient
```
SimpleHttpClient client = SimpleHttpClient.builder(taskArgs).build();
```
#####taskArgs
```
#Host Properties
host              |      |
port              |  or  | uri
use-ssl           |      |

#Authentication Properties
username
password
auth-type : (optional) default BASIC

#Proxy Properties
proxy-host        | or | proxy-uri
proxy-port        |    |
proxy-username
proxy-password
```

#####Respose
```
Response response = client.target("https://www.google.com").get();
                    response.string();              // Reads the content as String
                    response.json(JsonPOJO.class);  // Reads the response as JSON with Jackson
                    response.xml(JaxbPOJO.class);   // Unmarshals the response into JAXB POJO
                    response.inputstream();         // Gets response as InputStream
```
###Maven POM
```
<dependency>
  <groupId>com.appdynamics</groupId>
  <artifactId>appd-exts-commons</artifactId>
  <version>1.0.0</version>
</dependency>
```

## Custom Dashboard
This feature provides the extension the ability to upload a Custom Dashboard to the Controller.
The dashboard needs to be provided with the extension and needs to have all valid Controller and Agent Information
 in order to establish a connection with the controller and upload the dashboard.
 1. If `enabled` is false, the dashboard will not upload. 
 2. If `dashboardName` is not present, the extension Monitor name will be used as the dashboard name.
 3. The path to the dashboard file needs to be present and it needs to be a valid path. Since the extension sits in 
 the Machine Agent, the path after the base directory of the machine agent is needed. 
 4. The extension developer needs to provide two dashboards, one for App Tier Node model and one for SIM. This is 
 because there are certain fields that are not present in both and are needed in their respective models 
 for the dashboard to work in a seemless manner.
 5. The dashboards files can only be named `normalDashboard.json` and `simDashboard.json`. This is because the 
 developer needs to provide both of them to meet all cases of the user using the Machine Agent with SIM or not.
 
Dashboards can not be overwritten on the controller and therefore if a dashboard is already present on the controller,
you will not be able to upload another dashboard of the same name.

Dashboards are uploaded from the MetricWriteHelper, where we upload the dashboard after running the extension 
for one cycle.

```
customDashboard:
    enabled: true
    dashboardName: "Custom Dashboard"
    pathToSIMDashboard: "monitors/<ExtensionName>/simDashboard.json"
    pathToNormalDashboard: "monitors/<ExtensionName>/normalDashboard.json"
```

## Controller Info
This feature provides the ability to get information about the Controller and Agent. 
This information is pulled in the following order: 
1. controller-info.xml
2. System Properties 
3. config.yml 

If you would like to have access to this information, you can call the getInstance() method of the ControllerInfo class.


```
controllerInfo:
    controllerHost: ""
    controllerPort: ""
    account: ""
    username: ""
    password: ""
    encryptedPassword: ""
    encryptionKey: ""
    controllerSslEnabled: ""
    enableOrchestration: ""
    uniqueHostId: ""
    accountAccessKey: ""
    machinePath: ""
    simEnabled: ""
    applicationName: ""
    tierName: ""
    nodeName: ""
```
