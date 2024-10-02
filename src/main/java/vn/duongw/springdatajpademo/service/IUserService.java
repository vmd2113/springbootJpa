package vn.duongw.springdatajpademo.service;

import org.springframework.data.domain.Pageable;
import vn.duongw.springdatajpademo.dto.reponse.PageResponse;
import vn.duongw.springdatajpademo.dto.reponse.UserDetailResponse;
import vn.duongw.springdatajpademo.dto.request.UserRequestDTO;
import vn.duongw.springdatajpademo.enums.UserStatus;

public interface IUserService {

    long saveUser(UserRequestDTO request);

    void updateUser(long userId, UserRequestDTO request);

    void changeStatus(long userId, UserStatus status);

    void deleteUser(long userId);

    UserDetailResponse getUser(long userId);

    PageResponse<?> getAllUsersWithSortBy(int pageNo, int pageSize, String sortBy);
    PageResponse<?> getAllUsersWithSortByColumnsAndSearch(int pageNo,  int pageSize, String search, String sortBy);

    PageResponse<?> getAllUsersWithSortByMultipleColumns(int pageNo, int pageSize, String... sorts);

    PageResponse<?> getAllUsersAndSearchWithPagingAndSorting(int pageNo, int pageSize, String search, String sortBy);

    PageResponse<?> advanceSearchWithCriteria(int pageNo, int pageSize, String sortBy, String... search);

    PageResponse<?> advanceSearchWithSpecifications(Pageable pageable, String[] user, String[] address);
}
