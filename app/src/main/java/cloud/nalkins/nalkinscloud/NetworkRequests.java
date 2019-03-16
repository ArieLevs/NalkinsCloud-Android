package cloud.nalkins.nalkinscloud;

import android.app.Application;
import android.text.TextUtils;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.HurlStack;
import com.android.volley.toolbox.Volley;

import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.security.KeyManagementException;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.security.cert.Certificate;
import java.security.cert.CertificateException;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;

import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSession;
import javax.net.ssl.SSLSocketFactory;
import javax.net.ssl.TrustManager;
import javax.net.ssl.TrustManagerFactory;
import javax.net.ssl.X509TrustManager;

import static cloud.nalkins.nalkinscloud.AppConfig.ENVIRONMENT;
import static cloud.nalkins.nalkinscloud.AppConfig.TRUST_ALL_CERTIFICATES;

/**
 * Created by Arie on 3/8/2017.
 *
 * Class responsible for network communication, Sends requests from application
 */
public class NetworkRequests extends Application {

    public static final String TAG = NetworkRequests.class.getSimpleName();

    private RequestQueue encryptedRequestQueue;
    private RequestQueue notEncryptedRequestQueue;

    private static NetworkRequests mInstance;
    private static HurlStack hurlStack;

    @Override
    public void onCreate() {
        super.onCreate();
        mInstance = this;

        // Initialize the HurlStack with the trusted certificated
        hurlStack = new HurlStack() {
            @Override
            protected HttpURLConnection createConnection(URL url) throws IOException {
                HttpsURLConnection httpsURLConnection = (HttpsURLConnection) super.createConnection(url);
                try {
                    // Set up the trusted certificate
                    if (TRUST_ALL_CERTIFICATES)
                        httpsURLConnection.setSSLSocketFactory(trustAllCertificated());
                    else
                        httpsURLConnection.setSSLSocketFactory(getSSLSocketFactory());
                    // Set up the host name that matched that certificate
                    httpsURLConnection.setHostnameVerifier(getHostnameVerifier());
                } catch (Exception e) {
                    e.printStackTrace();
                }
                return httpsURLConnection;
            }
        };
    }


    public static synchronized NetworkRequests getInstance() {
        return mInstance;
    }


    /**
     * If received 'isSSLMode' = true will return the encrypted queue
     * @param isSSLMode Indicates 'RequestQueue' Type should be returned
     * @return An RequestQueue object, depending on the param 'isSSLMode'
     */
    public RequestQueue getRequestQueue(boolean isSSLMode) {
        if (ENVIRONMENT.equals("dev"))
            isSSLMode = false;
        if(isSSLMode) {
            if (encryptedRequestQueue == null)
                encryptedRequestQueue = Volley.newRequestQueue(getApplicationContext(), hurlStack);
            return encryptedRequestQueue;
        }
        else
            if (notEncryptedRequestQueue == null)
                notEncryptedRequestQueue = Volley.newRequestQueue(getApplicationContext());
            return notEncryptedRequestQueue;
    }


    /**
     * Receive 'isSSLMode' boolean,
     * This indicates if the request should use SSL/TLS encryption,
     * If set to 'true' request will load certificates files later
      */
    public <T> void addToRequestQueue(Request<T> req, String tag, boolean isSSLMode) {
        req.setTag(TextUtils.isEmpty(tag) ? TAG : tag);
        getRequestQueue(isSSLMode).add(req);
    }

    /*
    public void cancelPendingRequests(Object tag) {
        if (mRequestQueue != null) {
            mRequestQueue.cancelAll(tag);
        }
    }*/


    /**
     * Check if host name match the 'Issues to' inside the certificate
     * @return true if match, else false
      */
    private HostnameVerifier getHostnameVerifier() {
        return new HostnameVerifier() {
            @Override
            public boolean verify(String hostname, SSLSession session) {
                HostnameVerifier hostnameVerifier = HttpsURLConnection.getDefaultHostnameVerifier();
                // Compare the host name, with the one inside the certificate
                return hostnameVerifier.verify(AppConfig.API_SERVER_HOST, session);
            }
        };
    }


    /**
     * Set up the trusted self-signed certificate
     *
     * @return SSLContext object
     *
     * @throws CertificateException desc
     * @throws KeyStoreException desc
     * @throws IOException desc
     * @throws NoSuchAlgorithmException desc
     * @throws KeyManagementException desc
     */
    private SSLSocketFactory getSSLSocketFactory()
            throws CertificateException, KeyStoreException, IOException, NoSuchAlgorithmException, KeyManagementException {
        CertificateFactory cf = CertificateFactory.getInstance("X.509");

        InputStream caInput = getApplicationContext().getAssets().open(AppConfig.SERVER_SSL_CRT_FILE);

        Certificate ca = cf.generateCertificate(caInput);
        caInput.close();

        // Create a KeyStore containing our trusted CAs
        KeyStore keyStore = KeyStore.getInstance("BKS");
        keyStore.load(null, null);
        keyStore.setCertificateEntry("ca", ca);

        // Create a TrustManager that trusts the CAs in our KeyStore
        String tmfAlgorithm = TrustManagerFactory.getDefaultAlgorithm();
        TrustManagerFactory tmf = TrustManagerFactory.getInstance(tmfAlgorithm);
        tmf.init(keyStore);

        // This should be used for multiple certificates
        //TrustManager[] wrappedTrustManagers = getWrappedTrustManagers(tmf.getTrustManagers());

        // Create an SSLContext that uses our TrustManager
        SSLContext sslContext = SSLContext.getInstance("TLS");
        sslContext.init(null, tmf.getTrustManagers(), null);

        return sslContext.getSocketFactory();
    }


    /*
    // This function is not in use, this should be used if multiple certificated should be trusted
    private TrustManager[] getWrappedTrustManagers(TrustManager[] trustManagers) {
        final X509TrustManager originalTrustManager = (X509TrustManager) trustManagers[0];
        return new TrustManager[]{
                new X509TrustManager() {
                    public X509Certificate[] getAcceptedIssuers() {
                        return originalTrustManager.getAcceptedIssuers();
                    }

                    public void checkClientTrusted(X509Certificate[] certs, String authType) {
                        try {
                            if (certs != null && certs.length > 0){
                                certs[0].checkValidity();
                            } else {
                                originalTrustManager.checkClientTrusted(certs, authType);
                            }
                        } catch (CertificateException e) {
                            Log.w("checkClientTrusted", e.toString());
                        }
                    }

                    public void checkServerTrusted(X509Certificate[] certs, String authType) {
                        try {
                            if (certs != null && certs.length > 0){
                                certs[0].checkValidity();
                            } else {
                                originalTrustManager.checkServerTrusted(certs, authType);
                            }
                        } catch (CertificateException e) {
                            Log.w("checkServerTrusted", e.toString());
                        }
                    }
                }
        };
    } */


    // This function should be used, in order to trust (###WARNING DANGEROUS###) ALL certificated
    public SSLSocketFactory trustAllCertificated()
        throws NoSuchAlgorithmException, KeyManagementException {

        TrustManager[] trustAllCerts = new TrustManager[] {
                new X509TrustManager() {
                    public X509Certificate[] getAcceptedIssuers() {
                        return new X509Certificate[0];
                    }

                    @Override
                    public void checkClientTrusted(X509Certificate[] certs, String authType) {}

                    @Override
                    public void checkServerTrusted(X509Certificate[] certs, String authType) {}
                }
        };

        SSLContext sc = SSLContext.getInstance("SSL");
        sc.init(null, trustAllCerts, new SecureRandom());
        return sc.getSocketFactory();
    }

}
