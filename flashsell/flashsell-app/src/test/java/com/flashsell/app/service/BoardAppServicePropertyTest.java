package com.flashsell.app.service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicLong;
import java.util.stream.Collectors;

import net.jqwik.api.Arbitraries;
import net.jqwik.api.Arbitrary;
import net.jqwik.api.Combinators;
import net.jqwik.api.ForAll;
import net.jqwik.api.Label;
import net.jqwik.api.Property;
import net.jqwik.api.Provide;
import net.jqwik.api.lifecycle.BeforeTry;

/**
 * 看板属性测试
 * 
 * Property 11: 看板数量限制
 * *对于任意* 免费用户，创建超过 10 个看板应该被拒绝；
 * *对于任意* 高级用户，创建超过 50 个看板应该被拒绝。
 * 
 * Validates: Requirements 4.5
 * 
 * Feature: flashsell-technical-solution, Property 11: 看板数量限制
 */
class BoardAppServicePropertyTest {

    // ========== Test Infrastructure ==========

    /**
     * 订阅等级枚举（简化版）
     */
    enum SubscriptionLevel {
        FREE("免费版", 10),
        BASIC("基础版", 30),
        PRO("专业版", 50);

        private final String displayName;
        private final int maxBoards;

        SubscriptionLevel(String displayName, int maxBoards) {
            this.displayName = displayName;
            this.maxBoards = maxBoards;
        }

        public int getMaxBoards() {
            return maxBoards;
        }

        public String getDisplayName() {
            return displayName;
        }
    }

    /**
     * 用户实体（简化版）
     */
    record User(
            Long id,
            String phone,
            String nickname,
            SubscriptionLevel subscriptionLevel,
            LocalDate subscriptionExpireDate
    ) {
        /**
         * 获取有效的订阅等级
         */
        public SubscriptionLevel getEffectiveSubscriptionLevel() {
            if (isSubscriptionExpired()) {
                return SubscriptionLevel.FREE;
            }
            return subscriptionLevel != null ? subscriptionLevel : SubscriptionLevel.FREE;
        }

        /**
         * 检查订阅是否已过期
         */
        public boolean isSubscriptionExpired() {
            if (subscriptionLevel == SubscriptionLevel.FREE) {
                return false;
            }
            if (subscriptionExpireDate == null) {
                return true;
            }
            return LocalDate.now().isAfter(subscriptionExpireDate);
        }

        /**
         * 检查用户是否可以创建新看板
         */
        public boolean canCreateBoard(int currentBoardCount) {
            return currentBoardCount < getEffectiveSubscriptionLevel().getMaxBoards();
        }
    }

    /**
     * 看板实体（简化版）
     */
    record Board(
            Long id,
            Long userId,
            String name,
            LocalDateTime createdAt,
            LocalDateTime deletedAt
    ) {
        public static Board create(Long userId, String name) {
            return new Board(null, userId, name, LocalDateTime.now(), null);
        }

        public boolean belongsToUser(Long userId) {
            return this.userId.equals(userId);
        }

        public boolean isDeleted() {
            return deletedAt != null;
        }
    }

    /**
     * 看板网关 - 内存实现用于测试
     */
    static class TestBoardGateway {
        private final Map<Long, Board> boards = new HashMap<>();
        private final AtomicLong idCounter = new AtomicLong(1);

        public Optional<Board> findById(Long id) {
            return Optional.ofNullable(boards.get(id))
                    .filter(board -> !board.isDeleted());
        }

        public List<Board> findByUserId(Long userId) {
            return boards.values().stream()
                    .filter(board -> board.userId().equals(userId))
                    .filter(board -> !board.isDeleted())
                    .collect(Collectors.toList());
        }

        public long countByUserId(Long userId) {
            return boards.values().stream()
                    .filter(board -> board.userId().equals(userId))
                    .filter(board -> !board.isDeleted())
                    .count();
        }

        public boolean existsByUserIdAndName(Long userId, String name) {
            return boards.values().stream()
                    .anyMatch(board -> board.userId().equals(userId) 
                            && board.name().equals(name)
                            && !board.isDeleted());
        }

        public Board save(Board board) {
            Long id = board.id() != null ? board.id() : idCounter.getAndIncrement();
            Board saved = new Board(
                    id,
                    board.userId(),
                    board.name(),
                    board.createdAt() != null ? board.createdAt() : LocalDateTime.now(),
                    board.deletedAt()
            );
            boards.put(id, saved);
            return saved;
        }

