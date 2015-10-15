/*
 * JetS3t : Java S3 Toolkit
 * Project hosted at http://bitbucket.org/jmurty/jets3t/
 *
 * Copyright 2006-2010 James Murty
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.jets3t.service.utils;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.text.ParseException;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.SortedMap;
import java.util.TreeMap;

import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.http.Header;
import org.apache.http.HttpConnection;
import org.apache.http.HttpHost;
import org.apache.http.HttpRequest;
import org.apache.http.HttpRequestInterceptor;
import org.apache.http.HttpResponse;
import org.apache.http.HttpVersion;
import org.apache.http.auth.AuthScope;
import org.apache.http.auth.AuthState;
import org.apache.http.auth.Credentials;
import org.apache.http.client.CredentialsProvider;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpRequestBase;
import org.apache.http.client.params.ClientPNames;
import org.apache.http.client.protocol.ClientContext;
import org.apache.http.conn.ClientConnectionManager;
import org.apache.http.conn.ClientConnectionManagerFactory;
import org.apache.http.conn.params.ConnManagerParams;
import org.apache.http.conn.scheme.Scheme;
import org.apache.http.conn.scheme.SchemeRegistry;
import org.apache.http.conn.ssl.SSLSocketFactory;
import org.apache.http.impl.auth.BasicScheme;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.impl.client.DefaultHttpRequestRetryHandler;
import org.apache.http.impl.client.RequestWrapper;
import org.apache.http.impl.conn.tsccm.AbstractConnPool;
import org.apache.http.impl.conn.tsccm.ConnPoolByRoute;
import org.apache.http.impl.conn.tsccm.ThreadSafeClientConnManager;
import org.apache.http.params.HttpConnectionParams;
import org.apache.http.params.HttpParams;
import org.apache.http.params.HttpProtocolParams;
import org.apache.http.params.SyncBasicHttpParams;
import org.apache.http.protocol.ExecutionContext;
import org.apache.http.protocol.HTTP;
import org.apache.http.protocol.HttpContext;
import org.apache.http.util.VersionInfo;
import org.jets3t.service.Constants;
import org.jets3t.service.Jets3tProperties;
import org.jets3t.service.S3ServiceException;
import org.jets3t.service.ServiceException;
import org.jets3t.service.impl.rest.httpclient.JetS3tRequestAuthorizer;
import org.jets3t.service.impl.rest.httpclient.RestS3Service;
import org.jets3t.service.io.UnrecoverableIOException;

/**
 * Utilities useful for REST/HTTP S3Service implementations.
 *
 * @author James Murty
 */
public class RestUtils {

    private static final Log log = LogFactory.getLog(RestUtils.class);

    /**
     * A list of HTTP-specific header names, that may be present in S3Objects as metadata but
     * which should be treated as plain HTTP headers during transmission (ie not converted into
     * S3 Object metadata items). All items in this list are in lower case.
     * <p>
     * This list includes the items:
     * <table>
     * <tr><th>Unchanged metadata names</th></tr>
     * <tr><td>content-type</td></tr>
     * <tr><td>content-md5</td></tr>
     * <tr><td>content-length</td></tr>
     * <tr><td>content-language</td></tr>
     * <tr><td>expires</td></tr>
     * <tr><td>cache-control</td></tr>
     * <tr><td>content-disposition</td></tr>
     * <tr><td>content-encoding</td></tr>
     * </table>
     */
    public static final List<String> HTTP_HEADER_METADATA_NAMES = Arrays.asList(
            "content-type",
            "content-md5",
            "content-length",
            "content-language",
            "expires",
            "cache-control",
            "content-disposition",
            "content-encoding");


    /**
     * Encodes a URL string, and ensures that spaces are encoded as "%20" instead of "+" to keep
     * fussy web browsers happier.
     *
     * @param path
     * @return
     * encoded URL.
     * @throws ServiceException
     */
    public static String encodeUrlString(String path) throws ServiceException {
        try {
            String encodedPath = URLEncoder.encode(path, Constants.DEFAULT_ENCODING);
            // Web browsers do not always handle '+' characters well, use the well-supported '%20' instead.
            encodedPath = encodedPath.replaceAll("\\+", "%20");
            // '@' character need not be URL encoded and Google Chrome balks on signed URLs if it is.
            encodedPath = encodedPath.replaceAll("%40", "@");
            return encodedPath;
        } catch (UnsupportedEncodingException uee) {
            throw new ServiceException("Unable to encode path: " + path, uee);
        }
    }

