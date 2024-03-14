import static org.junit.jupiter.api.Assertions.*;

import java.io.*;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;

import org.junit.jupiter.api.Test;

class EmployeePairTest {

	@Test
	public void testReadCSVFile()
	{
	    ArrayList<String> expectedData = new ArrayList<>();
	    expectedData.add("99,17,2017-11-08,2019-01-30");
	    expectedData.add("177,3,2014-04-16,2015-09-25");
	    expectedData.add("88,16,2009-07-07,2011-03-12");
	    expectedData.add("123,2,2016-01-23,2017-05-28");
	    expectedData.add("311,18,2018-08-19,2019-12-24");
	    
	    ArrayList<String> actualData = EmployeePairs.readCSVFile("/home/hmanolov/JavaProjects/EmployeePairs/src/testData.csv");
	    
	    assertEquals(expectedData, actualData);
	}
	
	@Test
	public void testParseDate()
	{
    	String[] formatStrings = {
			"dd-MMM-yyyy", "dd.MM.yyyy", "yyyy-MM-dd",
			"dd-MMMM-yyyy", "dd/MM/yyyy"
		};

	    String[] dateStrings = {
    		"30-Mar-2009", "30.03.2009", "2009-03-30",
			"30-March-2009", "30/03/2009"
		};
	    
		try
		{
			for (int i = 0; i < dateStrings.length; i++)
			{
				Date expectedDate = new SimpleDateFormat(formatStrings[i]).parse(dateStrings[i]);
				Date actualDate = EmployeePairs.parseDate(dateStrings[i]);
			    
			    assertEquals(expectedDate.getTime(), actualDate.getTime());
			}
		} catch (ParseException e)
		{
			e.printStackTrace();
		}
	}
	
	@Test
	public void testGetTimeDifference() {
	    Map<String, Map<String, Pair<Date, Date>>> projects = new HashMap<>();
	    
	    String projectID = "22";
	    String employeeID1 = "222";
	    String employeeID2 = "111";
	    
	    String[] emp1Dates = {"12/15/2012", "03/14/2024"};
	    String[] emp2Dates = {"03/12/2023", "03/11/2024"};

	    try
	    {
		    Date startDateEmp1 = new SimpleDateFormat("MM/dd/YYYY").parse(emp1Dates[0]);
		    Date endDateEmp1 = new SimpleDateFormat("MM/dd/YYYY").parse(emp1Dates[1]);
		    
		    Date startDateEmp2 = new SimpleDateFormat("MM/dd/YYYY").parse(emp2Dates[0]);
		    Date endDateEmp2 = new SimpleDateFormat("MM/dd/YYYY").parse(emp2Dates[1]);
		    
		    projects.put(projectID, new HashMap<String, Pair<Date, Date>>());
	
		    projects.get(projectID).put(employeeID1, new Pair<Date, Date>(startDateEmp1, endDateEmp1));
		    projects.get(projectID).put(employeeID2, new Pair<Date, Date>(startDateEmp2, endDateEmp2));
	
		    long expectedTimeDifference = 364;
		    long actualTimeDifference = EmployeePairs.getTimeDifference(projects, projectID, employeeID1, employeeID2);
		    actualTimeDifference = (long) Math.ceil(actualTimeDifference / EmployeePairs.DIVISOR_TO_DAYS);
		    
		    assertEquals(expectedTimeDifference, actualTimeDifference);
	    } catch (ParseException exc)
	    {
	    }
    }
	
	@Test
	void testReturnedResult()
	{
		ArrayList<String> employeeData = EmployeePairs.readCSVFile("/home/hmanolov/JavaProjects/EmployeePairs/src/employeeData.csv");
		String[][] expectedResult = {
			{"199", "218", "52", "650"},
			{"199", "218", "10", "825"},
			{"199", "218", "99", "365"},
			{"199", "218", "12", "472"}
		};

    	EmployeePairs.fillProjectsData(employeeData);
		
		ArrayList<String[]> longestWorkingPair = EmployeePairs.getLongestWorkingPair(EmployeePairs.projects);
		
		for (int i = 0; i < longestWorkingPair.size(); i++)
		{
			for (int j = 0; j < longestWorkingPair.get(i).length; j++)
			{
				assertEquals(longestWorkingPair.get(i)[j], expectedResult[i][j]);
			}
		}
	}

}
