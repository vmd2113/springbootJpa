package vn.duongw.springdatajpademo.repository;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.Query;
import jakarta.persistence.criteria.*;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Repository;
import org.springframework.util.StringUtils;
import vn.duongw.springdatajpademo.dto.reponse.PageResponse;
import vn.duongw.springdatajpademo.model.Address;
import vn.duongw.springdatajpademo.model.User;
import vn.duongw.springdatajpademo.repository.criteria.SearchCriteriaQuery;
import vn.duongw.springdatajpademo.repository.criteria.UserSearchQueryCriteriaConsumer;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Repository
@Slf4j
public class SearchRepository {
    final String LIKE_FORMAT = "%%%s%%%";
    final String SORT_BY = "(\\w+?)(:)(.*)";

    String SEARCH_OPERATOR = "(\\w+?)(:|<|>)(.*)";
    String SEARCH_SPEC_OPERATOR = "(\\w+?)([<:>~!])(.*)(\\p{Punct}?)(\\p{Punct}?)";

    @PersistenceContext
    private EntityManager entityManager;


    public PageResponse<?> getAllUsersAndSearchWithPagingAndSorting(int pageNo, int pageSize, String searchKey, String sortBy) {
        // query list user
        StringBuilder querySQl = new StringBuilder("SELECT u FROM User u WHERE   1=1 ");

        // write true query to get list of users by search (one column)
        if (StringUtils.hasLength(searchKey)) {
            querySQl.append("and lower(u.firstName) like lower(:firstName)");
            querySQl.append("or lower(u.lastName) like lower(:lastName)");
            querySQl.append("or lower(u.email) like lower(email)");
        }
        if (StringUtils.hasLength(sortBy)) {
            Pattern pattern = Pattern.compile(SORT_BY);
            Matcher matcher = pattern.matcher(sortBy);

            if (matcher.find()) {
                querySQl.append(String.format("order by u.%s %s", matcher.group(1), matcher.group(3)));
            }
        }

        Query selectQuery = entityManager.createQuery(querySQl.toString());

        if (StringUtils.hasLength(searchKey)) {
            selectQuery.setParameter("email", String.format(LIKE_FORMAT, searchKey));
            selectQuery.setParameter("firstName", String.format(LIKE_FORMAT, searchKey));
            selectQuery.setParameter("lastName", String.format(LIKE_FORMAT, searchKey));
        }
        selectQuery.setFirstResult(pageNo * pageSize);
        selectQuery.setMaxResults(pageSize);
        List users = selectQuery.getResultList();
        log.info("user list" + "{}", users);


        // query to count user
        StringBuilder countQuery = new StringBuilder("SELECT COUNT(*) FROM User u where 1=1");
        if (StringUtils.hasLength(searchKey)) {
//            querySQl.append( "and lower(u.firstName) like lower(:firstName)");
//            querySQl.append( "or lower(u.lastName) like lower(:lastName)");
//            querySQl.append( "or lower(u.email) like lower(email)");


            countQuery.append("and lower(u.firstName) like lower(?1)");
            countQuery.append("or lower(u.lastName) like lower(?2)");
            countQuery.append("or lower(u.email) like lower(?3)");
        }
        Query countQuerySQl = entityManager.createQuery(countQuery.toString());
        System.out.println(countQuerySQl);
        if (StringUtils.hasLength(searchKey)) {
            countQuerySQl.setParameter(1, String.format(LIKE_FORMAT, searchKey));
            countQuerySQl.setParameter(2, String.format(LIKE_FORMAT, searchKey));
            countQuerySQl.setParameter(3, String.format(LIKE_FORMAT, searchKey));
        }
        Long totalElement = (Long) countQuerySQl.getSingleResult();
        log.info("Count: " + "{}", totalElement);


        Pageable pageable = PageRequest.of(pageNo, pageSize);
        Page<?> page = new PageImpl<>(users, pageable, totalElement);
        PageResponse pageResponse = new PageResponse(pageNo, pageSize, totalElement, page);
        System.out.println(pageResponse);

        // query list user when searching
        return PageResponse.builder()
                .pageNo(pageNo)
                .pageSize(pageSize)
                .total(page.getTotalPages())
                .items(users)
                .build();
    }

    public PageResponse<?> advanceSearchWithCriteria(int pageNo, int pageSize, String sortBy, String... search) {
        log.info("Search user with search={} and sortBy={}", search, sortBy);
        // 1 lấy ra danh sách user
        List<SearchCriteriaQuery> criteriaQueryList = new ArrayList<>();
        if (search.length > 0) {
            Pattern patternSearch = Pattern.compile(SEARCH_OPERATOR);
            for (String s : search) {
                Matcher matcher = patternSearch.matcher(s);
                if (matcher.find()) {
                    criteriaQueryList.add(new SearchCriteriaQuery(matcher.group(1), matcher.group(2), matcher.group(3)));
                }
            }
        }

        if (StringUtils.hasLength(sortBy)) {
            Pattern patternSort = Pattern.compile(SORT_BY);
            for (String s : search) {
                Matcher matcher = patternSort.matcher(s);
                if (matcher.find()) {
                    criteriaQueryList.add(new SearchCriteriaQuery(matcher.group(1), matcher.group(2), matcher.group(3)));
                }
            }
        }


        // 2 lấy ra số luượng bản ghi và phaan trang
        List<User> users = getUsers(pageNo, pageSize, criteriaQueryList, sortBy);

        return PageResponse.builder()
                .pageNo(pageNo)
                .pageSize(pageSize)
                .total(0)
                .items(users)
                .build();
    }

    private List<User> getUsers(int offset, int pageSize, List<SearchCriteriaQuery> criteriaList, String sortBy){

        log.info("-------------- getUsers --------------");

        CriteriaBuilder criteriaBuilder = entityManager.getCriteriaBuilder();
        CriteriaQuery<User> query = criteriaBuilder.createQuery(User.class);
        Root<User> userRoot = query.from(User.class);

        Predicate userPredicate = criteriaBuilder.conjunction();
        UserSearchQueryCriteriaConsumer searchConsumer = new UserSearchQueryCriteriaConsumer(userPredicate, criteriaBuilder, userRoot);

        // thực hiện tìm kiếm với đối tượng (join column)
        String address = "";
        if (StringUtils.hasLength(address)){
            Join<Address, User> addressUserJoin = userRoot.join("addresses");
            Predicate addressPredicate = criteriaBuilder.like(addressUserJoin.get("city"), "%" +address+"%");
            query.where(userPredicate, addressPredicate);

        }else {

            criteriaList.forEach(searchConsumer);
            userPredicate = searchConsumer.getPredicate();
            query.where(userPredicate);
        }

        return entityManager.createQuery(query)
                .setFirstResult(offset*pageSize)
                .setMaxResults(pageSize)
                .getResultList();
    }

}