    /**
     * Encodes a URL string but leaves a delimiter string unencoded.
     * Spaces are encoded as "%20" instead of "+".
     *
     * @param path
     * @param delimiter
     * @return
     * encoded URL string.
     * @throws ServiceException
     */
    public static String encodeUrlPath(String path, String delimiter) throws ServiceException {
        StringBuilder result = new StringBuilder();
        String tokens[] = path.split(delimiter);
        for (int i = 0; i < tokens.length; i++) {
            result.append(encodeUrlString(tokens[i]));
            if (i < tokens.length - 1) {
                result.append(delimiter);
            }
        }
        return result.toString();
    }

    /**
     * Calculate the canonical string for a REST/HTTP request to a storage service.
     *
     * When expires is non-null, it will be used instead of the Date header.
     * @throws UnsupportedEncodingException
     */
    public static String makeServiceCanonicalString(String method, String resource,
        Map<String, Object> headersMap, String expires, String headerPrefix,
        List<String> serviceResourceParameterNames) throws UnsupportedEncodingException
    {
        StringBuilder canonicalStringBuf = new StringBuilder();
        canonicalStringBuf.append(method).append("\n");

        // Add all interesting headers to a list, then sort them.  "Interesting"
        // is defined as Content-MD5, Content-Type, Date, and x-amz-
        SortedMap<String, Object> interestingHeaders = new TreeMap<String, Object>();
        if (headersMap != null && headersMap.size() > 0) {
            for (Map.Entry<String, Object> entry: headersMap.entrySet()) {
                Object key = entry.getKey();
                Object value = entry.getValue();

                if (key == null) {
                    continue;
                }
                String lk = key.toString().toLowerCase(Locale.getDefault());

                // Ignore any headers that are not particularly interesting.
                if (lk.equals("content-type") || lk.equals("content-md5") || lk.equals("date") ||
                    lk.startsWith(headerPrefix))
                {
                    interestingHeaders.put(lk, value);
                }
            }
        }

        // Remove default date timestamp if "x-amz-date" or "x-goog-date" is set.
        if (interestingHeaders.containsKey(Constants.REST_METADATA_ALTERNATE_DATE_AMZ)
            || interestingHeaders.containsKey(Constants.REST_METADATA_ALTERNATE_DATE_GOOG)) {
          interestingHeaders.put("date", "");
        }

        // Use the expires value as the timestamp if it is available. This trumps both the default
        // "date" timestamp, and the "x-amz-date" header.
        if (expires != null) {
            interestingHeaders.put("date", expires);
        }

        // these headers require that we still put a new line in after them,
        // even if they don't exist.
        if (! interestingHeaders.containsKey("content-type")) {
            interestingHeaders.put("content-type", "");
        }
        if (! interestingHeaders.containsKey("content-md5")) {
            interestingHeaders.put("content-md5", "");
        }

        // Finally, add all the interesting headers (i.e.: all that start with x-amz- ;-))
        for (Map.Entry<String, Object> entry: interestingHeaders.entrySet()) {
            String key = entry.getKey();
            Object value = entry.getValue();

            if (key.startsWith(headerPrefix)) {
                canonicalStringBuf.append(key).append(':').append(value);
            } else {
                canonicalStringBuf.append(value);
            }
            canonicalStringBuf.append("\n");
        }

        // don't include the query parameters...
        int queryIndex = resource.indexOf('?');
        if (queryIndex == -1) {
            canonicalStringBuf.append(resource);
        } else {
            canonicalStringBuf.append(resource.substring(0, queryIndex));
        }

        // ...unless the parameter(s) are in the set of special params
        // that actually identify a service resource.
        if (queryIndex >= 0) {
            SortedMap<String, String> sortedResourceParams = new TreeMap<String, String>();

            // Parse parameters from resource string
            String query = resource.substring(queryIndex + 1);
            for (String paramPair: query.split("&")) {
                String[] paramNameValue = paramPair.split("=");
                String name = URLDecoder.decode(paramNameValue[0], "UTF-8");
                String value = null;
                if (paramNameValue.length > 1) {
                    value = URLDecoder.decode(paramNameValue[1], "UTF-8");
                }
                // Only include parameter (and its value if present) in canonical
                // string if it is a resource-identifying parameter
                if (serviceResourceParameterNames.contains(name)) {
                    sortedResourceParams.put(name, value);
                }
            }

            // Add resource parameters
            if (sortedResourceParams.size() > 0) {
                canonicalStringBuf.append("?");
            }
            boolean addedParam = false;
            for (Map.Entry<String, String> entry: sortedResourceParams.entrySet()) {
                if (addedParam) {
                    canonicalStringBuf.append("&");
                }
                canonicalStringBuf.append(entry.getKey());
                if (entry.getValue() != null) {
                    canonicalStringBuf.append("=").append(entry.getValue());
                }
                addedParam = true;
            }
        }

        return canonicalStringBuf.toString();
    }

