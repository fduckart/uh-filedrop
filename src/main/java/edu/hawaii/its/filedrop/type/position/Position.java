package edu.hawaii.its.filedrop.type.position;

import javax.persistence.Column;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.MappedSuperclass;
import javax.persistence.OneToOne;
import javax.persistence.Transient;

import edu.hawaii.its.filedrop.type.Office;
import edu.hawaii.its.filedrop.type.OfficeIdentifiable;
import edu.hawaii.its.filedrop.type.Person;
import edu.hawaii.its.filedrop.type.PersonIdentifiable;
import edu.hawaii.its.filedrop.type.Role;

@MappedSuperclass
public abstract class Position implements PersonIdentifiable, OfficeIdentifiable {

    @Id
    @Column(name = "id")
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    protected Integer id;

    @Column(name = "person_id", nullable = false)
    protected Integer personId;

    @Column(name = "office_id", nullable = false)
    protected Integer officeId;

    @Column(name = "role_id", nullable = false)
    protected Integer roleId;
    @OneToOne(fetch = FetchType.EAGER)

    @JoinColumn(name = "office_id", updatable = false, insertable = false)
    protected Office office;

    @OneToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "person_id", updatable = false, insertable = false)
    protected Person person;

    @OneToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "role_id", updatable = false, insertable = false)
    protected Role role;

    @Transient
    protected String uhUuid = "";

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public Integer getPersonId() {
        return personId;
    }

    public void setPersonId(Integer personId) {
        this.personId = personId;
    }

    @Override
    public Integer getOfficeId() {
        return officeId;
    }

    public void setOfficeId(Integer officeId) {
        this.officeId = officeId;
    }

    public Integer getRoleId() {
        return roleId;
    }

    public void setRoleId(Integer roleId) {
        this.roleId = roleId;
    }

    public Office getOffice() {
        return office;
    }

    public void setOffice(Office office) {
        this.office = office;
    }

    public Person getPerson() {
        return person;
    }

    public void setPerson(Person person) {
        this.person = person;
    }

    public Role getRole() {
        return role;
    }

    public void setRole(Role role) {
        this.role = role;
        setRoleId(role.getId());
    }

    @Override
    public String getUhUuid() {
        return uhUuid;
    }

    public void setUhUuid(String uhUuid) {
        this.uhUuid = uhUuid != null ? uhUuid : "";
    }

    @Transient
    public boolean isValid() {
        return getId() != null;
    }

}
