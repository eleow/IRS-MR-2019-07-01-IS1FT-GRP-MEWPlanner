package org.mew.domain.score;

import java.util.List;

import org.mew.domain.FoodItem;
import org.mew.domain.MealSlot;
import org.mew.domain.MealSolution;
import org.optaplanner.core.impl.heuristic.selector.common.decorator.SelectionFilter;
import org.optaplanner.core.impl.score.director.ScoreDirector;

public class MealSlotTypeFilter implements SelectionFilter {
 
	public boolean accept(ScoreDirector scoreDirector, Object oSelection) {
		MealSolution currSolution = (MealSolution) scoreDirector.getWorkingSolution();
		List<FoodItem> foodDB = currSolution.getFoodDB();
		MealSlot selection = (MealSlot)oSelection;
		
//		if (selection.getType() == 2 && selection.getFoodId() == 0) return true;
//		else return false;
						
//		if (selection.getFoodId() != 0) {
//			
//			FoodItem food = foodDB.get(selection.getFoodId());
//			
//			// Type of food must match required type
//			return (food.type != selection.getType());
//		}
		//else if (selection.getType() == 2) return false; // mains should not be empty
		
		//System.out.println("type:" + selection.getType() + ", itemId:" + selection.getFoodId());
		return true;
	}

//	@Override
//	public boolean accept(ScoreDirector scoreDirector, Object selection) {
//		// TODO Auto-generated method stub
//		return true;
//	}
}