    public static HttpClient initHttpsConnection(final JetS3tRequestAuthorizer requestAuthorizer,
        Jets3tProperties jets3tProperties,
        String userAgentDescription,
        CredentialsProvider credentialsProvider)
    {
        // Configure HttpClient properties based on Jets3t Properties.
        HttpParams params = createDefaultHttpParams();
        params.setParameter(Jets3tProperties.JETS3T_PROPERTIES_ID, jets3tProperties);

        params.setParameter(
            ClientPNames.CONNECTION_MANAGER_FACTORY_CLASS_NAME,
            jets3tProperties.getStringProperty(
                ClientPNames.CONNECTION_MANAGER_FACTORY_CLASS_NAME,
                ConnManagerFactory.class.getName()));

        HttpConnectionParams.setConnectionTimeout(params,
            jets3tProperties.getIntProperty("httpclient.connection-timeout-ms", 60000));
        HttpConnectionParams.setSoTimeout(params,
            jets3tProperties.getIntProperty("httpclient.socket-timeout-ms", 60000));
        HttpConnectionParams.setStaleCheckingEnabled(params,
            jets3tProperties.getBoolProperty("httpclient.stale-checking-enabled", true));

        // Connection properties to take advantage of S3 window scaling.
        if (jets3tProperties.containsKey("httpclient.socket-receive-buffer")) {
            HttpConnectionParams.setSocketBufferSize(params,
                jets3tProperties.getIntProperty("httpclient.socket-receive-buffer", 0));
        }

        HttpConnectionParams.setTcpNoDelay(params, true);

        // Set user agent string.
        String userAgent = jets3tProperties.getStringProperty("httpclient.useragent", null);
        if (userAgent == null) {
            userAgent = ServiceUtils.getUserAgentDescription(userAgentDescription);
        }
        if (log.isDebugEnabled()) {
            log.debug("Setting user agent string: " + userAgent);
        }
        HttpProtocolParams.setUserAgent(params, userAgent);

        boolean expectContinue
                = jets3tProperties.getBoolProperty("http.protocol.expect-continue", true);
        HttpProtocolParams.setUseExpectContinue(params, expectContinue);

        long connectionManagerTimeout
                = jets3tProperties.getLongProperty("httpclient.connection-manager-timeout", 0);
        ConnManagerParams.setTimeout(params, connectionManagerTimeout);

        DefaultHttpClient httpClient = wrapClient(params);
        httpClient.setHttpRequestRetryHandler(
            new JetS3tRetryHandler(
                jets3tProperties.getIntProperty("httpclient.retry-max", 5), requestAuthorizer));

        if (credentialsProvider != null) {
            if (log.isDebugEnabled()) {
                log.debug("Using credentials provider class: "
                        + credentialsProvider.getClass().getName());
            }
            httpClient.setCredentialsProvider(credentialsProvider);
            if (jets3tProperties.getBoolProperty(
                    "httpclient.authentication-preemptive",
                    false)) {
                // Add as the very first interceptor in the protocol chain
                httpClient.addRequestInterceptor(new PreemptiveInterceptor(), 0);
            }
        }
        return httpClient;
    }
    
