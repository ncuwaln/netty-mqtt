package cn.edu.ncu;

import cn.edu.ncu.common.RedisPoolUtil;
import cn.edu.ncu.common.Singleton;
import cn.edu.ncu.common.StringUtil;
import cn.edu.ncu.core.Consumer;
import cn.edu.ncu.core.MqttHandler;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.codec.mqtt.MqttDecoder;
import io.netty.handler.codec.mqtt.MqttEncoder;
import io.netty.handler.timeout.IdleStateHandler;
import org.apache.commons.cli.*;

import java.io.IOException;
import java.util.Properties;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

public class Server {
    class CliArgs{
        String host;
        String port;
        String configPath;
        String duridConfigPath;
    }

    public static void main(String[] args){
        Server server = new Server();
        CliArgs cliArgs = null;
        try {
            cliArgs = server.parseArgs(args);
            server.loadConf(cliArgs);
        } catch (ParseException e) {
            e.printStackTrace();
            System.exit(-1);
        } catch (IOException e) {
            e.printStackTrace();
            System.exit(-1);
        }
        server.run(Singleton.getServerConf());
    }

    private void run(final Properties serverConf) {
        startConsumer();

        final ServerBootstrap serverBootstrap = new ServerBootstrap();
        EventLoopGroup eventLoopGroup = new NioEventLoopGroup();
        try {
            serverBootstrap.group(eventLoopGroup)
                    .channel(NioServerSocketChannel.class)
                    .childHandler(new ChannelInitializer<SocketChannel>() {
                        @Override
                        protected void initChannel(SocketChannel socketChannel) throws Exception {
                            socketChannel.pipeline().addLast(new MqttDecoder());
                            socketChannel.pipeline().addLast(MqttEncoder.INSTANCE);
                            socketChannel.pipeline().addLast("MqttServerHandler", new MqttHandler());
                        }
                    });

            ChannelFuture channelFuture = serverBootstrap.bind(serverConf.getProperty("host"),
                    Integer.valueOf(serverConf.getProperty("port"))).sync();
            channelFuture.channel().closeFuture().sync();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }finally {
            eventLoopGroup.shutdownGracefully();
        }
    }

    private void startConsumer() {
        Consumer consumer = new Consumer(RedisPoolUtil.getConn());
        ExecutorService executorService = Executors.newSingleThreadExecutor();
        executorService.execute(consumer);
    }

    private void loadConf(CliArgs cliArgs) throws IOException {
        if (StringUtil.hasText(cliArgs.configPath)){
            Singleton.loadServerConf(cliArgs.configPath);
        }else {
            Singleton.loadServerConf();
        }
        if (StringUtil.hasText(cliArgs.host)){
            Singleton.getServerConf().setProperty("host", cliArgs.host);
        }
        if (StringUtil.hasText(cliArgs.port)){
            Singleton.getServerConf().setProperty("port", cliArgs.port);
        }
        if ((StringUtil.hasText(cliArgs.duridConfigPath))){
            Singleton.getServerConf().setProperty("druidConfigPath", cliArgs.duridConfigPath);
        }
    }

    private CliArgs parseArgs(String[] args) throws ParseException {
        Options options = new Options();

        options.addOption(
                Option.builder("h")
                .longOpt("host")
                .desc("server host")
                .argName("HOSTNAME")
                .build()
        );
        options.addOption(
                Option.builder("p")
                .longOpt("port")
                .desc("server bind port")
                .argName("PORT")
                .build()
        );
        options.addOption(
                Option.builder("c")
                .longOpt("config_path")
                .desc("server config file path")
                .build()
        );
        options.addOption(
                Option.builder("dc")
                .longOpt("druid_config_path")
                .desc("druid connection pool config file path")
                .build()
        );

        CliArgs cliArgs = new CliArgs();
        CommandLineParser parser = new DefaultParser();
        CommandLine cmd = parser.parse(options, args);
        cliArgs.configPath = cmd.getOptionValue("c");
        cliArgs.duridConfigPath = cmd.getOptionValue("dc");
        cliArgs.host = cmd.getOptionValue("h");
        cliArgs.port = cmd.getOptionValue("p");
        return cliArgs;
    }
}
