package cn.tm.ms.restful.core;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.List;
import java.util.Map;

import javax.ws.rs.core.Application;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.core.SecurityContext;
import javax.ws.rs.core.UriBuilder;

import org.glassfish.jersey.internal.MapPropertiesDelegate;
import org.glassfish.jersey.server.ApplicationHandler;
import org.glassfish.jersey.server.ContainerRequest;
import org.glassfish.jersey.server.ContainerResponse;
import org.glassfish.jersey.server.ResourceConfig;
import org.glassfish.jersey.server.spi.Container;

import cn.tm.ms.restful.support.ContainerRequestHelper;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufInputStream;
import io.netty.buffer.ByteBufOutputStream;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.handler.codec.http.DefaultFullHttpResponse;
import io.netty.handler.codec.http.FullHttpRequest;
import io.netty.handler.codec.http.FullHttpResponse;
import io.netty.handler.codec.http.HttpHeaderNames;
import io.netty.handler.codec.http.HttpHeaders;
import io.netty.handler.codec.http.HttpRequest;
import io.netty.handler.codec.http.HttpResponseStatus;
import io.netty.handler.codec.http.HttpUtil;
import io.netty.handler.codec.http.HttpVersion;
import io.netty.util.AsciiString;

@ChannelHandler.Sharable
public class JerseyHttpHandler extends ChannelInboundHandlerAdapter implements Container {

	private final boolean isSecure;
	private final String rootPath;
	private final ApplicationHandler appHandler;

	private static final AsciiString CONTENT_TYPE = new AsciiString("Content-Type");
	private static final AsciiString CONTENT_LENGTH = new AsciiString("Content-Length");
	private static final AsciiString CONNECTION = new AsciiString("Connection");
	private static final AsciiString KEEP_ALIVE = new AsciiString("keep-alive");
	private static final SecurityContext dummySecurityContext = new DummySecurityContext();

	/**
	 * The jersey http handler.
	 * 
	 * @param application
	 * @param isSecure https => true, http =>false
	 * @param rootPath
	 */
	public JerseyHttpHandler(Application application, boolean isSecure, String rootPath) {
		this.appHandler = new ApplicationHandler(application);
		this.isSecure = isSecure;
		this.rootPath = rootPath;
	}

	@Override
	public void channelReadComplete(ChannelHandlerContext ctx) {
		ctx.flush();
	}

	@Override
	public void channelRead(ChannelHandlerContext ctx, Object msg) {
		if (msg instanceof HttpRequest) {
			HttpRequest req = (HttpRequest) msg;
			if (HttpUtil.is100ContinueExpected(req)) {
				ctx.write(new DefaultFullHttpResponse(HttpVersion.HTTP_1_1, HttpResponseStatus.CONTINUE));
			}

			boolean keepAlive = HttpUtil.isKeepAlive(req);
			FullHttpResponse response = consumeRequest(ctx, req);
			response.headers().setInt(CONTENT_LENGTH, response.content().readableBytes());

			if (!keepAlive) {
				ctx.write(response).addListener(ChannelFutureListener.CLOSE);
			} else {
				response.headers().set(CONNECTION, KEEP_ALIVE);
				ctx.write(response);
			}
		}
	}

	/**
	 * 消费请求
	 * 
	 * @param ctx
	 * @param req
	 * @return
	 */
	@SuppressWarnings("deprecation")
	private FullHttpResponse consumeRequest(ChannelHandlerContext ctx, HttpRequest req) {
		FullHttpResponse response;
		final ByteBuf buffer = ctx.alloc().buffer();
		try {
			final ContainerRequest containerRequest = createContainerRequest(ctx, req);
			final ContainerResponse containerResponse = getApplicationHandler().apply(containerRequest, new ByteBufOutputStream(buffer)).get();

			response = createNettyResponse(containerResponse, buffer);

			// $NON-NLS-非正常响应(!200)说明信息$
			if (HttpResponseStatus.OK != response.getStatus()) {
				ByteBuf byteBuf = buffer.writeBytes(response.getStatus().toString().getBytes());
				response.replace(byteBuf);
			}
		} catch (Exception e) {
			// $NON-NLS-500错误处理$
			ByteBuf errorBuffer = buffer.copy().writeBytes(HttpResponseStatus.INTERNAL_SERVER_ERROR.toString().getBytes());
			buffer.release();
			e.printStackTrace();
			
			response = new DefaultFullHttpResponse(req.protocolVersion(), HttpResponseStatus.INTERNAL_SERVER_ERROR, errorBuffer, true);
			response.headers().set(CONTENT_TYPE, MediaType.TEXT_PLAIN);
		}

		return response;
	}

