package cn.tm.ms.restful.ctrl;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Request;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriInfo;

import cn.tm.ms.restful.annotation.Ctrl;
import jersey.repackaged.com.google.common.base.MoreObjects;

@Ctrl
@Path("/echo")
public class EchoEndpoint {


    @GET
    @Produces(MediaType.TEXT_PLAIN)
    public Response echo(@Context UriInfo uriInfo, @Context Request request, @Context HttpHeaders headers) {
    	String requestInfo = createRequestInfo(uriInfo, request, headers);
        return Response.ok().entity(requestInfo).build();
    }

    private String createRequestInfo(UriInfo uriInfo, Request request, HttpHeaders headers) {
        return MoreObjects.toStringHelper("request")
                .add("UriInfo", uriInfo)
                .add("Request", request)
                .add("Headers", headers.getRequestHeaders())
                .toString();
    }
}