        public void deleteById(Long id) {
            Board board = boards.get(id);
            if (board != null) {
                Board deleted = new Board(
                        board.id(),
                        board.userId(),
                        board.name(),
                        board.createdAt(),
                        LocalDateTime.now()
                );
                boards.put(id, deleted);
            }
        }

        public void clear() {
            boards.clear();
            idCounter.set(1);
        }
    }

    /**
     * 用户网关 - 内存实现用于测试
     */
    static class TestUserGateway {
        private final Map<Long, User> users = new HashMap<>();

        public Optional<User> findById(Long id) {
            return Optional.ofNullable(users.get(id));
        }

        public User save(User user) {
            users.put(user.id(), user);
            return user;
        }

        public void clear() {
            users.clear();
        }
    }

    /**
     * 看板服务 - 简化实现用于测试
     */
    static class TestBoardService {
        private final TestBoardGateway boardGateway;
        private final TestUserGateway userGateway;

        public TestBoardService(
                TestBoardGateway boardGateway,
                TestUserGateway userGateway
        ) {
            this.boardGateway = boardGateway;
            this.userGateway = userGateway;
        }

        /**
         * 创建看板
         */
        public Board createBoard(Long userId, String name) {
            // 1. 获取用户信息
            User user = userGateway.findById(userId)
                    .orElseThrow(() -> new IllegalArgumentException("用户不存在"));

            // 2. 检查看板数量限制
            long currentBoardCount = boardGateway.countByUserId(userId);
            int maxBoards = user.getEffectiveSubscriptionLevel().getMaxBoards();

            if (!user.canCreateBoard((int) currentBoardCount)) {
                throw new IllegalStateException(
                        String.format("已达到看板数量上限（%d/%d），请升级订阅以创建更多看板", 
                                currentBoardCount, maxBoards));
            }

            // 3. 检查看板名称是否已存在
            if (boardGateway.existsByUserIdAndName(userId, name)) {
                throw new IllegalArgumentException("看板名称已存在");
            }

            // 4. 创建看板
            Board board = Board.create(userId, name);
            return boardGateway.save(board);
        }

        /**
         * 获取看板数量
         */
        public long getBoardCount(Long userId) {
            return boardGateway.countByUserId(userId);
        }

        /**
         * 删除看板
         */
        public boolean deleteBoard(Long userId, Long boardId) {
            Board board = boardGateway.findById(boardId)
                    .orElseThrow(() -> new IllegalArgumentException("看板不存在"));

            if (!board.belongsToUser(userId)) {
                throw new IllegalArgumentException("看板不存在");
            }

            boardGateway.deleteById(boardId);
            return true;
        }
    }

    // ========== Test Setup ==========

    private TestBoardGateway boardGateway;
    private TestUserGateway userGateway;
    private TestBoardService boardService;

    @BeforeTry
    void setUp() {
        boardGateway = new TestBoardGateway();
        userGateway = new TestUserGateway();
        boardService = new TestBoardService(boardGateway, userGateway);
    }

    // ========== Generators ==========

    @Provide
    Arbitrary<Long> validUserIds() {
        return Arbitraries.longs().between(1L, 10000L);
    }

    @Provide
    Arbitrary<String> validBoardNames() {
        return Arbitraries.strings().alpha().ofMinLength(1).ofMaxLength(50);
    }

    @Provide
    Arbitrary<User> freeUsers() {
        return Combinators.combine(
                validUserIds(),
                Arbitraries.strings().numeric().ofLength(11),
                Arbitraries.strings().alpha().ofMinLength(2).ofMaxLength(20)
        ).as((id, phone, nickname) -> new User(
                id,
                phone,
                nickname,
                SubscriptionLevel.FREE,
                null
        ));
    }

    @Provide
    Arbitrary<User> basicUsers() {
        return Combinators.combine(
                validUserIds(),
                Arbitraries.strings().numeric().ofLength(11),
                Arbitraries.strings().alpha().ofMinLength(2).ofMaxLength(20),
                Arbitraries.integers().between(30, 365)
        ).as((id, phone, nickname, daysValid) -> new User(
                id,
                phone,
                nickname,
                SubscriptionLevel.BASIC,
                LocalDate.now().plusDays(daysValid)
        ));
    }

    @Provide
    Arbitrary<User> proUsers() {
        return Combinators.combine(
                validUserIds(),
                Arbitraries.strings().numeric().ofLength(11),
                Arbitraries.strings().alpha().ofMinLength(2).ofMaxLength(20),
                Arbitraries.integers().between(30, 365)
        ).as((id, phone, nickname, daysValid) -> new User(
                id,
                phone,
                nickname,
                SubscriptionLevel.PRO,
                LocalDate.now().plusDays(daysValid)
        ));
    }

