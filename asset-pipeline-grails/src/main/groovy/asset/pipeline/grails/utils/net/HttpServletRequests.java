package asset.pipeline.grails.utils.net;


import javax.servlet.http.HttpServletRequest;

import static asset.pipeline.utils.net.Urls.getDefaultPort;
import static asset.pipeline.utils.net.Urls.hasAuthority;
import static org.grails.web.util.WebUtils.getForwardURI;


/**
 * @author Ross Goldberg
 */
public final class HttpServletRequests {

	public static String getScheme(final HttpServletRequest req) {
		final String forwardedScheme = req.getHeader("x-forwarded-proto");
		return
			forwardedScheme == null
				? req.getScheme()
				: forwardedScheme
		;
	}


	public static int getPort(final HttpServletRequest req) {
		final String forwardedPort = req.getHeader("x-forwarded-port");
		return
			forwardedPort == null
				? req.getServerPort()
				: Integer.parseInt(forwardedPort)
		;
	}


	public static StringBuilder getAuthorityUrl(final HttpServletRequest req, final Object o) {
		return
			o == null
				? getAuthorityUrlWithScheme(req)
				: o instanceof Boolean
					? getAuthorityUrl(req, ((Boolean) o).booleanValue())
					: getAuthorityUrlWithScheme(req, o.toString())
		;
	}

	public static StringBuilder getAuthorityUrl(final HttpServletRequest req, final boolean retainScheme) {
		return
			retainScheme
				? getAuthorityUrlWithScheme(req)
				: getAuthorityUrlSansScheme(req)
		;
	}

	public static StringBuilder getAuthorityUrlWithScheme(final HttpServletRequest req) {
		return getAuthorityUrlWithScheme(req, getScheme(req), true);
	}

	public static StringBuilder getAuthorityUrlWithScheme(final HttpServletRequest req, final String scheme) {
		return getAuthorityUrlWithScheme(req, scheme, getScheme(req).equals(scheme));
	}

	public static StringBuilder getAuthorityUrlWithScheme(final HttpServletRequest req, final String scheme, final boolean includeNonDefaultPort) {
		return getAuthorityUrlSansScheme(req, includeNonDefaultPort, new StringBuilder().append(scheme).append(':'));
	}

	public static StringBuilder getAuthorityUrlSansScheme(final HttpServletRequest req) {
		return getAuthorityUrlSansScheme(req, false);
	}

	public static StringBuilder getAuthorityUrlSansScheme(final HttpServletRequest req, final String schemeToIncludeNonDefaultPort) {
		return getAuthorityUrlSansScheme(req, getScheme(req).equals(schemeToIncludeNonDefaultPort));
	}

	public static StringBuilder getAuthorityUrlSansScheme(final HttpServletRequest req, final boolean includeNonDefaultPort) {
		return getAuthorityUrlSansScheme(req, includeNonDefaultPort, new StringBuilder());
	}

	private static StringBuilder getAuthorityUrlSansScheme(final HttpServletRequest req, final boolean includeNonDefaultPort, final StringBuilder urlSb) {
		urlSb.append("//").append(req.getServerName());

		if (includeNonDefaultPort) {
			final int port = getPort(req);

			if (
				port >= 0 &&
				port != getDefaultPort(getScheme(req))
			) {
				urlSb.append(':').append(port);
			}
		}

		return urlSb;
	}


	public static StringBuilder getBaseUrl(final HttpServletRequest req, final Object o) {
		return
			o == null
				? getBaseUrlWithScheme(req)
				: o instanceof Boolean
					? getBaseUrl(req, ((Boolean) o).booleanValue())
					: getBaseUrlWithScheme(req, o.toString())
		;
	}

	public static StringBuilder getBaseUrl(final HttpServletRequest req, final boolean retainScheme) {
		return
			retainScheme
				? getBaseUrlWithScheme(req)
				: getBaseUrlSansScheme(req)
		;
	}

