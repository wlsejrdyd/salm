package kr.salm.core.util;

import org.owasp.encoder.Encode;

/**
 * XSS 방지를 위한 HTML Sanitizer
 */
public final class HtmlSanitizer {

    private HtmlSanitizer() {}

    /**
     * HTML 인코딩
     */
    public static String sanitize(String input) {
        if (input == null) return null;
        return Encode.forHtml(input);
    }

    /**
     * HTML 속성값 인코딩
     */
    public static String sanitizeAttribute(String input) {
        if (input == null) return null;
        return Encode.forHtmlAttribute(input);
    }

    /**
     * JavaScript 문자열 인코딩
     */
    public static String sanitizeJs(String input) {
        if (input == null) return null;
        return Encode.forJavaScript(input);
    }

    /**
     * URL 파라미터 인코딩
     */
    public static String sanitizeUrl(String input) {
        if (input == null) return null;
        return Encode.forUriComponent(input);
    }

    /**
     * 스크립트/이벤트 핸들러 제거 (게시글용)
     */
    public static String sanitizeContent(String input) {
        if (input == null) return null;
        
        String result = input;
        
        // script 태그 제거
        result = result.replaceAll("(?i)<script[^>]*>.*?</script>", "");
        
        // 이벤트 핸들러 제거
        result = result.replaceAll("(?i)\\s+on\\w+\\s*=\\s*['\"][^'\"]*['\"]", "");
        result = result.replaceAll("(?i)\\s+on\\w+\\s*=\\s*[^\\s>]+", "");
        
        // javascript: URL 제거
        result = result.replaceAll("(?i)javascript\\s*:", "");
        
        // vbscript: URL 제거
        result = result.replaceAll("(?i)vbscript\\s*:", "");
        
        // data: URL 제거 (이미지 제외)
        result = result.replaceAll("(?i)data\\s*:(?!image/)", "");
        
        return result;
    }
}
