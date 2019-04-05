import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.stream.Collectors;

public class TestThorPerformance { 
	Map<String, Integer> testConfigMap = new HashMap<String,Integer>();
	List<String> labs=new ArrayList<String>();
	List<String> timeslots=new ArrayList<String>();
	List<String> candidates= new ArrayList<String>();
	
	public TestThorPerformance(HashMap<String, String> configMap) {
		setTestConfigMap(configMap);
	}
	
	void setTestConfigMap(HashMap<String, String> configMap){
		ArrayList<String> testConfigurationKeys = new ArrayList<String>() {
			private static final long serialVersionUID = 1L;
			{
			    add("days");
			    add("hoursPerDay");
			    add("noOfLabs");
			    add("noOfDemonstrators");
			    add("maxNumberOfDemonstratorApplied");
			    add("minNumberofDemonstratorApplied");
			    add("maxDemonstratorCountPerLab");
			    add("minDemonstratorCountPerLab");
			    add("minRanking");
			    add("maxRanking");
			}};
		for (String testConfigurationKey : testConfigurationKeys) {
			if(!configMap.containsKey(testConfigurationKey)) {
				System.out.println(testConfigurationKey + " does not exist in configuration file. Exiting.");
				System.exit(1);
			}
			else {
				testConfigMap.put(testConfigurationKey, Integer.parseInt(configMap.get(testConfigurationKey)));
			}
		}
	}	
	
	public void performTest() {
		HashMap <String,String> labAndTimeslotmap=getLabAndTimeslotmap();
		initializeCandidateList();
		HashMap <String,Map<String,Integer>> labWiseDemonstratorRankingMap = getLabWiseCandidateRankingMap();
		HashMap <String,Integer> labAndNumberOfDemonstratorsmap=getLabAndNumberOfDemonstratorsmap();		
		ConstraintsMakerAndAsserter asserter = new ConstraintsMakerAndAsserter(labWiseDemonstratorRankingMap, labAndNumberOfDemonstratorsmap, labAndTimeslotmap);
		asserter.getSolution();
	}
	
	private void initializeCandidateList() {
		for(int i =0;i<testConfigMap.get("noOfDemonstrators");i++) {
			candidates.add(generateRandomString());
		}
	}
	
	private HashMap<String, String> getLabAndTimeslotmap() {
		for(int i =0;i<(testConfigMap.get("days")*testConfigMap.get("hoursPerDay"));i++) {
			String timeSlotId=generateRandomString();
			timeslots.add(timeSlotId);
		}
		HashMap<String, String> labVsTimeslot = new HashMap<String, String>();
		for(int i =0;i<testConfigMap.get("noOfLabs");i++) {
			String labId=generateRandomString();
			labs.add(labId);
			String timeSlodId= GetRandomFromList(timeslots);
			labVsTimeslot.put(labId, timeSlodId);
		}
		return labVsTimeslot;
	}

	private HashMap<String, Integer> getLabAndNumberOfDemonstratorsmap() {
		HashMap <String,Integer> labVsNumberOfDemonstrators=new HashMap<String,Integer>();
		for(String labName: labs) {
			labVsNumberOfDemonstrators.put(labName, getRandomNumber(testConfigMap.get("minDemonstratorCountPerLab"), testConfigMap.get("maxDemonstratorCountPerLab")));
		}
		
		return labVsNumberOfDemonstrators;
	}
	
	private String generateRandomString() {
		int length = 8;
		String chars = "ABCDEFGHIJKLMNOPQRSTUVWXYZ"
		             + "abcdefghijklmnopqrstuvwxyz"
		             + "0123456789";
		String str = new Random().ints(length, 0, chars.length())
		                         .mapToObj(i -> "" + chars.charAt(i))
		                         .collect(Collectors.joining());
		return str;
	}
	
	private String GetRandomFromList(List<String> myList) {
		if(!myList.isEmpty()) {
			return myList.get(new Random().nextInt(myList.size()));
		}
	        return null;
	}
	
	private int getRandomNumber(int min,int max) {
		return (new Random().nextInt(max-min)+min);
	}
	
	private HashMap<String, Map<String, Integer>> getLabWiseCandidateRankingMap() {
		HashMap <String,Map<String,Integer>> labVsdemonstratorRankings=new HashMap <String,Map<String,Integer>>();
		for(String labName: labs) {
			Map<String, Integer> demonstratorVsRank= new HashMap<String, Integer>();
			int noOfAppliedDemonstrators=getRandomNumber(testConfigMap.get("minNumberofDemonstratorApplied"),testConfigMap.get("maxNumberOfDemonstratorApplied"));
			for(int j =0;j<noOfAppliedDemonstrators;j++) {
				String demonstratorId=GetRandomFromList(candidates);
				candidates.add(demonstratorId);
				int ranking = getRandomNumber(testConfigMap.get("minRanking"),testConfigMap.get("maxRanking"));
				demonstratorVsRank.put(demonstratorId, ranking);
			}
			labVsdemonstratorRankings.put(labName, demonstratorVsRank);			
		}
		return labVsdemonstratorRankings;
	}

}
