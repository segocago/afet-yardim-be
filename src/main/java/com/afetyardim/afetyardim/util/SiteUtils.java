package com.afetyardim.afetyardim.util;

import com.afetyardim.afetyardim.model.Site;
import java.util.Collection;
import java.util.Optional;

public class SiteUtils {

  public static Optional<Site> findSiteByName(String siteName, Collection<Site> sites) {
    return sites.stream().filter(
        site -> siteName.toLowerCase().contains(site.getName().toLowerCase()) ||
            site.getName().toLowerCase().contains(siteName.toLowerCase())
    ).findAny();
  }

  public static boolean compareFloats(Float float1, Float fLoat2) {

    double threshold = 0.00001;
    return (Math.abs(float1 - fLoat2) < threshold);

  }
}
