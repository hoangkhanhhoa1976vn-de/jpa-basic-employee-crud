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

    /**
     * TODO 0.4 — READ: Tìm kiếm nhân viên theo ID.
     * - Dùng em.find(Employee.class, id)
     * - Trả về null nếu không tồn tại (không ném ngoại lệ)
     * - Tự mở và đóng EntityManager riêng
     *
     * @param id Khóa chính của nhân viên
     * @return Đối tượng Employee tìm thấy hoặc null
     */
    public Employee findById(Long id) {
        EntityManager em = JPAUtil.getEntityManager();
        try {
            return em.find(Employee.class, id);
        } finally {
            if (em != null && em.isOpen()) {
                em.close();
            }
        }
    }

    /**
     * TODO 0.4 — READ: Lấy danh sách tất cả nhân viên.
     * - Dùng câu lệnh JPQL "SELECT e FROM Employee e"
     * - Trả về List<Employee>
     * - Tự mở và đóng EntityManager riêng
     *
     * @return Danh sách tất cả nhân viên
     */
    public java.util.List<Employee> findAll() {
        EntityManager em = JPAUtil.getEntityManager();
        try {
            jakarta.persistence.TypedQuery<Employee> query = 
                    em.createQuery("SELECT e FROM Employee e", Employee.class);
            return query.getResultList();
        } finally {
            if (em != null && em.isOpen()) {
                em.close();
            }
        }
    }
}
