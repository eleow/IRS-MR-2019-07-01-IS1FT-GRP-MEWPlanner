package org.mew.domain;

public class TargetValues {
	public float calories_kcal;
	public float calories_deviation_threshold;
	public float sodium_mg;
	public float carbohyrates_kcal_frac;
	public float fat_kcal_frac;
	public float protein_kcal_frac;
	public int max_history;
	
	public TargetValues() {
		calories_kcal = 2200;
		sodium_mg = 2300;
		max_history = 7;
		calories_deviation_threshold = 0.05f;
	}
	
	public TargetValues(float c, float ct, float s, float carbo, float fat, float protein, int mhistory) {
		calories_kcal = c; calories_deviation_threshold = ct;
		sodium_mg = s;
		carbohyrates_kcal_frac = carbo;
		fat_kcal_frac = fat;
		protein_kcal_frac = protein;
		max_history = mhistory;
	}
	
	
}
