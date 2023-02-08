package com.afetyardim.afetyardim.repository;

import com.afetyardim.afetyardim.model.Site;
import java.util.Set;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface SiteRepository extends JpaRepository<Site, Long> {

  Set<Site> findByLocationCity(String city);
}