    public static DefaultHttpClient wrapClient(HttpParams params)
    {
        try
        {
            SSLContext ctx = SSLContext.getInstance("TLS");
            X509TrustManager tm = new X509TrustManager()
            {
                @Override
                public X509Certificate[] getAcceptedIssuers()
                {
                    return null;
                }
                @Override
                public void checkClientTrusted(X509Certificate[] arg0, String arg1) throws CertificateException {}
                @Override
                public void checkServerTrusted(X509Certificate[] arg0, String arg1) throws CertificateException {}
            };
            ctx.init(null, new TrustManager[] { tm }, null);
            SSLSocketFactory ssf = new SSLSocketFactory(ctx, SSLSocketFactory.ALLOW_ALL_HOSTNAME_VERIFIER);
            SchemeRegistry registry = new SchemeRegistry();
            registry.register(new Scheme("https", 443, ssf));
            ClientConnectionManager ccm = new ConnManagerFactory().newInstance(params, registry);
            return new DefaultHttpClient(ccm, params);
        } catch (Exception ex) {
            ex.printStackTrace();
            return null;
        }
    }
    
    /**
     * Initialises, or re-initialises, the underlying HttpConnectionManager and
     * HttpClient objects a service will use to communicate with an AWS service.
     * If proxy settings are specified in this service's {@link Jets3tProperties} object,
     * these settings will also be passed on to the underlying objects.
     */
    public static HttpClient initHttpConnection(
            final JetS3tRequestAuthorizer requestAuthorizer,
            Jets3tProperties jets3tProperties,
            String userAgentDescription,
            CredentialsProvider credentialsProvider) {
        // Configure HttpClient properties based on Jets3t Properties.
        HttpParams params = createDefaultHttpParams();
        params.setParameter(Jets3tProperties.JETS3T_PROPERTIES_ID, jets3tProperties);

        params.setParameter(
            ClientPNames.CONNECTION_MANAGER_FACTORY_CLASS_NAME,
            jets3tProperties.getStringProperty(
                ClientPNames.CONNECTION_MANAGER_FACTORY_CLASS_NAME,
                ConnManagerFactory.class.getName()));

        HttpConnectionParams.setConnectionTimeout(params,
            jets3tProperties.getIntProperty("httpclient.connection-timeout-ms", 60000));
        HttpConnectionParams.setSoTimeout(params,
            jets3tProperties.getIntProperty("httpclient.socket-timeout-ms", 60000));
        HttpConnectionParams.setStaleCheckingEnabled(params,
            jets3tProperties.getBoolProperty("httpclient.stale-checking-enabled", true));

        // Connection properties to take advantage of S3 window scaling.
        if (jets3tProperties.containsKey("httpclient.socket-receive-buffer")) {
            HttpConnectionParams.setSocketBufferSize(params,
                jets3tProperties.getIntProperty("httpclient.socket-receive-buffer", 0));
        }

        HttpConnectionParams.setTcpNoDelay(params, true);

        // Set user agent string.
        String userAgent = jets3tProperties.getStringProperty("httpclient.useragent", null);
        if (userAgent == null) {
            userAgent = ServiceUtils.getUserAgentDescription(userAgentDescription);
        }
        if (log.isDebugEnabled()) {
            log.debug("Setting user agent string: " + userAgent);
        }
        HttpProtocolParams.setUserAgent(params, userAgent);

        boolean expectContinue
                = jets3tProperties.getBoolProperty("http.protocol.expect-continue", true);
        HttpProtocolParams.setUseExpectContinue(params, expectContinue);

        long connectionManagerTimeout
                = jets3tProperties.getLongProperty("httpclient.connection-manager-timeout", 0);
        ConnManagerParams.setTimeout(params, connectionManagerTimeout);

        DefaultHttpClient httpClient = new DefaultHttpClient(params);
        httpClient.setHttpRequestRetryHandler(
            new JetS3tRetryHandler(
                jets3tProperties.getIntProperty("httpclient.retry-max", 5), requestAuthorizer));

        if (credentialsProvider != null) {
            if (log.isDebugEnabled()) {
                log.debug("Using credentials provider class: "
                        + credentialsProvider.getClass().getName());
            }
            httpClient.setCredentialsProvider(credentialsProvider);
            if (jets3tProperties.getBoolProperty(
                    "httpclient.authentication-preemptive",
                    false)) {
                // Add as the very first interceptor in the protocol chain
                httpClient.addRequestInterceptor(new PreemptiveInterceptor(), 0);
            }
        }

        return httpClient;
    }

