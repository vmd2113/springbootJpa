package vn.duongw.springdatajpademo.service.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import vn.duongw.springdatajpademo.dto.reponse.AddressDTO;
import vn.duongw.springdatajpademo.dto.reponse.PageResponse;
import vn.duongw.springdatajpademo.dto.reponse.UserDetailResponse;
import vn.duongw.springdatajpademo.dto.request.UserRequestDTO;
import vn.duongw.springdatajpademo.enums.UserStatus;
import vn.duongw.springdatajpademo.enums.UserType;
import vn.duongw.springdatajpademo.exception.ResourceNotFoundException;
import vn.duongw.springdatajpademo.model.Address;
import vn.duongw.springdatajpademo.model.User;
import vn.duongw.springdatajpademo.repository.SearchRepository;
import vn.duongw.springdatajpademo.repository.UserRepository;
import vn.duongw.springdatajpademo.service.IUserService;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Service
@RequiredArgsConstructor
@Slf4j

public class UserService implements IUserService {
    final String SEARCH_OPERATOR = "(\\w+?)(:|<|>)(.*)";
    final String SEARCH_SPEC_OPERATOR = "(\\w+?)([<:>~!])(.*)(\\p{Punct}?)(\\p{Punct}?)";
    final String SORT_BY = "(\\w+?)(:)(.*)";

    private final UserRepository userRepository;
    private final SearchRepository searchRepository;


    /**
     * Lưu người dùng mới vào cơ sở dữ liệu.
     *
     * @param request DTO chứa thông tin người dùng cần tạo
     * @return ID của người dùng đã được lưu
     */

    @Override
    public long saveUser(UserRequestDTO request) {
        User user = User.builder()
                .firstName(request.getFirstName())
                .lastName(request.getLastName())
                .dateOfBirth(request.getDateOfBirth())
                .gender(request.getGender())
                .phone(request.getPhone())
                .email(request.getEmail())
                .username(request.getUsername())
                .password(request.getPassword())
                .status(request.getStatus())
                .type(UserType.valueOf(request.getType().toUpperCase()))
                .build();
        request.getAddresses().forEach(a ->
                user.saveAddress(Address.builder()
                        .apartmentNumber(a.getApartmentNumber())
                        .floor(a.getFloor())
                        .building(a.getBuilding())
                        .streetNumber(a.getStreetNumber())
                        .street(a.getStreet())
                        .city(a.getCity())
                        .country(a.getCountry())
                        .addressType(a.getAddressType())
                        .build()));

        userRepository.save(user);

        log.info("User has save!");

        return user.getId();
    }



    private User getUserById(long userId) {
        return userRepository.findById(userId).orElseThrow(() -> new ResourceNotFoundException("User not found"));
    }


    /**
     * Cập nhật thông tin của người dùng hiện có.
     *
     * @param userId  ID của người dùng cần cập nhật
     * @param request DTO chứa thông tin mới của người dùng
     */
    @Override
    public void updateUser(long userId, UserRequestDTO request) {
        User user = getUserById(userId);
        user.setFirstName(request.getFirstName());
        user.setLastName(request.getLastName());
        user.setDateOfBirth(request.getDateOfBirth());
        user.setGender(request.getGender());
        user.setPhone(request.getPhone());
        if (!request.getEmail().equals(user.getEmail())) {
            // check email from database if not exist then allow update email otherwise throw exception
            user.setEmail(request.getEmail());
        }
        user.setUsername(request.getUsername());
        user.setPassword(request.getPassword());
        user.setStatus(request.getStatus());
        user.setType(UserType.valueOf(request.getType().toUpperCase()));
        user.setAddresses(convertToAddress(request.getAddresses()));
        userRepository.save(user);

    }

    /**
     * Thay đổi trạng thái của người dùng.
     *
     * @param userId ID của người dùng cần thay đổi trạng thái
     * @param status Trạng thái mới cho người dùng
     */

    @Override
    public void changeStatus(long userId, UserStatus status) {
        User user = getUserById(userId);
        user.setStatus(status);
        userRepository.save(user);
        log.info("status changed");

    }
    /**
     * Xóa người dùng khỏi cơ sở dữ liệu.
     *
     * @param userId ID của người dùng cần xóa
     */
    @Override
    public void deleteUser(long userId) {
        userRepository.deleteById(userId);

    }

    /**
     * Lấy thông tin chi tiết của người dùng dựa trên ID.
     *
     * @param userId ID của người dùng cần tìm
     * @return Thông tin chi tiết về người dùng
     */

    @Override
    public UserDetailResponse getUser(long userId) {
        User user = getUserById(userId);
        return UserDetailResponse.builder()
                .id(userId)
                .firstName(user.getFirstName())
                .lastName(user.getLastName())
                .phone(user.getPhone())
                .email(user.getEmail())
                .build();
    }

    /**
     * Lấy tất cả người dùng với phân trang và sắp xếp theo một cột.
     *
     * @param pageNo  Số trang (bắt đầu từ 0)
     * @param pageSize Số lượng người dùng trên mỗi trang
     * @param sortBy  Thông tin cột sắp xếp và thứ tự (VD: firstName:asc)
     * @return Trang kết quả chứa danh sách người dùng
     */

