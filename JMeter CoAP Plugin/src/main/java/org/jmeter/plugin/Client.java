package org.jmeter.plugin;

import java.io.FileInputStream;
import java.io.InputStream;
import java.net.InetSocketAddress;
import java.security.KeyStore;
import java.security.PrivateKey;
import java.security.cert.Certificate;

import org.apache.log.Logger;
import org.eclipse.californium.core.CoapClient;
import org.eclipse.californium.core.network.CoAPEndpoint;
import org.eclipse.californium.core.network.Endpoint;
import org.eclipse.californium.core.network.EndpointManager;
import org.eclipse.californium.core.network.EndpointManager.ClientMessageDeliverer;
import org.eclipse.californium.core.network.config.NetworkConfig;
import org.eclipse.californium.scandium.DTLSConnector;
import org.eclipse.californium.scandium.config.DtlsConnectorConfig;
import org.eclipse.californium.scandium.dtls.pskstore.StaticPskStore;

public class Client {

    private static CoapClient client = null;
    private static DTLSConnector connector = null;
    private static Endpoint dtlsEndpoint = null;

    public static CoapClient getClient( String url, Logger logger ) {
        if (client != null) {
            return client;
        }
        try {
            client = new CoapClient(url);
        } catch (Exception e) {
            logger.error("Error her" + e.getMessage());
        }
        return client;
    }

    public static void getDtlsClient( String keyStorePath, String keyStorePass, String trustStorePath,
            String trustStorePass, Logger logger ) {

        if (connector != null) {
            logger.error("Retuirning ");
            return;
        }

        try {
            logger.error("In Init ");
            KeyStore keyStore = KeyStore.getInstance("JKS");
            InputStream in = new FileInputStream(keyStorePath);
            keyStore.load(in, keyStorePass.toCharArray());

            // load trust store
            KeyStore trustStore = KeyStore.getInstance("JKS");
            InputStream inTrust = new FileInputStream(trustStorePath);
            trustStore.load(inTrust, trustStorePass.toCharArray());

            // You can load multiple certificates if needed
            Certificate[] trustedCertificates = new Certificate[1];
            trustedCertificates[0] = trustStore.getCertificate("root");

            DtlsConnectorConfig.Builder builder = new DtlsConnectorConfig.Builder(new InetSocketAddress(0));
            builder.setPskStore(new StaticPskStore("Client_identity", "secretPSK".getBytes()));
            builder.setIdentity((PrivateKey) keyStore.getKey("client", keyStorePass.toCharArray()),
                    keyStore.getCertificateChain("client"), true);
            builder.setTrustStore(trustedCertificates);
            connector = new DTLSConnector(builder.build(), null);

            dtlsEndpoint = new CoAPEndpoint(connector, NetworkConfig.getStandard());
            dtlsEndpoint.setMessageDeliverer(new ClientMessageDeliverer());
            dtlsEndpoint.start();

            EndpointManager.getEndpointManager().setDefaultSecureEndpoint(dtlsEndpoint);
            logger.error("Done Setup ");
        } catch (Exception e) {
            logger.error("Error her" + e.getMessage());
        }
    }
}
