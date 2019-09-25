package edu.hawaii.its.filedrop.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import edu.hawaii.its.filedrop.type.Message;

public interface MessageRepository extends JpaRepository<Message, Integer> {

    @Override
    Optional<Message> findById(Integer id);
}
