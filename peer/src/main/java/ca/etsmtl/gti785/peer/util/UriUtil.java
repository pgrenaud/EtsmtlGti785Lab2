package ca.etsmtl.gti785.peer.util;

import android.content.Context;
import android.net.Uri;
import android.os.Environment;
import android.provider.DocumentsContract;

import java.io.File;

public class UriUtil {

    // See: http://stackoverflow.com/a/29789495
    // See: https://gist.github.com/asifmujteba/d89ba9074bc941de1eaa
    public static String getDocumentPath(Context context, Uri uri) {
        Uri documentUri = DocumentsContract.buildDocumentUriUsingTree(uri, DocumentsContract.getTreeDocumentId(uri));

        // ExternalStorageProvider
        if ("com.android.externalstorage.documents".equals(documentUri.getAuthority())) {
            String docId = DocumentsContract.getDocumentId(documentUri);
            String[] split = docId.split(":");
            String type = split[0];

            // Primary storage
            if ("primary".equalsIgnoreCase(type)) {
                if (split.length < 2) {
                    return Environment.getExternalStorageDirectory().getPath();
                } else {
                    return new File(Environment.getExternalStorageDirectory(), split[1]).getPath();
                }
            }

            // TODO handle non-primary storage
        }

        // TODO: Handle other DocumentProvider

        return null;
    }
}
