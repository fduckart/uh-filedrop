package edu.hawaii.its.filedrop.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import edu.hawaii.its.filedrop.type.Allowlist;

public interface AllowlistRepository extends JpaRepository<Allowlist, Integer> {

    @Override
    Optional<Allowlist> findById(Integer id);

    Allowlist findByEntry(String entry);

}
