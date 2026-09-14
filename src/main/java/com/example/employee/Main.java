package com.example.employee;

import com.example.employee.dao.EmployeeDAO;
import com.example.employee.entity.Employee;
import com.example.employee.entity.Gender;
import com.example.employee.util.JPAUtil;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

/**
 * Lớp chạy demo thực thi toàn bộ luồng từ TODO 0.1 đến TODO 0.10:
 * - TODO 0.8: Luồng CRUD tuần tự (Create -> Read -> Update -> Read kiểm tra -> Delete -> Read kiểm tra).
 * - TODO 0.9: Kiểm chứng ràng buộc UNIQUE trên cột email (bắt lỗi cố ý trùng email).
 * - TODO 0.10: Giải thích rõ ràng 4 trạng thái Entity Lifecycle (Transient, Managed, Detached, Removed) qua comment tại từng bước.
 */
public class Main {

    public static void main(String[] args) {
        System.out.println("==========================================================================");
        System.out.println("     BÀI 0 — JPA CƠ BẢN: ENTITY ĐƠN (DEMO TOÀN BỘ TỪ TODO 0.1 ĐẾN 0.10)   ");
        System.out.println("==========================================================================");

        EmployeeDAO dao = new EmployeeDAO();

        try {
            // =========================================================================
            // BƯỚC 1: CREATE (Tạo mới và lưu nhân viên vào CSDL)
            // =========================================================================
            System.out.println("\n>>> [BƯỚC 1: CREATE] Tạo mới nhân viên vào CSDL...");

            // [TODO 0.10 - LIFECYCLE: NEW / TRANSIENT]
            // Đối tượng vừa được tạo bằng toán tử 'new', chưa có ID (id == null),
            // hoàn toàn chưa được gắn kết (associated) với bất kỳ EntityManager/Persistence Context nào.
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

            System.out.println("• [LIFECYCLE: NEW/TRANSIENT] Trước save(), e1.getId() = " + e1.getId());

            // Gọi dao.save(e1):
            // - Bên trong save(): khi gọi em.persist(e1), entity chuyển sang trạng thái [MANAGED].
            // - Khi tx.commit() và em.close(): EntityManager đóng lại, entity chuyển sang trạng thái [DETACHED].
            dao.save(e1);
            System.out.println("✓ [LIFECYCLE: DETACHED sau khi save() kết thúc] Đã lưu e1 thành công! ID sinh từ DB = " + e1.getId());

            dao.save(e2);
            System.out.println("✓ Đã lưu e2 thành công! ID = " + e2.getId());

            dao.save(e3);
            System.out.println("✓ Đã lưu e3 thành công! ID = " + e3.getId());

            Long id1 = e1.getId();
            Long id3 = e3.getId();

            // =========================================================================
            // BƯỚC 2: READ (Đọc dữ liệu với findById, findAll và JPQL có điều kiện)
            // =========================================================================
            System.out.println("\n>>> [BƯỚC 2: READ] Đọc dữ liệu từ CSDL...");

            // 2.1: findById
            System.out.println("--- 2.1: findById(" + id1 + ") ---");
            // [TODO 0.10 - LIFECYCLE: MANAGED -> DETACHED]
            // Bên trong dao.findById(): em.find() trả về entity ở trạng thái Managed trong Persistence Context.
            // Nhưng khi finally { em.close(); } thực thi, entity trả về biến foundEmp ở Main trở thành [DETACHED].
            Employee foundEmp = dao.findById(id1);
            System.out.println("Kết quả findById: " + foundEmp);
            if (foundEmp != null) {
                System.out.println("-> Thâm niên làm việc (@Transient yearsOfService): " 
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

            // =========================================================================
            // BƯỚC 3: UPDATE (Cập nhật thông tin nhân viên)
            // =========================================================================
            System.out.println("\n>>> [BƯỚC 3: UPDATE] Cập nhật nhân viên id=" + id1 + "...");
            System.out.println("Thông tin trước update: " + foundEmp);

            // [TODO 0.10 - LIFECYCLE: DETACHED]
            // foundEmp đang ở trạng thái Detached (bên ngoài EntityManager). Ta sửa đổi field trên Java object:
            foundEmp.setFullName("Nguyễn Văn An (Trưởng Phòng)");
            foundEmp.setSalary(new BigDecimal("29000000.00"));

            // [TODO 0.10 - LIFECYCLE: em.merge()]
            // Trong dao.update():
            // - Đối tượng foundEmp truyền vào vẫn là [DETACHED].
            // - em.merge(foundEmp) sao chép state sang bản sao [MANAGED] trong Persistence Context.
            // - Khi commit(), Hibernate phát hiện thay đổi và sinh câu lệnh UPDATE.
            // - Sau khi em.close(), đối tượng trả về 'updatedEmp' chuyển thành [DETACHED].
            Employee updatedEmp = dao.update(foundEmp);
            System.out.println("✓ Đã gọi update(), kết quả trả về từ merge: " + updatedEmp);

            // =========================================================================
            // BƯỚC 4: READ LẠI KIỂM TRA (Đối chiếu sau update)
            // =========================================================================
            System.out.println("\n>>> [BƯỚC 4: READ LẠI KIỂM TRA SAU UPDATE]...");
            Employee verifyEmp = dao.findById(id1);
            System.out.println("Dữ liệu đọc lại từ DB: " + verifyEmp);
            boolean isUpdated = verifyEmp != null 
                    && verifyEmp.getSalary().compareTo(new BigDecimal("29000000.00")) == 0
                    && "Nguyễn Văn An (Trưởng Phòng)".equals(verifyEmp.getFullName());
            System.out.println("-> Kiểm tra giá trị mới đã khớp trong CSDL: " + (isUpdated ? "CHÍNH XÁC (PASSED)" : "KHÔNG KHỚP (FAILED)"));

            // =========================================================================
            // BƯỚC 5: DELETE (Xóa nhân viên)
            // =========================================================================
            System.out.println("\n>>> [BƯỚC 5: DELETE] Xóa nhân viên id=" + id3 + " (" + e3.getFullName() + ")...");
            // [TODO 0.10 - LIFECYCLE: MANAGED -> REMOVED]
            // Trong dao.delete(id3):
            // - em.find() lấy entity đưa vào trạng thái [MANAGED].
            // - em.remove(entity) chuyển trạng thái entity sang [REMOVED].
            // - Khi tx.commit(): Hibernate sinh câu lệnh DELETE và xóa bản ghi khỏi DB.
            boolean deleteSuccess = dao.delete(id3);
            System.out.println("✓ Kết quả xóa id=" + id3 + ": " + (deleteSuccess ? "Thành công" : "Thất bại"));

            // =========================================================================
            // BƯỚC 6: READ LẠI KIỂM TRA ĐÃ XÓA
            // =========================================================================
            System.out.println("\n>>> [BƯỚC 6: READ LẠI KIỂM TRA ĐÃ XÓA]...");
            Employee deletedCheck = dao.findById(id3);
            System.out.println("Kết quả findById(" + id3 + "): " + deletedCheck);
            if (deletedCheck == null) {
                System.out.println("-> Xác nhận: Nhân viên đã được xóa hoàn toàn khỏi CSDL (kết quả null - PASSED)!");
            } else {
                System.err.println("-> Cảnh báo: Nhân viên vẫn còn tồn tại!");
            }

            // =========================================================================
            // TODO 0.9: KIỂM CHỨNG RÀNG BUỘC UNIQUE TRÊN EMAIL
            // =========================================================================
            System.out.println("\n==========================================================================");
            System.out.println(">>> [TODO 0.9: KIỂM CHỨNG RÀNG BUỘC UNIQUE TRÊN EMAIL]...");
            System.out.println("==========================================================================");

            String duplicateEmail = "trung.email." + System.currentTimeMillis() + "@company.com";

            Employee empOriginal = new Employee(
                    "Nhân viên A",
                    duplicateEmail,
                    new BigDecimal("18000000.00"),
                    Gender.MALE,
                    LocalDate.of(2022, 1, 1),
                    true
            );
            dao.save(empOriginal);
            System.out.println("1. Đã lưu nhân viên A với email: " + duplicateEmail);

            Employee empDuplicate = new Employee(
                    "Nhân viên B (cố tình trùng email)",
                    duplicateEmail,
                    new BigDecimal("20000000.00"),
                    Gender.FEMALE,
                    LocalDate.of(2023, 2, 2),
                    true
            );

            System.out.println("2. Cố tình lưu nhân viên B với cùng email: " + duplicateEmail + " (kỳ vọng ném ngoại lệ)...");
            try {
                dao.save(empDuplicate);
                System.err.println("❌ THẤT BẠI: Ràng buộc unique không chặn được email trùng!");
            } catch (Exception ex) {
                System.out.println("✓ [TODO 0.9 - PASSED] Đã chặn trùng email thành công!");
                System.out.println("-> Ngoại lệ bắt được đúng như kỳ vọng: " + ex.getClass().getSimpleName());
                System.out.println("-> Thông điệp lỗi: " + ex.getMessage());
            }

            System.out.println("\n==========================================================================");
            System.out.println("   ✓ HOÀN THÀNH XUẤT SẮC TOÀN BỘ CÁC YÊU CẦU TỪ TODO 0.1 ĐẾN TODO 0.10!   ");
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
