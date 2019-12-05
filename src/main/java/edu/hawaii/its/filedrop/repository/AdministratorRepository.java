package edu.hawaii.its.filedrop.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import edu.hawaii.its.filedrop.type.Administrator;

public interface AdministratorRepository extends JpaRepository<Administrator, Integer> {

    @Override
    Optional<Administrator> findById(Integer id);

    @Override
    @Query(value = "select a from Administrator a "
            + "where a.roleId = 13 or a.roleId = 14 "
            + "order by a.officeId, a.id")
    public List<Administrator> findAll();

    @Query(value = "select a from Administrator a "
            + "where (a.roleId = 13 or a.roleId = 14) "
            + "and a.person.uhUuid = ?1 "
            + "order by a.role")
    List<Administrator> findAllByPersonUhUuid(String uhUuid);

    @Query(value = "select c from Administrator c "
            + "where c.roleId = 13 or c.roleId = 14 "
            + "order by c.officeId, c.id")
    public List<Administrator> findAdministrators();

    @Query(value = "select (count(a) > 0) from Administrator a "
            + "where (a.roleId = 13 or a.roleId = 14) "
            + "and a.person.uhUuid = ?1")
    boolean isAdministrator(String uhUuid);

    @Query(value = "select (count(a) > 0) from Administrator a "
            + "where a.roleId = 14 "
            + "and a.person is not null "
            + "and a.person.uhUuid = ?1")
    boolean isSuperuser(String uhUuid);

}
