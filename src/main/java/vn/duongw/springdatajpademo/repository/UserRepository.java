package vn.duongw.springdatajpademo.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import vn.duongw.springdatajpademo.model.User;
@Repository
public interface UserRepository extends JpaRepository<User, Long> {
}
