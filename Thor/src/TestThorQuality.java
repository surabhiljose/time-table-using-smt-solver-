import java.util.HashMap;
import java.util.Map;

public class TestThorQuality {
	int noOfDemonstrators=10,noOfLabs=10,noOfDemonstratorsPerLab=10;
	
	Map<String, Integer> testConfigMap = new HashMap<String,Integer>();
	HashMap <String,Map<String,Integer>> labWiseCandidateRankingMap=new HashMap <String,Map<String,Integer>>();
	HashMap <String,Integer> labAndNumberOfDemonstratorsmap=new HashMap <String,Integer> ();
	HashMap <String,String> labAndTimeslotmap=new HashMap <String,String>();
	
	public TestThorQuality(HashMap<String, String> configMap) {
		setTestConfigMap(configMap);
	}
	
	void setTestConfigMap(HashMap<String, String> configMap) {
		if (configMap.containsKey("noOfDemonstrators") && configMap.containsKey("noOfLabs")&& configMap.containsKey("minDemonstratorCountPerLab")){
			noOfDemonstrators=Integer.parseInt(configMap.get("noOfDemonstrators"));
			noOfLabs=Integer.parseInt(configMap.get("noOfLabs"));
			noOfDemonstratorsPerLab=Integer.parseInt(configMap.get("minDemonstratorCountPerLab"));
		}
		else {
			System.out.println("noOfDemonstrators or noOfLabs or minDemonstratorCountPerLab is not set in configuration properties");
		}
	}
	
	public void performTest() {
		initializelabWiseCandidateRankingMap();
		System.out.println(labWiseCandidateRankingMap);
		initializelabAndNumberOfDemonstratorsmap();
		initializelabAndTimeslotmap();
		System.out.println(labAndTimeslotmap);

		ConstraintsMakerAndAsserter asserter = new ConstraintsMakerAndAsserter(labWiseCandidateRankingMap, labAndNumberOfDemonstratorsmap, labAndTimeslotmap);
		asserter.getSolution();
	}

	private void initializelabAndTimeslotmap() {
		for(int i =0;i<noOfLabs;i++) {
			labAndTimeslotmap.put("L_"+i, "Single_timeSlot");
		}
	}

	private void initializelabAndNumberOfDemonstratorsmap() {
		for(int i =0;i<noOfLabs;i++) {
			labAndNumberOfDemonstratorsmap.put("L_"+i, noOfDemonstratorsPerLab);
		}
	}

	private void initializelabWiseCandidateRankingMap() {
		for(int i =0;i<noOfLabs;i++) {
			labWiseCandidateRankingMap.put("L_"+i, new HashMap<String,Integer>());
			for(int j=0;j<noOfDemonstrators;j++) {
				labWiseCandidateRankingMap.get("L_"+i).put("D_"+j, j);
			}
		}	
	}

}
