package ca.etsmtl.gti785.lib.web;

import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.URISyntaxException;

import cz.msebera.android.httpclient.HttpEntity;
import cz.msebera.android.httpclient.client.methods.CloseableHttpResponse;
import cz.msebera.android.httpclient.client.methods.HttpGet;
import cz.msebera.android.httpclient.client.utils.URIBuilder;
import cz.msebera.android.httpclient.impl.client.CloseableHttpClient;
import cz.msebera.android.httpclient.impl.client.HttpClients;
import cz.msebera.android.httpclient.util.EntityUtils;

public class HttpClientWrapper {

    private final CloseableHttpClient client;

    private String host;

    public HttpClientWrapper(String host) {
        this.host = host;

        client = HttpClients.createDefault();
    }

    public void performHttpGet(String path, HttpResponseCallback callback) {
        try {
            URI uri = new URIBuilder()
                .setScheme("http")
                .setHost(host)
                .setPath(path)
                .build();

            HttpGet get = new HttpGet(uri);
            CloseableHttpResponse response = client.execute(get);

            int status = response.getStatusLine().getStatusCode();

            try {
                HttpEntity entity = response.getEntity();
                String json = EntityUtils.toString(entity);
                EntityUtils.consume(entity);

                callback.onHttpResponse(status, json);
            } finally {
                response.close();
            }
        } catch (IOException | URISyntaxException e) {
            callback.onException(e);
        }
    }

    public void performBinaryHttpGet(String path, BinaryHttpResponseCallback callback) {
        try {
            URI uri = new URIBuilder()
                .setScheme("http")
                .setHost(host)
                .setPath(path)
                .build();

            HttpGet get = new HttpGet(uri);
            CloseableHttpResponse response = client.execute(get);

            int status = response.getStatusLine().getStatusCode();

            try {
                HttpEntity entity = response.getEntity();

                callback.onHttpResponse(status, entity.getContent());

                EntityUtils.consume(entity);
            } finally {
                response.close();
            }
        } catch (IOException | URISyntaxException e) {
            callback.onException(e);
        }
    }

    public void close() throws IOException {
        client.close();
    }

    public interface HttpResponseCallback {
        void onHttpResponse(int status, String content);

        /**
         *
         * @param exception Can be either a IOException or a URISyntaxException.
         */
        void onException(Exception exception);
    }

    public interface BinaryHttpResponseCallback {
        void onHttpResponse(int status, InputStream is);

        /**
         *
         * @param exception Can be either a IOException or a URISyntaxException.
         */
        void onException(Exception exception);
    }
}
