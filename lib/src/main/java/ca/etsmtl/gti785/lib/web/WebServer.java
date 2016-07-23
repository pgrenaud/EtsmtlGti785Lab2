package ca.etsmtl.gti785.lib.web;

import android.util.Log;

import com.google.gson.Gson;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import ca.etsmtl.gti785.lib.entity.EventEntity;
import ca.etsmtl.gti785.lib.handler.RequestHandler;
import fi.iki.elonen.NanoHTTPD;

// TODO: Split this class into two, one for base methods and the other for routing
public class WebServer extends NanoHTTPD {

    public static final String MIME_JSON = "application/json";
    private static final String MIME_STREAM = "application/octet-stream";

    public static final Pattern FILE_REQUEST_URL_PATTERN = Pattern.compile("^/api/v1/file/([a-zA-Z0-9-]+)$");
    public static final Pattern PEER_POLLING_URL_PATTERN = Pattern.compile("^/api/v1/polling/([a-zA-Z0-9-]+)$");

    private final RequestHandler handler;

    public WebServer(int port, RequestHandler handler) {
        super(port);

        this.handler = handler;
    }

    @Override
    public Response serve(IHTTPSession session) {
        Method method = session.getMethod();
        String URI = session.getUri();

        if (method.equals(Method.GET)) {
            if (URI.equals("/api/v1/debug")) {
                return sendDebug(session);
//            } else if (URI.equals("/api/v1/command")) {
//                return handleCommand(session);
            } else if (URI.equals("/api/v1/error")) {
                return sendServerError("An error occurred.");
            } else if (URI.equals("/api/v1/empty")) {
                return sendEmpty();
            } else if (URI.equals("/api/v1/timeout")) {
                return sendTimeout();
            } else if (URI.equals("/api/v1/ping")) {
                return handlePing(session);
            } else if (URI.startsWith("/api/v1/polling") && PEER_POLLING_URL_PATTERN.matcher(URI).matches()) {
                return handlePolling(session);
            } else if (URI.equals("/api/v1/files")) {
                return handleFileList(session);
            } else if (URI.startsWith("/api/v1/file/") && FILE_REQUEST_URL_PATTERN.matcher(URI).matches()) {
                return handleFileRequest(session);
            } else {
                return sendError("Invalid URI (unknown API endpoint).");
            }
        } if (method.equals(Method.POST)) {
            if (URI.equals("/api/v1/debug")) {
                return sendDebug(session);
            } else {
                return sendError("Invalid URI (unknown API endpoint).");
            }
        } else {
            return sendError("Invalid HTTP method (expected GET or POST).");
        }
    }

//    public Response handleCommand(IHTTPSession session) {
//        String body;
//        try {
//            body = getBody(session);
//        } catch (IOException ioe) {
//            return sendServerError("SERVER INTERNAL ERROR: IOException: " + ioe.getMessage());
//        } catch (ResponseException re) {
//            return newFixedLengthResponse(re.getStatus(), NanoHTTPD.MIME_PLAINTEXT, re.getMessage());
//        }
//
//        if (!isValidJson(body)) {
//            return sendError("Invalid body (expected valid JSON string).");
//        }
//
//        Command command;
//        try {
//            command = Command.decode(body);
//        } catch (JsonSyntaxException e) {
//            return sendError("Invalid Command object.");
//        }
//
//        // Append IpAddress to Command (required by Subscribe)
//        command.getParams().setIpAddress(session.getRemoteIpAddress());
//
//        return commandHandler.handle(command);
//    }

    private Response handlePing(IHTTPSession session) {
        return sendOk("pong");
    }

    private Response handlePolling(IHTTPSession session) {
        String param = getRouteParam(session, PEER_POLLING_URL_PATTERN, 1);

        if (param != null) {
            try {
                UUID uuid = UUID.fromString(param);

                return handler.handlePolling(uuid);
            } catch (IllegalArgumentException e) {
                return sendError("Invalid UUID.");
            }
        }

        return sendError("Invalid URI (UUID is missing).");
    }

    private Response handleFileList(IHTTPSession session) {
        return handler.handleFileList();
    }

    private Response handleFileRequest(IHTTPSession session) {
        String param = getRouteParam(session, FILE_REQUEST_URL_PATTERN, 1);

        if (param != null) {
            try {
                UUID uuid = UUID.fromString(param);

                return handler.handleFileRequest(uuid);
            } catch (IllegalArgumentException e) {
                return sendError("Invalid UUID.");
            }
        }

        return sendError("Invalid URI (UUID is missing).");
    }

