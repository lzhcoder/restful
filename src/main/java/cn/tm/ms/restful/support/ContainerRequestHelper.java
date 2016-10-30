package cn.tm.ms.restful.support;

import org.glassfish.jersey.server.ContainerRequest;

import io.netty.channel.ChannelHandlerContext;

public class ContainerRequestHelper {

	public static final String CHANNEL_HANDLER_CONTEXT_PROPERTY = ChannelHandlerContext.class.getName();

    ContainerRequestHelper() {
        // to prevent instantiation
    }

    public static ChannelHandlerContext getChannelHandlerContext(ContainerRequest containerRequest) {
    	 
        return (ChannelHandlerContext) containerRequest.getProperty(CHANNEL_HANDLER_CONTEXT_PROPERTY);
    }
}