    @Override
    public PageResponse<?> getAllUsersWithSortBy(int pageNo, int pageSize, String sortBy) {
        int page = 0;
        if (pageNo >0) page =  pageNo-1;
        List<Sort.Order> sorts = new ArrayList<>();

        if (StringUtils.hasLength(sortBy)) {
            // firstName:asc|desc
            Pattern pattern = Pattern.compile(SORT_BY);
            log.info("sort by: {}", sortBy);
            Matcher matcher = pattern.matcher(sortBy);

            if (matcher.find()) {
                if (matcher.group(3).equalsIgnoreCase("asc")) {

                    sorts.add(new Sort.Order(Sort.Direction.ASC, matcher.group(1)));
                } else {
                    sorts.add(new Sort.Order(Sort.Direction.DESC, matcher.group(1)));
                }
            }
        }

        Pageable pageable = PageRequest.of(page, pageSize, Sort.by(sorts));

        Page<User> users = userRepository.findAll(pageable);

        return convertToPageResponse(users, pageable);
    }

    @Override
    public PageResponse<?> getAllUsersWithSortByColumnsAndSearch(int pageNo, int pageSize, String search, String sortBy) {
        return searchRepository.getAllUsersAndSearchWithPagingAndSorting(pageNo, pageSize, search, sortBy);
    }

    /**
     * Lấy tất cả người dùng với phân trang và sắp xếp theo nhiều cột.
     *
     * @param pageNo  Số trang (bắt đầu từ 0)
     * @param pageSize Số lượng người dùng trên mỗi trang
     * @param sorts   Danh sách cột sắp xếp và thứ tự (VD: firstName:asc, lastName:desc)
     * @return Trang kết quả chứa danh sách người dùng
     */

    @Override
    public PageResponse<?> getAllUsersWithSortByMultipleColumns(int pageNo, int pageSize, String... sorts) {
        int page = 0;
        if (pageNo > 0) {
            page = pageNo - 1;
        }

        List<Sort.Order> orders = new ArrayList<>();

        if (sorts != null) {
            for (String sortBy : sorts) {
                log.info("sortBy: {}", sortBy);
                // firstName:asc|desc
                Pattern pattern = Pattern.compile("(\\w+?)(:)(.*)");
                Matcher matcher = pattern.matcher(sortBy);
                if (matcher.find()) {
                    if (matcher.group(3).equalsIgnoreCase("asc")) {
                        orders.add(new Sort.Order(Sort.Direction.ASC, matcher.group(1)));
                    } else {
                        orders.add(new Sort.Order(Sort.Direction.DESC, matcher.group(1)));
                    }
                }
            }
        }

        Pageable pageable = PageRequest.of(page, pageSize, Sort.by(orders));

        Page<User> users = userRepository.findAll(pageable);

        return convertToPageResponse(users, pageable);
    }

    /**
     * Tìm kiếm và sắp xếp người dùng với phân trang.
     *
     * @param pageNo  Số trang (bắt đầu từ 0)
     * @param pageSize Số lượng người dùng trên mỗi trang
     * @param search  Chuỗi tìm kiếm
     * @param sortBy  Cột sắp xếp
     * @return Trang kết quả chứa danh sách người dùng
     */

    @Override
    public PageResponse<?> getAllUsersAndSearchWithPagingAndSorting(int pageNo, int pageSize, String search, String sortBy) {
        return searchRepository.getAllUsersAndSearchWithPagingAndSorting(pageNo, pageSize, search, sortBy);
    }

    /**
     * Tìm kiếm nâng cao người dùng với tiêu chí và phân trang.
     *
     * @param pageNo  Số trang (bắt đầu từ 0)
     * @param pageSize Số lượng người dùng trên mỗi trang
     * @param sortBy  Cột sắp xếp

     * @param search  Các tiêu chí tìm kiếm khác
     * @return Trang kết quả chứa danh sách người dùng
     */

    @Override
    public PageResponse<?> advanceSearchWithCriteria(int pageNo, int pageSize, String sortBy, String... search) {
        return searchRepository.advanceSearchWithCriteria(pageNo, pageSize,  sortBy, search);
    }

    /**
     * Tìm kiếm nâng cao với phân trang và specifications.
     *
     * @param pageable Thông tin phân trang
     * @param user     Các tiêu chí tìm kiếm liên quan đến người dùng
     * @param address  Các tiêu chí tìm kiếm liên quan đến địa chỉ
     * @return Trang kết quả chứa danh sách người dùng
     */

    @Override
    public PageResponse<?> advanceSearchWithSpecifications(Pageable pageable, String[] user, String[] address) {
        return null;
    }

    private Set<Address> convertToAddress(Set<AddressDTO> addresses) {
        Set<Address> result = new HashSet<>();
        addresses.forEach(a ->
                result.add(Address.builder()
                        .apartmentNumber(a.getApartmentNumber())
                        .floor(a.getFloor())
                        .building(a.getBuilding())
                        .streetNumber(a.getStreetNumber())
                        .street(a.getStreet())
                        .city(a.getCity())
                        .country(a.getCountry())
                        .addressType(a.getAddressType())
                        .build())
        );
        return result;
    }


    /**
     * Chuyển đổi dữ liệu người dùng thành trang kết quả.
     *
     * @param users    Trang chứa danh sách người dùng
     * @param pageable Thông tin phân trang
     * @return Trang kết quả chứa danh sách người dùng dưới dạng DTO
     */

    private PageResponse<?> convertToPageResponse(Page<User> users, Pageable pageable) {
        List<UserDetailResponse> response = users.stream().map(user -> UserDetailResponse.builder()
                .id(user.getId())
                .firstName(user.getFirstName())
                .lastName(user.getLastName())
                .email(user.getEmail())
                .phone(user.getPhone())
                .build()).toList();
        return PageResponse.builder()
                .pageNo(pageable.getPageNumber())
                .pageSize(pageable.getPageSize())
                .total(users.getTotalPages())
                .items(response)
                .build();
    }
}
