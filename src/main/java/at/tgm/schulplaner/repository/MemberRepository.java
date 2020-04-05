package at.tgm.schulplaner.repository;

import at.tgm.schulplaner.model.Member;
import org.springframework.data.r2dbc.repository.Query;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import org.springframework.lang.NonNull;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.UUID;

public interface MemberRepository extends ReactiveCrudRepository<Member, UUID> {
    @Query("SELECT * FROM member WHERE uid = :uid")
    Flux<Member> getAllByUid(@NonNull UUID uid);

    @Query("SELECT * FROM member WHERE gid = :gid")
    Flux<Member> getAllByGid(@NonNull UUID gid);

    @Query("SELECT * FROM member WHERE uid = :uid AND gid = :gid")
    Mono<Member> getByUidAndGid(@NonNull UUID uid, @NonNull UUID gid);
}
