import java.io.InputStreamReader;
import java.io.InputStream;
import java.io.IOException;

import java.net.URL;
import java.net.MalformedURLException;

import java.util.ArrayList;

/**
 * This class is used to deal with HTTP requests (parse the request and save some information).
 *
 * @author Maxime Meurisse & Valentin Vermeylen
 * @version 2019.04.20
 */

public class HTTPHandler {
	private int numberCharRead;
	private String content;
	private URL url;
	private InputStreamReader inStreamRead;
	private ArrayList<String> headers;

	public HTTPHandler(InputStream inStream) {
		numberCharRead = 1000;
		content = "";
		inStreamRead = new InputStreamReader(inStream);
		headers = new ArrayList<String>();
	}

	/**
	 * This method is used to parse an HTTP request, i.e. to read its header and save the content.
	 *
	 * @throws HTTPException a exception if an HTTP exception occured
	 */
	public void parse() throws HTTPException {
		int i, contentLength, size, num;
		char c;
		char[] req;
		String request, contentLengthString, path;
		String[] split;

		try {
			req = new char[numberCharRead];
			inStreamRead.read(req, 0, numberCharRead);
			request = new String(req);
			split = request.split("\r\n");

			i = 0;

			headers.add("");

			/// we get the header fields of the HTTP request
			while(split[i].compareTo("") != 0) {
				if(i == 0)
					headers.set(headers.size() - 1, headers.get(headers.size() - 1) + split[i]);
				else
					headers.add(split[i]);

				i++;

				if(i == split.length) {
					req = new char[numberCharRead];
					inStreamRead.read(req, 0, numberCharRead);
					request = new String(req);
					split = request.split("\r\n");

					i = 0;
				}
			}

			/// we get the content of the query of the HTTP request
			for(; i < split.length; i++) {
				for(int j = 0; j < split[i].length(); j++) {
					if(Character.getNumericValue(split[i].charAt(j)) != -2 || split[i].charAt(j) == '=' || split[i].charAt(j) == '&') {
						c = split[i].charAt(j);
						content += c;
					}
				}
			}

			contentLength = -1;
			contentLengthString = searchHeader("Content-Length");

			if(contentLengthString != null) {
				contentLength = Integer.parseInt(searchHeader("Content-Length"));

				if(contentLength > content.length()) {
					size = contentLength - content.length();
					req = new char[size];
					inStreamRead.read(req, 0, size);
					request = new String(req);
					content += request;
				}
			}

			/// we check if the HTTP request is correct
			if(!headers.get(0).contains("HTTP/1.1"))
				throw new HTTPException("505");

			if(!headers.get(0).contains("GET") && !headers.get(0).contains("POST") && !headers.get(0).contains("HEAD") && !headers.get(0).contains("PUT") && !headers.get(0).contains("DELETE") && !headers.get(0).contains("CONNECT") && !headers.get(0).contains("OPTIONS") && !headers.get(0).contains("TRACE") && !headers.get(0).contains("PATCH"))
				throw new HTTPException("405");

			if(!headers.get(0).contains("GET") && !headers.get(0).contains("POST"))
				throw new HTTPException("501");

			if(headers.get(0).indexOf('/') == -1)
				throw new HTTPException("400");

			if(headers.get(0).contains("POST") && contentLength == -1)
				throw new HTTPException("411");

			path = headers.get(0).substring(headers.get(0).indexOf('/'), headers.get(0).indexOf(' ', headers.get(0).indexOf('/')));
			url = new URL(new URL("http://" + searchHeader("Host")), path);
		} catch(MalformedURLException mue) {
			throw new HTTPException("400");
		} catch(IOException ioe) {
			throw new HTTPException("400");
		} catch(Exception e) {
			System.err.println(e.getMessage());
		}
	}

	public String getContent() {
		return content;
	}

	public URL getURL() {
		return url;
	}

	public String getMethod() {
		if(headers.get(0).contains("GET"))
			return "GET";
		else if(headers.get(0).contains("POST"))
			return "POST";

		return null;
	}

	/**
	 * This method is used to return the content of a cookie.
	 *
	 * @param cookieName the name of the cookie
	 *
	 * @return the content of the cookie 'cookieName'
	 */
	public String searchCookie(String cookieName) {
		String cookies = searchHeader("Cookie");
		String[] content, split;

		if(cookies == null)
			return null;

		/// if there is more than 1 cookie
		if(cookies.contains(";")) {
			content = cookies.split(";");

			for(int i = 0; i < content.length; i++) {
				content[i] = content[i].replaceAll("\\s+", "");
				split = content[i].split("=");

				if(split[0].equals(cookieName))
					return content[i];
			}
		}

		/// if there is only 1 cookie
		else {
			split = cookies.split("=");

			if(split[0].equals(cookieName))
				return cookies;
		}

		return null;
	}

	/**
	 * This method is used to parse the query of an URL, i.e. to return its content.
	 * This function only works for the first value, and if this value is an integer.
	 * By example, if the query is "pos=32", the function returns 32.
	 *
	 * @param query the query to parse
	 *
	 * @return the value of the query, parsed to an integer
	 */
	public int parseQuery(String query) {
		int value = -1;
		String[] split;

		if(query == null)
			return value;

		query = query.replaceAll("\\s+", "");
		split = query.split("=");

		if(split.length > 0) {
			split[1] = split[1].replaceAll("[^0-9]", "");

			try {
				value = Integer.parseInt(split[1].substring(0, split[1].length()));

				return value;
			} catch(NumberFormatException nfe) {
				System.err.println("HTTPHandler : unable to parse the string to int.");
			} catch(Exception e) {
				System.err.println(e.getMessage());
			}
		}

		return value;
	}

	public boolean gzip() {
		String encoding = searchHeader("Accept-Encoding");

		if(encoding == null)
			return false;
		else if(encoding.contains("gzip"))
			return true;

		return false;
	}

	/**
	 * This function is used to search a specific header in the header field of the HTTP request.
	 * It returns the content of this header.
	 * For exemple, if the HTTP request contains "Cookie: cookie1=value1", the method "searchHeader('Cookie')" would return "cookie1=value1".
	 *
	 * @param name the header name
	 *
	 * @return the content of the header 'name'.
	 */
	private String searchHeader(String name) {
		String header;

		for(int i = 0; i < headers.size(); i++) {
			header = headers.get(i);

			if(header.contains(name))
				return header.substring(header.indexOf(':') + 2);
		}

		return null;
	}
}
