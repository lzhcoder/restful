package cn.tm.ms.restful.ctrl;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriInfo;

import cn.tm.ms.restful.annotation.Ctrl;
import cn.tm.ms.restful.annotation.RES;
import cn.tm.ms.restful.entity.Book;
import cn.tm.ms.restful.service.TestService;

@Ctrl
@Path("/helloworld")
public class HelloWorldResource {
	
	@RES
	private TestService testService1;
	
	@RES
	private TestService testService;
	
	public HelloWorldResource() {
		System.out.println("-----------");
	}
	
	@GET
	@Produces("text/html")
	@Path("/hello/{name}")
	public String getXml(@PathParam("name") String name, @Context final UriInfo uriInfo) {
		
		System.out.println("testService1---->"+testService1);
		System.out.println("testService---->"+testService);
		return "<html><body><h1>Hello World!"+name+"</h1></body></html>";
	}
	
	@GET
	@Produces(MediaType.APPLICATION_JSON)
	@Path("/map/{age}")
	public Map<String,Object> map(@PathParam("name") String name) {
		Map<String,Object> map=new HashMap<>();
		map.put("name", name);
		map.put("age", 12);
		return map;
	}
	
	@GET
	@Path("/books")
	@Consumes(MediaType.APPLICATION_XML)
	@Produces(MediaType.APPLICATION_XML)
	public List<Book> xml() {
		List<Book> list=new ArrayList<>();
		list.add(new Book());
		list.add(new Book());
		list.add(new Book());
	    return list;
	}
	
	@POST
	@Path("bio")
	public String postStream(final InputStream is) throws IOException {
		try (BufferedReader br = new BufferedReader(new InputStreamReader(is))) {
			StringBuilder result = new StringBuilder();
			String s = br.readLine();
			while (s != null) {
				result.append(s).append("\n");
				System.out.println(s);
				s = br.readLine();
			}
			System.out.println(result.toString().length());
			return result.toString();
		}
	}

	@POST
	@Path("cio")
	public Response postChars(final Reader r) throws IOException {
		try (BufferedReader br = new BufferedReader(r)) {
			StringBuilder result = new StringBuilder();
			String s = br.readLine();
			if (s == null) {
				throw new RuntimeException("NOT FOUND FROM READER");
			}
			while (s != null) {
				result.append(s).append("\n");
				System.out.println(s);
				s = br.readLine();
			}
			return Response.ok().entity(result.toString()).build();
		}
	}

}