import java.awt.BorderLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import javax.swing.table.DefaultTableModel;
import java.io.*;
import java.util.*;
import javax.swing.*;


public class EmployeePairs
{
	public static final double DIVISOR_TO_DAYS = 1000 * 60 * 60 * 24;
	public static Map<String, Map<String, Pair<Date, Date>>> projects = new HashMap<>();

	public static void main(String[] args)
	{
		var ui = new EmployeePairUI();
	}
	
	public static ArrayList<String[]> getLongestWorkingPair(Map<String, Map<String, Pair<Date, Date>>> projects)
	{
		Map<String, Map<String, Long>> commonDays = new HashMap<String, Map<String, Long>>();

		for (String project: projects.keySet())
		{
			for (String employee1: projects.get(project).keySet())
			{
				for (String employee2: projects.get(project).keySet())
				{
					if (!employee1.equals(employee2))
					{
						if (!commonDays.containsKey(employee1))
						{
							commonDays.put(employee1, new HashMap<>());
						}
						
						long timeDifference = getTimeDifference(projects, project, employee1, employee2);				
						if (timeDifference > 0)
						{
							Map<String, Long> emp1Days = commonDays.get(employee1);
							long previousVal = emp1Days.get(employee2) == null ? 0 : emp1Days.get(employee2);
							emp1Days.put(employee2, previousVal + timeDifference);
						}
					}
				}
			}
		}

		long longestTime = 0;
		String[] employeeIDs = {"", ""};
		for (String employee1: commonDays.keySet())
		{
			for (String employee2: commonDays.get(employee1).keySet())
			{
				Long daysWorkedTogether = (long) Math.ceil(commonDays.get(employee1).get(employee2) / DIVISOR_TO_DAYS);
				if (daysWorkedTogether > longestTime)
				{
					longestTime = daysWorkedTogether;
					employeeIDs[0] = employee1;
					employeeIDs[1] = employee2;
				}
			}
		}

		var details = new ArrayList<String[]>();

		for (String project: projects.keySet())
		{
			for (String employee1: projects.get(project).keySet())
			{
				for (String employee2: projects.get(project).keySet())
				{
					if (employee1.equals(employeeIDs[0]) && employee2.equals(employeeIDs[1]))
					{
						long timeDifference = getTimeDifference(projects, project, employee1, employee2);		

						if (timeDifference > 0)
						{
							String[] arr = new String[]{
								employee1, employee2, project,
								Long.toString((long)(timeDifference / DIVISOR_TO_DAYS))
							};
							details.add(arr);
						}
					}
				}
			}
		}
		
		return details;
	}
	
    public static ArrayList<String> readCSVFile(String fileName)
    {
    	var employeeData = new ArrayList<String>();
        try (BufferedReader br = new BufferedReader(new FileReader(fileName)))
        {
            String employeeDetails;
            while ((employeeDetails = br.readLine()) != null)
            {
                employeeData.add(employeeDetails);
            }
        } catch (IOException exc)
        {
            exc.printStackTrace();
        }

        return employeeData;
    }
    
    public static Date parseDate(String dateString)
    {
    	// Other formats are also available
    	String[] formatStrings = new String[] {
			"dd-MMM-yyyy", "dd.MM.yyyy", "yyyy-MM-dd",
			"dd-MMMM-yyyy", "dd/MM/yyyy"
		};

    	for (String formatString: formatStrings)
    	{
	    	try
	    	{
	    		return new SimpleDateFormat(formatString).parse(dateString);
	    	} catch (ParseException exc)
	    	{
	    	}
    	}
    	
    	return null;
    }
    
