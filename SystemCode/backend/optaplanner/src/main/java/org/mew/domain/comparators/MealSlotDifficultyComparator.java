package org.mew.domain.comparators;

import java.util.Comparator;

import org.apache.commons.lang3.builder.CompareToBuilder;
import org.mew.domain.MealSlot;

public class MealSlotDifficultyComparator implements Comparator<MealSlot> {

	@Override
	public int compare(MealSlot o1, MealSlot o2) {
		return new CompareToBuilder()
				.append(o1.getType(), o2.getType())
				.toComparison();
	}

}
