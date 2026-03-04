package chat.jace.config;



import com.google.common.base.Stopwatch;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.InetAddress;
import java.security.SecureRandom;
import java.time.LocalDateTime;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Stream;
import lombok.extern.log4j.Log4j2;
import org.slf4j.MDC;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;
import org.springframework.web.util.ContentCachingRequestWrapper;
import org.springframework.web.util.ContentCachingResponseWrapper;

import static chat.jace.config.DatetimeUtil.normalizeNowDateFormat;

@Log4j2
@Component
public class RequestAndResponseLoggingFilter extends OncePerRequestFilter {
    public static final String KEY_USERNAME = "uid";
    public static final String USRC_KEY = "usrc";
    public static final String REGEX_PATTERN_PKG_ORDER = "(?<!\\+|\\b0)\\b[1-9][0-9]{9}\\b";
    public static final String REGEX_PATTERN_MOBILE_PHONE =
            "[^0-9](0|0084|84|\\+84|\\+84 )([2|3|5|7|8|9])([0-9]{8})[^0-9]";
    public static final String REGEX_PATTERN_ADDRESS =
            "\\b(pick_money|money|need_to_collect|pick_first_address|customer_first_address|customer_last_address|address|address_name|return_last_address|return_first_address|tel|customer_tel|customer_tel2|pick_tel|phone|phone1|phone2|province|district|ward|product|product_name|order)\\b";
    private static final List<MediaType> VISIBLE_TYPES =
            Arrays.asList(
                    MediaType.valueOf("text/*"),
                    MediaType.APPLICATION_FORM_URLENCODED,
                    MediaType.APPLICATION_JSON,
                    MediaType.APPLICATION_XML,
                    MediaType.valueOf("application/*+json"),
                    MediaType.valueOf("application/*+xml"),
                    MediaType.MULTIPART_FORM_DATA);

    /** List of HTTP headers whose values should not be logged. */
    private static final Set<String> SENSITIVE_HEADERS =
            new LinkedHashSet<>(
                    Arrays.asList(
                            "Authorization",
                            "authorization",
                            "proxy-authorization",
                            "Verify-Token",
                            "verify-token",
                            "g-authorization",
                            "G-Authorization",
                            "token",
                            "Token",
                            "cookie",
                            "Cookie",
                            "c-authorization",
                            "C-Authorization"));

    private static final int REQUEST_LOG_TYPE = 0;
    private static final int RESPONSE_LOG_TYPE = 1;
    private final String PREFIX_API = "/xfast/";
    private final SecureRandom random = new SecureRandom();

    @Value("${sensitive-data.log.percentage}")
    private int sensitiveDataLogPercentage;

    private static void logRequestHeader(ContentCachingRequestWrapper request, String prefix) {
        StringBuilder msg = new StringBuilder();
        String queryString = request.getQueryString();
        if (queryString == null) {
            msg.append(String.format("%s %s %s", prefix, request.getMethod(), request.getRequestURI()))
                    .append('\n');
        } else {
            msg.append(
                            String.format(
                                    "%s %s %s?%s", prefix, request.getMethod(), request.getRequestURI(), queryString))
                    .append('\n');
        }
        Collections.list(request.getHeaderNames())
                .forEach(
                        headerName ->
                                Collections.list(request.getHeaders(headerName))
                                        .forEach(
                                                headerValue -> {
                                                    if (isSensitiveHeader(headerName)) {
                                                        msg.append(
                                                                        String.format(
                                                                                "%s %s: %s",
                                                                                prefix, headerName, maskSensitiveData(headerValue)))
                                                                .append('\n');
                                                    } else {
                                                        msg.append(String.format("%s %s: %s", prefix, headerName, headerValue))
                                                                .append('\n');
                                                    }
                                                }));
        MDC.put("headers", msg.toString());
    }

    private static void logRequestBody(ContentCachingRequestWrapper request, String prefix) {
        byte[] content = request.getContentAsByteArray();
        if (content.length > 0) {
            logContent(
                    content,
                    request.getContentType(),
                    request.getCharacterEncoding(),
                    prefix,
                    REQUEST_LOG_TYPE);
        } else {
            // logging GET params
            log.info("> Request params: " + request.getQueryString());
        }
    }

    private static void logResponse(ContentCachingResponseWrapper response, String prefix) {
        int status = response.getStatus();
        MDC.put("http_status", status + "");
        byte[] content = response.getContentAsByteArray();
        if (content.length > 0) {
            logContent(
                    content,
                    response.getContentType(),
                    response.getCharacterEncoding(),
                    prefix,
                    RESPONSE_LOG_TYPE);
        }
    }