    @Provide
    Arbitrary<User> expiredProUsers() {
        return Combinators.combine(
                validUserIds(),
                Arbitraries.strings().numeric().ofLength(11),
                Arbitraries.strings().alpha().ofMinLength(2).ofMaxLength(20),
                Arbitraries.integers().between(1, 365)
        ).as((id, phone, nickname, daysExpired) -> new User(
                id,
                phone,
                nickname,
                SubscriptionLevel.PRO,
                LocalDate.now().minusDays(daysExpired)
        ));
    }

    // ========== Property 11: 看板数量限制 ==========

    /**
     * Property 11.1: 免费用户创建第10个看板应该成功
     * 
     * *对于任意* 免费用户，创建第10个看板应该成功。
     */
    @Property(tries = 100)
    @Label("Property 11.1: 免费用户创建第10个看板成功")
    void freeUserCanCreateTenthBoard(
            @ForAll("freeUsers") User user
    ) {
        // 保存用户
        userGateway.save(user);

        // 创建9个看板
        for (int i = 1; i <= 9; i++) {
            boardService.createBoard(user.id(), "看板" + i);
        }

        // 创建第10个看板应该成功
        Board tenthBoard = boardService.createBoard(user.id(), "看板10");
        assert tenthBoard != null :
                "免费用户应该能创建第10个看板";

        // 验证看板数量
        long count = boardService.getBoardCount(user.id());
        assert count == 10 :
                String.format("免费用户创建10个看板后数量应该是10，实际是 %d", count);
    }

    /**
     * Property 11.2: 免费用户创建第11个看板应该被拒绝
     * 
     * *对于任意* 免费用户，创建超过10个看板应该被拒绝。
     */
    @Property(tries = 100)
    @Label("Property 11.2: 免费用户创建第11个看板被拒绝")
    void freeUserCannotCreateEleventhBoard(
            @ForAll("freeUsers") User user
    ) {
        // 保存用户
        userGateway.save(user);

        // 创建10个看板
        for (int i = 1; i <= 10; i++) {
            boardService.createBoard(user.id(), "看板" + i);
        }

        // 尝试创建第11个看板应该抛出异常
        try {
            boardService.createBoard(user.id(), "看板11");
            assert false : "免费用户创建第11个看板应该抛出异常";
        } catch (IllegalStateException e) {
            assert e.getMessage().contains("已达到看板数量上限") :
                    "异常消息应该包含'已达到看板数量上限'";
            assert e.getMessage().contains("10/10") :
                    "异常消息应该包含'10/10'";
        }

        // 验证看板数量仍然是10
        long count = boardService.getBoardCount(user.id());
        assert count == 10 :
                String.format("免费用户尝试创建第11个看板失败后数量应该仍是10，实际是 %d", count);
    }

    /**
     * Property 11.3: 基础版用户创建第30个看板应该成功
     * 
     * *对于任意* 基础版用户，创建第30个看板应该成功。
     */
    @Property(tries = 100)
    @Label("Property 11.3: 基础版用户创建第30个看板成功")
    void basicUserCanCreateThirtiethBoard(
            @ForAll("basicUsers") User user
    ) {
        // 保存用户
        userGateway.save(user);

        // 创建29个看板
        for (int i = 1; i <= 29; i++) {
            boardService.createBoard(user.id(), "看板" + i);
        }

        // 创建第30个看板应该成功
        Board thirtiethBoard = boardService.createBoard(user.id(), "看板30");
        assert thirtiethBoard != null :
                "基础版用户应该能创建第30个看板";

        // 验证看板数量
        long count = boardService.getBoardCount(user.id());
        assert count == 30 :
                String.format("基础版用户创建30个看板后数量应该是30，实际是 %d", count);
    }

    /**
     * Property 11.4: 基础版用户创建第31个看板应该被拒绝
     * 
     * *对于任意* 基础版用户，创建超过30个看板应该被拒绝。
     */
    @Property(tries = 100)
    @Label("Property 11.4: 基础版用户创建第31个看板被拒绝")
    void basicUserCannotCreateThirtyFirstBoard(
            @ForAll("basicUsers") User user
    ) {
        // 保存用户
        userGateway.save(user);

        // 创建30个看板
        for (int i = 1; i <= 30; i++) {
            boardService.createBoard(user.id(), "看板" + i);
        }

        // 尝试创建第31个看板应该抛出异常
        try {
            boardService.createBoard(user.id(), "看板31");
            assert false : "基础版用户创建第31个看板应该抛出异常";
        } catch (IllegalStateException e) {
            assert e.getMessage().contains("已达到看板数量上限") :
                    "异常消息应该包含'已达到看板数量上限'";
            assert e.getMessage().contains("30/30") :
                    "异常消息应该包含'30/30'";
        }

        // 验证看板数量仍然是30
        long count = boardService.getBoardCount(user.id());
        assert count == 30 :
                String.format("基础版用户尝试创建第31个看板失败后数量应该仍是30，实际是 %d", count);
    }

