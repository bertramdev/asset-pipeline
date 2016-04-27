package asset.pipeline.grails.utils.text;


/**
 * @author Ross Goldberg
 */
public final class StringBuilders {

    public static StringBuilder ensureEndsWith(final StringBuilder sb, final char suffix) {
        final int len = sb.length();
        return
            len != 0 && sb.charAt(len - 1) == suffix
                ? sb
                : sb.append(suffix)
        ;
    }


    public static StringBuilder from(final Object o) {
        final String s;
        return
            o instanceof CharSequence
                ? from((CharSequence) o)
                : o == null
                    ? null
                    : new StringBuilder((s = o.toString()).length() * 2).append(s)
        ;
    }

    public static StringBuilder from(final CharSequence cs) {
        return
            cs instanceof StringBuilder
                ? (StringBuilder) cs
                : cs == null
                    ? null
                    : new StringBuilder(cs.length() * 2).append(cs)
        ;
    }


    private StringBuilders() {}
}
