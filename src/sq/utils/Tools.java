package sq.utils;

import java.io.IOException;
import java.io.PrintStream;
import java.io.UnsupportedEncodingException;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Tools {
	public static PrintStream out;
	static {
		try {
			out = new PrintStream(System.out, true, "UTF-8");
		} catch (UnsupportedEncodingException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	public static String readFile(String path, String encoding)
			throws IOException {
		byte[] encoded = Files.readAllBytes(Paths.get(path));
		return new String(encoded, encoding);
	}

	public static void writeFile(String path, String text, String encoding)
			throws IOException {
		Files.write(Paths.get(path), text.getBytes(encoding));
	}

	public static String readFile(String path) throws IOException {
		return readFile(path, "UTF-8");
	}

	public static void writeFile(String path, String text) throws IOException {
		Files.write(Paths.get(path), text.getBytes("UTF-8"));
	}

	public static ArrayList<String> getLinks(String text) {
		ArrayList<String> links = new ArrayList<String>();

		String regex = "\\(?\\b(http://|www[.])[-A-Za-z0-9+&amp;@#/%?=~_()|!:,.;]*[-A-Za-z0-9+&amp;@#/%=~_()|]";
		Pattern p = Pattern.compile(regex);
		Matcher m = p.matcher(text);
		while (m.find()) {
			String urlStr = m.group();
			if (urlStr.startsWith("(") && urlStr.endsWith(")")) {
				urlStr = urlStr.substring(1, urlStr.length() - 1);
			}
//			System.out.println(urlStr.indexOf('.'));
			if (urlStr.indexOf('.') < 0) continue;
			links.add(urlStr);
		}
		return links;
	}

	public static String getDomainName(String url) throws URISyntaxException {
		URI uri = new URI(url);
		String domain = uri.getHost();
		return domain;
		// return domain.startsWith("www.") ? domain.substring(4) : domain;
	}
	
	public static String getDomain(String url) {
		String domain;
		if (url.startsWith("http://")) {
			domain = url.substring(7);
		} else if (url.startsWith("https://")) {
			domain = url.substring(8);
		} else {
			domain = url;
		}
		domain = domain.split("/|:")[0];
		return domain;
	}

}