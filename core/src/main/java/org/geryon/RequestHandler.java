package org.geryon;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.function.Function;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * @author Gabriel Francisco <gabfssilva@gmail.com>
 */
public class RequestHandler {
    private static final Pattern PATTERN_PATH_PARAM = Pattern.compile("^?(:(.)+/|:(.)+)");

    private String method;
    private String path;
    private String produces;
    private Function<Request, CompletableFuture<?>> func;
    private Function<Request, Boolean> matcher;
    private List<String> wantedPathParameters;
    private String pathAsPattern;
    private Map<String, String> defaultHeaders;

    public RequestHandler(String produces, Function<Request, CompletableFuture<?>> func) {
        this.produces = produces;
        this.func = func;
        this.matcher = AlwaysAllowMatcher.MATCHER;
    }

    public RequestHandler(String method, String path, String produces, Function<Request, CompletableFuture<?>> func, Function<Request, Boolean> matcher, Map<String, String> defaultHeaders) {
        this.method = method;
        this.path = path;
        this.produces = produces;
        this.func = func;
        this.matcher = matcher == null ? AlwaysAllowMatcher.MATCHER : matcher;
        this.wantedPathParameters = extractWantedPathParameters();
        this.pathAsPattern = extractPathAsPattern();
        this.defaultHeaders = defaultHeaders;
    }

    public String method() {
        return method;
    }

    public String path() {
        return path;
    }

    public String produces() {
        return produces;
    }

    public Function<Request, CompletableFuture<?>> func() {
        return func;
    }

    public Function<Request, Boolean> matcher() {
        return matcher;
    }

    public List<String> wantedPathParameters() {
        return wantedPathParameters;
    }

    public String pathAsPattern() {
        return pathAsPattern;
    }

    public Map<String, String> defaultHeaders() {
        return this.defaultHeaders;
    }

    private List<String> extractWantedPathParameters() {
        final Matcher matcher = PATTERN_PATH_PARAM.matcher(path);

        List<String> fields = new ArrayList<>();

        while (matcher.find()) {
            String group;

            if (matcher.group(1).contains("/")) {
                group = matcher.group(1).substring(0, matcher.group(1).indexOf("/")).replace(":", "");
            } else {
                group = matcher.group(1).replace(":", "");
            }

            fields.add(group);
        }

        return fields;
    }

    private String extractPathAsPattern() {
        String pathAsPattern = path;

        for (String f : wantedPathParameters) {
            pathAsPattern = pathAsPattern.replace("/:" + f, "(/.+)");
        }

        return pathAsPattern;
    }
}
