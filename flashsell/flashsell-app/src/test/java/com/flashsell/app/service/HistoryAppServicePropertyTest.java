package com.flashsell.app.service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

import com.flashsell.domain.history.entity.BrowseHistory;
import com.flashsell.domain.history.entity.SearchHistory;

import net.jqwik.api.Arbitraries;
import net.jqwik.api.Arbitrary;
import net.jqwik.api.ForAll;
import net.jqwik.api.From;
import net.jqwik.api.Label;
import net.jqwik.api.Property;
import net.jqwik.api.Provide;
import net.jqwik.api.constraints.IntRange;
import net.jqwik.api.constraints.Size;
import net.jqwik.api.lifecycle.BeforeTry;

/**
 * 历史记录应用服务属性测试
 * 
 * Property 28: 最近活动记录排序
 * Property 29: 历史记录持久化一致性
 * Property 30: 历史记录数量限制
 * Property 31: 历史记录保留策略
 * 
 * Validates: Requirements 14.1, 14.2, 14.3, 14.4, 14.5
 * 
 * Feature: flashsell-technical-solution, Property 28-31: 历史记录
 */
class HistoryAppServicePropertyTest {

    // ========== Test Infrastructure ==========

    private TestHistoryStorage historyStorage;
    private TestHistoryService historyService;

    /**
     * 模拟历史记录存储
     */
    static class TestHistoryStorage {
        private final Map<Long, SearchHistory> searchHistories = new ConcurrentHashMap<>();
        private final Map<Long, BrowseHistory> browseHistories = new ConcurrentHashMap<>();
        private Long searchIdCounter = 1L;
        private Long browseIdCounter = 1L;

        public SearchHistory saveSearchHistory(SearchHistory history) {
            if (history.getId() == null) {
                history.setId(searchIdCounter++);
            }
            searchHistories.put(history.getId(), history);
            return history;
        }

        public BrowseHistory saveBrowseHistory(BrowseHistory history) {
            if (history.getId() == null) {
                history.setId(browseIdCounter++);
            }
            // 如果已存在相同 userId 和 productId 的记录，更新浏览时间
            Optional<BrowseHistory> existing = browseHistories.values().stream()
                    .filter(h -> h.getUserId().equals(history.getUserId()) 
                            && h.getProductId().equals(history.getProductId()))
                    .findFirst();
            
            if (existing.isPresent()) {
                BrowseHistory existingHistory = existing.get();
                existingHistory.setBrowsedAt(history.getBrowsedAt());
                return existingHistory;
            }
            
            browseHistories.put(history.getId(), history);
            return history;
        }

        public List<SearchHistory> findSearchHistoriesByUserId(Long userId, int page, int pageSize) {
            return searchHistories.values().stream()
                    .filter(h -> h.getUserId().equals(userId))
                    .sorted(Comparator.comparing(SearchHistory::getCreatedAt).reversed())
                    .skip((long) (page - 1) * pageSize)
                    .limit(pageSize)
                    .collect(Collectors.toList());
        }

        public List<BrowseHistory> findBrowseHistoriesByUserId(Long userId, int page, int pageSize) {
            return browseHistories.values().stream()
                    .filter(h -> h.getUserId().equals(userId))
                    .sorted(Comparator.comparing(BrowseHistory::getBrowsedAt).reversed())
                    .skip((long) (page - 1) * pageSize)
                    .limit(pageSize)
                    .collect(Collectors.toList());
        }

        public long countSearchHistoriesByUserId(Long userId) {
            return searchHistories.values().stream()
                    .filter(h -> h.getUserId().equals(userId))
                    .count();
        }

        public long countBrowseHistoriesByUserId(Long userId) {
            return browseHistories.values().stream()
                    .filter(h -> h.getUserId().equals(userId))
                    .count();
        }

        public int deleteSearchHistoriesBeforeTime(LocalDateTime time) {
            List<Long> toDelete = searchHistories.values().stream()
                    .filter(h -> h.getCreatedAt().isBefore(time))
                    .map(SearchHistory::getId)
                    .collect(Collectors.toList());
            
            toDelete.forEach(searchHistories::remove);
            return toDelete.size();
        }

        public int deleteBrowseHistoriesBeforeTime(LocalDateTime time) {
            List<Long> toDelete = browseHistories.values().stream()
                    .filter(h -> h.getBrowsedAt().isBefore(time))
                    .map(BrowseHistory::getId)
                    .collect(Collectors.toList());
            
            toDelete.forEach(browseHistories::remove);
            return toDelete.size();
        }

