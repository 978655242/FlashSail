# Bright Data Scraping Browser 配置指南

本项目使用 Bright Data Scraping Browser API 通过 Playwright 进行真实的网页爬取。

## 步骤 1: 注册 Bright Data 账户

1. 访问 [Bright Data 官网](https://brightdata.com)
2. 点击 "Start Free Trial" 注册账户
3. 填写注册信息并完成邮箱验证

## 步骤 2: 创建 Scraping Browser Zone

1. 登录 Bright Data 控制面板
2. 进入 "Zones" 页面
3. 点击 "Add Zone" → 选择 "Scraping Browser"
4. 填写 Zone 信息：
   - Zone 名称：例如 `flashsell-scraping`
   - 选择类型：`Residential` 或 `Datacenter`
   - 点击 "Save"

## 步骤 3: 获取 Zone 凭证

在创建的 Zone 页面中，找到以下信息：

- **Proxy Host**: 通常为 `gw-brightdata.net` 或自定义域名
- **Proxy Port**: 根据类型不同
  - Scraping Browser: `24000`
  - Residential: `24001`
  - Datacenter: `24002`
- **Username**: Zone 用户名（格式如 `brd-customer-XXX-zone-xxx`）
- **Password**: Zone 密码

## 步骤 4: 配置应用

编辑 `start/src/main/resources/application-dev.yml`：

```yaml
brightdata:
  # 确保使用 Playwright 模式
  mode: playwright

  # 代理配置（从 Bright Data Zone 获取）
  proxy:
    enabled: true
    host: gw-brightdata.net           # 你的代理主机
    port: 24000                       # 你的代理端口
    username: brd-customer-XXX-zone-xxx  # 你的 Zone 用户名
    password: your_zone_password      # 你的 Zone 密码

  # 浏览器配置
  browser:
    headless: true                    # 无头模式（生产环境建议 true）
    timeout: 60000                    # 超时时间（毫秒）
```

或者使用环境变量（推荐用于生产环境）：

```bash
export BRIGHTDATA_PROXY_HOST=gw-brightdata.net
export BRIGHTDATA_PROXY_PORT=24000
export BRIGHTDATA_PROXY_USERNAME=brd-customer-XXX-zone-xxx
export BRIGHTDATA_PROXY_PASSWORD=your_zone_password
```

## 步骤 5: 安装 Playwright 浏览器

首次运行前，需要安装 Playwright Chromium 浏览器：

```bash
cd flashsell/flashsell-infrastructure
mvn exec:java -e -Dexec.mainClass=com.microsoft.playwright.CLI -Dexec.args="install chromium"
```

## 步骤 6: 验证配置

1. 启动应用：
```bash
cd flashsell/start
mvn spring-boot:run
```

2. 检查日志，确认看到：
```
初始化 Bright Data Gateway，模式: playwright
使用 Playwright 模式 - BrightDataScrapingGatewayImpl
```

3. 测试搜索功能：
```bash
./test-brightdata-api.sh
```

## 常见问题

### Q: 如何检查代理连接是否正常？

A: 查看应用日志，如果看到以下错误则说明代理配置有问题：
```
Bright Data Scraping Browser 不可用: Connection refused
```

### Q: 支持哪些网站？

A: 当前支持：
- Amazon (amazon.com, amazon.co.uk, amazon.de, amazon.co.jp)
- 1688 (s.1688.com)

### Q: 如何避免被封禁？

A: Bright Data 会自动处理 IP 轮换，但仍建议：
- 控制请求频率（已配置速率限制）
- 使用合理的 User-Agent
- 避免短时间内大量请求

### Q: 成本如何计算？

A: Bright Data 按使用量计费：
- Scraping Browser: 按带宽和请求数
- 免费试用额度：请查看控制面板

建议在 `application-dev.yml` 中配置成本监控：
```yaml
brightdata:
  cost-monitor:
    enabled: true
    daily-warning-threshold: 5000    # 每日警告阈值（请求数）
    monthly-warning-threshold: 100000 # 每月警告阈值
```

## 下一步

配置完成后，你可以：

1. 测试商品搜索功能
2. 查看缓存中的数据（Redis）
3. 监控 API 使用情况

## 技术支持

如有问题，请查看：
- [Bright Data 文档](https://docs.brightdata.com)
- [Playwright Java 文档](https://playwright.dev/java/)
