# Bài 0 — JPA cơ bản: Entity đơn (chưa có Relationship Mapping)

Dự án Java quản lý danh sách nhân viên (`Employee`) độc lập trong CSDL SQL Server bằng JPA 3.1 thuần kết hợp Hibernate 6.5.2, tuân thủ đúng pattern DAO và kiến trúc `EntityManager`.

---

## 1. Môi trường & Công nghệ
- **Java**: JDK 21 (tương thích Java 21+)
- **JPA**: Jakarta Persistence API 3.1 (`jakarta.persistence-api:3.1.0`)
- **ORM / Provider**: Hibernate Core 6.5.2 (`hibernate-core:6.5.2.Final`)
- **Database**: Microsoft SQL Server (MSSQL)
- **JDBC Driver**: `mssql-jdbc:12.6.1.jre11`
- **Cấu hình**: `META-INF/persistence.xml` + `EntityManager`
- **IDE khuyến nghị**: IntelliJ IDEA 2024.x+

---

## 2. Thiết kế bảng & Thực thể (`Employee`)

Bảng trong CSDL: `employees` (chỉ có 1 bảng đơn, không có khóa ngoại FK).

| Field | Kiểu Java | Cột CSDL / Mapping | Ghi chú |
| :--- | :--- | :--- | :--- |
| `id` | `Long` | `BIGINT IDENTITY(1,1) PRIMARY KEY` | PK, tự sinh (`@Id`, `@GeneratedValue(strategy = IDENTITY)`) |
| `fullName` | `String` | `NVARCHAR(255) NOT NULL` | `@Column(nullable = false, columnDefinition = "NVARCHAR(255)")` |
| `email` | `String` | `VARCHAR(255) UNIQUE` | `@Column(unique = true)` |
| `salary` | `BigDecimal` | `NUMERIC(18,2)` | `@Column(precision = 18, scale = 2)`, không dùng float/double |
| `gender` | `Gender` | `VARCHAR(20)` | `@Enumerated(EnumType.STRING)` (MALE, FEMALE, OTHER) |
| `hireDate` | `LocalDate` | `DATE` | JPA 2.2+ hỗ trợ trực tiếp, không cần `@Temporal` |
| `active` | `boolean` | `BIT` | Còn đang làm việc hay không |
| `yearsOfService`| `int` | *Không lưu CSDL* | `@Transient`, tính từ `hireDate` qua `Period.between(...)` |

---

## 3. Hướng dẫn khởi tạo CSDL MSSQL

