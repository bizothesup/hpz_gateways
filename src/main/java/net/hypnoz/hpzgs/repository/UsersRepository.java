package net.hypnoz.hpzgs.repository;

import net.hypnoz.hpzgs.domain.Authority;
import net.hypnoz.hpzgs.domain.Users;
import org.apache.commons.beanutils.BeanComparator;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.r2dbc.convert.R2dbcConverter;
import org.springframework.data.r2dbc.core.R2dbcEntityTemplate;
import org.springframework.data.r2dbc.repository.Query;
import org.springframework.data.r2dbc.repository.R2dbcRepository;
import org.springframework.r2dbc.core.DatabaseClient;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.util.function.Tuple2;
import reactor.util.function.Tuples;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import static org.springframework.data.relational.core.query.Criteria.where;
import static org.springframework.data.relational.core.query.Query.query;

@Repository
public interface UsersRepository extends R2dbcRepository<Users, Long>,UserRepositoryInternal {
}

interface DeleteExtended<T> {
    Mono<Void> delete(T user);
}

interface UserRepositoryInternal extends DeleteExtended<Users> {
    Mono<Users> findOneWithAuthoritiesByLogin(String login);

    Mono<Users> findOneWithAuthoritiesByEmailIgnoreCase(String email);

    Flux<Users> findAllWithAuthorities(Pageable pageable);
}


class UserRepositoryInternalImpl implements UserRepositoryInternal {

    private final DatabaseClient db;
    private final R2dbcEntityTemplate r2dbcEntityTemplate;
    private final R2dbcConverter r2dbcConverter;

    public UserRepositoryInternalImpl(DatabaseClient db, R2dbcEntityTemplate r2dbcEntityTemplate, R2dbcConverter r2dbcConverter) {
        this.db = db;
        this.r2dbcEntityTemplate = r2dbcEntityTemplate;
        this.r2dbcConverter = r2dbcConverter;
    }

    @Override
    public Mono<Users> findOneWithAuthoritiesByLogin(String login) {
        return findOneWithAuthoritiesBy("login", login);
    }

    @Override
    public Mono<Users> findOneWithAuthoritiesByEmailIgnoreCase(String email) {
        return findOneWithAuthoritiesBy("email", email.toLowerCase());
    }

    @Override
    public Flux<Users> findAllWithAuthorities(Pageable pageable) {
        String property = pageable.getSort().stream().map(Sort.Order::getProperty).findFirst().orElse("id");
        String direction = String.valueOf(
                pageable.getSort().stream().map(Sort.Order::getDirection).findFirst().orElse(Sort.DEFAULT_DIRECTION)
        );
        long page = pageable.getPageNumber();
        long size = pageable.getPageSize();

        return db
                .sql("SELECT * FROM users u LEFT JOIN users_authority ua ON u.id=ua.user_id")
                .map((row, metadata) ->
                        Tuples.of(r2dbcConverter.read(Users.class, row, metadata), Optional.ofNullable(row.get("authority_name", String.class)))
                )
                .all()
                .groupBy(t -> t.getT1().getLogin())
                .flatMap(l -> l.collectList().map(t -> updateUserWithAuthorities(t.get(0).getT1(), t)))
                .sort(
                        Sort.Direction.fromString(direction) == Sort.DEFAULT_DIRECTION
                                ? new BeanComparator<>(property)
                                : new BeanComparator<>(property).reversed()
                )
                .skip(page * size)
                .take(size);
    }

    @Override
    public Mono<Void> delete(Users user) {
        return db
                .sql("DELETE FROM users_authority WHERE user_id = :userId")
                .bind("userId", user.getId())
                .then()
                .then(r2dbcEntityTemplate.delete(Users.class).matching(query(where("id").is(user.getId()))).all().then());
    }

    private Mono<Users> findOneWithAuthoritiesBy(String fieldName, Object fieldValue) {
        return db
                .sql("SELECT * FROM users u LEFT JOIN users_authority ua ON u.id=ua.user_id WHERE u." + fieldName + " = :" + fieldName)
                .bind(fieldName, fieldValue)
                .map((row, metadata) ->
                        Tuples.of(r2dbcConverter.read(Users.class, row, metadata), Optional.ofNullable(row.get("authority_name", String.class)))
                )
                .all()
                .collectList()
                .filter(l -> !l.isEmpty())
                .map(l -> updateUserWithAuthorities(l.get(0).getT1(), l));
    }

    private Users updateUserWithAuthorities(Users user, List<Tuple2<Users, Optional<String>>> tuples) {
        user.setAuthorities(
                tuples
                        .stream()
                        .filter(t -> t.getT2().isPresent())
                        .map(t -> {
                            Authority authority = new Authority();
                            authority.setName(t.getT2().get());
                            return authority;
                        })
                        .collect(Collectors.toSet())
        );

        return user;
    }
}
