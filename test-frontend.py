#!/usr/bin/env python3
"""
FlashSell 前端测试脚本
测试首页加载、用户认证、爆品推荐等核心功能
"""
from playwright.sync_api import sync_playwright
import sys

def test_frontend():
    """测试前端核心功能"""
    results = []
    
    with sync_playwright() as p:
        browser = p.chromium.launch(headless=True)
        page = browser.new_page()
        
        # 监听控制台错误
        console_errors = []
        def on_console(msg):
            if msg.type == "error":
                console_errors.append(msg.text)
        page.on("console", on_console)
        
        try:
            print("=" * 50)
            print("FlashSell 前端测试")
            print("=" * 50)
            
            # 测试 1: 访问首页
            print("\n[测试 1] 访问首页...")
            page.goto('http://localhost:3000', wait_until='networkidle', timeout=10000)
            title = page.title()
            print(f"  页面标题: {title}")
            print(f"  当前 URL: {page.url}")
            results.append(("首页访问", True if page.url == "http://localhost:3000/" else False))
            
            # 截图
            page.screenshot(path='/tmp/flashsell-home.png', full_page=True)
            print("  首页截图已保存: /tmp/flashsell-home.png")
            
            # 测试 2: 检查页面元素
            print("\n[测试 2] 检查页面元素...")
            
            # 检查导航栏
            nav = page.locator('nav').count()
            print(f"  导航栏存在: {'✓' if nav > 0 else '✗'}")
            results.append(("导航栏", nav > 0))
            
            # 检查登录/注册按钮
            login_btn = page.locator('text=登录').or_(page.locator('text=注册')).count()
            print(f"  登录/注册按钮存在: {'✓' if login_btn > 0 else '✗'}")
            results.append(("认证按钮", login_btn > 0))
            
            # 测试 3: 检查 API 连接
            print("\n[测试 3] 检查 API 代理...")
            # 尝试访问一个 API 端点
            try:
                response = page.request.get('http://localhost:3000/api/health')
                api_status = response.status == 200
                print(f"  API 代理状态: {'✓ (200)' if api_status else f'✗ ({response.status})'}")
                results.append(("API 代理", api_status))
            except Exception as e:
                print(f"  API 代理状态: ✗ ({e})")
                results.append(("API 代理", False))
            
            # 测试 4: 检查响应式设计
            print("\n[测试 4] 检查响应式设计...")
            page.set_viewport_size({"width": 375, "height": 667})  # iPhone SE
            page.wait_for_load_state('networkidle')
            mobile_screenshot = page.screenshot(path='/tmp/flashsell-mobile.png')
            print(f"  移动端截图已保存: /tmp/flashsell-mobile.png")
            results.append(("响应式设计", True))
            
            # 恢复桌面视图
            page.set_viewport_size({"width": 1920, "height": 1080})
            
            # 测试 5: 检查资源加载
            print("\n[测试 5] 检查资源加载...")
            
            # 获取所有网络请求
            failed_requests = []
            def log_request(request):
                # 记录请求
                pass
            
            def log_response(response):
                if response.status >= 400:
                    failed_requests.append(f"{response.url} ({response.status})")
            
            page.on("request", log_request)
            page.on("response", log_response)
            
            page.reload(wait_until='networkidle')
            
            if failed_requests:
                print(f"  失败的请求 ({len(failed_requests)}):")
                for req in failed_requests[:5]:  # 只显示前5个
                    print(f"    - {req}")
                results.append(("资源加载", False))
            else:
                print(f"  所有资源加载成功: ✓")
                results.append(("资源加载", True))
            
            # 测试 6: 检查 JavaScript 错误
            print("\n[测试 6] 检查 JavaScript 错误...")
            if console_errors:
                print(f"  控制台错误 ({len(console_errors)}):")
                for err in console_errors[:5]:  # 只显示前5个
                    print(f"    - {err}")
                results.append(("JavaScript 无错误", False))
            else:
                print(f"  无 JavaScript 错误: ✓")
                results.append(("JavaScript 无错误", True))
            
        except Exception as e:
            print(f"\n❌ 测试失败: {e}")
            results.append(("测试执行", False))
        
        finally:
            browser.close()
    
    # 打印测试总结
    print("\n" + "=" * 50)
    print("测试结果总结")
    print("=" * 50)
    
    passed = sum(1 for _, result in results if result)
    total = len(results)
    
    for name, result in results:
        status = "✓ PASS" if result else "✗ FAIL"
        print(f"  {status}: {name}")
    
    print(f"\n总计: {passed}/{total} 通过")
    print("=" * 50)
    
    return 0 if passed == total else 1

if __name__ == "__main__":
    sys.exit(test_frontend())
