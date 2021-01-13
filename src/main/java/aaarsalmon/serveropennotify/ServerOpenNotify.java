package aaarsalmon.serveropennotify;

import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.common.config.Config;
import net.minecraftforge.common.config.Config.Comment;
import net.minecraftforge.common.config.Config.Type;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.Mod.EventHandler;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import net.minecraftforge.fml.common.event.FMLServerStartedEvent;
import net.minecraftforge.fml.common.event.FMLServerStoppedEvent;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import javax.net.ssl.HttpsURLConnection;
import java.io.*;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.Random;

@Mod(
	modid = ServerOpenNotify.MODID,
	version = ServerOpenNotify.VERSION,
	name = ServerOpenNotify.MODNAME,
	serverSideOnly = true,
	acceptableRemoteVersions = "*")
public class ServerOpenNotify {
	public static final String MODID = "serveropennotify";
	public static final String VERSION = "1.0.1";
	public static final String MODNAME = "Server Open Notify";

	@EventHandler
	public void preInit(FMLPreInitializationEvent event) {
		MinecraftForge.EVENT_BUS.register(this);
	}

	@Config(modid = MODID, name = MODID, type = Type.INSTANCE)
	public static class CONFIG {
		public static boolean enableOpen = true;
		public static boolean enableClose = true;
		public static String[] messagesOpen = {"The server is ready."};
		public static String[] messagesClose = {"The server is stopped."};

		@Comment({
			"Webhook URLs",
			"Now supports Discord only."
		})
		public static String[] urlWebhooks = {};
	}

	Logger logger = LogManager.getLogger(MODID);

	@EventHandler
	public void serverStarted(FMLServerStartedEvent event) {
		Random random = new Random();
		String selectedMessage = CONFIG.messagesOpen[random.nextInt(CONFIG.messagesOpen.length)];
		logger.info(selectedMessage);
		if (CONFIG.enableOpen) {
			for (String wh : CONFIG.urlWebhooks){
				httpPostJson(wh, "{\"content\": \"" + selectedMessage + "\"}");
			}
		}
	}

	@EventHandler
	public void serverClosed(FMLServerStoppedEvent event) {
		Random random = new Random();
		String selectedMessage = CONFIG.messagesClose[random.nextInt(CONFIG.messagesClose.length)];
		logger.info(selectedMessage);
		if (CONFIG.enableClose) {
			for (String wh : CONFIG.urlWebhooks){
				httpPostJson(wh, "{\"content\": \"" + selectedMessage + "\"}");
			}
		}
	}

	private static String httpPostJson(String url_string, String json) {
		HttpsURLConnection uc;
		try {
			URL url = new URL(url_string);
			uc = (HttpsURLConnection) url.openConnection();
			uc.setRequestMethod("POST");
			uc.setUseCaches(false);
			uc.setDoOutput(true);
			uc.setRequestProperty("User-Agent", "Java");
			uc.setRequestProperty("Content-Type", "application/json; charset=UTF-8");
			OutputStreamWriter out = new OutputStreamWriter(new BufferedOutputStream(uc.getOutputStream()), StandardCharsets.UTF_8);
			out.write(json);
			out.close();

			BufferedReader in = new BufferedReader(new InputStreamReader(uc.getInputStream()));
			String line = in.readLine();
			StringBuilder body = new StringBuilder();
			while (line != null) {
				body.append(line);
				line = in.readLine();
			}
			uc.disconnect();
			return body.toString();
		} catch (IOException e) {
			e.printStackTrace();
			return "client - IOException : " + e.getMessage();
		}
	}
}