    private static String maskSensitiveData(String input) {
        return "";
    }

    private static void logContent(
            byte[] content, String contentType, String contentEncoding, String prefix, int type) {
        String msg = getContent(content, contentType, contentEncoding, prefix);
        log.info(msg);
    }

    /**
     * Determine if a given header name should have its value logged.
     *
     * @param headerName HTTP header name.
     * @return True if the header is sensitive (i.e. its value should <b>not</b> be logged).
     */
    private static boolean isSensitiveHeader(String headerName) {
        return SENSITIVE_HEADERS.contains(headerName.toLowerCase());
    }

    private static ContentCachingRequestWrapper wrapRequest(HttpServletRequest request) {
        if (request instanceof ContentCachingRequestWrapper) {
            return (ContentCachingRequestWrapper) request;
        } else {
            return new ContentCachingRequestWrapper(request);
        }
    }

    private static ContentCachingResponseWrapper wrapResponse(HttpServletResponse response) {
        if (response instanceof ContentCachingResponseWrapper) {
            return (ContentCachingResponseWrapper) response;
        } else {
            return new ContentCachingResponseWrapper(response);
        }
    }

    private static String getContent(
            byte[] content, String contentType, String contentEncoding, String prefix) {

        StringBuilder msg = new StringBuilder();
        if (Objects.isNull(contentType) || contentType.isEmpty()) {
            msg.append(String.format("%s [%d bytes content]", prefix, content.length)).append('\n');
            return msg.toString();
        }
        MediaType mediaType = MediaType.valueOf(contentType);
        boolean visible =
                VISIBLE_TYPES.stream().anyMatch(visibleType -> visibleType.includes(mediaType));
        if (visible) {
            try {
                String contentString = new String(content, contentEncoding);
                Stream.of(contentString.split("\r\n|\r|\n"))
                        .forEach(line -> msg.append(prefix).append(' ').append(line).append('\n'));
            } catch (UnsupportedEncodingException e) {
                msg.append(String.format("%s [%d bytes content]", prefix, content.length)).append('\n');
            }
        } else {
            msg.append(String.format("%s [%d bytes content]", prefix, content.length)).append('\n');
        }
        return msg.toString();
    }

