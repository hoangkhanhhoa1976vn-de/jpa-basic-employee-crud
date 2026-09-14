package com.example.employee.dao;

import com.example.employee.entity.Employee;
import com.example.employee.util.JPAUtil;
import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityTransaction;

/**
 * Data Access Object (DAO) cho Entity Employee.
 * Triển khai các thao tác CRUD sử dụng EntityManager theo chuẩn JPA.
 */
public class EmployeeDAO {

    /**
     * TODO 0.3 — CREATE: Lưu mới một Employee vào CSDL.
     * - Mở transaction (tx.begin())
     * - Gọi em.persist(e)
     * - Commit transaction (tx.commit())
     * - Có rollback() trong catch nếu gặp lỗi
     * - Có close() trong finally để giải phóng tài nguyên
     * - Sau khi persist thành công, e.getId() khác null (chuyển sang Managed và được DB cấp ID)
     *
     * @param e Thực thể Employee mới cần lưu
     */
    public void save(Employee e) {
        EntityManager em = JPAUtil.getEntityManager();
        EntityTransaction tx = em.getTransaction();
        try {
            tx.begin();
            em.persist(e);
            tx.commit();
        } catch (Exception ex) {
            if (tx != null && tx.isActive()) {
                tx.rollback();
            }
            throw new RuntimeException("Lỗi khi lưu Employee: " + ex.getMessage(), ex);
        } finally {
            if (em != null && em.isOpen()) {
                em.close();
            }
        }
    }
}
