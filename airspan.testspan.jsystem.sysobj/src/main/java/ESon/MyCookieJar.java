package ESon;

import java.util.ArrayList;
import java.util.List;

import Utils.GeneralUtils;
import okhttp3.Cookie;
import okhttp3.CookieJar;
import okhttp3.HttpUrl;

public class MyCookieJar implements CookieJar {

	private List<Cookie> cookies;

	@Override
	public void saveFromResponse(HttpUrl url, List<Cookie> cookies) {
		GeneralUtils.printToConsole("Cookie Jar - save : " + cookies);
		this.cookies = cookies;
	}

	@Override
	public List<Cookie> loadForRequest(HttpUrl url) {
		GeneralUtils.printToConsole("Cookie Jar - load : " + cookies);
		if (cookies != null) {
			return cookies;
		}
		return new ArrayList<Cookie>();

	}

	public String getCookieValue(String cookieName) {
		if(cookies == null) {
			GeneralUtils.printToConsole("no cookies!");
			return "";
		}
		for (Cookie cookie : cookies) {
			if (cookie.name() != null && cookie.name().equals(cookieName)) {
				GeneralUtils.printToConsole("Cookkie jar - get cookie value: " + cookie.value());
				return cookie.value();
			}
		}
		return null;
	}
}