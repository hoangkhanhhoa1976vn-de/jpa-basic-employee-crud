package com.example.employee;

import com.example.employee.dao.EmployeeDAO;
import com.example.employee.entity.Employee;
import com.example.employee.entity.Gender;
import com.example.employee.util.JPAUtil;
import org.junit.jupiter.api.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class EmployeeDAOTest {

    private static EmployeeDAO dao;

    @BeforeAll
    public static void setUp() {
        dao = new EmployeeDAO();
    }

    @AfterAll
    public static void tearDown() {
        JPAUtil.shutdown();
    }

    @Test
    @Order(1)
    @DisplayName("TODO 0.3: Kiểm tra save() sinh ID tự động và lưu thành công")
    public void testSave() {
        Employee emp = new Employee(
                "Đặng Văn Lâm",
                "lam.dv." + System.nanoTime() + "@company.com",
                new BigDecimal("30000000.00"),
                Gender.MALE,
                LocalDate.of(2019, 1, 15),
                true
        );

        assertNull(emp.getId(), "Trước khi save, ID phải là null (Transient state)");
        dao.save(emp);
        assertNotNull(emp.getId(), "Sau khi save, ID phải được DB sinh tự động (Managed state)");

        Employee retrieved = dao.findById(emp.getId());
        assertNotNull(retrieved, "Phải tìm thấy nhân viên vừa lưu");
        assertEquals("Đặng Văn Lâm", retrieved.getFullName());
        assertTrue(retrieved.getYearsOfService() > 0, "Thuộc tính @Transient yearsOfService phải được tính toán chính xác");
    }

    @Test
    @Order(2)
    @DisplayName("TODO 0.4: Kiểm tra findById và findAll")
    public void testFindByIdAndFindAll() {
        // findById với ID không tồn tại phải trả về null (không ném exception)
        Employee nonExistent = dao.findById(-999L);
        assertNull(nonExistent, "ID không tồn tại phải trả về null");

        // findAll
        List<Employee> list = dao.findAll();
        assertNotNull(list, "Danh sách không được null");
        assertFalse(list.isEmpty(), "Danh sách phải có ít nhất 1 nhân viên");
    }

    @Test
    @Order(3)
    @DisplayName("TODO 0.5: Kiểm tra các truy vấn JPQL có điều kiện (findByEmail, findBySalary, findByActive)")
    public void testJPQLQueries() {
        String uniqueEmail = "test.jpql." + System.nanoTime() + "@company.com";
        Employee emp = new Employee(
                "Phạm Thị Hằng",
                uniqueEmail,
                new BigDecimal("45000000.00"),
                Gender.FEMALE,
                LocalDate.of(2017, 3, 10),
                true
        );
        dao.save(emp);

        // findByEmail
        Employee byEmail = dao.findByEmail(uniqueEmail);
        assertNotNull(byEmail, "Phải tìm thấy nhân viên theo email");
        assertEquals(uniqueEmail, byEmail.getEmail());

        // findBySalaryGreaterThan
        List<Employee> highSalaryList = dao.findBySalaryGreaterThan(new BigDecimal("40000000.00"));
        assertFalse(highSalaryList.isEmpty());
        for (Employee e : highSalaryList) {
            assertTrue(e.getSalary().compareTo(new BigDecimal("40000000.00")) > 0);
        }

        // findByActive
        List<Employee> activeList = dao.findByActive(true);
        assertFalse(activeList.isEmpty());
        for (Employee e : activeList) {
            assertTrue(e.isActive());
        }
    }

    @Test
    @Order(4)
    @DisplayName("TODO 0.6: Kiểm tra update() với merge")
    public void testUpdate() {
        Employee emp = new Employee(
                "Hoàng Trung",
                "trung.h." + System.nanoTime() + "@company.com",
                new BigDecimal("18000000.00"),
                Gender.MALE,
                LocalDate.of(2022, 6, 1),
                true
        );
        dao.save(emp);

        // Thay đổi thông tin trên đối tượng Detached
        emp.setSalary(new BigDecimal("22000000.00"));
        emp.setActive(false);

        Employee updated = dao.update(emp);
        assertNotNull(updated);

        // Đọc lại từ DB để kiểm chứng
        Employee fresh = dao.findById(emp.getId());
        assertEquals(0, fresh.getSalary().compareTo(new BigDecimal("22000000.00")));
        assertFalse(fresh.isActive());
    }

    @Test
    @Order(5)
    @DisplayName("TODO 0.7: Kiểm tra delete() và kiểm chứng đã xóa")
    public void testDelete() {
        Employee emp = new Employee(
                "Cần Xóa",
                "canxoa." + System.nanoTime() + "@company.com",
                new BigDecimal("10000000.00"),
                Gender.OTHER,
                LocalDate.of(2024, 1, 1),
                false
        );
        dao.save(emp);
        Long id = emp.getId();

        // Xóa thành công
        boolean deleted = dao.delete(id);
        assertTrue(deleted, "Xóa ID tồn tại phải trả về true");

        // Kiểm chứng findById phải trả về null
        Employee checkDeleted = dao.findById(id);
        assertNull(checkDeleted, "Sau khi xóa, findById phải trả về null");

        // Xóa lại ID đã xóa phải trả về false
        boolean deleteAgain = dao.delete(id);
        assertFalse(deleteAgain, "Xóa ID không còn tồn tại phải trả về false");
    }

    @Test
    @Order(6)
    @DisplayName("TODO 0.9: Kiểm chứng ràng buộc UNIQUE trên cột email phải ném exception")
    public void testUniqueEmailConstraint() {
        String sharedEmail = "duplicate." + System.nanoTime() + "@company.com";
        Employee emp1 = new Employee("A", sharedEmail, new BigDecimal("10000000"), Gender.MALE, LocalDate.now(), true);
        dao.save(emp1);

        Employee emp2 = new Employee("B", sharedEmail, new BigDecimal("12000000"), Gender.FEMALE, LocalDate.now(), true);
        assertThrows(RuntimeException.class, () -> {
            dao.save(emp2);
        }, "Lưu nhân viên với email trùng phải ném ngoại lệ RuntimeException/PersistenceException");
    }
}