    public static Response sendOk(String message) {
        try {
            JSONObject json = new JSONObject();
            json.put("message", message);
            return newFixedLengthResponse(Response.Status.OK, MIME_JSON, json.toString());
        } catch (JSONException e) {
            return sendServerError("SERVER INTERNAL ERROR: JSONException: " + e.getMessage());
        }
    }

    // FIXME: Remove that thing
    public static Response sendJSON(Object object) {
        Gson gson = new Gson();

        return newFixedLengthResponse(Response.Status.OK, MIME_JSON, gson.toJson(object));
    }

    public static Response sendStream(File file) {
        try {
            FileInputStream fis = new FileInputStream(file);

            return newFixedLengthResponse(Response.Status.OK, getMimeTypeForFile(file.getName()), fis, file.length());
        } catch (FileNotFoundException e) {
           return sendServerError("SERVER INTERNAL ERROR: FileNotFoundException: " + e.getMessage());
        }
    }

    public static Response sendEvent(EventEntity event) {
        return newFixedLengthResponse(Response.Status.OK, MIME_JSON, event.encode());
    }

    public static Response sendTimeout() {
        try {
            JSONObject json = new JSONObject();
            json.put("message", "Request timeout.");
            return newFixedLengthResponse(Response.Status.REQUEST_TIMEOUT, MIME_JSON, json.toString());
        } catch (JSONException e) {
            return sendServerError("SERVER INTERNAL ERROR: JSONException: " + e.getMessage());
        }
    }

    public static Response sendError(String message) {
        try {
            JSONObject json = new JSONObject();
            json.put("message", message);
            return newFixedLengthResponse(Response.Status.BAD_REQUEST, MIME_JSON, json.toString());
        } catch (JSONException e) {
            return sendServerError("SERVER INTERNAL ERROR: JSONException: " + e.getMessage());
        }
    }

    public static Response sendServerError(String message) {
        return newFixedLengthResponse(Response.Status.INTERNAL_ERROR, NanoHTTPD.MIME_PLAINTEXT, message);
    }

    public static Response sendEmpty() {
        return newFixedLengthResponse(Response.Status.NO_CONTENT, null, null);
    }

    public static Response sendDebug(IHTTPSession session) {
        String body;
        try {
            body = getBody(session);
        } catch (IOException ioe) {
            return sendServerError("SERVER INTERNAL ERROR: IOException: " + ioe.getMessage());
        } catch (ResponseException re) {
            return newFixedLengthResponse(re.getStatus(), NanoHTTPD.MIME_PLAINTEXT, re.getMessage());
        }

        String str = "";

        str += "Hostname: " + session.getRemoteHostName() + "\n";
        str += "IPv4Addr: " + session.getRemoteIpAddress() + "\n";

        if (isValidJson(body)) {
            str += "JSONBody: Body is a valid JSON string" + "\n";
        } else  {
            str += "JSONBody: Body is NOT a valid JSON string" + "\n";
        }

        str += "\n";
        str += session.getMethod() + " " + session.getUri()+ "\n";

        for (Map.Entry<String, String> header : session.getHeaders().entrySet()) {
            str += header.getKey() + ": " + header.getValue() + "\n";
        }

        str += "\n";
        str += body + "\n";

        return newFixedLengthResponse(Response.Status.OK, NanoHTTPD.MIME_PLAINTEXT, str);
    }

    public static String getBody(IHTTPSession session) throws IOException, ResponseException {
        Method method = session.getMethod();
        Map<String, String> files = new HashMap<>();
        String body = "";

        if (method.equals(Method.PUT) || method.equals(Method.POST)) {
            session.parseBody(files);

            if (files.containsKey("postData")) {
                body = files.get("postData");
                Log.d("WebServer", "body: " + body);
            }
        }

        return body;
    }

    public static boolean isValidJson(String str) {
        try {
            JSONObject json = new JSONObject(str);
            return true;
        } catch (JSONException e) {
            return false;
        }
    }

    public static String getRouteParam(IHTTPSession session, Pattern pattern, int param) {
        Matcher m = pattern.matcher(session.getUri());

        if (m.find()) {
            if (param > 0 && param <= m.groupCount()) {
                return m.group(param);
            }
        }

        return null;
    }
}
