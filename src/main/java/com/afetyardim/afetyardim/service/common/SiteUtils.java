package com.afetyardim.afetyardim.service.common;

import com.afetyardim.afetyardim.model.Site;
import java.util.Collection;
import java.util.Optional;

public class SiteUtils {

  public static Optional<Site> findSiteByNameAndDistrict(String siteName,String district, Collection<Site> sites) {
    return sites.stream().filter(
        site ->  compareLowerCaseEquality(siteName,site.getName()) && compareLowerCaseEquality(district,site.getLocation().getDistrict())
    ).findAny();
  }

  public static boolean compareLowerCaseEquality(String string1, String string2){
    return string1.toLowerCase().equals(string2.toLowerCase());
  }

  public static boolean compareFloats(Float float1, Float fLoat2) {

    double threshold = 0.00001;
    return (Math.abs(float1 - fLoat2) < threshold);

  }
}
