package juniper.polytone;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import juniper.polytone.command.TaskQueue;
import juniper.polytone.init.PolytoneCommand;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;

public class Polytone implements ClientModInitializer {
	public static String MODID = "polytone";
    public static Logger LOGGER = LoggerFactory.getLogger(MODID);

	@Override
	public void onInitializeClient() {
		LOGGER.info(MODID + " init");

		PolytoneCommand.init();
        ClientTickEvents.END_CLIENT_TICK.register(TaskQueue::tick);
	}
}