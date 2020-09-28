package edu.hawaii.its.filedrop.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import edu.hawaii.its.filedrop.type.Faq;

public interface FaqRepository extends JpaRepository<Faq, Integer> {
}