Bạn có thể chạy tệp [schema.sql](file:///C:/Users/hoang/.gemini/antigravity-ide/scratch/jpa-employee-crud/schema.sql) bằng **SQL Server Management Studio (SSMS)**, **Azure Data Studio** hoặc cửa sổ dòng lệnh `sqlcmd`:

```sql
-- 1. Tạo Database
IF NOT EXISTS (SELECT * FROM sys.databases WHERE name = 'EmployeeDB')
BEGIN
    CREATE DATABASE EmployeeDB;
END
GO

USE EmployeeDB;
GO

-- 2. Tạo bảng employees
IF NOT EXISTS (SELECT * FROM sys.tables WHERE name = 'employees')
BEGIN
    CREATE TABLE employees (
        id BIGINT IDENTITY(1,1) PRIMARY KEY,
        fullName NVARCHAR(255) NOT NULL,
        email VARCHAR(255) UNIQUE,
        salary DECIMAL(18, 2),
        gender VARCHAR(20),
        hireDate DATE,
        active BIT NOT NULL DEFAULT 1
    );
END
GO

-- 3. Tạo Login và User cho ứng dụng
IF NOT EXISTS (SELECT * FROM sys.server_principals WHERE name = 'employee_user')
BEGIN
    CREATE LOGIN employee_user WITH PASSWORD = 'Employee@123456', CHECK_POLICY = OFF;
END
GO

USE EmployeeDB;
GO

IF NOT EXISTS (SELECT * FROM sys.database_principals WHERE name = 'employee_user')
BEGIN
    CREATE USER employee_user FOR LOGIN employee_user;
    ALTER ROLE db_owner ADD MEMBER employee_user;
END
GO
```

> **Lưu ý:** Trong `src/main/resources/META-INF/persistence.xml` đã cấu hình sẵn thuộc tính `hibernate.hbm2ddl.auto = update`. Nếu database `EmployeeDB` đã tồn tại, Hibernate sẽ tự động sinh/cập nhật bảng `employees` khi ứng dụng khởi chạy.

---

## 4. Danh sách TODO và Checklist nghiệm thu

### TODO 0.1 — Tạo entity `Employee` & Enum `Gender`
- [x] Tạo enum riêng `Gender.java` gồm `MALE`, `FEMALE`, `OTHER`.
- [x] Class `Employee.java` có `@Entity` và `@Table(name = "employees")`.
- [x] `id` được đánh dấu `@Id` + `@GeneratedValue(strategy = GenerationType.IDENTITY)`.
- [x] `fullName` có `@Column(nullable = false)`.
- [x] `email` có `@Column(unique = true)`.
- [x] `salary` khai báo kiểu `BigDecimal` (không dùng float/double).
- [x] `gender` map bằng `@Enumerated(EnumType.STRING)` — không để ORDINAL mặc định.
- [x] `hireDate` kiểu `LocalDate` (không cần `@Temporal`).
- [x] `yearsOfService` có `@Transient`, kèm method tính toán `Period.between(hireDate, LocalDate.now()).getYears()`.
- [x] Đầy đủ constructor không tham số (bắt buộc cho JPA), constructor tiện dụng và getter/setter.

### TODO 0.2 — Cấu hình `persistence.xml`
- [x] Đăng ký `<class>com.example.employee.entity.Employee</class>` vào `persistence-unit`.
- [x] Bật `hibernate.show_sql=true`, `hibernate.format_sql=true`, `hibernate.highlight_sql=true`.
- [x] Cấu hình MSSQL dialect và MSSQL JDBC driver.

### TODO 0.3 — CREATE: `EmployeeDAO.save(Employee e)`
- [x] Mở transaction (`tx.begin()`), gọi `em.persist(e)`, `tx.commit()`.
- [x] Có `rollback()` trong catch khi phát sinh lỗi, `close()` trong finally.
- [x] Sau khi lưu, `e.getId()` khác `null` (chuyển sang Managed và được DB cấp ID tự động).

### TODO 0.4 — READ: `findById` và `findAll`
- [x] `findById(Long id)` dùng `em.find(Employee.class, id)`, trả về `null` nếu không tìm thấy (không ném ngoại lệ).
- [x] `findAll()` dùng JPQL `SELECT e FROM Employee e`, trả về `List<Employee>`.
- [x] Cả 2 phương thức đều tự mở và đóng `EntityManager` riêng.

### TODO 0.5 — READ có điều kiện (JPQL)
- [x] `findByEmail(String email)`: tìm nhân viên theo email để kiểm tra trùng trước khi tạo mới.
- [x] `findBySalaryGreaterThan(BigDecimal minSalary)`: tìm danh sách nhân viên có lương > minSalary.
- [x] `findByActive(boolean active)`: tìm danh sách nhân viên theo trạng thái làm việc.
- [x] Sử dụng `query.setParameter(...)` phòng chống JPQL Injection (không nối chuỗi trực tiếp).

### TODO 0.6 — UPDATE: `EmployeeDAO.update(Employee e)`
- [x] Mở transaction, gọi `em.merge(e)` và gán lại kết quả (`Employee updated = em.merge(e)`).
- [x] Thao tác an toàn đối với entity ở trạng thái Detached.

### TODO 0.7 — DELETE: `EmployeeDAO.delete(Long id)`
- [x] Mở transaction, gọi `em.find(...)` kiểm tra `entity != null` trước khi gọi `em.remove(...)`.
- [x] Sau khi xóa, gọi `findById(id)` xác nhận trả về `null`.

### TODO 0.8 — Demo luồng CRUD đầy đủ trong `Main`
- [x] Triển khai luồng demo tuần tự: `Create -> Read -> Update -> Read lại kiểm tra -> Delete -> Read lại kiểm tra đã xóa`.
- [x] In kết quả chi tiết từng bước ra màn hình bằng `System.out.println`.
- [x] Luồng chạy mượt mà, không phát sinh bất kỳ ngoại lệ nào.

### TODO 0.9 — Kiểm chứng ràng buộc UNIQUE trên email
- [x] Cố ý tạo 2 Employee cùng email và gọi `save()`.
- [x] Lần `save()` thứ 2 bị MSSQL và Hibernate chặn lại bằng ngoại lệ duplicate key / unique constraint.
- [x] Bắt ngoại lệ bằng try/catch trong `Main` (không để crash chương trình) và in thông báo lỗi rõ ràng.

### TODO 0.10 — Giải thích Entity Lifecycle (bắt buộc viết comment)
- [x] Tại mỗi bước trong `Main.java`, ghi chú rõ ràng trạng thái của entity trong chu trình sống:
  - **New / Transient**: Vừa được khởi tạo bằng `new`, chưa có ID, chưa gắn với `EntityManager`.
  - **Managed**: Đang được `EntityManager` theo dõi và quản lý (sau khi gọi `persist()` hoặc trong câu lệnh `find()`).
  - **Detached**: Khi `EntityManager` đã đóng (`close()`) hoặc đối tượng trước khi truyền vào `merge()`.
  - **Removed**: Khi gọi `em.remove()`, entity được đánh dấu sẽ bị xóa khỏi DB khi commit transaction.

---

## 5. Hướng dẫn mở và chạy dự án trên IntelliJ IDEA

1. Mở **IntelliJ IDEA**.
2. Chọn **File -> Open...** và điều hướng đến thư mục dự án:
   `C:\Users\hoang\.gemini\antigravity-ide\scratch\jpa-employee-crud`
3. Nhấn **Trust Project** và đợi IntelliJ IDEA tải các thư viện Maven (`pom.xml`).
4. Để chạy chương trình demo:
   - Điều hướng đến file `src/main/java/com/example/employee/Main.java`.
   - Nhấp chuột phải vào file `Main.java` và chọn **Run 'Main.main()'** (hoặc tổ hợp phím `Shift + F10`).
5. Để chạy bộ kiểm thử tự động:
   - Điều hướng đến `src/test/java/com/example/employee/EmployeeDAOTest.java`.
   - Nhấp chuột phải và chọn **Run 'EmployeeDAOTest'**.

---

## 6. Chạy qua dòng lệnh (Command Line / Terminal)

```bash
# 1. Biên dịch dự án
mvn clean compile

# 2. Chạy toàn bộ Unit Tests
mvn test

# 3. Chạy chương trình demo CRUD
mvn exec:java -Dexec.mainClass="com.example.employee.Main"
```
