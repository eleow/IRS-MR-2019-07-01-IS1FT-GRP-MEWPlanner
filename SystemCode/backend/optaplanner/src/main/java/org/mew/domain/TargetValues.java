package org.mew.domain;

import java.util.Optional;

public class TargetValues {
	public float calories_kcal;
	public float calories_deviation_threshold;
	public float sodium_mg;
	public float carbs_kcal_frac;
	public float fat_kcal_frac;
	public float protein_kcal_frac;
	public int max_history;
	
	public float sugar_g;
	public float carbs_deviation_threshold;
	
	// additional options after 2nd consultation with dietitian
	public boolean isDiabetic;
	public boolean takesBeef;
	public String food_preference;	// none, chinese, malay, indian, western
	public int max_num_caffeine;
	
	
	public TargetValues() {
		calories_kcal = 2200;
		sodium_mg = 2300;
		max_history = 7;
		calories_deviation_threshold = 0.05f;
		
		sugar_g = 30;
		carbs_kcal_frac = 0.5f;
		carbs_deviation_threshold = 0.05f;
		
		isDiabetic = true;
		takesBeef = true;
		food_preference = "none";
		max_num_caffeine = 1;
	}
	
	public TargetValues(float c, float ct, float s, float carbo, float fat, float protein, 
			int mhistory, float sugar, float carbs_threshold,
			boolean isD, String prefers, int caffeine, boolean beef) {
		calories_kcal = c; calories_deviation_threshold = ct;
		sodium_mg = s;
		carbs_kcal_frac = carbo;
		fat_kcal_frac = fat;
		protein_kcal_frac = protein;
		max_history = mhistory;
		
		sugar_g = sugar;
		carbs_deviation_threshold = carbs_threshold;
		
		isDiabetic = isD;
		takesBeef = beef;
		food_preference = prefers;
		max_num_caffeine = caffeine;
	}
	
	public String toString() {
		return "calories (kcal): " + calories_kcal
				+ ", cal threshold: " + calories_deviation_threshold
				+ ", sodium (mg):" + sodium_mg 
				+ ", sugar (g):" + sugar_g
				+ ", carbs: " + carbs_kcal_frac + " (+-" + carbs_deviation_threshold + ")"
				+ ", isDiabetic: " + isDiabetic
				+ ", takesBeef:" + takesBeef
				+ ", max_history: " + max_history;
				 
	}
	
	
}
