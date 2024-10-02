package vn.duongw.springdatajpademo.repository.criteria;

import java.util.function.Consumer;

import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.Predicate;
import jakarta.persistence.criteria.Root;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import vn.duongw.springdatajpademo.model.User;

@Setter
@Getter
@NoArgsConstructor
@AllArgsConstructor
public class UserSearchQueryCriteriaConsumer implements Consumer<SearchCriteriaQuery> {

    private Predicate predicate;
    private CriteriaBuilder builder;
    private Root root;




    @Override
    public void accept(SearchCriteriaQuery param) {
        if (param.getOperator().equalsIgnoreCase(">")) {
            predicate = builder.and(predicate, builder
                    .greaterThanOrEqualTo(root.get(param.getFieldKey()), param.getValue().toString()));
        } else if (param.getOperator().equalsIgnoreCase("<")) {
            predicate = builder.and(predicate, builder.lessThanOrEqualTo(
                    root.get(param.getFieldKey()), param.getValue().toString()));
        } else if (param.getOperator().equalsIgnoreCase(":")) {
            if (root.get(param.getFieldKey()).getJavaType() == String.class) {
                predicate = builder.and(predicate, builder.like(
                        root.get(param.getFieldKey()), "%" + param.getValue() + "%"));
            } else {
                predicate = builder.and(predicate, builder.equal(
                        root.get(param.getFieldKey()), param.getValue()));
            }

        }
    }


}
