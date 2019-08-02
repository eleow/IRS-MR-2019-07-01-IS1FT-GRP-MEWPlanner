package org.mew;

import java.io.File;

import org.ini4j.OptionMap;
import org.ini4j.Wini;
import org.mew.domain.FoodItem;
import org.mew.domain.FoodItem.FoodType;
import org.mew.domain.MealSlot;
import org.mew.domain.MealSolution;
import org.mew.domain.TargetValues;
import org.optaplanner.core.api.solver.Solver;
import org.optaplanner.core.api.solver.SolverFactory;
import org.optaplanner.core.impl.score.director.ScoreDirector;

public class MealPlannerApp {
//    public static final String SOLVER_CONFIG_XML = "org/mew/solver/mealPlannerSolverConfig.xml";
	public static final String SOLVER_CONFIG_XML = "mealPlannerSolverConfig.xml"; // in eclipse, this is relative to
																					// "resource" folder
	public static ScoreDirector<MealSolution> scoreDirector;
//	public static int printCount = 0;	
	private static int debug_mode = 1;

	public static void main(String[] args) {
		TargetValues targets = null;
		int numDays = 7;
		
		if (args.length > 0) {
			try {
				String fullFilePath = new File("").getAbsolutePath().concat("/" + args[0]);				
//				FileReader filereader = new FileReader(fullFilePath);
				Wini ini = new Wini(new File(fullFilePath));
				
				OptionMap mapTargets = ini.get("targets");
				float cal = mapTargets.get("sp_calories", float.class, 2000f);
				float dev_cal = mapTargets.get("dev_calories", float.class, 0.05f);
				float max_sodium = mapTargets.get("max_sodium", float.class, 2300f);
				int max_history = mapTargets.get("max_history", int.class, 7);
				targets = new TargetValues(cal, dev_cal, max_sodium, 0f, 0f, 0f, max_history);
						
				numDays = mapTargets.get("days", int.class, 7);
				debug_mode = ini.get("settings", "debug_mode", int.class);
				
				System.out.println(targets.toString());
				
			} catch (Exception e) {
				e.printStackTrace();
			}
			
		} else {
			 targets = new TargetValues(2072f, 0.05f, 2300f, 0f, 0f, 0f, 7);
		}

		SolverFactory<MealSolution> solverFactory = SolverFactory.createFromXmlResource(SOLVER_CONFIG_XML);
		Solver<MealSolution> solver = solverFactory.buildSolver();

		MealSolution mealSolution = MealSolution.getInstance();
		mealSolution.setTargets(targets);
		mealSolution.debug_mode = debug_mode;
		
		if (debug_mode == 0) System.out.println("Day,Meal,Type,Name,Calories,Carbohydrates,Fats,Protein,Sodium,Serving Size");

		// Get solutions
		for (int i = 1; i <= numDays; ++i) {
			// scoreDirector.setWorkingSolution(mealSolution);

			if (debug_mode == 1) System.out.println("\n\nSolving for Day " + i);						
			solver.solve(mealSolution);

			MealSolution bestMealSolution = (MealSolution) solver.getBestSolution();
			printSolutionUpdateRecency(bestMealSolution, i);
		}
	}

	static void printSolutionUpdateRecency(MealSolution best, int runNumber) {

		// Set recency to 0 for items that have just been used
		best.getMealsFor1Day().stream()
			.map( m -> m.getFoodId())
			.forEach(id -> {
				FoodItem item = best.getFoodDB().get(id);
				if (item.recency >= 0) best.getFoodDB().get(id).recency = 0;
			});
		
		// if drink is empty, default to plain water
		for (MealSlot s : best.getMealsFor1Day()) {
			if (s.getType() == FoodType.BEVERAGE && s.getFoodId() == 0) s.setFoodId(best.plainWaterId);
		}

		// Print solution either for debugging or as output to frontend
		printSolution(best, runNumber);
		
		
		// Update recency for next cycle
		for (FoodItem i : best.getFoodDB()) {
			if (i.recency >= 0 && i.recency < best.getTargets().max_history)
				i.recency++;
		}
		
	}
	
	static void printSolution(MealSolution best, int runNumber) {
		
		float na = 0;
		float cal = 0;
		float tcal = best.getTargets().calories_kcal;
		float tna = best.getTargets().sodium_mg;
		
		// Printing for Debugging Mode
		if (debug_mode == 1) {
			System.out.println("\n\nSolution for Day " + runNumber);
			
			for (MealSlot s : best.getMealsFor1Day()) {
				int id = s.getFoodId();							
				FoodItem item = best.getFoodDB().get(id);
				
				if (s.getType() == FoodType.BEVERAGE) System.out.println();	
				System.out.println(s.getType().getValue() + "_" + item.type.getValue() + " " + item.recency + ":"
						+ item.name + " (Cal:" + item.calories + ", C:" + item.carbohydrates_kcal + ", F:" + item.fat_kcal
						+ ", P:" + item.protein_kcal + ", Na:" + item.sodium + ")" + item.place + " (" + item.serving + ")");
				
				na += item.sodium;
				cal += item.calories;

				// Update recency for food items that have been chosen
//				if (item.recency >= 0)
//					best.getFoodDB().get(id).recency = 0;
			}

			System.out.println();
			System.out.println("Total calories: " + cal + "/" + tcal + ". Total sodium: " + na + "/" + tna);
			System.out.println(best.getScore().toString());
		}
		else {
			for (MealSlot s : best.getMealsFor1Day()) {
				
				int id = s.getFoodId();
				if (id != 0) {
					FoodItem item = best.getFoodDB().get(id);
					String fName = item.name.replaceAll(",", "");
								
					//
					String line = String.format("%d,%d,%d,%s,%.2f,%.2f,%.2f,%.2f,%.2f,%s", 
							runNumber, s.meal.getValue(), item.type.getValue(), fName, 
							item.calories, item.carbohydrates_kcal, item.fat_kcal, item.protein_kcal, item.sodium, item.serving);
					System.out.println(line);
				}
				
			}
		}
	}

}
