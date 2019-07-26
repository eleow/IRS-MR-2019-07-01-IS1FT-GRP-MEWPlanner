package org.mew.domain;

import java.io.File;
import java.io.FileReader;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import javax.swing.JFileChooser;
import javax.swing.filechooser.FileNameExtensionFilter;
import javax.swing.filechooser.FileSystemView;

import org.mew.domain.FoodItem.FoodType;
import org.mew.domain.MealSlot.Meal;
import org.optaplanner.core.api.domain.solution.PlanningEntityCollectionProperty;
import org.optaplanner.core.api.domain.solution.PlanningScore;
import org.optaplanner.core.api.domain.solution.PlanningSolution;
import org.optaplanner.core.api.score.buildin.hardsoft.HardSoftScore;

import com.opencsv.CSVReader;

@PlanningSolution
public class MealSolution implements Serializable {
	
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private static MealSolution single_instance = null;  // make MealSolution a singleton	
	private HardSoftScore score;
	private int mode = 1; // 0 - manual mode. 1 - automatic mode
	public int debug_mode = 1;

	List<FoodItem> foodDB;
	List<MealSlot> mealsFor1Day; // List of 3 Meals. Each meal can have 1 drink, 1 main, up to 2 sides
	
	List<Integer> foodIds;
	TargetValues targets;
	
	// make constructor private for singleton
	private MealSolution() {
		readCSVDatabase("FoodDatabaseInput.csv");
//		readCSVDatabase("D:\\Users\\Edmund\\Desktop\\FoodDatabaseInput.csv");
		//generateFoodDB();		
		initialiseUnusedIds();
		targets = new TargetValues();
	}
	
    // static method to create instance of Singleton class 
    public static MealSolution getInstance() 
    { 
        if (single_instance == null) 
            single_instance = new MealSolution(); 
  
        return single_instance; 
    } 
	
