package io.anyway.bigbang.framework.bootstrap.ssl;

import lombok.extern.slf4j.Slf4j;

import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManager;
import javax.net.ssl.TrustManagerFactory;
import javax.net.ssl.X509TrustManager;
import java.io.InputStream;
import java.security.KeyStore;

@Slf4j
public abstract class SSLContextGenerator {

    private SSLContextGenerator(){}

    public static SSLContext generate(String location,String password){
        ClassLoader classLoader= SSLContextGenerator.class.getClassLoader();
        try (InputStream in = classLoader.getResourceAsStream(location)){
            KeyStore ks = KeyStore.getInstance(KeyStore.getDefaultType());
            ks.load(in, password.toCharArray());
            SSLContext context = SSLContext.getInstance("TLS");
            TrustManagerFactory tmf = TrustManagerFactory
                    .getInstance(TrustManagerFactory.getDefaultAlgorithm());
            tmf.init(ks);
            X509TrustManager defaultTrustManager = (X509TrustManager) tmf
                    .getTrustManagers()[0];
            ThirdPartCACertTrustManager tm = new ThirdPartCACertTrustManager(defaultTrustManager);
            context.init(null, new TrustManager[]{tm}, null);
            return context;
        } catch (Exception e) {
            log.error("Generate SSLContext error",e);
            throw new RuntimeException(e);
        }
    }

}
