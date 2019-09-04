package edu.hawaii.its.filedrop.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import edu.hawaii.its.filedrop.type.Whitelist;

public interface WhitelistRepository extends JpaRepository<Whitelist, Integer> {

    @Override
    Optional<Whitelist> findById(Integer id);

    Whitelist findByEntry(String entry);

}
