package com.example.employee;

import com.example.employee.dao.EmployeeDAO;
import com.example.employee.entity.Employee;
import com.example.employee.entity.Gender;
import com.example.employee.util.JPAUtil;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

/**
 * Lớp chạy demo thực thi TODO 0.8:
 * Luồng demo tuần tự: Create -> Read -> Update -> Read lại kiểm tra -> Delete -> Read lại kiểm tra đã xóa.
 */
public class Main {

    public static void main(String[] args) {
        System.out.println("==========================================================================");
        System.out.println("     BÀI 0 — JPA CƠ BẢN: ENTITY ĐƠN (CRUD VỚI ENTITYMANAGER & DAO)        ");
        System.out.println("==========================================================================");

        EmployeeDAO dao = new EmployeeDAO();

        try {
            // -------------------------------------------------------------------------
            // BƯỚC 1: CREATE (Tạo mới và lưu nhân viên vào CSDL)
            // -------------------------------------------------------------------------
            System.out.println("\n>>> [BƯỚC 1: CREATE] Tạo mới nhân viên vào CSDL...");

            Employee e1 = new Employee(
                    "Nguyễn Văn An",
                    "an.nv." + System.currentTimeMillis() + "@company.com",
                    new BigDecimal("25000000.00"),
                    Gender.MALE,
                    LocalDate.of(2021, 5, 10),
                    true
            );

            Employee e2 = new Employee(
                    "Trần Thị Bình",
                    "binh.tt." + System.currentTimeMillis() + "@company.com",
                    new BigDecimal("32500000.00"),
                    Gender.FEMALE,
                    LocalDate.of(2018, 8, 20),
                    true
            );

            Employee e3 = new Employee(
                    "Lê Hoàng Nam",
                    "nam.lh." + System.currentTimeMillis() + "@company.com",
                    new BigDecimal("15000000.00"),
                    Gender.OTHER,
                    LocalDate.of(2023, 11, 1),
                    false
            );

            System.out.println("Trạng thái e1 trước khi save (ID kỳ vọng null): id = " + e1.getId());
            dao.save(e1);
            System.out.println("✓ Đã lưu e1 thành công! (ID sau khi DB sinh): " + e1);

            dao.save(e2);
            System.out.println("✓ Đã lưu e2 thành công! (ID sau khi DB sinh): " + e2);

            dao.save(e3);
            System.out.println("✓ Đã lưu e3 thành công! (ID sau khi DB sinh): " + e3);

            Long id1 = e1.getId();
            Long id3 = e3.getId();

            // -------------------------------------------------------------------------
            // BƯỚC 2: READ (Đọc dữ liệu với findById, findAll và JPQL có điều kiện)
            // -------------------------------------------------------------------------
            System.out.println("\n>>> [BƯỚC 2: READ] Đọc dữ liệu từ CSDL...");

            // 2.1: findById
            System.out.println("--- 2.1: findById(" + id1 + ") ---");
            Employee foundEmp = dao.findById(id1);
            System.out.println("Kết quả findById: " + foundEmp);
            if (foundEmp != null) {
                System.out.println("-> Thâm niên làm việc (yearsOfService tính từ hireDate): " 
                        + foundEmp.getYearsOfService() + " năm");
            }

            // 2.2: findAll
            System.out.println("\n--- 2.2: findAll() ---");
            List<Employee> allEmployees = dao.findAll();
            System.out.println("Tổng số nhân viên trong CSDL: " + allEmployees.size());
            allEmployees.forEach(e -> System.out.println("  • " + e));

            // 2.3: findByEmail (JPQL có điều kiện)
            System.out.println("\n--- 2.3: findByEmail('" + e2.getEmail() + "') ---");
            Employee empByEmail = dao.findByEmail(e2.getEmail());
            System.out.println("Tìm thấy theo email: " + empByEmail);

            // 2.4: findBySalaryGreaterThan (JPQL có điều kiện)
            BigDecimal minSalary = new BigDecimal("20000000.00");
            System.out.println("\n--- 2.4: findBySalaryGreaterThan(" + minSalary + ") ---");
            List<Employee> highSalaryEmps = dao.findBySalaryGreaterThan(minSalary);
            System.out.println("Số lượng nhân viên có lương > 20 triệu: " + highSalaryEmps.size());
            highSalaryEmps.forEach(e -> System.out.println("  • " + e.getFullName() + " - Lương: " + e.getSalary()));

            // 2.5: findByActive
            System.out.println("\n--- 2.5: findByActive(true) ---");
            List<Employee> activeEmps = dao.findByActive(true);
            System.out.println("Nhân viên đang làm việc (active=true): " + activeEmps.size());

            // -------------------------------------------------------------------------
            // BƯỚC 3: UPDATE (Cập nhật thông tin nhân viên)
            // -------------------------------------------------------------------------
            System.out.println("\n>>> [BƯỚC 3: UPDATE] Cập nhật nhân viên id=" + id1 + "...");
            System.out.println("Thông tin trước update: " + foundEmp);

            foundEmp.setFullName("Nguyễn Văn An (Trưởng Phòng)");
            foundEmp.setSalary(new BigDecimal("29000000.00"));
            Employee updatedEmp = dao.update(foundEmp);
            System.out.println("✓ Đã gọi update(), kết quả trả về từ merge: " + updatedEmp);

            // -------------------------------------------------------------------------
            // BƯỚC 4: READ LẠI KIỂM TRA (Đối chiếu sau update)
            // -------------------------------------------------------------------------
            System.out.println("\n>>> [BƯỚC 4: READ LẠI KIỂM TRA SAU UPDATE]...");
            Employee verifyEmp = dao.findById(id1);
            System.out.println("Dữ liệu đọc lại từ DB: " + verifyEmp);
            boolean isUpdated = verifyEmp != null 
                    && verifyEmp.getSalary().compareTo(new BigDecimal("29000000.00")) == 0
                    && "Nguyễn Văn An (Trưởng Phòng)".equals(verifyEmp.getFullName());
            System.out.println("-> Kiểm tra giá trị mới đã khớp trong CSDL: " + (isUpdated ? "CHÍNH XÁC (PASSED)" : "KHÔNG KHỚP (FAILED)"));

            // -------------------------------------------------------------------------
            // BƯỚC 5: DELETE (Xóa nhân viên)
            // -------------------------------------------------------------------------
            System.out.println("\n>>> [BƯỚC 5: DELETE] Xóa nhân viên id=" + id3 + " (" + e3.getFullName() + ")...");
            boolean deleteSuccess = dao.delete(id3);
            System.out.println("✓ Kết quả xóa id=" + id3 + ": " + (deleteSuccess ? "Thành công" : "Thất bại"));

            // -------------------------------------------------------------------------
            // BƯỚC 6: READ LẠI KIỂM TRA ĐÃ XÓA
            // -------------------------------------------------------------------------
            System.out.println("\n>>> [BƯỚC 6: READ LẠI KIỂM TRA ĐÃ XÓA]...");
            Employee deletedCheck = dao.findById(id3);
            System.out.println("Kết quả findById(" + id3 + "): " + deletedCheck);
            if (deletedCheck == null) {
                System.out.println("-> Xác nhận: Nhân viên đã được xóa hoàn toàn khỏi CSDL (kết quả null - PASSED)!");
            } else {
                System.err.println("-> Cảnh báo: Nhân viên vẫn còn tồn tại!");
            }

            System.out.println("\n==========================================================================");
            System.out.println("     ✓ TOÀN BỘ LUỒNG CRUD ĐÃ HOÀN TẤT THÀNH CÔNG VÀ CHÍNH XÁC!           ");
            System.out.println("==========================================================================");

        } catch (Exception ex) {
            System.err.println("Đã xảy ra lỗi ngoài ý muốn: " + ex.getMessage());
            ex.printStackTrace();
        } finally {
            // Giải phóng EntityManagerFactory
            JPAUtil.shutdown();
        }
    }
}
