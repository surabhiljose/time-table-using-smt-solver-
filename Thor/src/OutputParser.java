import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.microsoft.z3.FuncDecl;
import com.microsoft.z3.Model;



public class OutputParser {
	
	
	final int DEMONSTRATOR_PART=1,LAB_PART=0;
	final String IS_ALOTTED="1";
	HashMap <String,Map<String,Integer>> labWiseCandidateRanking;
	
	void parseModel(Model model,HashMap <String,Map<String,Integer>> labWiseDemonstratorRankingMap) {
		labWiseCandidateRanking=labWiseDemonstratorRankingMap;
		FuncDecl[] results =model.getConstDecls();
		Map<String, List<String>> labsAndAlottedDemonstrators = new HashMap<String, List<String>>();
		for(int i=0;i<results.length;i++) {
			if(model.getConstInterp(results[i]).toString().equals(IS_ALOTTED))
			{
				String labId=getPart(results[i].getName().toString(),LAB_PART);
				String DemonstratorId=getPart(results[i].getName().toString(),DEMONSTRATOR_PART);
				if(!labsAndAlottedDemonstrators.containsKey(labId)) 
					labsAndAlottedDemonstrators.put(labId, new ArrayList<>());
				labsAndAlottedDemonstrators.get(labId).add(DemonstratorId);
			}			
		}
		String timeTable=convertToString(labsAndAlottedDemonstrators);
		writeToFile(timeTable);
	}

	private String getPart(String string,int part) {
		String[] allParts = string.split("-");
		return allParts[part];
	}
	
	private String convertToString(Map<String, List<String>> labsAndAlottedDemonstrators) {
		String timetable="";
		int noOfLabs=0;
		int sumOfRankingOfAllocatedDemonstrator=0;
		for (Map.Entry<String, List<String>> entry : labsAndAlottedDemonstrators.entrySet())
		{
			String LabId=entry.getKey();
		    timetable+="Lab Id : " +LabId+"\n";
		    int sumRankofLab=0;
		    int demonstratorCount=0;
		    for (String demonsrtatorId : entry.getValue()) {
		    	timetable+="\t\t" +demonsrtatorId+"\n";
		    	sumRankofLab=sumRankofLab+labWiseCandidateRanking.get(LabId).get(demonsrtatorId);
		    	demonstratorCount++;
			}
		    float RankingPerLab =sumRankofLab/demonstratorCount;
		    sumOfRankingOfAllocatedDemonstrator+=RankingPerLab;
		    noOfLabs++;
		    timetable+="------------------------------------------\n";
		}
		System.out.println("global average rank of a demonstrator = "+sumOfRankingOfAllocatedDemonstrator/noOfLabs);
		HashMap<String,String> configMap=new TimeTableScheduler().getConfigurationMap();
		if (configMap.get("testMode").equals("qualityTest")){
			//finding out the ideal rank.
			if (!configMap.containsKey("noOfDemonstrators") || !configMap.containsKey("noOfLabs")|| !configMap.containsKey("minDemonstratorCountPerLab"))
				return "configurations not found for quality testing";				
			int D=Integer.parseInt(configMap.get("noOfDemonstrators"));
			int k=Integer.parseInt(configMap.get("noOfLabs"));
			int l=Integer.parseInt(configMap.get("minDemonstratorCountPerLab"));
			int idealRank=(2*(D)-(k+l))/2;
			if (idealRank<0)
				return "give suitable values in the config file.";
			return "Quality Test. ideal Ranking = "+idealRank+"Average ranking = " +sumOfRankingOfAllocatedDemonstrator/noOfLabs;
		}
		else
		return timetable;
	}
	
	private void writeToFile(String timeTable) {
		String targetDirectoryName =System.getProperty("user.dir")+"/target";
		File targetDirectory = new File(targetDirectoryName);
		if (! targetDirectory.exists())
		    	targetDirectory.mkdir();
		File file = new File(targetDirectoryName + "/timetable"+Calendar.getInstance().getTimeInMillis()+".txt" );
		try {
			FileWriter fw = new FileWriter(file.getAbsoluteFile());
	        BufferedWriter bw = new BufferedWriter(fw);
	        bw.write(timeTable);
	        bw.close();
		} catch (IOException e) {
			System.out.println("Error occured while writing output");
			e.printStackTrace();
		}
	}
	
}
  