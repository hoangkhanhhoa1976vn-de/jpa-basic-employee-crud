package com.example.employee.util;

import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityManagerFactory;
import jakarta.persistence.Persistence;

/**
 * Lớp tiện ích quản lý EntityManagerFactory tập trung theo Singleton pattern.
 * Cung cấp EntityManager cho các thao tác DAO và đóng EntityManagerFactory khi tắt ứng dụng.
 */
public class JPAUtil {

    private static final String PERSISTENCE_UNIT_NAME = "EmployeePU";
    private static EntityManagerFactory emf;

    static {
        try {
            emf = Persistence.createEntityManagerFactory(PERSISTENCE_UNIT_NAME);
        } catch (Throwable ex) {
            System.err.println("Lỗi khởi tạo EntityManagerFactory: " + ex.getMessage());
            ex.printStackTrace();
            throw new ExceptionInInitializerError(ex);
        }
    }

    /**
     * Lấy EntityManager mới từ Factory.
     */
    public static EntityManager getEntityManager() {
        return emf.createEntityManager();
    }

    /**
     * Đóng EntityManagerFactory khi kết thúc toàn bộ chương trình.
     */
    public static void shutdown() {
        if (emf != null && emf.isOpen()) {
            emf.close();
        }
    }
}
