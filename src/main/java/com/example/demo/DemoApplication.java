package com.example.demo;

import com.ea.agentloader.AgentLoader;
import com.ea.agentloader.AgentLoaderHotSpot;
import com.ea.agentloader.ClassPathUtils;
import io.atomix.cluster.Node;
import io.atomix.cluster.discovery.BootstrapDiscoveryProvider;
import io.atomix.core.Atomix;
import io.atomix.core.AtomixBuilder;
import io.atomix.core.profile.Profile;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.builder.SpringApplicationBuilder;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.net.*;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.jar.JarFile;

@SpringBootApplication
public class DemoApplication {

	public static void main(String[] args) throws ClassNotFoundException, IOException {

		AtomixBuilder builder = Atomix.builder();
		builder.withMemberId("member1")
			.withAddress("30.8.27.179")
			.build();
		builder.withMembershipProvider(BootstrapDiscoveryProvider.builder()
			.withNodes(
				Node.builder()
					.withId("member1")
					.withAddress("30.8.27.179")
					.build(),
				Node.builder()
					.withId("member2")
					.withAddress("10.192.19.182")
					.build(),
				Node.builder()
					.withId("member3")
					.withAddress("10.192.19.183")
					.build())
			.build());
		builder.addProfile(Profile.dataGrid());
		Atomix atomix = builder.build();

		atomix.start().join();

	}
	public void startTest(){
		//InputStream inputStream= HelloAgentWorld.class.getResourceAsStream(HelloAgentWorld.class.getSimpleName()+".class");
		//String path="/Users/sdhjl2000/Projects/ea-agent-loader/agent-loader/target/";
		//Files.copy(inputStream, Paths.get(path,"HelloAgentWorld.class"));
		//ExtClasspathLoader.loadClasspath(path);
		if(HelloAgentWorld.class.getClassLoader() != ClassLoader.getSystemClassLoader()) {
			ClassPathUtils.appendToSystemPath(ClassPathUtils.getClassPathFor(HelloAgentWorld.class));
		}
		AgentLoader.loadAgentClass(HelloAgentWorld.class.getName(), "Hello!");
		//		final File jarFile;
		//		try
		//		{
		//			jarFile = AgentLoader.createTemporaryAgentJar(HelloAgentWorld.class.getName(), null, true, true, false);
		//		}
		//		catch (IOException ex)
		//		{
		//			throw new RuntimeException("Can't write jar file for agent", ex);
		//		}
		//		ByteBuddyAgent.attach(jarFile, AgentLoaderHotSpot.getPid());

	}
	public static String findPathJar(Class<?> context) throws IllegalStateException {
		if (context == null) return null;
		String rawName = context.getName();
		String classFileName;
    /* rawName is something like package.name.ContainingClass$ClassName. We need to turn this into ContainingClass$ClassName.class. */ {
			int idx = rawName.lastIndexOf('.');
			classFileName = (idx == -1 ? rawName : rawName.substring(idx+1)) + ".class";
		}

		String uri = context.getResource(classFileName).toString();
		if (uri.startsWith("file:")) throw new IllegalStateException("This class has been loaded from a directory and not from a jar file.");
		if (!uri.startsWith("jar:file:")) {
			int idx = uri.indexOf(':');
			String protocol = idx == -1 ? "(unknown)" : uri.substring(0, idx);
			throw new IllegalStateException("This class has been loaded remotely via the " + protocol +
					" protocol. Only loading from a jar on the local file system is supported.");
		}

		int idx = uri.indexOf('!');
		//As far as I know, the if statement below can't ever trigger, so it's more of a sanity check thing.
		if (idx == -1) throw new IllegalStateException("You appear to have loaded this class from a local jar file, but I can't make sense of the URL!");

		try {
			String fileName = URLDecoder.decode(uri.substring("jar:file:".length(), idx), Charset.defaultCharset().name());
			return new File(fileName).getAbsolutePath();
		} catch (UnsupportedEncodingException e) {
			throw new InternalError("default charset doesn't exist. Your VM is borked.");
		}
	}
}
