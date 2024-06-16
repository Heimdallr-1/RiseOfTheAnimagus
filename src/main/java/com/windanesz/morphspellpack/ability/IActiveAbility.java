package com.windanesz.morphspellpack.ability;

public interface IActiveAbility {

	void toggleAbility();

	default boolean conditionPredicate() { return true; }

	default void toggleAbility(boolean secondary) { return; }
}