    /**
     * Calculates a time offset value to reflect the time difference between your
     * computer's clock and the current time according to an AWS server, and
     * returns the calculated time difference.
     *
     * Ideally you should not rely on this method to overcome clock-related
     * disagreements between your computer and AWS. If you computer is set
     * to update its clock periodically and has the correct timezone setting
     * you should never have to resort to this work-around.
     */
    public static long getAWSTimeAdjustment() throws IOException, S3ServiceException, ParseException {
        RestS3Service restService = new RestS3Service(null);
        HttpClient client = restService.getHttpClient();
        long timeOffset = 0;

        // Connect to an AWS server to obtain response headers.
        HttpGet getMethod = new HttpGet("http://aws.amazon.com/");
        HttpResponse result = client.execute(getMethod);

        if (result.getStatusLine().getStatusCode() == 200) {
            Header dateHeader = result.getHeaders("Date")[0];
            // Retrieve the time according to AWS, based on the Date header
            Date awsTime = ServiceUtils.parseRfc822Date(dateHeader.getValue());

            // Calculate the difference between the current time according to AWS,
            // and the current time according to your computer's clock.
            Date localTime = new Date();
            timeOffset = awsTime.getTime() - localTime.getTime();

            if (log.isDebugEnabled()) {
                log.debug("Calculated time offset value of " + timeOffset +
                        " milliseconds between the local machine and an AWS server");
            }
        } else {
            if (log.isWarnEnabled()) {
                log.warn("Unable to calculate value of time offset between the "
                    + "local machine and AWS server");
            }
        }

        return timeOffset;
    }

    public static Map<String, String> convertHeadersToMap(Header[] headers) {
        Map<String, String> s3Headers = new HashMap<String, String>();
        for (Header header: headers) {
            s3Headers.put(header.getName(), header.getValue());
        }
        return s3Headers;
    }

    /**
     * Default Http parameters got from the DefaultHttpClient implementation.
     *
     * @return
     * Default HTTP connection parameters
     */
    public static HttpParams createDefaultHttpParams() {
        HttpParams params = new SyncBasicHttpParams();
        HttpProtocolParams.setVersion(params, HttpVersion.HTTP_1_1);
        HttpProtocolParams.setContentCharset(params,
                HTTP.DEFAULT_CONTENT_CHARSET);
        HttpConnectionParams.setTcpNoDelay(params, true);
        HttpConnectionParams.setSocketBufferSize(params, 8192);

        // determine the release version from packaged version info
        final VersionInfo vi = VersionInfo.loadVersionInfo("org.apache.http.client",
                HttpClient.class.getClassLoader());
        final String release = (vi != null)
                ? vi.getRelease()
                : VersionInfo.UNAVAILABLE;
        HttpProtocolParams.setUserAgent(params, "Apache-HttpClient/" + release
                + " (java 1.5)");

        return params;
    }

    /**
     * A ClientConnectionManagerFactory that creates ThreadSafeClientConnManager
     */
    public static class ConnManagerFactory implements
            ClientConnectionManagerFactory {
        /*
         * @see ClientConnectionManagerFactory#newInstance(HttpParams, SchemeRegistry)
         */
        public ClientConnectionManager newInstance(HttpParams params,
                SchemeRegistry schemeRegistry) {
            return new ThreadSafeConnManager(params, schemeRegistry);
        }

    } //ConnManagerFactory

