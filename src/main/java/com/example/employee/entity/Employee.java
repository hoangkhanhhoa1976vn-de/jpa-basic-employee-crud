package com.example.employee.entity;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.Period;

/**
 * Entity Employee đại diện cho bảng employees trong CSDL.
 * Tuân thủ yêu cầu TODO 0.1 của bài tập JPA cơ bản.
 */
@Entity
@Table(name = "employees")
public class Employee {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String fullName;

    @Column(unique = true)
    private String email;

    @Column(precision = 18, scale = 2)
    private BigDecimal salary;

    @Enumerated(EnumType.STRING)
    private Gender gender;

    private LocalDate hireDate;

    private boolean active;

    @Transient
    private int yearsOfService;

    // 1. Constructor không tham số (Bắt buộc cho JPA khi khởi tạo đối tượng)
    public Employee() {
    }

    // 2. Constructor tiện dụng để khởi tạo đối tượng mới chưa có ID
    public Employee(String fullName, String email, BigDecimal salary, Gender gender, LocalDate hireDate, boolean active) {
        this.fullName = fullName;
        this.email = email;
        this.salary = salary;
        this.gender = gender;
        this.hireDate = hireDate;
        this.active = active;
    }

    // 3. Constructor đầy đủ tham số bao gồm cả ID
    public Employee(Long id, String fullName, String email, BigDecimal salary, Gender gender, LocalDate hireDate, boolean active) {
        this.id = id;
        this.fullName = fullName;
        this.email = email;
        this.salary = salary;
        this.gender = gender;
        this.hireDate = hireDate;
        this.active = active;
    }

    // 4. Getter / Setter
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getFullName() {
        return fullName;
    }

    public void setFullName(String fullName) {
        this.fullName = fullName;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public BigDecimal getSalary() {
        return salary;
    }

    public void setSalary(BigDecimal salary) {
        this.salary = salary;
    }

    public Gender getGender() {
        return gender;
    }

    public void setGender(Gender gender) {
        this.gender = gender;
    }

    public LocalDate getHireDate() {
        return hireDate;
    }

    public void setHireDate(LocalDate hireDate) {
        this.hireDate = hireDate;
    }

    public boolean isActive() {
        return active;
    }

    public void setActive(boolean active) {
        this.active = active;
    }

    /**
     * Thuộc tính @Transient không lưu xuống CSDL.
     * Tính toán số năm làm việc dựa trên hireDate và ngày hiện tại.
     */
    public int getYearsOfService() {
        if (hireDate == null) {
            return 0;
        }
        return Period.between(hireDate, LocalDate.now()).getYears();
    }

    public void setYearsOfService(int yearsOfService) {
        this.yearsOfService = yearsOfService;
    }

    @Override
    public String toString() {
        return "Employee{" +
                "id=" + id +
                ", fullName='" + fullName + '\'' +
                ", email='" + email + '\'' +
                ", salary=" + salary +
                ", gender=" + gender +
                ", hireDate=" + hireDate +
                ", active=" + active +
                ", yearsOfService=" + getYearsOfService() +
                '}';
    }
}
