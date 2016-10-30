package cn.tm.ms.restful;

import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.ws.rs.core.Application;

import cn.tm.ms.restful.core.JaxrsHttpChannelInitializer;
import cn.tm.ms.restful.support.RestFulScanCtrl;
import cn.tm.ms.restful.type.SSLType;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.Channel;
import io.netty.channel.ChannelOption;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.logging.LogLevel;
import io.netty.handler.logging.LoggingHandler;

/**
 * 基于Netty实现的RESTFUL服务器
 * 
 * @author lry
 */
public class RestFulServer {

	private static final Logger logger = Logger.getLogger(RestFulServer.class.getName());

	public final int PORT;
	public final boolean IS_SECURE;
	public final String ROOT_PATH;
	public final String HOST = "0.0.0.0";

	private volatile Channel serverChannel;
	private final ServerBootstrap serverBootstrap;

	private final EventLoopGroup bossGroup;
	private final EventLoopGroup workerGroup;

	public RestFulServer(boolean isSecure, String rootPath, int port, String pack) {
		this(isSecure, SSLType.SIMPLE, rootPath, port, pack, null);
	}

	/**
	 * @param isSecure
	 *            是否暴露HTTPS
	 * @param rootPath
	 *            服务根节点
	 * @param port
	 *            暴露端口号
	 * @param pack
	 *            扫描包路径
	 * @param customServMAP
	 *            自定义服务
	 */
	public RestFulServer(boolean isSecure, String rootPath, int port, String pack, Map<String, Object> customServMAP) {
		this(isSecure, SSLType.SIMPLE, rootPath, port, pack, customServMAP);
	}

	public RestFulServer(boolean isSecure, SSLType sslType, String rootPath, int port, String pack,
			Map<String, Object> customServMAP) {
		this(isSecure, sslType, rootPath, port, new RestFulScanCtrl(pack, customServMAP));
	}

	public RestFulServer(boolean isSecure, SSLType sslType, String rootPath, int port, Application application) {
		this.PORT = port;
		this.IS_SECURE = isSecure;
		this.ROOT_PATH = rootPath;

		// Configure the server.
		bossGroup = new NioEventLoopGroup();
		workerGroup = new NioEventLoopGroup();
		serverBootstrap = new ServerBootstrap();
		serverBootstrap.option(ChannelOption.SO_BACKLOG, 1024);

		serverBootstrap.group(bossGroup, workerGroup).channel(NioServerSocketChannel.class)
				.handler(new LoggingHandler(LogLevel.INFO))
				.childHandler(new JaxrsHttpChannelInitializer(application, IS_SECURE, sslType, ROOT_PATH));
	}

	/**
	 * 启动
	 */
	public void start() {
		try {
			serverChannel = serverBootstrap.bind(HOST, PORT).sync().channel();
			logger.info("Server started. Open your web browser and navigate to " + (IS_SECURE ? "https" : "http")
					+ "://" + HOST + ":" + PORT + "/" + ROOT_PATH + "/");
		} catch (InterruptedException e) {
			close();
		}
	}

	/**
	 * 停止
	 */
	public void stop() {
		try {
			if (serverChannel != null) {
				serverChannel.disconnect();
				serverChannel.closeFuture().sync();
			}

			logger.info("Server stopped");
		} catch (InterruptedException e) {
			logger.log(Level.SEVERE, "Error while stopping server, error is:" + e.getMessage());
		} finally {
			close();
		}
	}

	/**
	 * 关闭
	 */
	private void close() {
		if (bossGroup != null) {
			bossGroup.shutdownGracefully();
		}

		if (workerGroup != null) {
			workerGroup.shutdownGracefully();
		}
	}

	public static void main(String[] args) {
		RestFulServer server = null;
		try {
			if (args.length == 4) {
				boolean isSecure = Boolean.valueOf(args[0]);
				String rootPath = args[1];
				int port = Integer.valueOf(args[2]);
				String pack = args[3];
				server = new RestFulServer(isSecure, rootPath, port, pack);
				server.start();
			} else {
				throw new RuntimeException("参数个数必须为4个!!!");
			}
		} catch (Exception e) {
			if (server != null) {
				server.stop();
			}
			e.printStackTrace();
		}
	}

}