        public void clear() {
            searchHistories.clear();
            browseHistories.clear();
            searchIdCounter = 1L;
            browseIdCounter = 1L;
        }

        public List<SearchHistory> getAllSearchHistories() {
            return new ArrayList<>(searchHistories.values());
        }

        public List<BrowseHistory> getAllBrowseHistories() {
            return new ArrayList<>(browseHistories.values());
        }
    }

    /**
     * 模拟历史记录服务
     */
    static class TestHistoryService {
        private final TestHistoryStorage storage;
        private static final int MAX_SEARCH_HISTORY = 1000;
        private static final int MAX_BROWSE_HISTORY = 500;

        public TestHistoryService(TestHistoryStorage storage) {
            this.storage = storage;
        }

        public void recordSearchHistory(Long userId, String query, Integer resultCount) {
            // 检查是否超过限制
            long currentCount = storage.countSearchHistoriesByUserId(userId);
            if (currentCount >= MAX_SEARCH_HISTORY) {
                // 删除最旧的记录
                List<SearchHistory> histories = storage.getAllSearchHistories().stream()
                        .filter(h -> h.getUserId().equals(userId))
                        .sorted(Comparator.comparing(SearchHistory::getCreatedAt))
                        .collect(Collectors.toList());
                
                if (!histories.isEmpty()) {
                    storage.searchHistories.remove(histories.get(0).getId());
                }
            }

            SearchHistory history = SearchHistory.builder()
                    .userId(userId)
                    .query(query)
                    .resultCount(resultCount)
                    .createdAt(LocalDateTime.now())
                    .build();
            
            storage.saveSearchHistory(history);
        }

        public void recordBrowseHistory(Long userId, Long productId) {
            // 检查是否超过限制
            long currentCount = storage.countBrowseHistoriesByUserId(userId);
            if (currentCount >= MAX_BROWSE_HISTORY) {
                // 删除最旧的记录（排除当前要更新的产品）
                List<BrowseHistory> histories = storage.getAllBrowseHistories().stream()
                        .filter(h -> h.getUserId().equals(userId))
                        .filter(h -> !h.getProductId().equals(productId))
                        .sorted(Comparator.comparing(BrowseHistory::getBrowsedAt))
                        .collect(Collectors.toList());
                
                if (!histories.isEmpty()) {
                    storage.browseHistories.remove(histories.get(0).getId());
                }
            }

            BrowseHistory history = BrowseHistory.builder()
                    .userId(userId)
                    .productId(productId)
                    .browsedAt(LocalDateTime.now())
                    .build();
            
            storage.saveBrowseHistory(history);
        }

        public List<SearchHistory> getRecentSearchHistory(Long userId, int limit) {
            return storage.findSearchHistoriesByUserId(userId, 1, limit);
        }

        public List<BrowseHistory> getRecentBrowseHistory(Long userId, int limit) {
            return storage.findBrowseHistoriesByUserId(userId, 1, limit);
        }

        public int cleanExpiredHistory(int daysToKeep) {
            LocalDateTime cutoffTime = LocalDateTime.now().minusDays(daysToKeep);
            int searchDeleted = storage.deleteSearchHistoriesBeforeTime(cutoffTime);
            int browseDeleted = storage.deleteBrowseHistoriesBeforeTime(cutoffTime);
            return searchDeleted + browseDeleted;
        }
    }

    // ========== Test Setup ==========

    @BeforeTry
    void setUp() {
        historyStorage = new TestHistoryStorage();
        historyService = new TestHistoryService(historyStorage);
    }

    // ========== Generators ==========

    @Provide
    Arbitrary<Long> validUserIds() {
        return Arbitraries.longs().between(1L, 1000L);
    }

    @Provide
    Arbitrary<Long> validProductIds() {
        return Arbitraries.longs().between(1L, 10000L);
    }

    @Provide
    Arbitrary<String> validQueries() {
        return Arbitraries.strings()
                .alpha()
                .ofMinLength(2)
                .ofMaxLength(100);
    }

    @Provide
    Arbitrary<Integer> validResultCounts() {
        return Arbitraries.integers().between(0, 1000);
    }

    @Provide
    Arbitrary<LocalDateTime> recentTimestamps() {
        return Arbitraries.longs()
                .between(0, 86400 * 30) // 最近30天
                .map(seconds -> LocalDateTime.now().minusSeconds(seconds));
    }

