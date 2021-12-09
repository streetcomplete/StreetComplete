package de.westnordost.streetcomplete.data.meta;

import java.io.Serializable;
import java.util.Collections;
import java.util.List;
import java.util.Locale;

public class CountryInfo implements Serializable, Cloneable
{
	public static final long serialVersionUID = 1L;

	// this value is not defined in the yaml file but it is the ISO language code part of the file name!
	// i.e. US for US-TX.yml
	String countryCode;

	// sorted alphabetically for better overview

	List<String> additionalStreetsignLanguages;
	String additionalValidHousenumberRegex;
	String advisorySpeedLimitSignStyle;
	List<String> atmOperators;
	String centerLineStyle;
	List<String> chargingStationOperators;
	List<String> clothesContainerOperators;
	String firstDayOfWorkweek;
	Boolean hasAdvisorySpeedLimitSign;
	Boolean hasBiWeeklyAlternateSideParkingSign;
	Boolean hasCenterLeftTurnLane;
	Boolean hasDailyAlternateSideParkingSign;
	Boolean hasLivingStreet;
	Boolean hasNoStandingSign;
	Boolean hasSlowZone;
	Boolean isLeftHandTraffic;
	Boolean isUsuallyAnyGlassRecyclableInContainers;
	List<String> lengthUnits;
	String livingStreetSignStyle;
	Integer mobileCountryCode;
	String noParkingSignStyle;
	String noParkingLineStyle;
	String noStandingSignStyle;
	String noStandingLineStyle;
	String noStoppingSignStyle;
	String noStoppingLineStyle;
	List<String> officialLanguages;
	List<String> orchardProduces;
	List<String> popularReligions;
	List<String> popularSports;
	Integer regularShoppingDays;
	Boolean roofsAreUsuallyFlat;
	String edgeLineStyle;
	String slowZoneLabelPosition;
	String slowZoneLabelText;
	List<String> speedUnits;
	List<String> weightLimitUnits;
	Integer workweekDays;

	// sorted alphabetically for better overview

	public List<String> getAdditionalStreetsignLanguages()
	{
		if(additionalStreetsignLanguages == null) return Collections.emptyList();
		return Collections.unmodifiableList(additionalStreetsignLanguages);
	}
	public String getAdditionalValidHousenumberRegex() { return additionalValidHousenumberRegex; }
	public String getAdvisorySpeedLimitSignStyle() { return advisorySpeedLimitSignStyle; }
	public List<String> getAtmOperators() { return atmOperators; }
	public String getCenterLineStyle() { return centerLineStyle; }
	public List<String> getChargingStationOperators() { return chargingStationOperators; }
	public List<String> getClothesContainerOperators() { return clothesContainerOperators; }
	public String getCountryCode() { return countryCode; }
	public String getFirstDayOfWorkweek() { return firstDayOfWorkweek; }
	public List<String> getLengthUnits() { return lengthUnits; }
	public String getLivingStreetSignStyle() { return livingStreetSignStyle; }
	public Locale getLocale()
	{
		List<String> languages = getOfficialLanguages();
		if (!languages.isEmpty())
		{
			return new Locale(languages.get(0), countryCode);
		}
		return Locale.getDefault();
	}
	public Integer getMobileCountryCode() { return mobileCountryCode; }
	public String getNoParkingSignStyle() { return noParkingSignStyle; }
	public String getNoParkingLineStyle() { return noParkingLineStyle; }
	public String getNoStandingSignStyle() { return noStandingSignStyle; }
	public String getNoStandingLineStyle() { return noStandingLineStyle; }
	public String getNoStoppingSignStyle() { return noStoppingSignStyle; }
	public String getNoStoppingLineStyle() { return noStoppingLineStyle; }
	public List<String> getOfficialLanguages()
	{
		if(officialLanguages == null) return Collections.emptyList();
		return Collections.unmodifiableList(officialLanguages);
	}
	public List<String> getOrchardProduces()
	{
		if(orchardProduces == null) return Collections.emptyList();
		return Collections.unmodifiableList(orchardProduces);
	}
	public List<String> getPopularReligions()
	{
		if(popularReligions == null) return Collections.emptyList();
		return Collections.unmodifiableList(popularReligions);
	}
	public List<String> getPopularSports()
	{
		if(popularSports == null) return Collections.emptyList();
		return Collections.unmodifiableList(popularSports);
	}
	public Integer getRegularShoppingDays() { return regularShoppingDays; }
	public String getEdgeLineStyle() { return edgeLineStyle; }
	public String getSlowZoneLabelPosition() { return slowZoneLabelPosition; }
	public String getSlowZoneLabelText() { return slowZoneLabelText; }
	public List<String> getSpeedUnits() { return speedUnits; }
	public List<String> getWeightLimitUnits() { return weightLimitUnits; }
	public Integer getWorkweekDays() { return workweekDays; }
	public boolean hasAdvisorySpeedLimitSign() { return hasAdvisorySpeedLimitSign; }
	public Boolean hasBiWeeklyAlternateSideParkingSign() { return hasBiWeeklyAlternateSideParkingSign; }
	public Boolean hasCenterLeftTurnLane() { return hasCenterLeftTurnLane; }
	public Boolean hasDailyAlternateSideParkingSign() { return hasDailyAlternateSideParkingSign; }
	public boolean hasLivingStreet() { return hasLivingStreet; }
	public Boolean hasNoStandingSign() { return hasNoStandingSign; }
	public boolean hasSlowZone() { return hasSlowZone; }
	public boolean isLeftHandTraffic() { return isLeftHandTraffic; }
	public Boolean isRoofsAreUsuallyFlat() { return roofsAreUsuallyFlat; }
	public Boolean isUsuallyAnyGlassRecyclableInContainers() { return isUsuallyAnyGlassRecyclableInContainers; }
}
