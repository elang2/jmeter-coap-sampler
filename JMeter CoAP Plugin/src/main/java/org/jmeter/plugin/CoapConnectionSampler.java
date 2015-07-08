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

public class CoapConnectionSampler extends AbstractJavaSamplerClient {

    private final String DEFAULT_METHOD = "POST";
    private final String DEFAULT_CONNECTION_TIMEOUT = "30";
    private final String DEFAULT_PAYLOAD = "Enter payload here";
    private final String DEFAULT_ENDPOINT = "coap://localhost:5684/register";
    private final String SERVER_ENDPOINT = "SERVER_ENDPOINT";
    private final String MESSAGE_PAYLOAD = "MESSAGE_PAYLOAD";
    private final String CONNECTION_TIME_OUT = "CONNECTION_TIME_OUT";
    private final String METHOD = "METHOD";

    private String messagePayload;
    private String serverEndPoint;
    private String connectionTimeout;
    private String method;

    Logger logger = getLogger();

    public CoapConnectionSampler() {
        logger.error("init Server");
    }

    @Override
    public Arguments getDefaultParameters() {
        Arguments result = new Arguments();
        result.addArgument(SERVER_ENDPOINT, DEFAULT_ENDPOINT);
        result.addArgument(MESSAGE_PAYLOAD, DEFAULT_PAYLOAD);
        result.addArgument(CONNECTION_TIME_OUT, DEFAULT_CONNECTION_TIMEOUT);
        result.addArgument(METHOD, DEFAULT_METHOD);
        return result;
    }

    @Override
    public void setupTest( JavaSamplerContext context ) {
        messagePayload = context.getParameter(MESSAGE_PAYLOAD, DEFAULT_ENDPOINT);
        serverEndPoint = context.getParameter(SERVER_ENDPOINT, DEFAULT_ENDPOINT);
        connectionTimeout = context.getParameter(CONNECTION_TIME_OUT, DEFAULT_CONNECTION_TIMEOUT);
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
        Client.getClient(serverEndPoint, logger);
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
