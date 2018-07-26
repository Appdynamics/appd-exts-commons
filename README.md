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
accountAccessKey
auth-type : (optional) default BASIC

#Proxy Properties
proxy-host        | or | proxy-uri
proxy-port        |    |
proxy-username
proxy-accountAccessKey
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
