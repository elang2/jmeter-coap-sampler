##Jmeter plugin for testing API Endpoints using CoAP Protocol

Reference : 
https://en.wikipedia.org/wiki/Constrained_Application_Protocol
http://coap.technology/ 

CoAP (Constrained Application protocol) is an application layer protocol intended for use in resource constrained environments like in low power electronic devices and other electrical appliances which need to be able to send and receive instructions remotely.

CoAP is a good candidate to build a IoT (Internet of Things) framework to help devices talk to remote application or servers.

[Californium](http://www.eclipse.org/californium/) is a Java Library which can be used to setup API endpoints in your application which operate using the **Constrained Application Protocol**.

**Jmeter-coap-Sampler** is a Jmeter plugin which can be used for Automation testing or Load testing your API endpoints implemented in **CoAP**.

There are separate samplers to test DTLS endpoints and non DTLS endpoints.


Pre-requisite

1. Java 7
2. Maven 2.x+
3. Jmeter 2.10+


Installation Instructions:
----------------------------

1. Get the code.
2. Execute the following maven code

  > mvn dependency:copy-dependencies install -DexcludeGroupIds=org.apache.jmeter
  
3. Copy the following jars from target/dependency to  /jmeter_install_path/lib
  
  1. californium-core-1.0.0-SNAPSHOT.jar
  2. scandium-1.0.0-SNAPSHOT.jar
  3. element-connector-1.0.0-SNAPSHOT.jar
 
4. Execute the following maven code

  > mvn clean package 

5. Copy the following jar from target/ to  /jmeter_install_path/lib/ext

 1.  jmeter-coap-sampler-1.0.0.jar


Using the Sampler
----------------------

1. Open JMeter.
2. Add a new Thread Group.
3. Right click on the Thread Group -> Add -> Sampler -> Java Request
4. Choose the Java Request from the navigation panel.
5. Click on the 'classname' drop down and choose between

       org.jmeter.plugin.CoapDtlsConnectionSampler
       org.jmeter.plugin.CoapConnectionSampler


Screenshots
---------------------


![enter image description here](https://github.com/elang2/my-repo/blob/master/images/Java-request.png)

![enter image description here](https://github.com/elang2/my-repo/blob/master/images/coap.png)

![enter image description here](https://github.com/elang2/my-repo/blob/master/images/dtls.png)


    



  
  
  

  
