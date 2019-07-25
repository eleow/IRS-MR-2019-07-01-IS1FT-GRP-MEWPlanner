package org.mew;

import org.mew.domain.FoodItem;
import org.mew.domain.FoodItem.FoodType;
import org.mew.domain.MealSlot;
import org.mew.domain.MealSolution;
import org.mew.domain.TargetValues;
import org.optaplanner.core.api.solver.Solver;
import org.optaplanner.core.api.solver.SolverFactory;
import org.optaplanner.core.impl.score.director.ScoreDirector;

public class MealPlannerApp {
    public static final String SOLVER_CONFIG_XML = "org/mew/solver/mealPlannerSolverConfig.xml";
    public static ScoreDirector<MealSolution> scoreDirector;
    public static int printCount = 0;
    
    public static void main(String[] args) {
    	
    	 //Solver solver = SolverFactory.createFromXmlResource(SOLVER_CONFIG_XML).buildSolver();
         //ScoreDirectorFactory<MealSolution> scoreDirectorFactory = solver.getScoreDirectorFactory();
    	 SolverFactory<MealSolution> solverFactory = SolverFactory.createFromXmlResource(SOLVER_CONFIG_XML);
    	 Solver<MealSolution> solver = solverFactory.buildSolver();
         //scoreDirector = scoreDirectorFactory.buildScoreDirector();
         
         MealSolution mealSolution = MealSolution.getInstance(); 
         mealSolution.setTargets(new TargetValues(2072f, 0.05f, 2300f, 0f, 0f, 0f, 7));
         
         // Get solutions
         int numDays = 7;
         for (int i = 1; i <= numDays; ++i ) {
             //scoreDirector.setWorkingSolution(mealSolution);
             
             System.out.println("\n\nSolving for Day " + i);
             solver.solve(mealSolution);
             
             MealSolution bestMealSolution = (MealSolution) solver.getBestSolution();
             System.out.println("\n\nSolution");
             
             printSolutionUpdateRecency(bestMealSolution);                                   
         }               
    }
    
    static void printSolutionUpdateRecency(MealSolution best) {

    	float na = 0;
    	float cal = 0;
    	float tcal = best.getTargets().calories_kcal;
    	float tna = best.getTargets().sodium_mg;
    	
    	for (MealSlot s: best.getMealsFor1Day()) {
    		int id = s.getFoodId();
    		FoodItem item = best.getFoodDB().get(id);
    		if (s.getType()== FoodType.BEVERAGE) System.out.println();
    		System.out.println(s.getType().getValue() + "_" + item.type.getValue() + " " + item.recency + ":" + item.name + " (Cal:" + item.calories + ", C:" + item.carbohydrates_kcal + ", F:" + item.fat_kcal + ", P:" + item.protein_kcal + ", Na:" + item.sodium + ")" + item.place);
    		
    		na += item.sodium;
    		cal += item.calories;
    		
    		// Update recency for food items that have been chosen
    		if (item.recency >= 0) best.getFoodDB().get(id).recency = 0;
    	}
    	
    	for (FoodItem i: best.getFoodDB()) {
    		if (i.recency >= 0 && i.recency < best.getTargets().max_history) i.recency++;
    	}
    	
    	System.out.println();
    	System.out.println("Total calories: " + cal + "/" + tcal + ". Total sodium: " + na + "/" + tna);
    	
    	System.out.println(best.getScore().toString());
    	
    	
//    	System.out.println(best.getFoodDB().get(19).getDetails());
    }

}