    @Override
    protected void doFilterInternal(
            HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {
        if (isAsyncDispatch(request)) {
            filterChain.doFilter(request, response);
        } else {
            doFilterWrapped(wrapRequest(request), wrapResponse(response), filterChain);
        }
    }

    protected void doFilterWrapped(
            ContentCachingRequestWrapper request,
            ContentCachingResponseWrapper response,
            FilterChain filterChain)
            throws ServletException, IOException {

        Stopwatch watch = Stopwatch.createStarted();
        MDC.put("rqt", LocalDateTime.now().toString());
        try {
            filterChain.doFilter(request, response);
        } finally {
            watch.stop();
            MDC.put("response_time", watch.elapsed().toMillis() + "");
            afterRequest(request, response);
            logDbal(watch, request, response);
            logSensitiveData(watch, request, response);
            response.copyBodyToResponse();
            MDC.clear();
        }
    }

    private void logDbal(
            Stopwatch stopwatch,
            ContentCachingRequestWrapper request,
            ContentCachingResponseWrapper response) {
        try {
            Map<String, Object> map = new HashMap();
            map.put("lt", "dbal");
            map.put("urp", request.getRequestURI());
            map.put("urq", request.getQueryString() == null ? "" : request.getQueryString());
            map.put("url", request.getRequestURL().toString());
            map.put("rt", (stopwatch.elapsed().toMillis() / 1000.0));
            map.put("st", response.getStatus());
            map.put("mt", request.getMethod());
            map.put("rmip", request.getHeader("x-real-ip") == null ? "" : request.getHeader("x-real-ip"));
            map.put(
                    "cip",
                    request.getHeader("x-origin-client-ip") == null
                            ? ""
                            : request.getHeader("x-origin-client-ip"));
            map.put("bbs", response.getContentAsByteArray().length);
            map.put("cl", request.getContentLength());
            map.put("rf", request.getHeader("referer") == null ? "" : request.getHeader("referer"));
            map.put("ua", request.getHeader("user-agent"));
            map.put("host", request.getHeader("host"));
            map.put("tl", normalizeNowDateFormat());
            map.put("rid", "");
            map.put("uid", getAuthUserId());
            map.put(
                    "usrc",
                    request.getAttribute(USRC_KEY) == null
                            ? "anonymous"
                            : request.getAttribute(USRC_KEY).toString());
            map.put("gAppId", request.getHeader("gAppId") == null ? "" : request.getHeader("gAppId"));
            log.info(map);
        } catch (Exception ignore) {
        }
    }

    protected void afterRequest(
            ContentCachingRequestWrapper request, ContentCachingResponseWrapper response) {
        if (log.isInfoEnabled()) {
            logRequestHeader(request, "");
            MDC.put("endpoint", request.getRequestURI());
            logRequestBody(request, "");
            MDC.remove("headers");
            logResponse(response, "");
        }
    }

    private boolean checkNeedToLog(HttpServletRequest request) {
        return request.getRequestURI().startsWith("/api/");
    }

    private void logSensitiveData(
            Stopwatch stopwatch,
            ContentCachingRequestWrapper request,
            ContentCachingResponseWrapper response) {
        try {
            Map<String, Object> map = new HashMap();
            String resContent =
                    getContent(
                            response.getContentAsByteArray(),
                            response.getContentType(),
                            response.getCharacterEncoding(),
                            "");
            Set<String> sensitiveKeys = getSensitiveKeys(resContent);
            String ghtkId = null;
            try {
                ghtkId = getAuthUserId();
            } catch (Exception ignore) {
            }

            int randomValue = random.nextInt(100);
            if (Objects.nonNull(ghtkId)
                    && !Objects.equals(ghtkId, 0L)
                    && !sensitiveKeys.isEmpty()
                    && randomValue < sensitiveDataLogPercentage) {
                map.put("urp", request.getRequestURI());
                map.put("urq", request.getQueryString() == null ? "" : request.getQueryString());
                map.put("url", request.getRequestURL().toString());
                map.put("rt", (stopwatch.elapsed().toMillis() / 1000.0));
                map.put("st", response.getStatus());
                map.put("mt", request.getMethod());
                map.put(
                        "rmip", request.getHeader("x-real-ip") == null ? "" : request.getHeader("x-real-ip"));
                map.put(
                        "cip",
                        request.getHeader("x-origin-client-ip") == null
                                ? ""
                                : request.getHeader("x-origin-client-ip"));
                map.put("bbs", response.getContentAsByteArray().length);
                map.put("cl", request.getContentLength());
                map.put("rf", request.getHeader("referer") == null ? "" : request.getHeader("referer"));
                map.put("ua", request.getHeader("user-agent"));
                map.put("host", request.getHeader("host"));
                map.put(
                        "sn",
                        InetAddress.getLocalHost().getHostName() == null
                                ? ""
                                : InetAddress.getLocalHost().getHostName());
                map.put("tl", normalizeNowDateFormat());
                map.put("rid", "");
                map.put("uid", ghtkId);
                map.put(
                        "usrc",
                        request.getAttribute(USRC_KEY) == null
                                ? "anonymous"
                                : request.getAttribute(USRC_KEY).toString());
                map.put("rqt", MDC.get("rqt"));
                map.put("lt", "sensitive");
                map.put("tags", sensitiveKeys);
                map.put("xss", request.getHeader("X-Screen-Src") == null ? "" : request.getHeader("X-Screen-Src"));
                map.put("xcs", request.getHeader("X-Client-Source") == null ? "" : request.getHeader("X-Client-Source"));

                log.info(map);
            }
        } catch (Exception e) {
            log.error("> error in logSensitiveData:", e);
        }
    }

    private Set<String> getSensitiveKeys(String resContent) {
        return new HashSet<>(extractDataFromStr(resContent, REGEX_PATTERN_ADDRESS));
    }

    private List<String> extractDataFromStr(String strPattern, String patternRegex) {

        if (Objects.isNull(strPattern) || Objects.isNull(patternRegex)) {
            return Collections.emptyList();
        }
        Pattern pattern = Pattern.compile(patternRegex);
        Matcher matcher = pattern.matcher(strPattern);
        List<String> foundNumbers = new ArrayList<>();

        while (matcher.find()) {
            foundNumbers.add(matcher.group());
        }
        return foundNumbers;
    }

    private boolean isMatcher(String strPattern, String patternRegex) {
        if (Objects.isNull(strPattern) || Objects.isNull(patternRegex)) {
            return false;
        }

        Pattern pattern = Pattern.compile(patternRegex);
        Matcher matcher = pattern.matcher(strPattern);
        return matcher.find();
    }

    private String getAuthUserId() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth != null && auth.getName() != null) {
            return auth.getName();
        }

        return "";
    }
}
