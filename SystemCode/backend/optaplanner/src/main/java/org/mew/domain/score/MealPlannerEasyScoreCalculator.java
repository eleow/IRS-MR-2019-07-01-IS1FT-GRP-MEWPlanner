package org.mew.domain.score;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.stream.Collectors;

import org.mew.domain.FoodItem;
import org.mew.domain.FoodItem.FoodType;
import org.mew.domain.MealSlot;
import org.mew.domain.MealSlot.Meal;
import org.mew.domain.MealSolution;
import org.optaplanner.core.api.score.Score;
import org.optaplanner.core.api.score.buildin.hardsoft.HardSoftScore;
import org.optaplanner.core.impl.score.director.easy.EasyScoreCalculator;

public class MealPlannerEasyScoreCalculator implements EasyScoreCalculator<MealSolution>{

	public Score calculateScore(MealSolution solution) {
        int hardScore = 0;
        int softScore = 0;
				
		float target_cal = solution.getTargets().calories_kcal; //2200;
		float max_sodium = solution.getTargets().sodium_mg; //2300;
		float deviation_threshold = solution.getTargets().calories_deviation_threshold; //0.05f;
		float max_history = solution.getTargets().max_history;
		
//		float target_carbs = target_cal * 0.5f;
//		float carbs_deviation_threshold = target_cal * 0.05f;

		// Carbs should be 45-55% of total kcal/day
		float target_carbs = solution.getTargets().carbs_kcal_frac; //0.5f;	// of total calories in kcal
		float carbs_deviation_threshold = solution.getTargets().carbs_deviation_threshold; // 0.05f;
		float max_sugar = solution.getTargets().sugar_g; //30;


		List<MealSlot> meals = solution.getMealsFor1Day();
		Set<Integer> idSet = new HashSet<Integer>();
		float total_cal = 0;
		float total_sodium = 0;
		int dup_counter = 0;
		int mismatch_counter = 0;
		int main_counter = 0;
		int place_counter = 0;
		
		float total_carbs = 0;
		float carbs_distribution_penalty = 0; // if meal has carbs exceeding range, penalise by difference
		float total_sugar = 0;
		Map<Meal, Float> carbs_in_meal = new HashMap<Meal, Float>();
		
		int unknown_sugar = 0;
		
		for (MealSlot slot: meals) {
			int id = slot.getFoodId();
			FoodItem item = solution.getFoodDB().get(id);
			total_cal += item.calories;
			total_sodium += item.sodium;
			total_carbs += item.carbohydrates_kcal;
			if (item.sugar_g > 0) total_sugar += item.sugar_g;
			else unknown_sugar++;
			
			// check for duplicates
			if (id != 0 && idSet.add(id) == false) {
				dup_counter++;
			}
			
//			// check for mismatch
//			if (id != 0 && slot.getType() != item.type) {
//				mismatch_counter++;
//			}
			
			// check for mains
			if (item.type == FoodType.MAIN || item.type == FoodType.BREAKFAST_MAIN) main_counter++;
			
			// check for recency for items that have a cool-down
			if (item.recency >= 0 && item.recency < max_history) {
				hardScore += -(max_history - item.recency) * 100;
			}
			
			// check for carbs/meal distribution
			carbs_in_meal.put(slot.meal, 
					carbs_in_meal.getOrDefault(slot.meal, 0f) + item.carbohydrates_kcal);
		}
		
		// Penalise if carb distribution is skewed
		float carbs_mean = 0;
		float carbs_variance = 0;
		for (Float m : carbs_in_meal.values()) {
			carbs_mean += m;
		}
		carbs_mean /= 2.8;	// we want breakfast, lunch, dinner to be 0.8:1:1
		
		for (Entry<Meal, Float> m: carbs_in_meal.entrySet()) {
			if (m.getKey() == Meal.BREAKFAST) carbs_variance += Math.pow(m.getValue() - 0.8 * carbs_mean, 2);
			else carbs_variance += Math.pow(m.getValue() - carbs_mean, 2);
		}
		softScore -= carbs_variance;
		
		
		// Penalize if there are meals that are in different places
		place_counter += (getPlaceCounter(getSetPlaces(solution, meals, Meal.BREAKFAST)));
		place_counter += (getPlaceCounter(getSetPlaces(solution, meals, Meal.LUNCH)));
		place_counter += (getPlaceCounter(getSetPlaces(solution, meals, Meal.DINNER)));			
		hardScore += -place_counter * 100;
				
		// Penalize duplicates and mismatch
		hardScore += -((dup_counter + mismatch_counter) * 100);
		
		// Penalize similar foods in a meal 
		// TODO - Use word vector to ensure dissimilar foods
		//
		//
					
		// Allow deviation from target calories within a threshold, else penalize
		float cal_deviation = Math.abs((target_cal - total_cal)/(target_cal));
		if (cal_deviation > deviation_threshold) {
			hardScore += -(int)(cal_deviation * 1000);
		}
		
		float carbs_deviation = Math.abs((target_carbs * total_cal - total_carbs)/ total_cal);
		if (carbs_deviation > carbs_deviation_threshold) {
			hardScore += -(int)(carbs_deviation * 1000);
		}
				
		// Must always have total of 3 mains else penalize
		if (main_counter < 3) {
			hardScore += (main_counter - 3) * 100;
		}
		
		// Do not exceed sodium threshold
		float sodium_deviation = max_sodium - total_sodium;
		if (sodium_deviation < 0) {
			hardScore -= Math.abs(sodium_deviation) * 10;
		} 
		
		// total sugars should be < max_sugar
		if (total_sugar > max_sugar) {
			softScore -= Math.abs(total_sugar - max_sugar);
		}
		// slight penalty for unknown sugar 
		softScore -= unknown_sugar * 10;
		
	
//		System.out.print("|" + place_counter);;
		return HardSoftScore.valueOf(hardScore, softScore);
		
	}
	
	Set<String> getSetPlaces(MealSolution solution, List<MealSlot> meals, Meal m) {
				
		Set<String> setPlaces = meals.stream()
				.filter(x -> x.getFoodId() != 0 && x.meal == m)
				.map(x -> solution.getFoodDB().get(x.getFoodId()).place)
				.collect(Collectors.<String>toSet());
		return setPlaces;
	}
	
	int getPlaceCounter(Set<String> setPlaces) {
		return (setPlaces.size() > 1 ? setPlaces.size(): 0);
	}

}