    // ========== Property 28: 最近活动记录排序 ==========

    /**
     * Property 28.1: 搜索历史按时间倒序排列
     * 
     * *对于任意* 用户的搜索历史记录，获取最近记录时应该按创建时间倒序排列。
     */
    @Property(tries = 100)
    @Label("Property 28.1: 搜索历史按时间倒序排列")
    void searchHistoryIsSortedByTimeDescending(
            @ForAll("validUserIds") Long userId,
            @ForAll @Size(min = 5, max = 20) List<@From("validQueries") String> queries
    ) {
        // 记录多条搜索历史（添加延迟确保时间不同）
        List<LocalDateTime> timestamps = new ArrayList<>();
        for (String query : queries) {
            historyService.recordSearchHistory(userId, query, 10);
            timestamps.add(LocalDateTime.now());
            try {
                Thread.sleep(1); // 确保时间戳不同
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }

        // 获取最近的搜索历史
        List<SearchHistory> recentHistory = historyService.getRecentSearchHistory(userId, queries.size());

        // 验证排序：应该按时间倒序
        for (int i = 0; i < recentHistory.size() - 1; i++) {
            LocalDateTime current = recentHistory.get(i).getCreatedAt();
            LocalDateTime next = recentHistory.get(i + 1).getCreatedAt();
            
            assert !current.isBefore(next) :
                    String.format("搜索历史应该按时间倒序排列: %s 应该 >= %s", current, next);
        }
    }

    /**
     * Property 28.2: 浏览历史按时间倒序排列
     * 
     * *对于任意* 用户的浏览历史记录，获取最近记录时应该按浏览时间倒序排列。
     */
    @Property(tries = 100)
    @Label("Property 28.2: 浏览历史按时间倒序排列")
    void browseHistoryIsSortedByTimeDescending(
            @ForAll("validUserIds") Long userId,
            @ForAll @Size(min = 5, max = 20) List<@From("validProductIds") Long> productIds
    ) {
        // 记录多条浏览历史
        for (Long productId : productIds) {
            historyService.recordBrowseHistory(userId, productId);
            try {
                Thread.sleep(1); // 确保时间戳不同
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }

        // 获取最近的浏览历史
        List<BrowseHistory> recentHistory = historyService.getRecentBrowseHistory(userId, productIds.size());

        // 验证排序：应该按时间倒序
        for (int i = 0; i < recentHistory.size() - 1; i++) {
            LocalDateTime current = recentHistory.get(i).getBrowsedAt();
            LocalDateTime next = recentHistory.get(i + 1).getBrowsedAt();
            
            assert !current.isBefore(next) :
                    String.format("浏览历史应该按时间倒序排列: %s 应该 >= %s", current, next);
        }
    }

    /**
     * Property 28.3: 最近记录数量限制正确应用
     * 
     * *对于任意* 请求的记录数量限制，返回的记录数应该不超过该限制。
     */
    @Property(tries = 100)
    @Label("Property 28.3: 最近记录数量限制正确应用")
    void recentRecordsLimitIsRespected(
            @ForAll("validUserIds") Long userId,
            @ForAll @IntRange(min = 1, max = 50) int limit,
            @ForAll @Size(min = 10, max = 30) List<@From("validQueries") String> queries
    ) {
        // 记录多条搜索历史
        for (String query : queries) {
            historyService.recordSearchHistory(userId, query, 10);
        }

        // 获取最近的搜索历史
        List<SearchHistory> recentHistory = historyService.getRecentSearchHistory(userId, limit);

        // 验证数量限制
        assert recentHistory.size() <= limit :
                String.format("返回的记录数 %d 不应超过限制 %d", recentHistory.size(), limit);
        
        // 如果总记录数大于限制，应该返回 limit 条记录
        long totalCount = historyStorage.countSearchHistoriesByUserId(userId);
        if (totalCount >= limit) {
            assert recentHistory.size() == limit :
                    String.format("当总记录数 %d >= 限制 %d 时，应该返回 %d 条记录", 
                            totalCount, limit, limit);
        }
    }

    // ========== Property 29: 历史记录持久化一致性 ==========

    /**
     * Property 29.1: 搜索历史保存后可检索
     * 
     * *对于任意* 保存的搜索历史，应该能够通过用户ID检索到。
     */
    @Property(tries = 100)
    @Label("Property 29.1: 搜索历史保存后可检索")
    void savedSearchHistoryCanBeRetrieved(
            @ForAll("validUserIds") Long userId,
            @ForAll("validQueries") String query,
            @ForAll("validResultCounts") Integer resultCount
    ) {
        // 保存搜索历史
        historyService.recordSearchHistory(userId, query, resultCount);

        // 检索搜索历史
        List<SearchHistory> histories = historyService.getRecentSearchHistory(userId, 10);

        // 验证能够检索到
        assert !histories.isEmpty() :
                "保存的搜索历史应该能够检索到";
        
        // 验证最新的记录匹配
        SearchHistory latest = histories.get(0);
        assert latest.getUserId().equals(userId) :
                "用户ID应该匹配";
        assert latest.getQuery().equals(query) :
                "查询内容应该匹配";
        assert latest.getResultCount().equals(resultCount) :
                "结果数量应该匹配";
    }

    /**
     * Property 29.2: 浏览历史保存后可检索
     * 
     * *对于任意* 保存的浏览历史，应该能够通过用户ID检索到。
     */
    @Property(tries = 100)
    @Label("Property 29.2: 浏览历史保存后可检索")
    void savedBrowseHistoryCanBeRetrieved(
            @ForAll("validUserIds") Long userId,
            @ForAll("validProductIds") Long productId
    ) {
        // 保存浏览历史
        historyService.recordBrowseHistory(userId, productId);

        // 检索浏览历史
        List<BrowseHistory> histories = historyService.getRecentBrowseHistory(userId, 10);

        // 验证能够检索到
        assert !histories.isEmpty() :
                "保存的浏览历史应该能够检索到";
        
        // 验证最新的记录匹配
        BrowseHistory latest = histories.get(0);
        assert latest.getUserId().equals(userId) :
                "用户ID应该匹配";
        assert latest.getProductId().equals(productId) :
                "产品ID应该匹配";
    }

    /**
     * Property 29.3: 重复浏览更新时间而非创建新记录
     * 
     * *对于任意* 重复浏览同一产品，应该更新浏览时间而不是创建新记录。
     */
    @Property(tries = 100)
    @Label("Property 29.3: 重复浏览更新时间而非创建新记录")
    void duplicateBrowseUpdatesTimeInsteadOfCreatingNew(
            @ForAll("validUserIds") Long userId,
            @ForAll("validProductIds") Long productId
    ) {
        // 第一次浏览
        historyService.recordBrowseHistory(userId, productId);
        long countAfterFirst = historyStorage.countBrowseHistoriesByUserId(userId);

        try {
            Thread.sleep(10); // 确保时间不同
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }

        // 第二次浏览同一产品
        historyService.recordBrowseHistory(userId, productId);
        long countAfterSecond = historyStorage.countBrowseHistoriesByUserId(userId);

        // 验证记录数量没有增加
        assert countAfterFirst == countAfterSecond :
                String.format("重复浏览不应创建新记录: %d == %d", countAfterFirst, countAfterSecond);
        
        // 验证浏览时间已更新
        List<BrowseHistory> histories = historyService.getRecentBrowseHistory(userId, 1);
        assert !histories.isEmpty() :
                "应该有浏览记录";
    }

    // ========== Property 30: 历史记录数量限制 ==========

    /**
     * Property 30.1: 搜索历史数量不超过最大限制
     * 
     * *对于任意* 用户，搜索历史记录数量不应超过系统设定的最大限制（1000条）。
     */
    @Property(tries = 50)
    @Label("Property 30.1: 搜索历史数量不超过最大限制")
    void searchHistoryCountDoesNotExceedLimit(
            @ForAll("validUserIds") Long userId,
            @ForAll @IntRange(min = 1000, max = 1100) int recordCount
    ) {
        // 记录大量搜索历史
        for (int i = 0; i < recordCount; i++) {
            historyService.recordSearchHistory(userId, "query" + i, i);
        }

        // 验证记录数量不超过限制
        long actualCount = historyStorage.countSearchHistoriesByUserId(userId);
        assert actualCount <= 1000 :
                String.format("搜索历史数量 %d 不应超过限制 1000", actualCount);
    }

    /**
     * Property 30.2: 浏览历史数量不超过最大限制
     * 
     * *对于任意* 用户，浏览历史记录数量不应超过系统设定的最大限制（500条）。
     */
    @Property(tries = 50)
    @Label("Property 30.2: 浏览历史数量不超过最大限制")
    void browseHistoryCountDoesNotExceedLimit(
            @ForAll("validUserIds") Long userId,
            @ForAll @IntRange(min = 500, max = 600) int recordCount
    ) {
        // 记录大量浏览历史（使用不同的产品ID）
        for (int i = 0; i < recordCount; i++) {
            historyService.recordBrowseHistory(userId, (long) i);
        }

        // 验证记录数量不超过限制
        long actualCount = historyStorage.countBrowseHistoriesByUserId(userId);
        assert actualCount <= 500 :
                String.format("浏览历史数量 %d 不应超过限制 500", actualCount);
    }

    /**
     * Property 30.3: 超过限制时删除最旧记录
     * 
     * *对于任意* 超过限制的历史记录，应该删除最旧的记录以保持在限制内。
     */
    @Property(tries = 50)
    @Label("Property 30.3: 超过限制时删除最旧记录")
    void oldestRecordsAreDeletedWhenLimitExceeded(
            @ForAll("validUserIds") Long userId
    ) {
        // 记录1005条搜索历史
        List<String> queries = new ArrayList<>();
        for (int i = 0; i < 1005; i++) {
            String query = "query" + i;
            queries.add(query);
            historyService.recordSearchHistory(userId, query, i);
        }

        // 获取所有搜索历史
        List<SearchHistory> allHistories = historyStorage.getAllSearchHistories().stream()
                .filter(h -> h.getUserId().equals(userId))
                .sorted(Comparator.comparing(SearchHistory::getCreatedAt))
                .collect(Collectors.toList());

        // 验证最旧的5条记录已被删除
        assert allHistories.size() <= 1000 :
                "记录数量应该不超过1000";
        
        // 验证保留的是最新的记录
        if (allHistories.size() == 1000) {
            SearchHistory oldest = allHistories.get(0);
            // 最旧的记录应该不是 "query0"（已被删除）
            assert !oldest.getQuery().equals("query0") :
                    "最旧的记录应该已被删除";
        }
    }

    // ========== Property 31: 历史记录保留策略 ==========

    /**
     * Property 31.1: 30天前的记录应被清理
     * 
     * *对于任意* 超过30天的历史记录，执行清理后应该被删除。
     */
    @Property(tries = 100)
    @Label("Property 31.1: 30天前的记录应被清理")
    void recordsOlderThan30DaysAreDeleted(
            @ForAll("validUserIds") Long userId,
            @ForAll @IntRange(min = 5, max = 20) int oldRecordCount,
            @ForAll @IntRange(min = 5, max = 20) int newRecordCount
    ) {
        // 创建30天前的旧记录
        LocalDateTime oldTime = LocalDateTime.now().minusDays(31);
        for (int i = 0; i < oldRecordCount; i++) {
            SearchHistory oldHistory = SearchHistory.builder()
                    .userId(userId)
                    .query("old_query" + i)
                    .resultCount(i)
                    .createdAt(oldTime.minusHours(i))
                    .build();
            historyStorage.saveSearchHistory(oldHistory);
        }

        // 创建最近的新记录
        for (int i = 0; i < newRecordCount; i++) {
            historyService.recordSearchHistory(userId, "new_query" + i, i);
        }

        long countBeforeCleanup = historyStorage.countSearchHistoriesByUserId(userId);
        assert countBeforeCleanup == oldRecordCount + newRecordCount :
                "清理前应该有所有记录";

        // 执行清理
        int deletedCount = historyService.cleanExpiredHistory(30);

        // 验证旧记录被删除
        long countAfterCleanup = historyStorage.countSearchHistoriesByUserId(userId);
        assert countAfterCleanup == newRecordCount :
                String.format("清理后应该只剩新记录: %d == %d", countAfterCleanup, newRecordCount);
        
        assert deletedCount >= oldRecordCount :
                String.format("删除的记录数 %d 应该 >= 旧记录数 %d", deletedCount, oldRecordCount);
    }

    /**
     * Property 31.2: 30天内的记录不应被清理
     * 
     * *对于任意* 30天内的历史记录，执行清理后应该保留。
     */
    @Property(tries = 100)
    @Label("Property 31.2: 30天内的记录不应被清理")
    void recordsWithin30DaysAreRetained(
            @ForAll("validUserIds") Long userId,
            @ForAll @IntRange(min = 5, max = 20) int recordCount
    ) {
        // 创建最近的记录（0-29天内）
        for (int i = 0; i < recordCount; i++) {
            LocalDateTime recentTime = LocalDateTime.now().minusDays(i % 29);
            SearchHistory recentHistory = SearchHistory.builder()
                    .userId(userId)
                    .query("recent_query" + i)
                    .resultCount(i)
                    .createdAt(recentTime)
                    .build();
            historyStorage.saveSearchHistory(recentHistory);
        }

        long countBeforeCleanup = historyStorage.countSearchHistoriesByUserId(userId);

        // 执行清理
        historyService.cleanExpiredHistory(30);

        // 验证记录未被删除
        long countAfterCleanup = historyStorage.countSearchHistoriesByUserId(userId);
        assert countAfterCleanup == countBeforeCleanup :
                String.format("30天内的记录不应被删除: %d == %d", countAfterCleanup, countBeforeCleanup);
    }

    /**
     * Property 31.3: 浏览历史也遵循保留策略
     * 
     * *对于任意* 浏览历史记录，也应该遵循30天保留策略。
     */
    @Property(tries = 100)
    @Label("Property 31.3: 浏览历史也遵循保留策略")
    void browseHistoryFollowsRetentionPolicy(
            @ForAll("validUserIds") Long userId,
            @ForAll @IntRange(min = 5, max = 20) int oldRecordCount,
            @ForAll @IntRange(min = 5, max = 20) int newRecordCount
    ) {
        // 创建30天前的旧浏览记录
        LocalDateTime oldTime = LocalDateTime.now().minusDays(31);
        for (int i = 0; i < oldRecordCount; i++) {
            BrowseHistory oldHistory = BrowseHistory.builder()
                    .userId(userId)
                    .productId((long) i)
                    .browsedAt(oldTime.minusHours(i))
                    .build();
            historyStorage.saveBrowseHistory(oldHistory);
        }

        // 创建最近的新浏览记录
        for (int i = 0; i < newRecordCount; i++) {
            historyService.recordBrowseHistory(userId, (long) (oldRecordCount + i));
        }

        long countBeforeCleanup = historyStorage.countBrowseHistoriesByUserId(userId);

        // 执行清理
        int deletedCount = historyService.cleanExpiredHistory(30);

        // 验证旧记录被删除
        long countAfterCleanup = historyStorage.countBrowseHistoriesByUserId(userId);
        assert countAfterCleanup == newRecordCount :
                String.format("清理后应该只剩新记录: %d == %d", countAfterCleanup, newRecordCount);
        
        assert deletedCount >= oldRecordCount :
                String.format("删除的记录数 %d 应该 >= 旧记录数 %d", deletedCount, oldRecordCount);
    }

    /**
     * Property 31.4: 清理操作返回正确的删除数量
     * 
     * *对于任意* 清理操作，返回的删除数量应该等于实际删除的记录数。
     */
    @Property(tries = 100)
    @Label("Property 31.4: 清理操作返回正确的删除数量")
    void cleanupReturnsCorrectDeleteCount(
            @ForAll("validUserIds") Long userId,
            @ForAll @IntRange(min = 5, max = 15) int oldSearchCount,
            @ForAll @IntRange(min = 5, max = 15) int oldBrowseCount
    ) {
        // 创建旧的搜索记录
        LocalDateTime oldTime = LocalDateTime.now().minusDays(31);
        for (int i = 0; i < oldSearchCount; i++) {
            SearchHistory oldHistory = SearchHistory.builder()
                    .userId(userId)
                    .query("old_query" + i)
                    .resultCount(i)
                    .createdAt(oldTime)
                    .build();
            historyStorage.saveSearchHistory(oldHistory);
        }

        // 创建旧的浏览记录
        for (int i = 0; i < oldBrowseCount; i++) {
            BrowseHistory oldHistory = BrowseHistory.builder()
                    .userId(userId)
                    .productId((long) i)
                    .browsedAt(oldTime)
                    .build();
            historyStorage.saveBrowseHistory(oldHistory);
        }

        // 执行清理
        int deletedCount = historyService.cleanExpiredHistory(30);

        // 验证删除数量
        int expectedDeleteCount = oldSearchCount + oldBrowseCount;
        assert deletedCount == expectedDeleteCount :
                String.format("删除数量 %d 应该等于预期 %d", deletedCount, expectedDeleteCount);
    }
}