	public static StringBuilder getBaseUrlWithScheme(final HttpServletRequest req) {
		return getBaseUrlWithScheme(req, getScheme(req), true);
	}

	public static StringBuilder getBaseUrlWithScheme(final HttpServletRequest req, final String scheme) {
		return getBaseUrlWithScheme(req, scheme, getScheme(req).equals(scheme));
	}

	public static StringBuilder getBaseUrlWithScheme(final HttpServletRequest req, final String scheme, final boolean includeNonDefaultPort) {
		return getBaseUrlSansScheme(req, includeNonDefaultPort, new StringBuilder().append(scheme).append(':'));
	}

	public static StringBuilder getBaseUrlSansScheme(final HttpServletRequest req) {
		return getBaseUrlSansScheme(req, false);
	}

	public static StringBuilder getBaseUrlSansScheme(final HttpServletRequest req, final String schemeToIncludeNonDefaultPort) {
		return getBaseUrlSansScheme(req, getScheme(req).equals(schemeToIncludeNonDefaultPort));
	}

	public static StringBuilder getBaseUrlSansScheme(final HttpServletRequest req, final boolean includeNonDefaultPort) {
		return getBaseUrlSansScheme(req, includeNonDefaultPort, new StringBuilder());
	}

	private static StringBuilder getBaseUrlSansScheme(final HttpServletRequest req, final boolean includeNonDefaultPort, final StringBuilder urlSb) {
		return getAuthorityUrlSansScheme(req, includeNonDefaultPort, urlSb).append(req.getContextPath());
	}


	public static String prependBaseUrlWithScheme(final HttpServletRequest req, final String url) {
		return
			hasAuthority(url)
				? url
				: getBaseUrlWithScheme(req).append(url).toString()
		;
	}


	public static StringBuilder getForwardUrl(final HttpServletRequest req, final Object o) {
		return
			o == null
				? getForwardUrlWithScheme(req)
				: o instanceof Boolean
					? getForwardUrl(req, ((Boolean) o).booleanValue())
					: getForwardUrlWithScheme(req, o.toString())
		;
	}

	public static StringBuilder getForwardUrl(final HttpServletRequest req, final boolean retainScheme) {
		return
			retainScheme
				? getForwardUrlWithScheme(req)
				: getForwardUrlSansScheme(req)
		;
	}

	public static StringBuilder getForwardUrlWithScheme(final HttpServletRequest req) {
		return getForwardUrlWithScheme(req, getScheme(req), true);
	}

	public static StringBuilder getForwardUrlWithScheme(final HttpServletRequest req, final String scheme) {
		return getForwardUrlWithScheme(req, scheme, getScheme(req).equals(scheme));
	}

	public static StringBuilder getForwardUrlWithScheme(final HttpServletRequest req, final String scheme, final boolean includeNonDefaultPort) {
		return getForwardUrlSansScheme(req, includeNonDefaultPort, new StringBuilder().append(scheme).append(':'));
	}

	public static StringBuilder getForwardUrlSansScheme(final HttpServletRequest req) {
		return getForwardUrlSansScheme(req, false);
	}

	public static StringBuilder getForwardUrlSansScheme(final HttpServletRequest req, final String schemeToIncludeNonDefaultPort) {
		return getForwardUrlSansScheme(req, getScheme(req).equals(schemeToIncludeNonDefaultPort));
	}

	public static StringBuilder getForwardUrlSansScheme(final HttpServletRequest req, final boolean includeNonDefaultPort) {
		return getForwardUrlSansScheme(req, includeNonDefaultPort, new StringBuilder());
	}

	private static StringBuilder getForwardUrlSansScheme(final HttpServletRequest req, final boolean includeNonDefaultPort, final StringBuilder urlSb) {
		return getAuthorityUrlSansScheme(req, includeNonDefaultPort, urlSb).append(getForwardURI(req));
	}


	private HttpServletRequests() {}
}
