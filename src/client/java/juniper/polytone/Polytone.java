package juniper.polytone;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import juniper.polytone.init.PolytoneCommand;
import net.fabricmc.api.ClientModInitializer;

//TODO navigation/pathfinding/flight
public class Polytone implements ClientModInitializer {
	public static String MODID = "polytone";
    public static Logger LOGGER = LoggerFactory.getLogger(MODID);

	@Override
	public void onInitializeClient() {
		LOGGER.info(MODID + " init");

		PolytoneCommand.init();
	}
}