    /**
     * Property 11.5: 专业版用户创建第50个看板应该成功
     * 
     * *对于任意* 专业版用户，创建第50个看板应该成功。
     */
    @Property(tries = 100)
    @Label("Property 11.5: 专业版用户创建第50个看板成功")
    void proUserCanCreateFiftiethBoard(
            @ForAll("proUsers") User user
    ) {
        // 保存用户
        userGateway.save(user);

        // 创建49个看板
        for (int i = 1; i <= 49; i++) {
            boardService.createBoard(user.id(), "看板" + i);
        }

        // 创建第50个看板应该成功
        Board fiftiethBoard = boardService.createBoard(user.id(), "看板50");
        assert fiftiethBoard != null :
                "专业版用户应该能创建第50个看板";

        // 验证看板数量
        long count = boardService.getBoardCount(user.id());
        assert count == 50 :
                String.format("专业版用户创建50个看板后数量应该是50，实际是 %d", count);
    }

    /**
     * Property 11.6: 专业版用户创建第51个看板应该被拒绝
     * 
     * *对于任意* 专业版用户，创建超过50个看板应该被拒绝。
     */
    @Property(tries = 100)
    @Label("Property 11.6: 专业版用户创建第51个看板被拒绝")
    void proUserCannotCreateFiftyFirstBoard(
            @ForAll("proUsers") User user
    ) {
        // 保存用户
        userGateway.save(user);

        // 创建50个看板
        for (int i = 1; i <= 50; i++) {
            boardService.createBoard(user.id(), "看板" + i);
        }

        // 尝试创建第51个看板应该抛出异常
        try {
            boardService.createBoard(user.id(), "看板51");
            assert false : "专业版用户创建第51个看板应该抛出异常";
        } catch (IllegalStateException e) {
            assert e.getMessage().contains("已达到看板数量上限") :
                    "异常消息应该包含'已达到看板数量上限'";
            assert e.getMessage().contains("50/50") :
                    "异常消息应该包含'50/50'";
        }

        // 验证看板数量仍然是50
        long count = boardService.getBoardCount(user.id());
        assert count == 50 :
                String.format("专业版用户尝试创建第51个看板失败后数量应该仍是50，实际是 %d", count);
    }

    /**
     * Property 11.7: 订阅过期后应该降级到免费版限制
     * 
     * *对于任意* 订阅已过期的专业版用户，应该只能创建10个看板。
     */
    @Property(tries = 100)
    @Label("Property 11.7: 订阅过期降级到免费版限制")
    void expiredSubscriptionDowngradesToFreeLimit(
            @ForAll("expiredProUsers") User user
    ) {
        // 保存用户
        userGateway.save(user);

        // 验证用户订阅已过期
        assert user.isSubscriptionExpired() :
                "用户订阅应该已过期";

        // 验证有效订阅等级是FREE
        assert user.getEffectiveSubscriptionLevel() == SubscriptionLevel.FREE :
                "过期用户的有效订阅等级应该是FREE";

        // 创建10个看板应该成功
        for (int i = 1; i <= 10; i++) {
            boardService.createBoard(user.id(), "看板" + i);
        }

        // 尝试创建第11个看板应该被拒绝
        try {
            boardService.createBoard(user.id(), "看板11");
            assert false : "订阅过期的用户创建第11个看板应该抛出异常";
        } catch (IllegalStateException e) {
            assert e.getMessage().contains("已达到看板数量上限") :
                    "异常消息应该包含'已达到看板数量上限'";
            assert e.getMessage().contains("10/10") :
                    "异常消息应该包含'10/10'（降级到免费版限制）";
        }
    }