	/**
	 * The create jersey container request context.
	 * 
	 * @param ctx
	 * @param req
	 * @return
	 * @throws URISyntaxException
	 */
	private ContainerRequest createContainerRequest(ChannelHandlerContext ctx, HttpRequest req) throws URISyntaxException {
		HttpHeaders headers = req.headers();
		URI baseUri = new URI((isSecure ? "https" : "http") + "://" + headers.get(HttpHeaderNames.HOST) + "/" + rootPath + "/");
		URI requestUri = UriBuilder.fromUri(req.uri()).scheme(baseUri.getScheme()).host(baseUri.getHost()).port(baseUri.getPort()).build();
		
		String httpMethod = req.method().name();// 请求类型
		ContainerRequest requestContext = new ContainerRequest(// 请求报文体
				baseUri, requestUri, httpMethod, dummySecurityContext, new MapPropertiesDelegate());
		requestContext.setProperty(ContainerRequestHelper.CHANNEL_HANDLER_CONTEXT_PROPERTY, ctx);

		if (req instanceof FullHttpRequest) {
			consumeEntity((FullHttpRequest) req, requestContext);
		}
		processRequestHeaders(headers, requestContext);

		return requestContext;
	}

	private static void processRequestHeaders(HttpHeaders headers, ContainerRequest requestContext) {
		for (Map.Entry<String, String> header : headers) {
			String value = header.getValue();
			String headerName = header.getKey();
			
			if (HttpHeaderNames.CONTENT_TYPE.contentEqualsIgnoreCase(headerName) && value.indexOf(';') > 0) {
				value = value.substring(0, value.indexOf(';'));
			}
			
			requestContext.headers(headerName, value);
		}
	}

	private void consumeEntity(FullHttpRequest req, ContainerRequest requestContext) {
		ByteBuf content = req.content();
		if (content != null) {
			requestContext.setEntityStream(new ByteBufInputStream(content));
		}
	}

	private FullHttpResponse createNettyResponse(ContainerResponse containerResponse, ByteBuf buffer) {
		HttpResponseStatus status = HttpResponseStatus.valueOf(containerResponse.getStatus());
		DefaultFullHttpResponse result = new DefaultFullHttpResponse(HttpVersion.HTTP_1_1, status, buffer, true);
		prepareResponseHeaders(containerResponse, result);

		return result;
	}

	private static void prepareResponseHeaders(ContainerResponse containerResponse, DefaultFullHttpResponse result) {
		MultivaluedMap<String, Object> containerResponseHeaders = containerResponse.getHeaders();
		HttpHeaders responseHeaders = result.headers();
		for (Map.Entry<String, List<Object>> stringListEntry : containerResponseHeaders.entrySet()) {
			String headerName = stringListEntry.getKey();
			List<Object> headerValues = stringListEntry.getValue();
			responseHeaders.add(headerName, headerValues);
		}
	}

	@Override
	public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
		cause.printStackTrace();
		ctx.close();
	}

	public ApplicationHandler getApplicationHandler() {
		return appHandler;
	}

	@Override
	public ResourceConfig getConfiguration() {
		return appHandler.getConfiguration();
	}

	@Override
	public void reload() {
		reload(getConfiguration());
	}

	@Override
	public void reload(final ResourceConfig configuration) {
		throw new UnsupportedOperationException();
	}
	
}
