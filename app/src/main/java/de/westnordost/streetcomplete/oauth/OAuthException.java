package de.westnordost.streetcomplete.oauth;

import android.annotation.TargetApi;
import android.os.Build;
import android.webkit.WebResourceError;
import android.webkit.WebResourceRequest;
import android.webkit.WebResourceResponse;

import java.util.Locale;

import de.westnordost.streetcomplete.tools.ApiHelper;

class OAuthException extends Exception {
    OAuthException(int errorCode, String description, String failingUrl) {
        super(String.format(Locale.US, "Code=%d, reason=%s, url=%s", errorCode, description, failingUrl));
    }

    OAuthException(WebResourceRequest request, WebResourceResponse error) {
        super(formatErrorStringApiAware(request, error));
    }

    @TargetApi(Build.VERSION_CODES.M)
    OAuthException(WebResourceRequest request, WebResourceError error) {
        super(formatErrorString(error.getErrorCode(), error.getDescription().toString(), request.getUrl().toString()));
    }

    private static String formatErrorStringApiAware(WebResourceRequest request, WebResourceResponse errorResponse) {
        if (ApiHelper.hasLolliPop()) {
            return formatErrorString(errorResponse.getStatusCode(), errorResponse.getReasonPhrase(), request.getUrl().toString());
        } else {
            return errorResponse.toString();
        }
    }

    private static String formatErrorString(int code, String reason, String url) {
        return "Code=" + code + ", Reason=" + reason + ", Url=" + url;
    }

}
