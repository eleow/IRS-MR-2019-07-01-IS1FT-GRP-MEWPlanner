package org.mew.domain;

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
	
	
	public TargetValues() {
		calories_kcal = 2200;
		sodium_mg = 2300;
		max_history = 7;
		calories_deviation_threshold = 0.05f;
		
		sugar_g = 30;
		carbs_kcal_frac = 0.5f;
		carbs_deviation_threshold = 0.05f;
	}
	
	public TargetValues(float c, float ct, float s, float carbo, float fat, float protein, int mhistory, float sugar, float carbs_threshold) {
		calories_kcal = c; calories_deviation_threshold = ct;
		sodium_mg = s;
		carbs_kcal_frac = carbo;
		fat_kcal_frac = fat;
		protein_kcal_frac = protein;
		max_history = mhistory;
		
		sugar_g = sugar;
		carbs_deviation_threshold = carbs_threshold;
	}
	
	public String toString() {
		return "calories (kcal): " + calories_kcal
				+ ", cal threshold: " + calories_deviation_threshold
				+ ", sodium (mg):" + sodium_mg 
				+ ", sugar (g):" + sugar_g
				+ ", carbs: " + carbs_kcal_frac + " (+-" + carbs_deviation_threshold + ")"
				+ ", max_history: " + max_history;
				 
	}
	
	
}
