package java.time.chrono;

public enum IsoEra implements Era {
	BCE,
	CE;

	native public static IsoEra of(int isoEra);

	native public int getValue();
}