    /**
     * Property 11.8: 删除看板后应该能再次创建
     * 
     * *对于任意* 达到看板上限的用户，删除一个看板后应该能再次创建新看板。
     */
    @Property(tries = 100)
    @Label("Property 11.8: 删除看板后可再次创建")
    void canCreateAfterDeletingBoard(
            @ForAll("freeUsers") User user
    ) {
        // 保存用户
        userGateway.save(user);

        // 创建10个看板（达到上限）
        for (int i = 1; i <= 10; i++) {
            boardService.createBoard(user.id(), "看板" + i);
        }

        // 验证已达到上限
        long countBefore = boardService.getBoardCount(user.id());
        assert countBefore == 10 :
                String.format("创建10个看板后数量应该是10，实际是 %d", countBefore);

        // 删除第一个看板
        List<Board> boards = boardGateway.findByUserId(user.id());
        Board firstBoard = boards.get(0);
        boardService.deleteBoard(user.id(), firstBoard.id());

        // 验证看板数量减少
        long countAfterDelete = boardService.getBoardCount(user.id());
        assert countAfterDelete == 9 :
                String.format("删除1个看板后数量应该是9，实际是 %d", countAfterDelete);

        // 现在应该能创建新看板
        Board newBoard = boardService.createBoard(user.id(), "新看板");
        assert newBoard != null :
                "删除看板后应该能创建新看板";

        // 验证最终看板数量
        long countFinal = boardService.getBoardCount(user.id());
        assert countFinal == 10 :
                String.format("删除后再创建，最终数量应该是10，实际是 %d", countFinal);
    }

    /**
     * Property 11.9: 不同用户的看板数量应该相互独立
     * 
     * *对于任意* 两个不同用户，他们的看板数量限制应该相互独立。
     */
    @Property(tries = 100)
    @Label("Property 11.9: 不同用户看板数量独立")
    void differentUsersHaveIndependentBoardLimits(
            @ForAll("freeUsers") User user1,
            @ForAll("proUsers") User user2
    ) {
        // 确保两个用户ID不同
        User modifiedUser2 = new User(
                user1.id() + 10000L,
                user2.phone(),
                user2.nickname(),
                user2.subscriptionLevel(),
                user2.subscriptionExpireDate()
        );

        // 保存用户
        userGateway.save(user1);
        userGateway.save(modifiedUser2);

        // 用户1创建10个看板（达到免费版上限）
        for (int i = 1; i <= 10; i++) {
            boardService.createBoard(user1.id(), "用户1看板" + i);
        }

        // 用户2创建20个看板（未达到专业版上限）
        for (int i = 1; i <= 20; i++) {
            boardService.createBoard(modifiedUser2.id(), "用户2看板" + i);
        }

        // 验证用户1已达到上限
        try {
            boardService.createBoard(user1.id(), "用户1看板11");
            assert false : "用户1创建第11个看板应该抛出异常";
        } catch (IllegalStateException e) {
            // 预期异常
        }

        // 验证用户2还能继续创建
        Board user2NewBoard = boardService.createBoard(modifiedUser2.id(), "用户2看板21");
        assert user2NewBoard != null :
                "用户2应该还能创建看板";

        // 验证各自的看板数量
        long user1Count = boardService.getBoardCount(user1.id());
        long user2Count = boardService.getBoardCount(modifiedUser2.id());

        assert user1Count == 10 :
                String.format("用户1的看板数量应该是10，实际是 %d", user1Count);
        assert user2Count == 21 :
                String.format("用户2的看板数量应该是21，实际是 %d", user2Count);
    }

    /**
     * Property 11.10: 看板数量限制应该只计算未删除的看板
     * 
     * *对于任意* 用户，看板数量限制应该只计算未被软删除的看板。
     */
    @Property(tries = 100)
    @Label("Property 11.10: 只计算未删除的看板")
    void boardLimitOnlyCountsNonDeletedBoards(
            @ForAll("freeUsers") User user
    ) {
        // 保存用户
        userGateway.save(user);

        // 创建10个看板
        for (int i = 1; i <= 10; i++) {
            boardService.createBoard(user.id(), "看板" + i);
        }

        // 删除5个看板
        List<Board> boards = boardGateway.findByUserId(user.id());
        for (int i = 0; i < 5; i++) {
            boardService.deleteBoard(user.id(), boards.get(i).id());
        }

        // 验证看板数量是5（只计算未删除的）
        long count = boardService.getBoardCount(user.id());
        assert count == 5 :
                String.format("删除5个看板后，未删除的看板数量应该是5，实际是 %d", count);

        // 应该能再创建5个看板
        for (int i = 11; i <= 15; i++) {
            Board newBoard = boardService.createBoard(user.id(), "看板" + i);
            assert newBoard != null :
                    String.format("应该能创建第%d个看板", i);
        }

        // 验证最终看板数量是10
        long finalCount = boardService.getBoardCount(user.id());
        assert finalCount == 10 :
                String.format("最终看板数量应该是10，实际是 %d", finalCount);
    }
}
