package edu.hawaii.its.filedrop.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import edu.hawaii.its.filedrop.type.Whitelist;

public interface WhitelistRepository extends JpaRepository<Whitelist, Integer> {
}
