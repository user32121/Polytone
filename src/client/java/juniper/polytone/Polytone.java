package juniper.polytone;

import java.util.logging.Logger;

import juniper.polytone.init.PolytoneCommand;
import net.fabricmc.api.ClientModInitializer;

public class Polytone implements ClientModInitializer {
	public static String MODID = "polytone";
	public static Logger LOGGER = Logger.getLogger(MODID);

	@Override
	public void onInitializeClient() {
		LOGGER.info(MODID + " init");

		PolytoneCommand.init();
	}
}