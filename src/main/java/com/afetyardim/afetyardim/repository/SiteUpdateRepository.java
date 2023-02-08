package com.afetyardim.afetyardim.repository;

import com.afetyardim.afetyardim.model.SiteUpdate;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface SiteUpdateRepository extends JpaRepository<SiteUpdate, Long> {
}