	private void readCSVDatabase(String filePath) {
		
		if (null != foodDB) return; 
		if (mode == 0) {
			JFileChooser jfc = new JFileChooser(FileSystemView.getFileSystemView().getHomeDirectory());
			jfc.setDialogTitle("Select food database file");
			jfc.setAcceptAllFileFilterUsed(false);
			jfc.addChoosableFileFilter(new FileNameExtensionFilter("CSV files", "csv"));
			int returnValue = jfc.showOpenDialog(null);
			
			if (returnValue == JFileChooser.APPROVE_OPTION) {
				File selectedFile = jfc.getSelectedFile();
				filePath = selectedFile.getAbsolutePath();
				System.out.println(selectedFile.getAbsolutePath());
			}			
		}
		
		try {					
			String fullFilePath = new File("").getAbsolutePath().concat("/" + filePath);
			
			FileReader filereader = new FileReader(fullFilePath);
			CSVReader csvReader = new CSVReader(filereader);
			String[] nextRecord;
			int linenum = 1;
			int foodId = 1;
			
			foodDB = new ArrayList<FoodItem>();
			foodDB.add(new FoodItem(0, FoodType.OTHERS, "Empty", 0f, 0f, 0f, 0f, 0f, -1)); // Empty item
			
			// Read data line by line
			while ((nextRecord = csvReader.readNext()) != null) {
				if (linenum != 1) {
					FoodType FTYPE = FoodType.OTHERS;
					switch(nextRecord[4]) {
						case "Drink": FTYPE = FoodType.BEVERAGE; break;
						case "Main": FTYPE = FoodType.MAIN; break;
						case "Side": FTYPE = FoodType.SIDE; break;
						case "Breakfast": FTYPE = FoodType.BREAKFAST_MAIN; break;
						case "BreakfastSide": FTYPE = FoodType.BREAKFAST_SIDE; break;
						default: FTYPE = FoodType.OTHERS; break;
					}
					
					if (FTYPE != FoodType.OTHERS) {
					
						String FNAME = nextRecord[0];
						float FCALORIES = Float.valueOf(nextRecord[19]);
						float FNA = Float.valueOf(nextRecord[15]);
						float FCARBO = Float.valueOf(nextRecord[16]);
						float FFAT = Float.valueOf(nextRecord[17]);
						float FPROTEIN = Float.valueOf(nextRecord[18]);
						String FPLACE = nextRecord[5];
						int FRECENCY = FTYPE == FoodType.BEVERAGE ? -1: 7;
												
						//System.out.println(linenum);
						FoodItem item = new FoodItem(foodId++, FTYPE, FNAME, FCALORIES, FNA, FCARBO, FFAT, FPROTEIN, FRECENCY);
						item.place = FPLACE;
						foodDB.add(item);
					}
					
				}
				linenum++;
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	public void generateFoodDB() {
		
		if (null == foodDB) {
			foodDB = new ArrayList<FoodItem>();
			// create some food items for testing
			int count = 1;
			foodDB.add(new FoodItem(0, FoodType.OTHERS, "Empty", 0f, 0f, 0f, 0f, 0f, -1)); // Empty item
			
			foodDB.add(new FoodItem(count++, FoodType.BEVERAGE, "Milo", 120.18f, 51.05f, 87.80f, 15.03f, 17.35f, -1));
			foodDB.add(new FoodItem(count++, FoodType.BEVERAGE, "Green Tea", 35f, 7.5f, 35f, 0f, 0f, -1));
			foodDB.add(new FoodItem(count++, FoodType.BEVERAGE, "Brewed Coffee", 3.7f, 3.1f, 2.94f, 0f, 0.76f, -1));
			foodDB.add(new FoodItem(count++, FoodType.BEVERAGE, "Bubble Tea with Pearls", 441.33f, 46.95f, 441.33f, 0f, 0f, -1));
			
			foodDB.add(new FoodItem(count++, FoodType.MAIN, "Fish and Chips", 849.56f, 624.44f, 427f, 263.35f, 159.21f, 7));
			foodDB.add(new FoodItem(count++, FoodType.MAIN, "Fish ball mee pok dry", 481.5f, 2551.5f, 276.91f, 62f, 142.59f, 7));
			foodDB.add(new FoodItem(count++, FoodType.MAIN, "Zinger Burger", 632.1f, 1167.6f, 264.65f, 212.4f, 155.04f, 7));
			foodDB.add(new FoodItem(count++, FoodType.MAIN, "Zha Jiang Mian", 669.9f, 1404.48f, 392.85f, 143.79f, 133.26f, 7));
			foodDB.add(new FoodItem(count++, FoodType.MAIN, "Fried Rice", 499.65f, 1850.91f, 306.17f, 102.24f, 91.24f, 7));
			foodDB.add(new FoodItem(count++, FoodType.MAIN, "Wanton noodles dry", 411.06f, 1454.24f, 278.01f, 66.63f, 96.42f, 7));
			foodDB.add(new FoodItem(count++, FoodType.MAIN, "Wanton noodles soup", 318.04f, 1969.63f, 169.27f, 32.4f, 116.37f, 7));
			foodDB.add(new FoodItem(count++, FoodType.MAIN, "Nasi Goreng", 741.57f, 1466.53f, 503.61f, 132.80f, 105.16f, 7));
			
			foodDB.add(new FoodItem(count++, FoodType.SIDE, "Papaya slice", 76.43f, 15.75f, 71.25f, 1.06f, 4.13f, 7));
			foodDB.add(new FoodItem(count++, FoodType.SIDE, "Watermelon slice", 24.69f, 2.67f, 20.88f, 1.04f, 2.77f, 7));
			foodDB.add(new FoodItem(count++, FoodType.SIDE, "Deep fried carrot cake", 179.4f, 475.8f, 132.02f, 30.95f, 16.42f, 7));						
		}
		
	}
	
	public void initialiseUnusedIds() {
		if (null == foodIds) {
			foodIds = new ArrayList<Integer>();
			for (FoodItem i: getFoodDB()) {
				foodIds.add(Integer.valueOf(i.id));
			}
		}
	}
	
	public void initialiseMealFor1Day() {
		if (null == mealsFor1Day) {
			mealsFor1Day = new ArrayList<MealSlot>();
	
			// breakfast
			mealsFor1Day.add(new MealSlot(Meal.BREAKFAST, FoodType.BEVERAGE));
			mealsFor1Day.add(new MealSlot(Meal.BREAKFAST, FoodType.BREAKFAST_MAIN));
			mealsFor1Day.add(new MealSlot(Meal.BREAKFAST, FoodType.BREAKFAST_SIDE));
			mealsFor1Day.add(new MealSlot(Meal.BREAKFAST, FoodType.BREAKFAST_SIDE));
			
			// lunch
			mealsFor1Day.add(new MealSlot(Meal.LUNCH, FoodType.BEVERAGE));
			mealsFor1Day.add(new MealSlot(Meal.LUNCH, FoodType.MAIN));
			mealsFor1Day.add(new MealSlot(Meal.LUNCH, FoodType.SIDE));
//			mealsFor1Day.add(new MealSlot(Meal.LUNCH, FoodType.SIDE));
			
			// dinner
			mealsFor1Day.add(new MealSlot(Meal.DINNER, FoodType.BEVERAGE));
			mealsFor1Day.add(new MealSlot(Meal.DINNER, FoodType.MAIN));
			mealsFor1Day.add(new MealSlot(Meal.DINNER, FoodType.SIDE));
//			mealsFor1Day.add(new MealSlot(Meal.DINNER, FoodType.SIDE));
		}
	}
	
//	@ValueRangeProvider(id = "foodIdRange")
//	@ProblemFactCollectionProperty
//	public List<Integer> getFoodIds() {
//		return this.foodIds;
//	}
	
	public List<FoodItem> getFoodDB() {
		return foodDB;
	}

	public void setFoodDB(List<FoodItem> foodDB) {		
		this.foodDB = foodDB;
	}

	@PlanningEntityCollectionProperty
	public List<MealSlot> getMealsFor1Day() {
		if (null == mealsFor1Day)
			initialiseMealFor1Day();
		return mealsFor1Day;
	}

	public void setMealsFor1Day(List<MealSlot> mealsFor1Day) {
		this.mealsFor1Day = mealsFor1Day;
	}
	
	
	@PlanningScore
    public HardSoftScore getScore() {
 //       if (null==MealPlannerApp.scoreDirector)
  //          System.out.println("WOOPS YOU SHOULD EXPECT SOME ISSUES HERE");
 //       HardSoftScore hardSoftScore = (HardSoftScore)MealPlannerApp.scoreDirector.calculateScore();
 //       System.out.println("SCORE "+ hardSoftScore.toString() );
//        return hardSoftScore;
        if (null != score && debug_mode == 1)
          System.out.println("SCORE "+ score.toString() );
        return score;
    }

    public void setScore(HardSoftScore score) {
        this.score = score;
    }

	public Collection<? extends Object> getProblemFacts() {
		 List<Object> facts = new ArrayList<Object>();
		return facts;
	}

	public TargetValues getTargets() {
		return targets;
	}

	public void setTargets(TargetValues targets) {
		this.targets = targets;
	}
	
}