    /**
     * ThreadSafeConnManager is a ThreadSafeClientConnManager configured via
     * jets3tProperties.
     *
     * @see Jets3tProperties#JETS3T_PROPERTIES_ID
     */
    public static class ThreadSafeConnManager extends
            ThreadSafeClientConnManager {
        public ThreadSafeConnManager(final HttpParams params,
                final SchemeRegistry schreg) {
            super(params, schreg);
        }

        @Override
        protected AbstractConnPool createConnectionPool(final HttpParams params) {
            // Set the maximum connections per host for the HTTP connection manager,
            // *and* also set the maximum number of total connections (new in 0.7.1).
            // The max connections per host setting is made the same value as the max
            // global connections if there is no per-host property.
            Jets3tProperties props = (Jets3tProperties) params.getParameter(
                    Jets3tProperties.JETS3T_PROPERTIES_ID);
            int maxConn = 20;
            int maxConnectionsPerHost = 0;
            if (props != null) {
                maxConn = props.getIntProperty("httpclient.max-connections", 20);
                maxConnectionsPerHost = props.getIntProperty(
                        "httpclient.max-connections-per-host",
                        0);
            }
            if (maxConnectionsPerHost == 0) {
                maxConnectionsPerHost = maxConn;
            }
            connPerRoute.setDefaultMaxPerRoute(maxConnectionsPerHost);
            return new ConnPoolByRoute(connOperator, connPerRoute, maxConn);
        }
    } //ThreadSafeConnManager

    public static class JetS3tRetryHandler extends DefaultHttpRequestRetryHandler {
        private final JetS3tRequestAuthorizer requestAuthorizer;

        public JetS3tRetryHandler(int pRetryMaxCount, JetS3tRequestAuthorizer requestAuthorizer) {
            super(pRetryMaxCount, false);
            this.requestAuthorizer = requestAuthorizer;
        }

        @Override
        public boolean retryRequest(IOException exception,
                int executionCount,
                HttpContext context) {
            if (super.retryRequest(exception, executionCount, context)){

                if (exception instanceof UnrecoverableIOException) {
                    if (log.isDebugEnabled()) {
                        log.debug("Deliberate interruption, will not retry");
                    }
                    return false;
                }
                HttpRequest request = (HttpRequest) context.getAttribute(
                        ExecutionContext.HTTP_REQUEST);

                // Convert RequestWrapper to original HttpBaseRequest (issue #127)
                if (request instanceof RequestWrapper) {
                    request = ((RequestWrapper)request).getOriginal();
                }

                if (!(request instanceof HttpRequestBase)) {
                    return false;
                }
                HttpRequestBase method = (HttpRequestBase) request;

                // Release underlying connection so we will get a new one (hopefully) when we retry.
                HttpConnection conn = (HttpConnection) context.getAttribute(
                        ExecutionContext.HTTP_CONNECTION);
                try {
                    conn.close();
                } catch (Exception e) {
                    //ignore
                }

                if (log.isDebugEnabled()) {
                    log.debug("Retrying " + method.getMethod()
                            + " request with path '" + method.getURI()
                            + "' - attempt " + executionCount + " of "
                            + getRetryCount());
                }

                // Build the authorization string for the method.
                try {
                    if (requestAuthorizer != null){
                        requestAuthorizer.authorizeHttpRequest(method, context);
                    }
                    return true; // request OK'd for retry by base handler and myself
                } catch (Exception e) {
                    if (log.isWarnEnabled()) {
                        log.warn("Unable to generate updated authorization string for retried request",
                                e);
                    }
                }
            }

            return false;
        }
    } //AWSRetryHandler

    /**
     * PreemptiveInterceptor
     */
    // A preemptive interceptor (copied from doc).
    private static class PreemptiveInterceptor implements
            HttpRequestInterceptor {

        public void process(final HttpRequest request, final HttpContext context) {
            AuthState authState = (AuthState) context.getAttribute(
                    ClientContext.TARGET_AUTH_STATE);
            CredentialsProvider credsProvider = (CredentialsProvider) context.getAttribute(
                    ClientContext.CREDS_PROVIDER);
            HttpHost targetHost = (HttpHost) context.getAttribute(
                    ExecutionContext.HTTP_TARGET_HOST);
            // If not auth scheme has been initialized yet
            if (authState.getAuthScheme() == null) {
                AuthScope authScope = new AuthScope(targetHost.getHostName(),
                        targetHost.getPort());
                // Obtain credentials matching the target host
                Credentials creds = credsProvider.getCredentials(authScope);
                // If found, generate BasicScheme preemptively
                if (creds != null) {
                    authState.setAuthScheme(new BasicScheme());
                    authState.setCredentials(creds);
                }
            }
        }
    } //PreemptiveInterceptor
}