    public static long getTimeDifference(
		Map<String, Map<String, Pair<Date, Date>>> projects,
		String project, String employee1, String employee2
	)
    {
    	Pair<Date, Date> pairEmp1 = projects.get(project).get(employee1);
		Pair<Date, Date> pairEmp2 = projects.get(project).get(employee2);

		long startDateEmp1 = pairEmp1.first.getTime();
		long endDateEmp1 = pairEmp1.second.getTime();
		long startDateEmp2 = pairEmp2.first.getTime();
		long endDateEmp2 = pairEmp2.second.getTime();

		long startDate = startDateEmp1 > startDateEmp2 ? startDateEmp1 : startDateEmp2;
		long endDate = endDateEmp1 < endDateEmp2 ? endDateEmp1 : endDateEmp2;
		
		return endDate - startDate;
    }
    
    public static void fillProjectsData(ArrayList<String> employeeData)
    {
		for (String data: employeeData)
		{
			String[] person_info = data.split(",");
			String employeeID = person_info[0].trim();
			String projectID = person_info[1].trim();
			
			String dateFromStr = person_info[2].trim();
			String dateToStr = person_info[3].trim().equalsIgnoreCase("null") ? 
					new SimpleDateFormat("YYYY-MM-dd").format(new Date()) : person_info[3].trim();

			Date dateFrom = EmployeePairs.parseDate(dateFromStr);
			Date dateTo = EmployeePairs.parseDate(dateToStr);

			if (!EmployeePairs.projects.containsKey(projectID))
			{
				EmployeePairs.projects.put(projectID, new HashMap<>());
			}
			
			EmployeePairs.projects.get(projectID).put(employeeID, new Pair<Date, Date>(dateFrom, dateTo));
		}
    }
}

class EmployeePairUI extends JFrame
{
	private static final long serialVersionUID = 1L;
	private JFileChooser fileChooser;
    private JButton openButton;
    private JTable dataTable;
    private JLabel totalDaysLabel;

    public EmployeePairUI()
    {
        setTitle("Best Employee Pair");
        setSize(600, 400);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLayout(new BorderLayout());
        
        fileChooser = new JFileChooser();
        openButton = new JButton("Open File");
        openButton.addActionListener(new ActionListener()
        {
            public void actionPerformed(ActionEvent event)
            {
                int returnValue = fileChooser.showOpenDialog(null);
                if (returnValue == JFileChooser.APPROVE_OPTION)
                {
                    File selectedFile = fileChooser.getSelectedFile();
                    ArrayList<String> employeeData = EmployeePairs.readCSVFile(selectedFile.getAbsolutePath());
                    
                    DefaultTableModel model = new DefaultTableModel();
                    model.addColumn("Employee ID #1");
                    model.addColumn("Employee ID #2");
                    model.addColumn("Project ID");
                    model.addColumn("Days worked");

                    try
                    {
                    	EmployeePairs.fillProjectsData(employeeData);
                		
                		ArrayList<String[]> longestWorkingPair = EmployeePairs.getLongestWorkingPair(EmployeePairs.projects);
                		long totalDays = 0;
                		
                		for (int i = 0; i < longestWorkingPair.size(); i++)
                		{
                			model.addRow(new Object[] {
            					longestWorkingPair.get(i)[0], longestWorkingPair.get(i)[1],
            					longestWorkingPair.get(i)[2], longestWorkingPair.get(i)[3]
        					});
                			totalDays += Long.parseLong(longestWorkingPair.get(i)[3]);
                		}
                        
                        dataTable.setModel(model);
                        
                        totalDaysLabel.setText("Total Days Worked Together: " + totalDays);
                    } catch (Exception exc)
                    {
                        exc.printStackTrace();
                    }
                }
            }
        });
        
        add(openButton, BorderLayout.NORTH);
        
        dataTable = new JTable();
        JScrollPane scrollPane = new JScrollPane(dataTable);
        add(scrollPane, BorderLayout.CENTER);
        
        totalDaysLabel = new JLabel("");
        add(totalDaysLabel, BorderLayout.SOUTH);
        
        setVisible(true);
    }
}

class Pair<A, B>
{
	public A first;
	public B second;
	
	public Pair(A first, B second)
	{
		this.first = first;
		this.second = second;
	}
}
