import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.HashMap;

public class TimeTableScheduler {

	public HashMap<String, String> getConfigurationMap() {
		HashMap<String, String> configMap = new HashMap<String, String>();
		String configFileName = System.getProperty("user.dir") + "/configuration/configuration.properties";
		File configFile = new File(configFileName);
		if (configFile.exists()) {
			try {
				BufferedReader br = new BufferedReader(new FileReader(configFile));
				String configuration;
				while ((configuration = br.readLine()) != null) {
					String[] parts = configuration.split("=", 2);
					if (parts.length >= 2) {
						String key = parts[0].trim();
						String value = parts[1].trim();
						configMap.put(key, value);
					} else {
						System.out.println("ignoring line: " + configuration);
					}
				}
				br.close();
			} catch (IOException e) {
				System.out.println("Error occured while reading from " + configFileName);
				e.printStackTrace();
			}
		}
		return configMap;
	}

	private void runTest(HashMap<String, String> configMap) {
		if (!configMap.containsKey("testMode")) {
			System.out.println("Key: testMode is not present in configuration map");
			return;
		}
		switch (configMap.get("testMode")) {
		case "performanceTest":
			TestThorPerformance testThor = new TestThorPerformance(configMap);
			testThor.performTest();
			break;
		case "qualityTest":
			TestThorQuality testThorQuality = new TestThorQuality(configMap);
			testThorQuality.performTest();
			break;
		default:
			System.out.println(
					"testmode is not rightly given in the configuration file. \n example testMode=performanceTest");
		}
	}

	private boolean runningInTestMode(HashMap<String, String> configMap) {
		if (configMap.containsKey("runMode") && (configMap.get("runMode").equals("test")))
			return true;
		return false;
	}

	public static void main(String[] args) {
		TimeTableScheduler timeTableScheduler = new TimeTableScheduler();
		HashMap<String, String> configMap = timeTableScheduler.getConfigurationMap();
		if (timeTableScheduler.runningInTestMode(configMap)) {
			timeTableScheduler.runTest(configMap);
		} 
	}

}
