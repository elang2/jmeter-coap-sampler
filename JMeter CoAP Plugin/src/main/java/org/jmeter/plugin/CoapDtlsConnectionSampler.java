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

    private static final String DEFAULT_METHOD = "POST";
    private static final String DEFAULT_TIMEOUT = "10";
    private static final String DEFAULT_TRUST_STORE_PASSWORD = "rootPass";
    private static final String DEFAULT_KEYSTORE_PASSWORD = "endPass";
    private static final String DEFAULT_TRUST_STORE = "c:\\certs\\trustStore.jks";
    private static final String DEFAULT_SERVER_ENDPOINT = "coaps://localhost:5684/register";
    private static final String DEFAULT_PAYLOAD = "Enter payload here";
    private static final String DEFAULT_KEYSTORE_PATH = "c:\\certs\\keyStore.jks";
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
    }

    @Override
    public Arguments getDefaultParameters() {
        Arguments result = new Arguments();
        result.addArgument(SERVER_ENDPOINT, DEFAULT_SERVER_ENDPOINT);
        result.addArgument(MESSAGE_PAYLOAD, DEFAULT_PAYLOAD);
        result.addArgument(KEYSTORE_PATH, DEFAULT_KEYSTORE_PATH);
        result.addArgument(TRUSTSTORE_PATH, DEFAULT_TRUST_STORE);
        result.addArgument(KEYSTORE_PASS, DEFAULT_KEYSTORE_PASSWORD);
        result.addArgument(TRUST_STORE_PASS, DEFAULT_TRUST_STORE_PASSWORD);
        result.addArgument(CONNECTION_TIME_OUT, DEFAULT_TIMEOUT);
        result.addArgument(METHOD, DEFAULT_METHOD);
        return result;
    }

    @Override
    public void setupTest( JavaSamplerContext context ) {
        messagePayload = context.getParameter(MESSAGE_PAYLOAD, "Enter Payload here");
        serverEndPoint = context.getParameter(SERVER_ENDPOINT, DEFAULT_SERVER_ENDPOINT);
        keyStorePath = context.getParameter(KEYSTORE_PATH, DEFAULT_KEYSTORE_PATH);
        trustStorePath = context.getParameter(TRUSTSTORE_PATH, DEFAULT_TRUST_STORE);
        trustStorePass = context.getParameter(TRUST_STORE_PASS, DEFAULT_TRUST_STORE_PASSWORD);
        keyStorePass = context.getParameter(KEYSTORE_PASS, DEFAULT_KEYSTORE_PASSWORD);
        connectionTimeout = context.getParameter(CONNECTION_TIME_OUT, DEFAULT_TIMEOUT);
        method = context.getParameter(METHOD, DEFAULT_METHOD);
    }

    public SampleResult runTest( JavaSamplerContext context ) {
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
            request = newRequest(method, serverEndPoint);
            request.setPayload(message);
            request.getOptions().setContentFormat(MediaTypeRegistry.APPLICATION_JSON);
            request.send();
            Response response = receiveResponse(request);
            if (!response.isTimedOut()) {
                sampleResult.setResponseMessage(Utils.prettyPrint(response));
                sampleResult.setSuccessful(true);
            } else {
                sampleResult.setResponseMessage("Request Timed Out");
                sampleResult.setSuccessful(false);
            }
        } catch (Exception e) {
            sampleResult.setResponseMessage("Exception Occured: " + e.getMessage());
            sampleResult.setSuccessful(false);
        }
    }

    public Response receiveResponse( Request request ) throws Exception {
        Response response = null;
        try {
            response = request.waitForResponse(1000 * Integer.parseInt(connectionTimeout));
        } catch (InterruptedException e) {
            throw e;
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
