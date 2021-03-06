package rocks.inspectit.server.rmi;

import java.io.IOException;
import java.util.concurrent.ExecutorService;

import org.slf4j.Logger;
import org.springframework.beans.factory.BeanInitializationException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.DependsOn;

import com.esotericsoftware.kryo.Kryo;

import rocks.inspectit.shared.all.kryonet.Connection;
import rocks.inspectit.shared.all.kryonet.ExtendedSerializationImpl;
import rocks.inspectit.shared.all.kryonet.IExtendedSerialization;
import rocks.inspectit.shared.all.kryonet.Listener;
import rocks.inspectit.shared.all.kryonet.Server;
import rocks.inspectit.shared.all.kryonet.rmi.ObjectSpace;
import rocks.inspectit.shared.all.spring.logger.Log;
import rocks.inspectit.shared.all.storage.nio.stream.StreamProvider;
import rocks.inspectit.shared.all.storage.serializer.provider.SerializationManagerProvider;

/**
 * COnfiguration of the {@link Server} that will be used for communication with the agent.
 *
 * @author Ivan Senic
 *
 */
@Configuration
public class KryoNetServerCreator {

	/**
	 * Logger for the class.
	 */
	@Log
	Logger log;

	/**
	 * Port to bind service to.
	 */
	@Value("${cmr.port}")
	private int port;

	/**
	 * Serialization manager to provide {@link Kryo} instance.
	 */
	@Autowired
	private SerializationManagerProvider serializationManagerProvider;

	/**
	 * {@link StreamProvider} to hook with the KryoNet.
	 */
	@Autowired
	private StreamProvider streamProvider;

	/**
	 * Executor service for object space. This will enable that multiple incoming communication
	 * requests can be handled in parallel.
	 */
	@Autowired
	@Qualifier("kryoNetObjectSpaceExecutorService")
	private ExecutorService executorService;

	/**
	 * Start the kryonet server and binds it to the specified port.
	 *
	 * @return Start the kryonet server and binds it to the specified port.
	 */
	@Bean(name = "kryonet-server", destroyMethod = "stop")
	public Server createServer() {
		IExtendedSerialization serialization = new ExtendedSerializationImpl(serializationManagerProvider);

		Server server = new Server(serialization, streamProvider);
		server.start();

		try {
			server.bind(port);
			log.info("|-Kryonet server successfully started and running on port " + port);
		} catch (IOException e) {
			throw new BeanInitializationException("Could not bind the kryonet server to the specified ports.", e);
		}

		return server;
	}

	/**
	 * Creates the {@link ObjectSpace}, registers kryo classes and connect the space to the server.
	 *
	 * @param server
	 *            KryoNet {@link Server}.
	 * @return Created {@link ObjectSpace}.
	 */
	@Bean(name = "kryonet-server-objectspace")
	@DependsOn("kryonet-server")
	@Autowired
	public ObjectSpace createObjectSpace(Server server) {
		final ObjectSpace objectSpace = new ObjectSpace();
		objectSpace.setExecutor(executorService);
		server.addListener(new Listener() {
			@Override
			public void connected(Connection connection) {
				objectSpace.addConnection(connection);
			}
		});

		return objectSpace;
	}

}
