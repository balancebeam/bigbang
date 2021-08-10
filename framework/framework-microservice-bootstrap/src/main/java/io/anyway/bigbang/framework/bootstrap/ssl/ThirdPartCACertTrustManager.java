package io.anyway.bigbang.framework.bootstrap.ssl;

import javax.net.ssl.X509TrustManager;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.util.Collection;
import java.util.List;

public class ThirdPartCACertTrustManager implements X509TrustManager {

    private final X509TrustManager tm;
    private X509Certificate[] x509Certificates;

    ThirdPartCACertTrustManager(X509TrustManager tm) {
        this.tm = tm;
    }

    public X509Certificate[] getAcceptedIssuers() {

        /**
         * This change has been done due to the following resolution advised for Java 1.7+
         http://infposs.blogspot.kr/2013/06/installcert-and-java-7.html
         **/

        return new X509Certificate[0];
        //throw new UnsupportedOperationException();
    }

    public void checkClientTrusted(X509Certificate[] chain, String authType)
            throws CertificateException {
        throw new UnsupportedOperationException();
    }

    public void checkServerTrusted(X509Certificate[] x509Certificates, String authType)
            throws CertificateException {
        this.x509Certificates = x509Certificates;

        tm.checkServerTrusted(x509Certificates, authType);
        for(X509Certificate cert: x509Certificates){
            Collection<List<?>> altNames = cert.getSubjectAlternativeNames();
            if (altNames != null) {
                for (List<?> altName : altNames) {
                    Integer altNameType = (Integer) altName.get(0);
                    if (altNameType != 2 && altNameType != 7) // dns or ip
                        continue;
                    String hostname= (String) altName.get(1);
                    if(!hostname.endsWith("djtfintech.com.cn")){
                        throw new CertificateException("It was invalid djtfintech.com.cn certificate. cert: "+cert);
                    }
                }
            }
        }
    }
}
