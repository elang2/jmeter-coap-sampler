package org.jmeter.plugin;

import org.apache.jmeter.config.Arguments;
import org.apache.jmeter.protocol.java.sampler.AbstractJavaSamplerClient;
import org.apache.jmeter.protocol.java.sampler.JavaSamplerContext;
import org.apache.jmeter.samplers.SampleResult;
import org.apache.log.Logger;
import org.eclipse.californium.core.Utils;
import org.eclipse.californium.core.coap.MediaTypeRegistry;
import org.eclipse.californium.core.coap.Request;
import org.eclipse.californium.core.coap.Response;

public class CoapDtlsConnectionSampler extends AbstractJavaSamplerClient {

    private static final String SERVER_ENDPOINT = "SERVER_ENDPOINT";
    private static final String MESSAGE_PAYLOAD = "MESSAGE_PAYLOAD";
    private static final String KEYSTORE_PATH = "KEYSTORE_PATH";
    private static final String TRUSTSTORE_PATH = "TRUSTSTORE_PATH";
    private static final String KEYSTORE_PASS = "KEYSTORE_PASS";
    private static final String TRUST_STORE_PASS = "TRUST_STORE_PASS";
    private static final String CONNECTION_TIME_OUT = "CONNECTION_TIME_OUT";
    private static final String METHOD = "METHOD";

    private String messagePayload;
    private String serverEndPoint;
    private String keyStorePath;
    private String trustStorePath;
    private String keyStorePass;
    private String trustStorePass;
    private String connectionTimeout;
    private String method;

    Logger logger = getLogger();

    public CoapDtlsConnectionSampler() {
        logger.error("init Server");
    }

    @Override
    public Arguments getDefaultParameters() {
        logger.error("Getting default Params");
        Arguments result = new Arguments();
        result.addArgument(SERVER_ENDPOINT, "coaps://localhost:5684/register");
        result.addArgument(MESSAGE_PAYLOAD, "put payload string here");
        result.addArgument(KEYSTORE_PATH, "c:\\certs\\keyStore.jks");
        result.addArgument(TRUSTSTORE_PATH, "c:\\certs\\trustStore.jks");
        result.addArgument(KEYSTORE_PASS, "endPass");
        result.addArgument(TRUST_STORE_PASS, "rootPass");
        result.addArgument(CONNECTION_TIME_OUT, "30");
        result.addArgument(METHOD, "POST");
        return result;
    }

    @Override
    public void setupTest( JavaSamplerContext context ) {
        logger.error("Setting up test");
        messagePayload = context.getParameter(MESSAGE_PAYLOAD, "enter json here");
        serverEndPoint = context.getParameter(SERVER_ENDPOINT, "coaps://localhost:5684/register");
        keyStorePath = context.getParameter(KEYSTORE_PATH, "");
        trustStorePath = context.getParameter(TRUSTSTORE_PATH, "");
        trustStorePass = context.getParameter(TRUST_STORE_PASS, "rootPass");
        keyStorePass = context.getParameter(KEYSTORE_PASS, "endPass");
        connectionTimeout = context.getParameter(CONNECTION_TIME_OUT, "10");
        method = context.getParameter(METHOD, "POST");
    }

    public SampleResult runTest( JavaSamplerContext context ) {
        logger.error("Running Test Case");
        SampleResult result = new SampleResult();
        result.setSampleLabel("Sample");
        result.setSamplerData(String.format("url: %s, msg: %s", serverEndPoint, messagePayload));
        result.sampleStart();
        establishDtlsConnection(messagePayload, result);
        result.sampleEnd();
        return result;
    }

    @Override
    public void teardownTest( JavaSamplerContext context ) {

    }

    private void establishDtlsConnection( String message, SampleResult sampleResult ) {

        Request request = null;
        Client.getDtlsClient(keyStorePath, keyStorePass, trustStorePath, trustStorePass, logger);
        try {
            logger.error(method + serverEndPoint);
            request = newRequest(method, serverEndPoint);
            request.setPayload(message);
            request.getOptions().setContentFormat(MediaTypeRegistry.APPLICATION_JSON);
            logger.error("before send" + request);
            request.send();
            logger.error("After send");
            Response response = receiveResponse(request);

            if (!response.isTimedOut()) {
                sampleResult.setResponseMessage(Utils.prettyPrint(response));
                sampleResult.setSuccessful(true);
            } else {
                sampleResult.setResponseMessage("failure");
                sampleResult.setSuccessful(false);
            }
        } catch (Exception e) {
            logger.error("Exception " + e.getMessage());
            sampleResult.setResponseMessage(e.getMessage());
            sampleResult.setSuccessful(false);
        }
    }

    public Response receiveResponse( Request request ) {
        Response response = null;
        logger.error("Conn-> " + connectionTimeout);
        try {
            response = request.waitForResponse(1000 * Integer.parseInt(connectionTimeout));
        } catch (InterruptedException e) {
            logger.error(e.getMessage());
            return response;
        }
        if (response != null) {
            System.out.println(Utils.prettyPrint(response));
            System.out.println("Time elapsed (ms): " + response.getRTT());
            // check of response contains resources
            if (response.getOptions().hasOption(MediaTypeRegistry.APPLICATION_LINK_FORMAT)) {
                String linkFormat = response.getPayloadString();
                // output discovered resources
                System.out.println("\nDiscovered resources:");
                System.out.println(linkFormat);
            } else {

            }
        } else {
            System.err.println("Request timed out");
        }
        return response;
    }

    private static Request newRequest( String method, String uri ) {
        if (method.equals("GET")) {
            return Request.newGet().setURI(uri);
        } else if (method.equals("POST")) {
            return Request.newPost().setURI(uri);
        } else if (method.equals("PUT")) {
            return Request.newPut().setURI(uri);
        } else if (method.equals("DELETE")) {
            return Request.newDelete().setURI(uri);
        }
        return Request.newPost().setURI(uri);
    }

}
