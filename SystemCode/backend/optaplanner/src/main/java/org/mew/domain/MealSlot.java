package org.mew.domain;

import java.util.HashMap;
import java.util.Set;
import java.util.stream.Collectors;

import org.mew.domain.FoodItem.FoodType;
import org.mew.domain.comparators.MealSlotDifficultyComparator;
import org.optaplanner.core.api.domain.entity.PlanningEntity;
import org.optaplanner.core.api.domain.solution.drools.ProblemFactCollectionProperty;
import org.optaplanner.core.api.domain.valuerange.ValueRangeProvider;
import org.optaplanner.core.api.domain.variable.PlanningVariable;

@PlanningEntity(difficultyComparatorClass = MealSlotDifficultyComparator.class)
public class MealSlot {
	public enum Meal {
		BREAKFAST(1), LUNCH(2), DINNER(3), SNACK1(4), SNACK2(5); // added snacks for diabetics
		
		private final int value;
		private Meal(int value) {
			this.value = value;
		}
		
		public int getValue() {
			return value;
		}
	}	
	public Meal meal;
	
	FoodType type;	
	private int foodId;
	
	public MealSlot() {
		cacheFoodMap = new HashMap<FoodType, Set<Integer>>();
	}
	
	public MealSlot(Meal meal, FoodType type, int foodId) {
		this.meal = meal;
		this.type = type;
		this.foodId = foodId;
	}
	
	public MealSlot(Meal meal, FoodType type) {
		this.meal = meal;
		this.type = type;
		this.foodId = 0;
	}

	@PlanningVariable(valueRangeProviderRefs = { "foodIdRange2" })
	public Integer getFoodId() {
		return foodId;
	}
	
	public void setFoodId(Integer foodId) {
		this.foodId = foodId.intValue();
	}

	public FoodType getType() {
		return type;
	}
	
	static public HashMap<FoodType, Set<Integer>> cacheFoodMap;
	
	@ValueRangeProvider(id = "foodIdRange2")
	@ProblemFactCollectionProperty
	public Set<Integer> getFoodIds() {
		
		if (cacheFoodMap.containsKey(type)) {
			return cacheFoodMap.get(type);
		}
		else {
			
			Set<Integer> test = MealSolution.getInstance(new TargetValues()).foodDB.stream()
					.filter(item -> item.type == type)
					.map(item -> item.id)
					.collect(Collectors.<Integer>toSet());
			test.add(0);	// always add id 0 for possible empty
			
			cacheFoodMap.put(type, test);
			
//			// Debug?
//			System.out.println("DEBUG: " + type);
//			TreeSet<Integer> t = new TreeSet<Integer>();
//			t.addAll(test);
//			System.out.println(t.toString());
			
			return test;
		}
		
//		Set<Integer> test = MealSolution.getInstance().foodDB.stream()
//				.filter(item -> item.type == type)
//				.map(item -> item.id)
//				.collect(Collectors.<Integer>toSet());
//		
//		test.add(0);	// always add id 0 for possible empty
//		
//		return test;
		
	}

}
