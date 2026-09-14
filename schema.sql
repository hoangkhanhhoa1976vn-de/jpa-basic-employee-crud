-- ====================================================================
-- Script tạo Database và bảng employees cho SQL Server (MSSQL)
-- Bài 0: JPA cơ bản - Entity đơn (chưa có Relationship Mapping)
-- ====================================================================

-- 1. Tạo Database (nếu chưa có)
IF NOT EXISTS (SELECT * FROM sys.databases WHERE name = 'EmployeeDB')
BEGIN
    CREATE DATABASE EmployeeDB;
    PRINT N'Đã tạo CSDL EmployeeDB thành công.';
END
ELSE
BEGIN
    PRINT N'CSDL EmployeeDB đã tồn tại.';
END
GO

USE EmployeeDB;
GO

-- 2. Tạo bảng employees
-- Ghi chú:
-- - id: Khóa chính, tự tăng (IDENTITY(1,1))
-- - fullName: Không được null (NVARCHAR hỗ trợ tiếng Việt có dấu)
-- - email: Duy nhất (UNIQUE)
-- - salary: Kiểu số thực tài chính DECIMAL(18,2) thay vì float/double
-- - gender: Chuỗi lưu tên Enum ('MALE', 'FEMALE', 'OTHER') do dùng @Enumerated(EnumType.STRING)
-- - hireDate: Kiểu DATE lưu ngày vào làm (LocalDate trong Java)
-- - active: Kiểu BIT lưu trạng thái đang làm việc (boolean trong Java)
-- - yearsOfService: KHÔNG có cột trong DB do đánh dấu @Transient
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
    PRINT N'Đã tạo bảng employees thành công.';
END
ELSE
BEGIN
    PRINT N'Bảng employees đã tồn tại.';
END
GO

-- 3. Tạo Login và User chuyên biệt cho ứng dụng (tùy chọn nhưng khuyến nghị)
-- Username: employee_user
-- Password: Employee@123456
IF NOT EXISTS (SELECT * FROM sys.server_principals WHERE name = 'employee_user')
BEGIN
    CREATE LOGIN employee_user WITH PASSWORD = 'Employee@123456', CHECK_POLICY = OFF;
    PRINT N'Đã tạo login employee_user.';
END
GO

USE EmployeeDB;
GO

IF NOT EXISTS (SELECT * FROM sys.database_principals WHERE name = 'employee_user')
BEGIN
    CREATE USER employee_user FOR LOGIN employee_user;
    ALTER ROLE db_owner ADD MEMBER employee_user;
    PRINT N'Đã cấp quyền db_owner cho user employee_user trên EmployeeDB.';
END
GO
