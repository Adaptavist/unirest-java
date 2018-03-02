/*
The MIT License

Copyright (c) 2013 OpenUnirest (http://github.com/OpenUnirest)

Permission is hereby granted, free of charge, to any person obtaining
a copy of this software and associated documentation files (the
"Software"), to deal in the Software without restriction, including
without limitation the rights to use, copy, modify, merge, publish,
distribute, sublicense, and/or sell copies of the Software, and to
permit persons to whom the Software is furnished to do so, subject to
the following conditions:

The above copyright notice and this permission notice shall be
included in all copies or substantial portions of the Software.

THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND,
EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF
MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND
NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE
LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION
OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION
WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 */


package com.mashape.unirest.test.http;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.http.entity.ContentType;
import spark.Request;
import spark.Response;
import spark.Route;
import spark.Spark;

import javax.servlet.MultipartConfigElement;
import javax.servlet.http.Part;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Scanner;

import static java.lang.System.getProperty;
import static spark.Spark.*;

public class MockServer {
	private static final ObjectMapper om = new ObjectMapper();
	public static int PORT = 4567;
	public static String HOST = "http://localhost:" + PORT;

	public static void start() {
		port(PORT);
		post("/post", ContentType.MULTIPART_FORM_DATA.getMimeType(), multipost);
		get("/get", new Route() {
			public Object handle(Request request, Response response) throws Exception {
				return "Hi Momn";
			}
		});
	}

	private static Route multipost = new Route() {
		public Object handle(Request req, Response res) throws Exception {
			req.raw().setAttribute("org.eclipse.jetty.multipartConfig", new MultipartConfigElement(getProperty("java.io.tmpdir")));

			ResponseBody body = new ResponseBody();
			for (Part p : req.raw().getParts()) {
				if (p.getContentType().equals(ContentType.APPLICATION_OCTET_STREAM.getMimeType())) {
					buildFilePart(p, body);
				} else {
					buildFormPart(p, body);
				}
			}

			return om.writeValueAsString(body);
		}
	};

	private static void buildFormPart(Part p, ResponseBody body) throws IOException {
		java.util.Scanner s = new Scanner(p.getInputStream()).useDelimiter("\\A");
		String value = s.hasNext() ? s.next() : "";
		body.form.put(p.getName(), value);
	}

	public static void buildFilePart(Part part, ResponseBody body){
		body.files = new File();
		body.files.fileName = part.getSubmittedFileName();
		body.files.type = part.getContentType();
		body.files.inputName = part.getName();
	}

	public static void shutdown() {
		Spark.stop();
	}

	public static class ResponseBody {
		public File files;
		public Map<String,String> form = new HashMap<String,String>();
	}

	public static class File {
		public String fileName;
		public String type;
		public String inputName;
	}
}
