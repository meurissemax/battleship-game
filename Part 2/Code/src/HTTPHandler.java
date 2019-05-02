import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.IOException;

import java.net.URL;
import java.net.MalformedURLException;

import java.util.ArrayList;

/**
 * This class is used to deal with HTTP requests (parse the request and save some information).
 *
 * @author Maxime Meurisse & Valentin Vermeylen
 * @version 2019.05.02
 */

public class HTTPHandler {
	private String content;
	private URL url;
	private BufferedReader bufReader;
	private ArrayList<String> headers;

	public HTTPHandler(InputStream inStream) {
		content = "";
		bufReader = new BufferedReader(new InputStreamReader(inStream));
		headers = new ArrayList<String>();
	}

	/**
	 * This method is used to parse an HTTP request, i.e. to read its header and save the content.
	 *
	 * @throws HTTPException a exception if an HTTP exception occured
	 */
	public void parse() throws HTTPException {
		int contentLength = -1;
		char[] buffer;
		String header, length, path;

		try {
			/* Header of the request */

			/// We first read the HTTP request line (first line of the header)
			header = bufReader.readLine();

			if(header.length() > 0)
				headers.add(header);

			/// We then read the rest of the header
			header = bufReader.readLine();

			while(header.length() > 0) {
				if(header.indexOf(":") == -1)
					throw new HTTPException("400");

				headers.add(header);
				header = bufReader.readLine();
			}

			/* Body of the request */

			/// We check if there is a 'Content-Length' field in the header (i.e. if there is a body)
			length = searchHeader("Content-Length");

			if(length != null) {
				contentLength = Integer.parseInt(length);
				buffer = new char[contentLength];

				bufReader.read(buffer, 0, contentLength);

				for(int i = 0; i < buffer.length; i++)
					if(Character.getNumericValue(buffer[i]) != -2 || buffer[i] == '=' || buffer[i] == '&')
						content += buffer[i];
			}

			/// We check if the HTTP request is correct
			if(!headers.get(0).contains("HTTP/1.1"))
				throw new HTTPException("505");

			if(!headers.get(0).contains("GET") && !headers.get(0).contains("POST"))
				throw new HTTPException("501");

			if(headers.get(0).indexOf('/') == -1)
				throw new HTTPException("400");

			if(headers.get(0).contains("POST") && contentLength == -1)
				throw new HTTPException("411");

			path = headers.get(0).substring(headers.get(0).indexOf("/"), headers.get(0).indexOf(" ", headers.get(0).indexOf("/")));
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
	 * It only returns the value of the query 'pos', if it exists, else returns -1.
	 * By example, if the query is "pos=32&abc=def", the function returns 32.
	 *
	 * @param query the query to parse
	 *
	 * @return the value of the query 'pos', parsed to an integer
	 */
	public int parseQuery(String query) {
		int value = -1;
		String posQuery;
		String[] queries, split;

		if(query == null)
			return value;

		query = query.replaceAll("\\s+", "");

		if(query.contains("&")) {
			queries = query.split("&");

			for(int i = 0; i < queries.length; i++) {
				split = queries[i].split("=");

				if(split[0].equals(GameConstants.QUERY_NAME)) {
					posQuery = queries[i];
					break;
				}
			}

			return value;
		} else {
			split = query.split("=");

			if(split[0].equals(GameConstants.QUERY_NAME))
				posQuery = query;
			else
				return value;
		}

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

	/**
	 * This function is used to known if GZIP compression is accepted.
	 *
	 * @return a boolean value indicating if GZIP compression is accepted or not.
	 */
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
