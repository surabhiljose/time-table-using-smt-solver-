To run this project, sh runme.sh

Output will be in the target folder with a time stamp. Hence old outputs arent replaced.

Z3 jar file is present in bin. Jar file should not not be moved as the bin is the class path.

Configuration.properties file has the properties for running and testing the project. There are two test modes. Comment one while using the other test mode. 


-----------------------------------------------------------------------
performance test is a reflection of general use case scenario. Hashmaps mimicking inputs are randomly generated from the values given in configuration.properties.

This test uses all the properties in the configuration.properties
---------------------------------------------------------------------
quality test is a intense test where THOR can some times get stuck. Start with smaller numbers. Three properties given below are used in this test 

noOfLabs=200
noOfDemonstrators=50
minDemonstratorCountPerLab=4 (quality test considers this property as the required number of demonstrators in each labs)

-------------------------------------------------------------------


RunMode is always 'test' as creating input for time table is difficult. It is automatically created in both tests. Any java program can pass the
the three hashmaps to Thor and get the time table but manually creating the input hashmaps are painful. 



Properties to be put in configuration.properties. Change these values and run the shell script again to see the changes.

runMode=test
#testMode=performanceTest
testMode=qualityTest
days=5
hoursPerDay=6
noOfLabs=200
noOfDemonstrators=20
maxNumberOfDemonstratorApplied=16
minNumberofDemonstratorApplied=14
maxDemonstratorCountPerLab=6
minDemonstratorCountPerLab=15
minRanking=1
maxRanking=99
