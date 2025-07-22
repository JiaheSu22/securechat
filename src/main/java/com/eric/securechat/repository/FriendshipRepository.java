package com.eric.securechat.repository;

import com.eric.securechat.model.Friendship;
import com.eric.securechat.model.FriendshipId;
import com.eric.securechat.model.FriendshipStatus;
import com.eric.securechat.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface FriendshipRepository extends JpaRepository<Friendship, FriendshipId> {


    /**
     * 根据请求者和接收者，精确查找一个好友请求。
     * 这个查询是单向的。
     *
     * @param requester 请求者
     * @param addressee 接收者
     * @return 存在则返回 Friendship 记录，否则返回 Optional.empty()
     */
    Optional<Friendship> findByRequesterAndAddressee(User requester, User addressee);

    /**
     * 根据两个用户查找他们之间的好友关系记录。
     * 由于请求者和接收者可能互换，所以需要检查两种情况。
     * 我们使用 @Query 注解来明确定义查询逻辑，避免方法名解析的歧义。
     *
     * @param user1 第一个用户
     * @param user2 第二个用户
     * @return 存在则返回 Friendship 记录，否则返回 Optional.empty()
     */
    @Query("SELECT f FROM Friendship f WHERE " +
            "(f.requester = :user1 AND f.addressee = :user2) OR " +
            "(f.requester = :user2 AND f.addressee = :user1)")
    Optional<Friendship> findFriendshipBetweenUsers(@Param("user1") User user1, @Param("user2") User user2);

    List<Friendship> findByRequesterAndStatus(User requester, FriendshipStatus status);
    List<Friendship> findByAddresseeAndStatus(User addressee, FriendshipStatus status);

    /**
     * [新增] 一次性查询与特定用户相关的所有指定状态的好友关系。
     * 这个查询会同时查找该用户作为请求者或接收者的情况。
     * @param user 用户实体
     * @param status 期望的关系状态
     * @return 符合条件的好友关系列表
     */
    @Query("SELECT f FROM Friendship f WHERE (f.requester = :user OR f.addressee = :user) AND f.status = :status")
    List<Friendship> findAllByUserAndStatus(@Param("user") User user, @Param("status") FriendshipStatus status);
}
