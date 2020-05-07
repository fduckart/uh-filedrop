package edu.hawaii.its.filedrop.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import edu.hawaii.its.filedrop.type.Setting;

public interface SettingRepository extends JpaRepository<Setting, Integer> {

    @Override
    Optional<Setting> findById(Integer id);

}
