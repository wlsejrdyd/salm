package kr.salm.core.util;

import org.owasp.encoder.Encode;

public class HtmlSanitizer {
    
    public static String sanitize(String input) {
        if (input == null) return null;
        return Encode.forHtml(input);
    }

    public static String sanitizeContent(String input) {
        if (input == null) return null;
        return input.replaceAll("<script[^>]*>.*?</script>", "")
                   .replaceAll("javascript:", "")
                   .replaceAll("on\\w+\\s*=", "");
    }
}
