package asset.pipeline.utils.net


import java.util.regex.Pattern


/**
 * @author Ross Goldberg
 */
final class Urls {

    /**
     * URL starts with either {@code "/"} or a <a href="https://tools.ietf.org/html/std66">URI scheme</a> followed by {@code "/"}
     */
    static final Pattern ABSOLUTE_URL_PATTERN = ~/(?:\/|\p{L}[\p{L}\d+-.]*:[\/]).*/


    /**
     * URL starts with either {@code "/"} or a <a href="https://tools.ietf.org/html/std66">URI scheme</a> followed by {@code "/"}
     */
    static isAbsolute(final String url) {
        ABSOLUTE_URL_PATTERN.matcher(url).matches()
    }

    /**
     * URL does not start with either {@code "/"} or a <a href="https://tools.ietf.org/html/std66">URI scheme</a> followed by {@code "/"}
     */
    static isRelative(final String url) {
        ! isAbsolute(url)
    }


    private Urls() {}
}
