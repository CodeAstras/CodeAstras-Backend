package com.codeastras.backend.codeastras.repository;

import com.codeastras.backend.codeastras.entity.RtcSignal;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public interface RtcSignalRepository extends JpaRepository<RtcSignal, UUID> {

}
